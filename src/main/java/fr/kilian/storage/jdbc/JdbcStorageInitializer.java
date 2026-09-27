package fr.kilian.storage.jdbc;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

public class JdbcStorageInitializer {

    private final DataSource dataSource;
    private final Executor executor;

    public JdbcStorageInitializer(
            DataSource dataSource,
            Executor executor
    ) {

        this.dataSource = Objects.requireNonNull(dataSource, "dataSource cannot be null");
        this.executor = Objects.requireNonNull(executor, "executor cannot be null");

    }

    public CompletableFuture<Void> initialize() {

        return CompletableFuture.runAsync(
                this::initializeBlocking, executor
        );

    }

    private void initializeBlocking() {

        String sql = """
            CREATE TABLE IF NOT EXISTS player_profiles (
                unique_id VARCHAR(36) PRIMARY KEY,
                username VARCHAR(64) NOT NULL,
                first_join BIGINT NOT NULL,
                last_join BIGINT NOT NULL
            )
            """;

        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.executeUpdate();

        } catch (SQLException exception) {
            throw new CompletionException(
                    "Failed to initialize JDBC storage",
                    exception
            );
        }
    }



}
