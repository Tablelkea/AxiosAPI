package fr.kilian.api.player;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface PlayerService {

    Optional<PlayerProfile> getLoaded(
            UUID uniqueId
    );

    CompletableFuture<
            Optional<PlayerProfile>
            > find(
            UUID uniqueId
    );

    CompletableFuture<PlayerProfile> getOrCreate(
            UUID uniqueId,
            String username
    );

}
