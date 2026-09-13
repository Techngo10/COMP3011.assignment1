package comp3011.assignment1.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

class GlobalStatsServiceTest {

    @Test
    void statisticsStartAtZero() {
        GlobalStatsService service = new GlobalStatsService();

        assertEquals(0, service.getInputTokens());
        assertEquals(0, service.getOutputTokens());
    }

    @Test
    void usageIsAccumulatedCorrectly() {
        GlobalStatsService service = new GlobalStatsService();

        service.addUsage(100, 20);
        service.addUsage(50, 10);

        assertEquals(150, service.getInputTokens());
        assertEquals(30, service.getOutputTokens());
    }

    @Test
    void concurrentUpdatesAreThreadSafe() throws InterruptedException {
        GlobalStatsService service = new GlobalStatsService();

        int numberOfRequests = 200;

        ExecutorService executor =
                Executors.newFixedThreadPool(20);

        for (int i = 0; i < numberOfRequests; i++) {
            executor.submit(() -> {
                service.addUsage(10, 2);
            });
        }

        executor.shutdown();

        boolean completed =
                executor.awaitTermination(
                        5,
                        TimeUnit.SECONDS
                );

        if (!completed) {
            throw new AssertionError(
                    "Concurrent tasks did not complete in time"
            );
        }

        assertEquals(2000, service.getInputTokens());
        assertEquals(400, service.getOutputTokens());
    }
}