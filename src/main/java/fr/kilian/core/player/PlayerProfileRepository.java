package fr.kilian.core.player;

import fr.kilian.api.player.PlayerProfile;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface PlayerProfileRepository {

    CompletableFuture<Optional<PlayerProfile>> find(
            UUID uniqueId
    );

    CompletableFuture<PlayerProfileResolution> createIfAbsent(
            PlayerProfile profile
    );

    CompletableFuture<Void> update(
            PlayerProfile profile
    );

}
