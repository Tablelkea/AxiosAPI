package fr.kilian.core.player;

import fr.kilian.api.player.PlayerProfile;
import org.jspecify.annotations.NonNull;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class PlayerProfileLifecycle {

    private final PlayerServiceImpl playerService;
    private final Clock clock;

    public PlayerProfileLifecycle(
            PlayerServiceImpl playerService,
            Clock clock
    ) {

        this.playerService = Objects.requireNonNull(playerService, "playerService cannot be null");
        this.clock = Objects.requireNonNull(clock, "clock cannot be null");

    }

    public @NonNull CompletableFuture<PlayerProfile> handleJoin(
            UUID uniqueId,
            String username
    ) {
        return playerService.resolve(uniqueId, username).thenCompose(
                resolution -> {
                    if(resolution.created()){
                        return CompletableFuture.completedFuture(
                                resolution.profile()
                        );
                    }

                    PlayerProfile profile = resolution.profile();
                    Instant joinTime = clock.instant();

                    profile.recordJoin(joinTime);
                    profile.updateUsername(username);

                    return playerService.flush(uniqueId)
                            .thenApply(ignored -> profile);
                }
        );
    }

    public @NonNull CompletableFuture<Void> handleQuit(
            UUID uniqueId
    ) {

        Objects.requireNonNull(uniqueId, "uniqueId cannot be null");

        return playerService.unload(uniqueId);

    }

    public @NonNull CompletableFuture<Void> handleAutosave(){
        return playerService.flushAll();
    }

    public @NonNull CompletableFuture<Void> handleShutdown(){
        return playerService.flushAll();
    }

}
