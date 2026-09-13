## Testing

The project includes automated regression tests for key functional
and non-functional behaviour.

### Global Statistics

Tests verify that token counters begin at zero, correctly accumulate
multiple transcription usage values, and remain accurate when updated
concurrently. The concurrency test performs 200 simultaneous updates
to verify the thread-safe behaviour of the AtomicLong counters.

### Transcription Controller

The transcription controller is tested independently using a mocked
TranscriptionService. This verifies that uploaded audio is delegated
to the service and that returned transcription text is exposed
correctly without requiring a real OpenAI API request.

### Administration API

Tests verify the structure and values returned by the uptime endpoint,
as well as the HTTP 202 Accepted and HTTP 409 Conflict behaviour of
the graceful shutdown endpoint.

Tests can be executed with:

    ./mvnw.cmd test

A full application build, including the regression tests, can be run
with:

    ./mvnw.cmd clean package