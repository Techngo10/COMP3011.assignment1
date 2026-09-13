package comp3011.assignment1.controller;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import comp3011.assignment1.service.ServerLifecycleService;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final ServerLifecycleService lifecycleService;


    public AdminController(
            ServerLifecycleService lifecycleService) {

        this.lifecycleService = lifecycleService;
    }


    @GetMapping("/uptime")
    public UptimeResponse getUptime() {

        ServerLifecycleService.UptimeInfo uptime =
            lifecycleService.getUptime();

        return new UptimeResponse(
            uptime.serverStart(),
            uptime.now(),
            uptime.uptimeSeconds()
        );
    }


    @PostMapping("/shutdown")
    public ResponseEntity<?> shutdown() {

        boolean accepted =
            lifecycleService.requestShutdown();

        if (!accepted) {

            return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(
                    new ErrorResponse(
                        Instant.now(),
                        409,
                        "Conflict",
                        "Graceful shutdown is already in progress.",
                        "/api/v1/admin/shutdown"
                    )
                );
        }

        return ResponseEntity
            .status(HttpStatus.ACCEPTED)
            .body(
                new ShutdownResponse(
                    "Graceful shutdown requested."
                )
            );
    }


    public record UptimeResponse(
        Instant utcServerStart,
        Instant utcNow,
        double serverUptimeSeconds
    ) {
    }


    public record ShutdownResponse(
        String message
    ) {
    }


    public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path
    ) {
    }
}