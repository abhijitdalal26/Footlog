# Voice Notes Implementation — Footlog

Covers recording, playback, storage, and the voice-or-type dual input model described in the spec.

---

## API choice: MediaRecorder

**Use MediaRecorder** (Android's built-in high-level recorder), not `AudioRecord`.

| API | When to use |
|---|---|
| `MediaRecorder` | Short voice memos, saves directly to file — perfect for this use case |
| `AudioRecord` | Low-level PCM access needed (DSP, real-time processing) — overkill here |

MediaRecorder records directly to a file, handles compression, and has a simple start/stop API. MediaPlayer then plays the file back. No processing pipeline needed.

---

## Audio format

**Format: AAC audio in MPEG-4 container (.m4a)**

```kotlin
mediaRecorder.apply {
    setAudioSource(MediaRecorder.AudioSource.MIC)
    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
    setAudioEncodingBitRate(64_000)   // 64 kbps — good for voice
    setAudioSamplingRate(44_100)       // 44.1 kHz standard
    setOutputFile(outputFile.absolutePath)
}
```

**Why AAC/M4A:**
- Excellent quality for voice at 64 kbps (~30 KB/minute)
- Natively supported by Android MediaPlayer — no codec installation needed
- Wide compatibility for sharing/playback outside the app
- Much better than AMR (older format) at the same bitrate

**File size estimates:**
- 30-second note at 64 kbps AAC: ~240 KB
- 2-minute note: ~960 KB
- Even 50 notes of 2 minutes each = ~48 MB — manageable in app-private storage

---

## File storage location

**Use app-private external files directory:**

```kotlin
val voiceNoteDir = File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "voice_notes")
voiceNoteDir.mkdirs()
val outputFile = File(voiceNoteDir, "note_${noteId}.m4a")
```

**Why external files dir (not internal):**
- On API 29+, `getExternalFilesDir()` requires no permissions (scoped storage)
- Files survive app restarts (unlike cache dir)
- Large enough for audio files (internal storage is limited)
- Automatically deleted when app is uninstalled

**No permission needed** for `getExternalFilesDir()` on API 29+ (our MinSDK is 26, so API 26-28 still technically needs `READ_EXTERNAL_STORAGE` for reading back, but `WRITE_EXTERNAL_STORAGE` with `maxSdkVersion="28"` handles the write side). In practice, for voice notes stored and played within the app, internal storage (`getFilesDir()`) works too and avoids all permission complexity — simpler choice.

**Simpler alternative: internal storage**
```kotlin
val voiceNoteDir = File(context.filesDir, "voice_notes")
```
No permissions needed at all. Slightly smaller capacity but sufficient for voice notes. Recommend this for v1.

---

## Recording implementation (ViewModel + Compose)

```kotlin
// VoiceRecorderState held in ViewModel
class NoteViewModel : ViewModel() {
    private var mediaRecorder: MediaRecorder? = null
    val isRecording = MutableStateFlow(false)
    val recordingDurationMs = MutableStateFlow(0L)

    fun startRecording(context: Context, noteId: String) {
        val file = getVoiceNoteFile(context, noteId)
        mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION") MediaRecorder()
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(64_000)
            setAudioSamplingRate(44_100)
            setOutputFile(file.absolutePath)
            prepare()
            start()
        }
        isRecording.value = true
        // Start duration timer in viewModelScope
    }

    fun stopRecording(): String {
        mediaRecorder?.apply { stop(); release() }
        mediaRecorder = null
        isRecording.value = false
        return currentNoteId // caller persists note to Room
    }

    override fun onCleared() {
        super.onCleared()
        mediaRecorder?.release() // safety cleanup
    }
}
```

**Amplitude visualization** (pulsing ring animation while recording):
```kotlin
// Poll in a coroutine while recording
val amplitude = mediaRecorder?.maxAmplitude ?: 0
// Map to ring scale: scale = 1f + (amplitude / 32767f) * 0.3f
```

---

## Playback implementation

```kotlin
class NoteViewModel : ViewModel() {
    private var mediaPlayer: MediaPlayer? = null
    val isPlaying = MutableStateFlow(false)
    val playbackPositionMs = MutableStateFlow(0L)

    fun startPlayback(filePath: String) {
        mediaPlayer = MediaPlayer().apply {
            setDataSource(filePath)
            prepare()  // or prepareAsync() for network URIs
            start()
            setOnCompletionListener { isPlaying.value = false }
        }
        isPlaying.value = true
    }

    fun pausePlayback() {
        mediaPlayer?.pause()
        isPlaying.value = false
    }

    fun stopPlayback() {
        mediaPlayer?.apply { stop(); release() }
        mediaPlayer = null
        isPlaying.value = false
        playbackPositionMs.value = 0L
    }

    override fun onCleared() {
        super.onCleared()
        mediaPlayer?.release()
    }
}
```

**In Compose, use `DisposableEffect` to tie player lifecycle to composition:**
```kotlin
DisposableEffect(noteId) {
    onDispose { viewModel.stopPlayback() }
}
```

---

## Voice-to-text (optional transcription)

**Use Android `SpeechRecognizer` — free, no API key.**

```kotlin
val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
}
recognizer.setRecognitionListener(object : RecognitionListener {
    override fun onResults(results: Bundle?) {
        val text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
        // Save transcription alongside or instead of voice file
    }
    // ... other callbacks
})
recognizer.startListening(intent)
```

**Limitations to communicate to user:**
- Best accuracy with internet connection
- Works offline but accuracy drops
- Transcription is real-time (while speaking), not from the saved .m4a file — so it's a second pass, not post-processing
- For post-recording transcription from file: would need a paid API (Google Cloud Speech). Not included in v1.

---

## Dual input: voice or type

The NoteWriting screen supports both. UX pattern:

```
State enum: NoteInputMode { VOICE, TEXT }

Default → VOICE mode
    → Large mic FAB (80dp circle, filled primary color)
    → Tap → starts recording (FAB pulses)
    → Tap again → stops recording, shows playback controls
    → "Transcribe" button appears after recording stops (optional)
    → "Type instead" text button → switches to TEXT mode

TEXT mode
    → Full-width multiline TextField
    → "Record instead" text button → switches back to VOICE mode
    → Save button enabled once text > 0 chars

Saving
    → If VOICE: save Note(type="voice", content=filePath)
    → If TEXT: save Note(type="text", content=text)
    → If VOICE + transcription was run: optionally also save transcribed text in content field alongside filePath
```

---

## Required permissions

```xml
<!-- In AndroidManifest.xml -->
<uses-permission android:name="android.permission.RECORD_AUDIO" />
```

`RECORD_AUDIO` is a dangerous permission — request at runtime the first time the user taps the mic button.

```kotlin
// In NoteWritingScreen
val permissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)

if (!permissionState.status.isGranted) {
    // Show rationale, then:
    permissionState.launchPermissionRequest()
} else {
    viewModel.startRecording(context, noteId)
}
```

Use `accompanist-permissions` or the Compose-native permission APIs (available since `activity-compose 1.9+`).

---

## What's explicitly NOT included in v1

- Paid STT (Google Cloud Speech-to-Text): adds cost and API key management
- Waveform display from .m4a file: requires decoding the audio — use a static placeholder waveform graphic instead
- Background recording while app is minimized: RECORD_AUDIO has foreground-only restrictions on Android 14+
