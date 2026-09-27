package fr.kilian.api.component;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.regex.Pattern;

public final class ComponentKey<T> {

    private static final Pattern NAMESPACE_PATTERN = Pattern.compile("[a-z0-9][a-z0-9_.-]*");
    private static final Pattern NAME_PATTERN = Pattern.compile("[a-z0-9][a-z0-9_./-]*");

    private final String namespace;
    private final String name;
    private final Class<T> type;

    public ComponentKey(
            String namespace,
            String name,
            Class<T> type
    ){

        Objects.requireNonNull(namespace, "namespace cannot be null");
        Objects.requireNonNull(name, "name cannot be null");
        Objects.requireNonNull(type, "type cannot be null");

        if(!NAMESPACE_PATTERN.matcher(namespace).matches()){
            throw new IllegalArgumentException("Invalid namespace: " + namespace);
        }

        if(!NAME_PATTERN.matcher(name).matches()){
            throw new IllegalArgumentException("Invalid name: " + name);

        }

        this.namespace = namespace;
        this.name = name;
        this.type = type;
    }

    public @NotNull String getNamespace() {
        return namespace;
    }

    public @NotNull String getName() {
        return name;
    }

    public @NotNull Class<T> getType() {
        return type;
    }

    @Override
    public boolean equals(
            Object object
    ){
        if (this == object) {
            return true;
        }

        if (!(object instanceof ComponentKey<?> other)) {
            return false;
        }

        return
                namespace.equals(other.namespace)
                        && name.equals(other.name)
                        && type.equals(other.type);
    }

    @Override
    public int hashCode(){

        return Objects.hash(namespace, name, type);

    }

    public @NotNull String id(){

        return namespace + ":" + name;

    }
}
