package fr.kilian.api.component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class ComponentRegistry {

    private final ConcurrentMap<String, Entry<?>> components =
            new ConcurrentHashMap<>();

    public <T> void register(
            ComponentKey<T> key,
            ComponentCodec<T> codec
    ) {
        Objects.requireNonNull(codec, "codec cannot be null");

        registerInternal(key, codec);
    }

    public <T> void register(
            ComponentKey<T> key
    ) {

        registerInternal(key, null);

    }

    public Optional<ComponentKey<?>> find(String id) {
        Objects.requireNonNull(id, "id cannot be null");

        Entry<?> entry = components.get(id);

        if (entry == null) {
            return Optional.empty();
        }

        return Optional.of(entry.key);
    }

    public <T> Optional<ComponentKey<T>> find(
            String id,
            Class<T> type
    ){

        Objects.requireNonNull(id, "id cannot be null");
        Objects.requireNonNull(type, "type cannot be null");

        Entry<?> existing = components.get(id);

        if (existing == null) {
            return Optional.empty();
        }

        ComponentKey<?> key = existing.key;

        if (key.getType() != type) {
            throw new IllegalArgumentException(
                    "Component '" + id + "' is registered with type '"
                            + key.getType().getSimpleName()
                            + "' but was requested as '"
                            + type.getSimpleName()
                            + "'"
            );
        }

        @SuppressWarnings("unchecked")
        ComponentKey<T> typedKey = (ComponentKey<T>) key;

        return Optional.of(typedKey);
    }

    public boolean isRegistered(
            ComponentKey<?> key
    ) {
        Objects.requireNonNull(key, "key cannot be null");

        String id = key.id();

        Entry<?> registered = components.get(id);

        if (registered == null) {
            return false;
        }

        return key.equals(registered.key);
    }

    private static final class Entry<T> {

        private final ComponentKey<T> key;
        private final ComponentCodec<T> codec;

        private Entry(
                ComponentKey<T> key,
                ComponentCodec<T> codec
        ) {

            this.key = key;
            this.codec = codec;

        }

    }

    private <T> void registerInternal(
            ComponentKey<T> key,
            ComponentCodec<T> codec
    ) {
        Objects.requireNonNull(key, "key cannot be null");

        Entry<T> entry = new Entry<>(key, codec);

        Entry<?> existing =
                components.putIfAbsent(key.id(), entry);

        if (existing != null) {
            throw new IllegalStateException(
                    "Component '" + key.id() + "' is already registered"
            );
        }
    }

    public <T> Optional<ComponentCodec<T>> findCodec(
            ComponentKey<T> key
    ) {
        Objects.requireNonNull(key, "key cannot be null");

        Entry<?> entry = components.get(key.id());

        if (entry == null) {
            return Optional.empty();
        }

        if (!key.equals(entry.key)) {
            throw new IllegalArgumentException(
                    "Component '" + key.id()
                            + "' is registered with a different type"
            );
        }

        if (entry.codec == null) {
            return Optional.empty();
        }

        @SuppressWarnings("unchecked")
        ComponentCodec<T> codec =
                (ComponentCodec<T>) entry.codec;

        return Optional.of(codec);
    }

    public List<ComponentKey<?>> persistentKeys() {

        List<ComponentKey<?>> keys = new ArrayList<>();

        for (Entry<?> entry : components.values()) {

            if (entry.codec != null) {
                keys.add(entry.key);
            }
        }

        return List.copyOf(keys);
    }

}
