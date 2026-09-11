package comp3011.assignment1.service;

import java.io.IOException;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class TranscriptionService {

    public String transcribe(MultipartFile audioFile) throws IOException {
        return "Test transcription";
    }
}