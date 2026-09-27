package fr.kilian.api;

import fr.kilian.api.component.ComponentRegistry;
import fr.kilian.api.player.PlayerService;

public interface AxiosApi {

    PlayerService players();

    ComponentRegistry components();
}
