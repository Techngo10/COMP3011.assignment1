package comp3011.assignment1.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import comp3011.assignment1.service.TranscriptionService;

class TranscriptionControllerTest {

    @Test
    void transcriptionResultIsReturned()
            throws Exception {

        TranscriptionService service =
                mock(TranscriptionService.class);

        MockMultipartFile audio =
                new MockMultipartFile(
                        "file",
                        "recording.webm",
                        "audio/webm",
                        new byte[] { 1, 2, 3, 4 }
                );

        when(service.transcribe(audio))
                .thenReturn("Hello world");

        TranscriptionController controller =
                new TranscriptionController(service);

        TranscriptionController.TranscriptionResponse response =
                controller.transcribe(audio);

        assertEquals(
                "Hello world",
                response.text()
        );
    }
}