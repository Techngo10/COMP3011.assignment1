package comp3011.assignment1.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import comp3011.assignment1.service.GlobalStatsService;

class GlobalStatsControllerTest {

    @Test
    void statsEndpointReturnsCurrentTotals() {
        GlobalStatsService service =
                new GlobalStatsService();

        service.addUsage(500, 75);

        GlobalStatsController controller =
                new GlobalStatsController(service);

        GlobalStatsController.GlobalStatsResponse response =
                controller.getGlobalStats();

        assertEquals(500, response.inputTokens());
        assertEquals(75, response.outputTokens());
    }
}