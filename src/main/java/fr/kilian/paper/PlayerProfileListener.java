package fr.kilian.paper;

import fr.kilian.core.player.PlayerProfileLifecycle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PlayerProfileListener implements Listener {

    private final PlayerProfileLifecycle lifecycle;
    private final Logger logger;

    public PlayerProfileListener(
            PlayerProfileLifecycle lifecycle,
            Logger logger
    ){

        this.lifecycle = Objects.requireNonNull(lifecycle, "lifecycle cannot be null");
        this.logger = Objects.requireNonNull(logger, "logger cannot be null");
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event){

        Player player = event.getPlayer();

        UUID uniqueId = player.getUniqueId();
        String username = player.getName();

        lifecycle.handleJoin(uniqueId, username)
                .exceptionally(error -> {
                    logger.log(Level.SEVERE,
                            "Failed to handle profile join for " + uniqueId,
                            error);
                    return null;
                });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event){

        Player player = event.getPlayer();

        UUID uniqueId = player.getUniqueId();

        lifecycle.handleQuit(uniqueId)
                .whenComplete((ignored, error) -> {

                    if(error != null) {

                        logger.log(Level.SEVERE,
                                "Failed to handle profile quit for " + uniqueId,
                                error);
                    }
                });
    }

}
