package comp3011.assignment1.service;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;

@Service
public class TranscriptionService {

    private static final Logger logger =
            LoggerFactory.getLogger(TranscriptionService.class);

    private static final String OPENAI_URL =
            "https://api.openai.com/v1/audio/transcriptions";

    private static final String MODEL =
            "gpt-4o-mini-transcribe";

    private final RestClient restClient;
    private final String apiKey;

    public TranscriptionService(
            RestClient.Builder restClientBuilder,
            @Value("${OPENAI_API_KEY:}") String apiKey) {

        this.restClient = restClientBuilder.build();
        this.apiKey = apiKey;
    }

    public String transcribe(MultipartFile audioFile)
            throws IOException {

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "OPENAI_API_KEY is not configured");
        }

        String filename = audioFile.getOriginalFilename();

        if (filename == null || filename.isBlank()) {
            filename = "recording.webm";
        }

        final String finalFilename = filename;

        ByteArrayResource audioResource =
                new ByteArrayResource(audioFile.getBytes()) {

                    @Override
                    public String getFilename() {
                        return finalFilename;
                    }
                };

        HttpHeaders fileHeaders = new HttpHeaders();

        if (audioFile.getContentType() != null) {
            fileHeaders.setContentType(
                    MediaType.parseMediaType(
                            audioFile.getContentType()));
        }

        HttpEntity<ByteArrayResource> filePart =
                new HttpEntity<>(
                        audioResource,
                        fileHeaders);

        MultiValueMap<String, Object> requestBody =
                new LinkedMultiValueMap<>();

        requestBody.add("file", filePart);
        requestBody.add("model", MODEL);

        logger.info(
                "Sending transcription request: filename={}, size={} bytes",
                filename,
                audioFile.getSize()
        );

        long startTime = System.currentTimeMillis();

        try {
            OpenAiTranscriptionResponse response =
                    restClient
                        .post()
                        .uri(OPENAI_URL)
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + apiKey)
                        .contentType(
                                MediaType.MULTIPART_FORM_DATA)
                        .body(requestBody)
                        .retrieve()
                        .body(
                                OpenAiTranscriptionResponse.class);

            long duration =
                    System.currentTimeMillis() - startTime;

            logger.info(
                    "OpenAI transcription completed in {} ms",
                    duration);

            if (response == null ||
                    response.text() == null) {

                throw new IllegalStateException(
                        "No transcription was returned");
            }

            return response.text();

        } catch (RestClientException exception) {

            logger.error(
                    "OpenAI transcription request failed: {}",
                    exception.getMessage());

            throw new IllegalStateException(
                    "Transcription service request failed");
        }
    }

    private record OpenAiTranscriptionResponse(
            String text) {
    }
}