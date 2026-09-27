package fr.kilian.api.component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ComponentContainer {

    private final ComponentRegistry registry;

    public ComponentContainer(
            ComponentRegistry registry
    ) {
        this.registry = Objects.requireNonNull(registry, "registry cannot be null");
    }

    private final ConcurrentMap<ComponentKey<?>, Object> components = new ConcurrentHashMap<>();

    public <T> void set(
            ComponentKey<T> key,
            T value
    ){

        Objects.requireNonNull(key, "key cannot be null");
        Objects.requireNonNull(value, "value cannot be null");

        if(!registry.isRegistered(key)){
            throw new IllegalStateException("Component '" + key.id() + "' is not registered");
        }

        if(!key.getType().isInstance(value)){
            throw new IllegalArgumentException("Component '"+key.id()+"' expects '"+key.getType().getSimpleName()+"' but received '"+value.getClass().getSimpleName()+"'");

        }

        components.put(key, value);

    }

    public <T> Optional<T> find(
            ComponentKey<T> key
    ) {

        Objects.requireNonNull(key, "key cannot be null");

        if(!registry.isRegistered(key)){
            throw new IllegalStateException("Component '" + key.id() + "' is not registered");
        }

        Object value = components.get(key);

        if(value == null){
            return Optional.empty();
        }

        return Optional.of(key.getType().cast(value));

    }

    public boolean has(
            ComponentKey<?> key
    ) {

        Objects.requireNonNull(key, "key cannot be null");

        return components.containsKey(key);

    }

    public <T> Optional<T> remove(
            ComponentKey<T> key
    ) {

       Objects.requireNonNull(key, "key cannot be null");

        Object removed = components.remove(key);

        if (!registry.isRegistered(key)) {
            throw new IllegalStateException(
                    "Component '" + key.id() + "' is not registered"
            );
        }

       if(removed == null){
           return Optional.empty();
       }

       return Optional.of(key.getType().cast(removed));

    }

    public List<ComponentEntry<?>> snapshot() {

        List<ComponentEntry<?>> snapshot = new ArrayList<>();

        for (Map.Entry<ComponentKey<?>, Object> entry : components.entrySet()) {
            snapshot.add(
                    createEntry(
                            entry.getKey(),
                            entry.getValue()
                    )
            );
        }

        return List.copyOf(snapshot);
    }

    private <T> ComponentEntry<T> createEntry(
            ComponentKey<T> key,
            Object value
    ) {
        T typedValue = key.getType().cast(value);

        return new ComponentEntry<>(
                key,
                typedValue
        );
    }



}
