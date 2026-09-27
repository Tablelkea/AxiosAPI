package fr.kilian.api.component;

import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class ComponentContainerTest {

    @Test
    void shouldSetAndFindComponent(){

        ComponentRegistry registry = new ComponentRegistry();

        ComponentKey<TestComponent> key =
                new ComponentKey<>(
                        "test",
                        "component",
                        TestComponent.class
                );

        registry.register(key);

        ComponentContainer container =
                new ComponentContainer(registry);

        TestComponent component =
                new TestComponent("hello");

        container.set(key, component);

        Optional<TestComponent> result =
                container.find(key);

        assertTrue(result.isPresent());
        assertSame(component, result.get());

    }

    @Test
    void shouldRejectUnregisteredComponent() {
        ComponentRegistry registry = new ComponentRegistry();

        ComponentContainer container =
                new ComponentContainer(registry);

        ComponentKey<TestComponent> key =
                new ComponentKey<>(
                        "test",
                        "component",
                        TestComponent.class
                );

        TestComponent component =
                new TestComponent("hello");

        assertThrows(
                IllegalStateException.class,
                () -> container.set(key, component)
        );
    }

    @Test
    void shouldDetectStoredComponent() {

        // Arrange
        ComponentRegistry registry = new ComponentRegistry();

        ComponentKey<TestComponent> key =
                new ComponentKey<>(
                        "test",
                        "component",
                        TestComponent.class
                );

        registry.register(key);

        ComponentContainer container =
                new ComponentContainer(registry);

        TestComponent component =
                new TestComponent("hello");

        // Act + Assert
        assertFalse(container.has(key));

        container.set(key, component);

        assertTrue(container.has(key));
    }

    @Test
    void shouldRemoveComponent() {

        // Arrange
        ComponentRegistry registry = new ComponentRegistry();

        ComponentKey<TestComponent> key =
                new ComponentKey<>(
                        "test",
                        "component",
                        TestComponent.class
                );

        registry.register(key);

        ComponentContainer container =
                new ComponentContainer(registry);

        TestComponent component =
                new TestComponent("hello");

        container.set(key, component);

        // ACT
        Optional<TestComponent> firstRemoval = container.remove(key);

        // ASSERT
        assertTrue(firstRemoval.isPresent());
        assertSame(component, firstRemoval.get());
        assertFalse(container.has(key));

        Optional<TestComponent> secondRemoval = container.remove(key);

        assertTrue(secondRemoval.isEmpty());

    }

    @Test
    void shouldAllowOnlyOneConcurrentRegistration() throws InterruptedException {

        ComponentRegistry registry = new ComponentRegistry();

        ComponentKey<TestComponent> first =
                new ComponentKey<>(
                        "test",
                        "concurrent",
                        TestComponent.class
                );

        ComponentKey<TestComponent> second =
                new ComponentKey<>(
                        "test",
                        "concurrent",
                        TestComponent.class
                );

        CountDownLatch readyLatch = new CountDownLatch(2);
        CountDownLatch startLatch = new CountDownLatch(1);

        AtomicInteger successes = new AtomicInteger();
        AtomicInteger failures = new AtomicInteger();

        Thread firstThread = new Thread(() -> {
            try {
                readyLatch.countDown();
                startLatch.await();

                registry.register(first);
                successes.incrementAndGet();

            } catch (IllegalStateException exception) {
                failures.incrementAndGet();

            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
        });

        Thread secondThread = new Thread(() -> {
            try {
                readyLatch.countDown();
                startLatch.await();

                registry.register(second);
                successes.incrementAndGet();

            } catch (IllegalStateException exception) {
                failures.incrementAndGet();

            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
        });

        firstThread.start();
        secondThread.start();

        readyLatch.await();

        startLatch.countDown();

        firstThread.join();
        secondThread.join();

        assertEquals(1, successes.get());
        assertEquals(1, failures.get());
        assertTrue(registry.isRegistered(first));
    }

    private static final class TestComponent {

        private final String value;

        private TestComponent(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

}
