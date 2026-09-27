package fr.kilian.core.player;

import fr.kilian.api.player.PlayerProfile;

import java.util.Objects;

public record PlayerProfileResolution(
        PlayerProfile profile,
        boolean created
) {

    public PlayerProfileResolution {
        Objects.requireNonNull(profile, "profile cannot be null");
    }

}
