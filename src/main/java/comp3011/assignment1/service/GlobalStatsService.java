package comp3011.assignment1.service;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

@Service
public class GlobalStatsService {

    private final AtomicLong inputTokens = new AtomicLong(0);
    private final AtomicLong outputTokens = new AtomicLong(0);

    public void addUsage(long input, long output) {
        inputTokens.addAndGet(input);
        outputTokens.addAndGet(output);
    }

    public long getInputTokens() {
        return inputTokens.get();
    }

    public long getOutputTokens() {
        return outputTokens.get();
    }
    
    //http://localhost:8080/api/v1/global/stats
    
    //AtomicLong rather than ordinary provide help with multiple 
    //transcription requests that could occur concurrently. 
    //So two users who finish recordings at the same time, both 
    //requests can safely update with totals without corrupting the 
    //counters.
}