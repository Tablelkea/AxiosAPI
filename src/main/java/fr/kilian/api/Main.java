package fr.kilian.api;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import fr.kilian.api.component.ComponentRegistry;
import fr.kilian.core.player.PlayerProfileLifecycle;
import fr.kilian.core.player.PlayerProfileRepository;
import fr.kilian.core.player.PlayerServiceImpl;
import fr.kilian.paper.PlayerProfileListener;
import fr.kilian.storage.jdbc.JdbcPlayerProfileRepository;
import fr.kilian.storage.jdbc.JdbcStorageInitializer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Clock;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.logging.Level;

public final class Main extends JavaPlugin {

    private HikariDataSource dataSource;
    private ExecutorService storageExecutor;

    private ComponentRegistry componentRegistry;
    private PlayerServiceImpl playerService;
    private PlayerProfileLifecycle playerLifecycle;

    @Override
    public void onEnable() {

        saveDefaultConfig();

        storageExecutor = Executors.newFixedThreadPool(
                getConfig().getInt("database.pool-size")
        );

        dataSource = createDataSource();

        JdbcStorageInitializer initializer =
                new JdbcStorageInitializer(
                        dataSource,
                        storageExecutor
                );

        initializer.initialize()
                .whenComplete((ignored, error) -> {

                    getServer().getScheduler().runTask(
                            this,
                            () -> {

                                if (error != null) {
                                    getLogger().log(
                                            Level.SEVERE,
                                            "Failed to initialize database",
                                            error
                                    );

                                    getServer()
                                            .getPluginManager()
                                            .disablePlugin(this);

                                    return;
                                }

                                finishBootstrap();
                            }
                    );
                });
    }

    @Override
    public void onDisable() {

        if (playerLifecycle != null) {
            try {
                playerLifecycle.handleShutdown()
                        .get(10, TimeUnit.SECONDS);

                getLogger().info("Player profiles saved successfully.");

            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();

                getLogger().log(
                        Level.SEVERE,
                        "Interrupted while saving player profiles",
                        exception
                );

            } catch (ExecutionException exception) {
                getLogger().log(
                        Level.SEVERE,
                        "Failed to save player profiles during shutdown",
                        exception.getCause()
                );

            } catch (TimeoutException exception) {
                getLogger().log(
                        Level.SEVERE,
                        "Timed out while saving player profiles during shutdown",
                        exception
                );
            }
        }

        if (storageExecutor != null) {
            storageExecutor.shutdown();
        }

        if (dataSource != null) {
            dataSource.close();
        }
    }

    private HikariDataSource createDataSource() {

        String host = getConfig().getString("database.host");
        int port = getConfig().getInt("database.port");
        String database = getConfig().getString("database.name");
        String username = getConfig().getString("database.username");
        String password = getConfig().getString("database.password");
        int poolSize = getConfig().getInt("database.pool-size");

        HikariConfig config = new HikariConfig();

        config.setJdbcUrl(
                "jdbc:mysql://" + host + ":" + port + "/" + database
        );

        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(poolSize);

        return new HikariDataSource(config);
    }

    private void finishBootstrap() {

        Clock clock = Clock.systemUTC();

        componentRegistry = new ComponentRegistry();

        PlayerProfileRepository repository =
                new JdbcPlayerProfileRepository(
                        dataSource,
                        componentRegistry,
                        storageExecutor
                );

        playerService = new PlayerServiceImpl(
                repository,
                componentRegistry,
                clock
        );

        playerLifecycle = new PlayerProfileLifecycle(
                playerService,
                clock
        );

        PlayerProfileListener listener =
                new PlayerProfileListener(
                        playerLifecycle,
                        getLogger()
                );

        getServer()
                .getPluginManager()
                .registerEvents(listener, this);

        loadOnlinePlayers();

        scheduleAutosave();

        getLogger().info("Player profile system initialized.");
    }

    private void scheduleAutosave() {

        long intervalMinutes =
                getConfig().getLong("autosave.interval-minutes");

        if (intervalMinutes <= 0) {
            throw new IllegalArgumentException(
                    "autosave.interval-minutes must be greater than 0"
            );
        }

        long intervalTicks =
                intervalMinutes * 60L * 20L;

        getServer().getScheduler().runTaskTimer(
                this,
                () -> {
                    playerLifecycle.handleAutosave()
                            .whenComplete((ignored, error) -> {

                                if (error != null) {
                                    getLogger().log(
                                            Level.SEVERE,
                                            "Failed to autosave player profiles",
                                            error
                                    );
                                }
                            });
                },
                intervalTicks,
                intervalTicks
        );
    }

    private void loadOnlinePlayers() {

        for (Player player : getServer().getOnlinePlayers()) {

            UUID uniqueId = player.getUniqueId();
            String username = player.getName();

            playerLifecycle.handleJoin(uniqueId, username)
                    .whenComplete((profile, error) -> {

                        if (error != null) {
                            getLogger().log(
                                    Level.SEVERE,
                                    "Failed to load profile for online player "
                                            + uniqueId,
                                    error
                            );
                        }
                    });
        }
    }
}
