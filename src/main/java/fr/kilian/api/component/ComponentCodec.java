package fr.kilian.api.component;

public interface ComponentCodec<T> {

    String encode(T value);

    T decode(String payload);

}
