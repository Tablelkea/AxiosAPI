package fr.kilian.api.component;

import java.util.Objects;
import java.util.Optional;

public record ComponentEntry<T>(
        ComponentKey<T> key,
        T value
) {

    public ComponentEntry {
        Objects.requireNonNull(key, "key cannot be null");
        Objects.requireNonNull(value, "value cannot be null");

        if (!key.getType().isInstance(value)) {
            throw new IllegalArgumentException(
                    "Component '" + key.id()
                            + "' expects type '"
                            + key.getType().getSimpleName()
                            + "'"
            );
        }
    }

}
