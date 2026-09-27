package fr.kilian.api.component;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class ComponentRegistryTest {

    @Test
    void shouldRegisterComponent(){

        ComponentRegistry registry = new ComponentRegistry();

        ComponentKey<TestComponent> key =
                new ComponentKey<>(
                        "test",
                        "component",
                        TestComponent.class
                );
        registry.register(key);

        assertThrows(
                IllegalStateException.class,
                () -> registry.register(key)
        );

    }

    @Test
    void shouldFindRegisteredComponent(){

        ComponentRegistry registry = new ComponentRegistry();

        ComponentKey<TestComponent> key =
                new ComponentKey<>(
                        "test",
                        "component",
                        TestComponent.class
                );
        registry.register(key);

        Optional<ComponentKey<TestComponent>> result =
                registry.find(
                        "test:component",
                        TestComponent.class
                );
        assertTrue(result.isPresent());
        assertEquals(key, result.get());
    }

    @Test
    void shouldReturnEmptyWhenComponentIsNotRegistered() {

            ComponentRegistry registry = new ComponentRegistry();

            Optional<ComponentKey<TestComponent>> result =
                    registry.find(
                            "test:missing",
                            TestComponent.class
                    );

            assertTrue(result.isEmpty());

    }

    @Test
    void shouldRejectComponentWithWrongType(){

        ComponentRegistry registry = new ComponentRegistry();

        ComponentKey<TestComponent> key =
                new ComponentKey<>(
                        "test",
                        "component",
                        TestComponent.class
                );
        registry.register(key);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> registry.find(
                        "test:component",
                        AnotherTestComponent.class
                )
        );

        assertTrue(exception.getMessage().contains("test:component"));
    }

    private static final class TestComponent{



    }

    private static final class AnotherTestComponent {
    }
}
