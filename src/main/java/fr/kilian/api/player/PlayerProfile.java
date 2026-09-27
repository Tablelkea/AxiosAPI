package fr.kilian.api.player;

import fr.kilian.api.component.ComponentContainer;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class PlayerProfile {

    private final UUID uniqueId;
    private volatile String username;

    private final Instant firstJoin;
    private volatile Instant lastJoin;

    private final ComponentContainer components;

    public PlayerProfile(
            UUID uniqueId,
            String username,
            Instant firstJoin,
            Instant lastJoin,
            ComponentContainer components
    ) {

        Objects.requireNonNull(uniqueId, "uniqueId cannot be null");
        Objects.requireNonNull(username, "username cannot be null");
        Objects.requireNonNull(firstJoin, "firstJoin cannot be null");
        Objects.requireNonNull(lastJoin, "lastJoin cannot be null");
        Objects.requireNonNull(components, "components cannot be null");

        if (username.isBlank()) {
            throw new IllegalArgumentException("username cannot be blank");
        }

        if (lastJoin.isBefore(firstJoin)) {
            throw new IllegalArgumentException("lastJoin cannot be before firstJoin");
        }

        this.uniqueId = uniqueId;
        this.username = username;
        this.firstJoin = firstJoin;
        this.lastJoin = lastJoin;
        this.components = components;

    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public String getUsername() {
        return username;
    }

    public Instant getFirstJoin() {
        return firstJoin;
    }

    public Instant getLastJoin() {
        return lastJoin;
    }

    public ComponentContainer getComponents() {
        return components;
    }

    public void updateUsername(String username) {

        Objects.requireNonNull(username, "username cannot be null");

        if(username.isBlank()){
            throw new IllegalArgumentException("username cannot be blank");
        }

        this.username = username;
    }

    public synchronized void recordJoin(Instant joinTime) {

        Objects.requireNonNull(joinTime, "joinTime cannot be null");

        if(joinTime.isBefore(this.lastJoin)){
            throw new IllegalArgumentException("joinTime cannot be before lastJoin");
        }

        this.lastJoin = joinTime;
    }
}
