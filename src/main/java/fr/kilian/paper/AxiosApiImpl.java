package fr.kilian.paper;

import fr.kilian.api.AxiosApi;
import fr.kilian.api.component.ComponentRegistry;
import fr.kilian.api.player.PlayerService;

import java.util.Objects;

public final class AxiosApiImpl implements AxiosApi {

    private final PlayerService playerService;
    private final ComponentRegistry componentRegistry;

    public AxiosApiImpl(
            PlayerService playerService,
            ComponentRegistry componentRegistry
    ) {
        this.playerService = Objects.requireNonNull(
                playerService,
                "playerService cannot be null"
        );

        this.componentRegistry = Objects.requireNonNull(
                componentRegistry,
                "componentRegistry cannot be null"
        );
    }

    @Override
    public PlayerService players() {
        return playerService;
    }

    @Override
    public ComponentRegistry components() {
        return componentRegistry;
    }
}
