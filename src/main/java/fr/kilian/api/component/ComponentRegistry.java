package fr.kilian.api.component;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class ComponentRegistry {

    private final ConcurrentMap<String, ComponentKey<?>> components = new ConcurrentHashMap<>();

    public void register(
            ComponentKey<?> componentKey
    ){

        Objects.requireNonNull(componentKey, "componentKey cannot be null");
        String id = componentKey.id();

        ComponentKey<?> existing = components.putIfAbsent(id, componentKey);

        if(existing != null){

            throw new IllegalStateException("Component '" + id + "' is already registered with type '" + existing.getType().getSimpleName() + "'");

        }

    }

    public <T> Optional<ComponentKey<T>> find(
            String id,
            Class<T> type
    ){

        Objects.requireNonNull(id, "id cannot be null");
        Objects.requireNonNull(type, "type cannot be null");

        ComponentKey<?> existing = components.get(id);

        if(existing == null){
            return Optional.empty();
        }



        if(existing.getType() != type){
            throw new IllegalArgumentException("Component '" + id + "' is registered with type '" + existing.getType().getSimpleName() + "' but was requested as '" + type.getSimpleName() + "'");

        }

        @SuppressWarnings("unchecked")
        ComponentKey<T> typedKey = (ComponentKey<T>) existing;
        return Optional.of(typedKey);
    }

    public boolean isRegistered(
            ComponentKey<?> key
    ){

        Objects.requireNonNull(key, "key cannot be null");

        String id = key.id();
        ComponentKey<?> registered = components.get(id);

        if(registered == null){
            return false;
        }

        return key.equals(registered);

    }



}
