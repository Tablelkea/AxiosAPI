package fr.kilian.api.component;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ComponentKeyTest {

    @Test
    void shouldBeEqualWhenPropertiesAreEqual(){

        ComponentKey<TestComponent> first =
                new ComponentKey<>(
                        "test",
                        "component",
                        TestComponent.class
                );

        ComponentKey<TestComponent> second =
                new ComponentKey<>(
                        "test",
                        "component",
                        TestComponent.class
                );

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());

    }

    @Test
    void shouldNotBeEqualWhenTypesAreDifferent() {

        ComponentKey<TestComponent> first =
                new ComponentKey<>(
                        "test",
                        "component",
                        TestComponent.class
                );

        ComponentKey<AnotherTestComponent> second =
                new ComponentKey<>(
                        "test",
                        "component",
                        AnotherTestComponent.class
                );

        assertNotEquals(first, second);

    }

    @Test
    void shouldRejectNullNamespace() {

        assertThrows(
                NullPointerException.class,
                () -> new ComponentKey<>(
                        null,
                        "component",
                        TestComponent.class
                )
        );
    }

    @Test
    void shouldRejectNullName() {

        assertThrows(
                NullPointerException.class,
                () -> new ComponentKey<>(
                        "test",
                        null,
                        TestComponent.class
                )
        );
    }

    @Test
    void shouldRejectNullType() {

        assertThrows(
                NullPointerException.class,
                () -> new ComponentKey<>(
                        "test",
                        "component",
                        null
                )
        );
    }

    private static final class TestComponent {
    }

    private static final class AnotherTestComponent {
    }

}
