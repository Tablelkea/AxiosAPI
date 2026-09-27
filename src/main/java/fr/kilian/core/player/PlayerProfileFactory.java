package fr.kilian.core.player;

import fr.kilian.api.component.ComponentContainer;
import fr.kilian.api.component.ComponentRegistry;
import fr.kilian.api.player.PlayerProfile;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class PlayerProfileFactory {

    private final ComponentRegistry componentRegistry;
    private final Clock clock;

    public PlayerProfileFactory(
            ComponentRegistry componentRegistry,
            Clock clock
    ) {

        Objects.requireNonNull(componentRegistry, "componentRegistry cannot be null");
        Objects.requireNonNull(clock, "clock cannot be null");

        this.componentRegistry = componentRegistry;
        this.clock = clock;

    }

    PlayerProfile create(
            UUID uniqueId,
            String username
    ) {

        Instant time = clock.instant();


        ComponentContainer componentContainer = new ComponentContainer(componentRegistry);

        return new PlayerProfile(
                uniqueId,
                username,
                time,
                time,
                componentContainer
        );

    }

}
