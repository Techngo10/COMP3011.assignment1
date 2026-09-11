const microphoneSelect = document.getElementById("microphone");
const recordButton = document.getElementById("record-button");
const recordTitle = recordButton.querySelector(".record-title");
const recordSubtitle = recordButton.querySelector(".record-subtitle");
const timerDisplay = document.getElementById("timer");
const transcriptDisplay = document.getElementById("transcript");

let mediaRecorder = null;
let audioChunks = [];
let currentStream = null;

let recordingStartTime = null;
let timerInterval = null;


// Ask for microphone permission and populate the selector.
async function loadMicrophones() {
    try {
        // Browser usually hides microphone names until permission is granted.
        const permissionStream =
            await navigator.mediaDevices.getUserMedia({ audio: true });

        permissionStream.getTracks().forEach(track => track.stop());

        const devices = await navigator.mediaDevices.enumerateDevices();

        const microphones =
            devices.filter(device => device.kind === "audioinput");

        microphoneSelect.innerHTML = "";

        microphones.forEach((microphone, index) => {
            const option = document.createElement("option");

            option.value = microphone.deviceId;
            option.textContent =
                microphone.label || `Microphone ${index + 1}`;

            microphoneSelect.appendChild(option);
        });

    } catch (error) {
        console.error("Could not access microphones:", error);

        microphoneSelect.innerHTML =
            "<option>Microphone access unavailable</option>";
    }
}


// Determine a recording format supported by the browser.
function getSupportedMimeType() {
    const types = [
        "audio/webm;codecs=opus",
        "audio/webm",
        "audio/ogg;codecs=opus",
        "audio/ogg"
    ];

    return types.find(type => MediaRecorder.isTypeSupported(type)) || "";
}


// Start recording.
async function startRecording() {
    try {
        console.log("Starting recording...");

        const selectedDevice = microphoneSelect.value;
        console.log("Selected microphone:", selectedDevice);

        const constraints = {
            audio: selectedDevice
                ? { deviceId: { exact: selectedDevice } }
                : true
        };

        currentStream =
            await navigator.mediaDevices.getUserMedia(constraints);

        const mimeType = getSupportedMimeType();
        console.log("Recording MIME type:", mimeType);

        const options = mimeType ? { mimeType } : {};

        mediaRecorder = new MediaRecorder(currentStream, options);

        audioChunks = [];

        mediaRecorder.addEventListener("dataavailable", event => {
            if (event.data.size > 0) {
                console.log("Audio chunk received:", event.data.size, "bytes");
                audioChunks.push(event.data);
            }
        });

        mediaRecorder.addEventListener("stop", handleRecordingStopped);

        mediaRecorder.start();

        console.log("Recording started");

        startTimer();
        showRecordingState();

    } catch (error) {
        console.error("Unable to start recording:", error);

        transcriptDisplay.textContent =
            "Unable to access the selected microphone.";
    }
}


// Stop recording.
function stopRecording() {
    if (!mediaRecorder || mediaRecorder.state !== "recording") {
        return;
    }

    console.log("Stopping recording...");

    mediaRecorder.stop();

    stopTimer();

    if (currentStream) {
        currentStream.getTracks().forEach(track => track.stop());
        currentStream = null;
    }

    showTranscribingState();
}


// Called once MediaRecorder has finished creating the audio.
async function handleRecordingStopped() {
    const mimeType = mediaRecorder.mimeType || "audio/webm";

    const audioBlob = new Blob(audioChunks, {
        type: mimeType
    });

    console.log("Recording completed");
    console.log("Audio size:", audioBlob.size, "bytes");
    console.log("Audio type:", audioBlob.type);

    const extension =
        mimeType.includes("ogg") ? "ogg" : "webm";

    await sendForTranscription(audioBlob, extension);
}

// Send the finished audio file to our Spring backend.
async function sendForTranscription(audioBlob, extension) {
    const formData = new FormData();

    formData.append(
        "file",
        audioBlob,
        `recording.${extension}`
    );

    transcriptDisplay.textContent = "Transcribing...";

    console.log("Sending audio to /api/transcribe");

    try {
        const response = await fetch("/api/transcribe", {
            method: "POST",
            body: formData
        });

        console.log("Server response status:", response.status);

        if (!response.ok) {
            throw new Error(
                `Server returned ${response.status}`
            );
        }

        const result = await response.json();

        console.log("Transcription response:", result);

        transcriptDisplay.textContent =
            result.text || "No transcription returned.";

    } catch (error) {
        console.error("Transcription failed:", error);

        transcriptDisplay.textContent =
            "Unable to transcribe the recording.";

    } finally {
        showIdleState();
    }
}


// Timer

function startTimer() {
    recordingStartTime = Date.now();

    timerDisplay.textContent = "00:00";

    timerInterval = setInterval(updateTimer, 250);
}


function updateTimer() {
    const elapsed =
        Math.floor((Date.now() - recordingStartTime) / 1000);

    const minutes =
        Math.floor(elapsed / 60)
            .toString()
            .padStart(2, "0");

    const seconds =
        (elapsed % 60)
            .toString()
            .padStart(2, "0");

    timerDisplay.textContent = `${minutes}:${seconds}`;
}


function stopTimer() {
    clearInterval(timerInterval);
    timerInterval = null;
}


// UI states

function showRecordingState() {
    recordButton.classList.add("recording");

    recordTitle.textContent = "STOP RECORDING";
    recordSubtitle.textContent = "Recording in progress...";
}


function showTranscribingState() {
    recordButton.classList.remove("recording");

    recordButton.disabled = true;

    recordTitle.textContent = "TRANSCRIBING...";
    recordSubtitle.textContent = "Please wait";
}


function showIdleState() {
    recordButton.classList.remove("recording");

    recordButton.disabled = false;

    recordTitle.textContent = "PRESS TO RECORD";
    recordSubtitle.textContent = "Click to begin recording";
}


// Record button toggles start / stop.
recordButton.addEventListener("click", () => {
    if (
        mediaRecorder &&
        mediaRecorder.state === "recording"
    ) {
        stopRecording();
    } else {
        startRecording();
    }
});


loadMicrophones();