package dev.simplified.util;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LazyTest {

    @Nested
    class SingleThreaded {

        @Test
        void initializerRunsOnFirstGetOnly() {
            AtomicInteger count = new AtomicInteger();
            Lazy<String> lazy = Lazy.of(() -> {
                count.incrementAndGet();
                return "value";
            });

            assertEquals(0, count.get());
            assertEquals("value", lazy.get());
            assertEquals(1, count.get());
            assertEquals("value", lazy.get());
            assertEquals("value", lazy.get());
            assertEquals(1, count.get());
        }

        @Test
        void cachedValueIsSameReference() {
            Object instance = new Object();
            Lazy<Object> lazy = Lazy.of(() -> instance);

            assertSame(instance, lazy.get());
            assertSame(instance, lazy.get());
        }

        @Test
        void nullValueIsMemoized() {
            AtomicInteger count = new AtomicInteger();
            Lazy<String> lazy = Lazy.of(() -> {
                count.incrementAndGet();
                return null;
            });

            assertNull(lazy.get());
            assertNull(lazy.get());
            assertEquals(1, count.get());
        }

        @Test
        void initializerExceptionPropagatesAndAllowsRetry() {
            AtomicInteger count = new AtomicInteger();
            Lazy<String> lazy = Lazy.of(() -> {
                if (count.incrementAndGet() == 1)
                    throw new IllegalStateException("boom");

                return "recovered";
            });

            assertThrows(IllegalStateException.class, lazy::get);
            assertEquals("recovered", lazy.get());
            assertEquals(2, count.get());
        }

    }

    @Nested
    class Concurrency {

        @Test
        void initializerRunsOnceUnderContention() throws Exception {
            int threadCount = 16;
            AtomicInteger initializerCalls = new AtomicInteger();
            CountDownLatch start = new CountDownLatch(1);

            Lazy<Integer> lazy = Lazy.of(() -> {
                int n = initializerCalls.incrementAndGet();

                try {
                    Thread.sleep(5);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }

                return n;
            });

            ExecutorService pool = Executors.newFixedThreadPool(threadCount);

            try {
                Future<?>[] futures = new Future<?>[threadCount];

                for (int i = 0; i < threadCount; i++) {
                    futures[i] = pool.submit(() -> {
                        start.await();
                        return lazy.get();
                    });
                }

                start.countDown();

                Integer first = (Integer) futures[0].get(5, TimeUnit.SECONDS);

                for (int i = 1; i < threadCount; i++)
                    assertEquals(first, futures[i].get(5, TimeUnit.SECONDS));

                assertEquals(1, initializerCalls.get());
            } finally {
                pool.shutdownNow();
            }
        }

    }

}
