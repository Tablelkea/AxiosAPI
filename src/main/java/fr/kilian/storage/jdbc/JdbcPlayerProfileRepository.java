package fr.kilian.storage.jdbc;

import fr.kilian.api.component.*;
import fr.kilian.api.player.PlayerProfile;
import fr.kilian.core.player.PlayerProfileRepository;
import fr.kilian.core.player.PlayerProfileResolution;

import javax.sql.DataSource;
import java.sql.*;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

public class JdbcPlayerProfileRepository implements PlayerProfileRepository {

    private final DataSource dataSource;
    private final ComponentRegistry componentRegistry;
    private final Executor executor;

    public JdbcPlayerProfileRepository(
            DataSource dataSource,
            ComponentRegistry componentRegistry,
            Executor executor
    ) {

        this.dataSource = Objects.requireNonNull(dataSource, "dataSource cannot be null");
        this.componentRegistry = Objects.requireNonNull(componentRegistry, "componentRegistry cannot be null");
        this.executor = Objects.requireNonNull(executor, "executor cannot be null");

    }

    @Override
    public CompletableFuture<Optional<PlayerProfile>> find(UUID uniqueId) {
        Objects.requireNonNull(uniqueId, "uniqueId cannot be null");

        return CompletableFuture.supplyAsync(
                () -> findBlocking(uniqueId), executor
        );
    }

    @Override
    public CompletableFuture<PlayerProfileResolution> createIfAbsent(PlayerProfile profile) {
        Objects.requireNonNull(profile, "profile cannot be null");

        return CompletableFuture.supplyAsync(
                () -> createIfAbsentBlocking(profile),
                executor
        );
    }

    @Override
    public CompletableFuture<Void> update(PlayerProfile profile) {

        Objects.requireNonNull(profile, "profile cannot be null");

        return CompletableFuture.runAsync(
                () -> updateBlocking(profile), executor
        );

    }

    private Optional<PlayerProfile> findBlocking(UUID uniqueId) {

        String sql = """
            SELECT username, first_join, last_join
            FROM player_profiles
            WHERE unique_id = ?
            """;

        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(
                    1,
                    uniqueId.toString()
            );

            String username;
            Instant firstJoin;
            Instant lastJoin;

            try (ResultSet resultSet = statement.executeQuery()) {

                if (!resultSet.next()) {
                    return Optional.empty();
                }

                username =
                        resultSet.getString("username");

                firstJoin = Instant.ofEpochMilli(
                        resultSet.getLong("first_join")
                );

                lastJoin = Instant.ofEpochMilli(
                        resultSet.getLong("last_join")
                );
            }

            // ResultSet du profil fermé à partir d'ici

            ComponentContainer components =
                    new ComponentContainer(componentRegistry);

            loadComponents(
                    connection,
                    uniqueId,
                    components
            );

            PlayerProfile profile = new PlayerProfile(
                    uniqueId,
                    username,
                    firstJoin,
                    lastJoin,
                    components
            );

            return Optional.of(profile);

        } catch (SQLException exception) {
            throw new CompletionException(
                    "Failed to load player profile " + uniqueId,
                    exception
            );
        }
    }

    private PlayerProfileResolution createIfAbsentBlocking(
            PlayerProfile profile
    ) {

        String sql = """
            INSERT INTO player_profiles (
                unique_id,
                username,
                first_join,
                last_join
            )
            VALUES (?, ?, ?, ?)
            """;

        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            statement.setString(
                    1,
                    profile.getUniqueId().toString()
            );

            statement.setString(
                    2,
                    profile.getUsername()
            );

            statement.setLong(
                    3,
                    profile.getFirstJoin().toEpochMilli()
            );

            statement.setLong(
                    4,
                    profile.getLastJoin().toEpochMilli()
            );

            statement.executeUpdate();

            return new PlayerProfileResolution(
                    profile,
                    true
            );

        } catch (SQLIntegrityConstraintViolationException exception) {

            Optional<PlayerProfile> existing =
                    findBlocking(profile.getUniqueId());

            PlayerProfile existingProfile =
                    existing.orElseThrow(() ->
                            new IllegalStateException(
                                    "Profile conflict occurred but existing profile could not be found"
                            )
                    );

            return new PlayerProfileResolution(
                    existingProfile,
                    false
            );

        } catch (SQLException exception) {

            throw new CompletionException(
                    "Failed to create player profile "
                            + profile.getUniqueId(),
                    exception
            );
        }
    }

    private void updateBlocking(PlayerProfile profile) {

        String sql = """
            UPDATE player_profiles
            SET username = ?,
                first_join = ?,
                last_join = ?
            WHERE unique_id = ?
            """;

        try (Connection connection = dataSource.getConnection()) {

            connection.setAutoCommit(false);

            try {

                try (PreparedStatement statement =
                             connection.prepareStatement(sql)) {

                    statement.setString(
                            1,
                            profile.getUsername()
                    );

                    statement.setLong(
                            2,
                            profile.getFirstJoin().toEpochMilli()
                    );

                    statement.setLong(
                            3,
                            profile.getLastJoin().toEpochMilli()
                    );

                    statement.setString(
                            4,
                            profile.getUniqueId().toString()
                    );

                    int updatedRows = statement.executeUpdate();

                    if (updatedRows != 1) {
                        throw new IllegalStateException(
                                "Expected to update exactly one player profile, but updated "
                                        + updatedRows
                        );
                    }
                }

                replaceComponents(
                        connection,
                        profile
                );

                connection.commit();

            } catch (SQLException | RuntimeException exception) {

                try {
                    connection.rollback();
                } catch (SQLException rollbackException) {
                    exception.addSuppressed(rollbackException);
                }

                throw exception;
            }

        } catch (SQLException exception) {
            throw new CompletionException(
                    "Failed to update player profile "
                            + profile.getUniqueId(),
                    exception
            );
        }
    }

    private <T> Optional<EncodedComponent> encodeComponent(
            ComponentEntry<T> entry
    ) {
        Optional<ComponentCodec<T>> codec =
                componentRegistry.findCodec(entry.key());

        if (codec.isEmpty()) {
            return Optional.empty();
        }

        String payload =
                codec.get().encode(entry.value());

        return Optional.of(
                new EncodedComponent(
                        entry.key().id(),
                        payload
                )
        );
    }

    private record EncodedComponent(
            String id,
            String payload
    ) {
    }

    private void replaceComponents(
            Connection connection,
            PlayerProfile profile
    ) throws SQLException {

        String deleteSql = """
            DELETE FROM player_components
            WHERE player_uuid = ?
              AND component_id = ?
            """;

        try (PreparedStatement statement =
                     connection.prepareStatement(deleteSql)) {

            for (ComponentKey<?> key :
                    componentRegistry.persistentKeys()) {

                statement.setString(
                        1,
                        profile.getUniqueId().toString()
                );

                statement.setString(
                        2,
                        key.id()
                );

                statement.addBatch();
            }

            statement.executeBatch();
        }

        String insertSql = """
            INSERT INTO player_components (
                player_uuid,
                component_id,
                payload
            )
            VALUES (?, ?, ?)
            """;

        try (PreparedStatement statement =
                     connection.prepareStatement(insertSql)) {

            for (ComponentEntry<?> entry :
                    profile.getComponents().snapshot()) {

                Optional<EncodedComponent> encoded =
                        encodeComponent(entry);

                if (encoded.isEmpty()) {
                    continue;
                }

                EncodedComponent component = encoded.get();

                statement.setString(
                        1,
                        profile.getUniqueId().toString()
                );

                statement.setString(
                        2,
                        component.id()
                );

                statement.setString(
                        3,
                        component.payload()
                );

                statement.addBatch();
            }

            statement.executeBatch();
        }
    }

    private void loadComponents(
            Connection connection,
            UUID uniqueId,
            ComponentContainer container
    ) throws SQLException {

        String sql = """
            SELECT component_id, payload
            FROM player_components
            WHERE player_uuid = ?
            """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    uniqueId.toString()
            );

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {

                    String componentId =
                            resultSet.getString("component_id");

                    String payload =
                            resultSet.getString("payload");

                    Optional<ComponentKey<?>> optionalKey =
                            componentRegistry.find(componentId);

                    if (optionalKey.isEmpty()) {
                        continue;
                    }

                    ComponentKey<?> key =
                            optionalKey.get();

                    decodeComponent(
                            key,
                            payload,
                            container
                    );
                }
            }
        }
    }

    private <T> void decodeComponent(
            ComponentKey<T> key,
            String payload,
            ComponentContainer container
    ) {
        Optional<ComponentCodec<T>> codec =
                componentRegistry.findCodec(key);

        if (codec.isEmpty()) {
            return;
        }

        T value = codec.get().decode(payload);

        container.set(key, value);
    }

}
