package fr.kilian.api.player;

import fr.kilian.api.component.ComponentContainer;
import fr.kilian.api.component.ComponentRegistry;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PlayerProfileTest {

    @Test
    void shouldRejectBlankUsername() {

        ComponentRegistry registry = new ComponentRegistry();
        ComponentContainer container = new ComponentContainer(registry);

        UUID uniqueId = UUID.randomUUID();
        Instant firstJoin = Instant.parse("2026-01-01T10:00:00Z");
        Instant lastJoin = Instant.parse("2026-01-02T10:00:00Z");

        assertThrows(
                IllegalArgumentException.class,
                () -> new PlayerProfile(
                        uniqueId,
                        "   ",
                        firstJoin,
                        lastJoin,
                        container
                )
        );

    }

    @Test
    void shouldRejectLastJoinBeforeFirstJoin() {

        ComponentRegistry registry = new ComponentRegistry();
        ComponentContainer container = new ComponentContainer(registry);

        UUID uniqueId = UUID.randomUUID();

        Instant firstJoin = Instant.parse("2026-01-02T10:00:00Z");
        Instant lastJoin = Instant.parse("2026-01-01T10:00:00Z");

        assertThrows(
                IllegalArgumentException.class,
                () -> new PlayerProfile(
                        uniqueId,
                        "Kilian",
                        firstJoin,
                        lastJoin,
                        container
                )
        );
    }

    @Test
    void shouldRejectNullUniqueId() {

        ComponentRegistry registry = new ComponentRegistry();
        ComponentContainer container = new ComponentContainer(registry);

        Instant firstJoin = Instant.parse("2026-01-01T10:00:00Z");
        Instant lastJoin = Instant.parse("2026-01-02T10:00:00Z");

        assertThrows(
                NullPointerException.class,
                () -> new PlayerProfile(
                        null,
                        "Kilian",
                        firstJoin,
                        lastJoin,
                        container
                )
        );
    }

    @Test
    void shouldUpdateUsername() {

        // Arrange
        ComponentRegistry registry = new ComponentRegistry();
        ComponentContainer container = new ComponentContainer(registry);

        PlayerProfile profile = new PlayerProfile(
                UUID.randomUUID(),
                "OldName",
                Instant.parse("2026-01-01T10:00:00Z"),
                Instant.parse("2026-01-01T10:00:00Z"),
                container
        );

        // Act
        profile.updateUsername("NewName");

        // Assert
        assertEquals("NewName", profile.getUsername());
    }

    @Test
    void shouldRejectBlankUsernameUpdate() {

        // Arrange
        ComponentRegistry registry = new ComponentRegistry();
        ComponentContainer container = new ComponentContainer(registry);

        PlayerProfile profile = new PlayerProfile(
                UUID.randomUUID(),
                "Kilian",
                Instant.parse("2026-01-01T10:00:00Z"),
                Instant.parse("2026-01-01T10:00:00Z"),
                container
        );

        // Act + Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> profile.updateUsername("   ")
        );
    }

    @Test
    void shouldRecordNewJoin() {

        // Arrange
        ComponentRegistry registry = new ComponentRegistry();
        ComponentContainer container = new ComponentContainer(registry);

        Instant firstJoin = Instant.parse("2026-01-01T09:00:00Z");
        Instant lastJoin = Instant.parse("2026-01-01T10:00:00Z");
        Instant newJoin = Instant.parse("2026-01-01T12:00:00Z");

        PlayerProfile profile = new PlayerProfile(
                UUID.randomUUID(),
                "Kilian",
                firstJoin,
                lastJoin,
                container
        );

        // Act
        profile.recordJoin(newJoin);

        // Assert
        assertEquals(newJoin, profile.getLastJoin());
    }

    @Test
    void shouldRejectJoinBeforeLastJoin() {

        // Arrange
        ComponentRegistry registry = new ComponentRegistry();
        ComponentContainer container = new ComponentContainer(registry);

        Instant firstJoin = Instant.parse("2026-01-01T09:00:00Z");
        Instant lastJoin = Instant.parse("2026-01-01T12:00:00Z");
        Instant invalidJoin = Instant.parse("2026-01-01T11:00:00Z");

        PlayerProfile profile = new PlayerProfile(
                UUID.randomUUID(),
                "Kilian",
                firstJoin,
                lastJoin,
                container
        );

        // Act + Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> profile.recordJoin(invalidJoin)
        );

        assertEquals(lastJoin, profile.getLastJoin());
    }

}
