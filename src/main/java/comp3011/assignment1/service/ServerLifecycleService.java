package comp3011.assignment1.service;

import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class ServerLifecycleService {

    private final Instant serverStart;

    private final AtomicBoolean shutdownRequested =
            new AtomicBoolean(false);

    private final ApplicationContext applicationContext;


    public ServerLifecycleService(
            ApplicationContext applicationContext) {

        this.applicationContext = applicationContext;

        this.serverStart = Instant.ofEpochMilli(
            ManagementFactory
                .getRuntimeMXBean()
                .getStartTime()
        );
    }


    public Instant getServerStart() {
        return serverStart;
    }


    public UptimeInfo getUptime() {

        Instant now = Instant.now();

        Duration uptime =
            Duration.between(serverStart, now);

        double uptimeSeconds =
            uptime.getSeconds()
            + uptime.getNano() / 1_000_000_000.0;

        return new UptimeInfo(
            serverStart,
            now,
            uptimeSeconds
        );
    }


    public boolean requestShutdown() {

        if (!shutdownRequested.compareAndSet(false, true)) {
            return false;
        }

        Thread shutdownThread = new Thread(() -> {

            try {
                // Give the HTTP response time to be returned.
                Thread.sleep(250);

                int exitCode =
                    SpringApplication.exit(
                        applicationContext
                    );

                System.exit(exitCode);

            } catch (InterruptedException exception) {

                Thread.currentThread().interrupt();
            }

        }, "graceful-shutdown");

        shutdownThread.start();

        return true;
    }


    public record UptimeInfo(
        Instant serverStart,
        Instant now,
        double uptimeSeconds
    ) {
    }
}