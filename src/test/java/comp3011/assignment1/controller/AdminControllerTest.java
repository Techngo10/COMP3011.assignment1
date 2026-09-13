package comp3011.assignment1.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import comp3011.assignment1.service.ServerLifecycleService;

class AdminControllerTest {

    @Test
    void uptimeReturnsExpectedValues() {
        ServerLifecycleService service =
                mock(ServerLifecycleService.class);

        Instant start =
                Instant.parse("2026-09-13T01:00:00Z");

        Instant now =
                Instant.parse("2026-09-13T01:01:30Z");

        when(service.getUptime())
                .thenReturn(
                    new ServerLifecycleService.UptimeInfo(
                        start,
                        now,
                        90.0
                    )
                );

        AdminController controller =
                new AdminController(service);

        AdminController.UptimeResponse response =
                controller.getUptime();

        assertEquals(
                start,
                response.utcServerStart()
        );

        assertEquals(
                now,
                response.utcNow()
        );

        assertEquals(
                90.0,
                response.serverUptimeSeconds()
        );
    }

    @Test
    void shutdownReturnsAcceptedWhenSuccessful() {
        ServerLifecycleService service =
                mock(ServerLifecycleService.class);

        when(service.requestShutdown())
                .thenReturn(true);

        AdminController controller =
                new AdminController(service);

        ResponseEntity<?> response =
                controller.shutdown();

        assertEquals(
                HttpStatus.ACCEPTED,
                response.getStatusCode()
        );
    }

    @Test
    void repeatedShutdownReturnsConflict() {
        ServerLifecycleService service =
                mock(ServerLifecycleService.class);

        when(service.requestShutdown())
                .thenReturn(false);

        AdminController controller =
                new AdminController(service);

        ResponseEntity<?> response =
                controller.shutdown();

        assertEquals(
                HttpStatus.CONFLICT,
                response.getStatusCode()
        );
    }
}