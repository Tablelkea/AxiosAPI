package fr.kilian.core.player;

import fr.kilian.api.player.PlayerProfile;
import fr.kilian.api.player.PlayerService;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class PlayerServiceImpl implements PlayerService {

    private final PlayerProfileRepository repository;
    private final PlayerProfileFactory factory;

    private final ConcurrentMap<UUID, PlayerProfile> loadedProfiles = new ConcurrentHashMap<>();

    public PlayerServiceImpl(
            PlayerProfileRepository repository,
            PlayerProfileFactory factory
    ) {

        Objects.requireNonNull(repository, "repository cannot be null");
        Objects.requireNonNull(factory, "componentRegistry cannot be null");

        this.repository = repository;
        this.factory = factory;

    }


    @Override
    public Optional<PlayerProfile> getLoaded(UUID uniqueId) {
        Objects.requireNonNull(uniqueId, "uniqueId cannot be null");

        PlayerProfile profile = loadedProfiles.get(uniqueId);

        if(profile == null){
            return Optional.empty();
        }

        return Optional.of(profile);
    }

    @Override
    public CompletableFuture<Optional<PlayerProfile>> find(UUID uniqueId) {

        Objects.requireNonNull(uniqueId, "uniqueId cannot be null");

        PlayerProfile loaded = loadedProfiles.get(uniqueId);

        if(loaded != null){
            return CompletableFuture.completedFuture(Optional.of(loaded));
        }

        return repository.find(uniqueId).thenApply(
                optionalProfile -> {
                    if(optionalProfile.isEmpty()){
                        return Optional.empty();
                    }

                    PlayerProfile profile = optionalProfile.get();

                    PlayerProfile existing = loadedProfiles.putIfAbsent(uniqueId, profile);

                    return Optional.of(Objects.requireNonNullElse(existing, profile));

                }
        );


    }

    @Override
    public CompletableFuture<PlayerProfile> getOrCreate(
            UUID uniqueId,
            String username
    ) {
        return resolve(uniqueId, username)
                .thenApply(PlayerProfileResolution::profile);
    }

    CompletableFuture<Void> unload(UUID uniqueId) {
        Objects.requireNonNull(uniqueId, "uniqueId cannot be null");

        PlayerProfile loaded = loadedProfiles.get(uniqueId);

        if(loaded == null){
            return CompletableFuture.completedFuture(null);
        }

        repository.update(loaded).thenRun(
                () -> loadedProfiles.remove(uniqueId, loaded)
        );

        return CompletableFuture.completedFuture(null);

    }

    CompletableFuture<Void> flush(UUID uniqueId){
        Objects.requireNonNull(uniqueId, "uniqueId cannot be null");

        PlayerProfile loaded = loadedProfiles.get(uniqueId);

        if(loaded != null){
            return repository.update(loaded);
        }

        return CompletableFuture.completedFuture(null);

    }

    CompletableFuture<PlayerProfileResolution> resolve(
            UUID uniqueId,
            String username
    ) {
        Objects.requireNonNull(uniqueId, "uniqueId cannot be null");
        Objects.requireNonNull(username, "username cannot be null");

        if (username.isBlank()) {
            throw new IllegalArgumentException("username cannot be blank");
        }

        return find(uniqueId)
                .thenCompose(optionalProfile -> {

                    if (optionalProfile.isPresent()) {
                        return CompletableFuture.completedFuture(
                                new PlayerProfileResolution(
                                        optionalProfile.get(),
                                        false
                                )
                        );
                    }

                    PlayerProfile created =
                            factory.create(uniqueId, username);

                    return repository.createIfAbsent(created)
                            .thenApply(resolution -> {

                                PlayerProfile stored =
                                        resolution.profile();

                                PlayerProfile existing =
                                        loadedProfiles.putIfAbsent(
                                                uniqueId,
                                                stored
                                        );

                                PlayerProfile canonical =
                                        existing != null
                                                ? existing
                                                : stored;

                                return new PlayerProfileResolution(
                                        canonical,
                                        resolution.created()
                                );
                            });
                });
    }

    CompletableFuture<Void> flushAll(){

        List<CompletableFuture<Void>> updates = new ArrayList<>();

        for(PlayerProfile profile : loadedProfiles.values()){
            updates.add(repository.update(profile));
        }

        return CompletableFuture.allOf(
                updates.toArray(CompletableFuture[]::new)
        );

    }
}
