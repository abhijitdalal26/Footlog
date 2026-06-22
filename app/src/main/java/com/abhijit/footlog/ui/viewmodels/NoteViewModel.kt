package com.abhijit.footlog.ui.viewmodels

import android.app.Application
import android.media.MediaPlayer
import android.media.MediaRecorder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.abhijit.footlog.data.entity.NoteEntity
import com.abhijit.footlog.data.entity.NoteType
import com.abhijit.footlog.data.repository.SessionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

class NoteViewModel(app: Application, private val sessionId: String) : AndroidViewModel(app) {
    private val repo = SessionRepository(app)

    val existingNote: StateFlow<NoteEntity?> = repo.getNoteForSession(sessionId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _transcription = MutableStateFlow("")
    val transcription: StateFlow<String> = _transcription

    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null
    private var audioFile: File? = null

    fun toggleRecording() {
        if (_isRecording.value) stopRecording() else startRecording()
    }

    private fun startRecording() {
        val dir = File(getApplication<Application>().filesDir, "voice_notes")
        dir.mkdirs()
        val file = File(dir, "${UUID.randomUUID()}.m4a")
        audioFile = file
        try {
            recorder = MediaRecorder(getApplication()).apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
            _isRecording.value = true
        } catch (e: Exception) {
            recorder?.release()
            recorder = null
            audioFile = null
        }
    }

    private fun stopRecording() {
        try {
            recorder?.apply { stop(); release() }
        } catch (_: Exception) {}
        recorder = null
        _isRecording.value = false
    }

    fun togglePlayback() {
        if (_isPlaying.value) {
            player?.pause()
            _isPlaying.value = false
        } else {
            val note = existingNote.value ?: return
            if (player == null) {
                player = MediaPlayer().apply {
                    setDataSource(note.content)
                    prepare()
                    setOnCompletionListener { _isPlaying.value = false }
                }
            }
            player?.start()
            _isPlaying.value = true
        }
    }

    fun saveVoiceNote() {
        val file = audioFile ?: return
        viewModelScope.launch {
            repo.saveNote(NoteEntity(
                id = UUID.randomUUID().toString(),
                sessionId = sessionId,
                type = NoteType.VOICE,
                content = file.absolutePath,
                createdAt = System.currentTimeMillis()
            ))
        }
    }

    fun saveTextNote(text: String) {
        viewModelScope.launch {
            repo.saveNote(NoteEntity(
                id = UUID.randomUUID().toString(),
                sessionId = sessionId,
                type = NoteType.TEXT,
                content = text,
                createdAt = System.currentTimeMillis()
            ))
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (_isRecording.value) stopRecording()
        try { player?.apply { stop(); release() } } catch (_: Exception) {}
    }

    companion object {
        fun Factory(sessionId: String) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(
                modelClass: Class<T>,
                extras: androidx.lifecycle.viewmodel.CreationExtras
            ): T {
                val app = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!
                return NoteViewModel(app, sessionId) as T
            }
        }
    }
}
