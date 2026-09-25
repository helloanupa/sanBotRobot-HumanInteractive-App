package com.ayesha3565.voicemobile

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.AudioManager
import android.media.MediaRecorder
import android.util.Base64
import android.util.Log
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.facebook.react.module.annotations.ReactModule
import java.util.ArrayDeque
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Streams backend Gemini PCM chunks directly to the Sanbot robot speaker.
 * Captures microphone PCM audio from Sanbot hardware mic array and sends to JS.
 */
@ReactModule(name = PcmAudioModule.NAME)
class PcmAudioModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
  companion object {
    const val NAME = "PcmAudio"
    private const val TAG = "PcmAudio"
    private const val DEFAULT_SAMPLE_RATE = 24_000
    private const val BYTES_PER_PCM_FRAME = 2

    private const val START_BUFFER_MS = 240
    private const val MAX_BUFFER_MS = 8_000
  }

  private val queueLock = Object()
  private val trackLock = Object()
  private val queue = ArrayDeque<ByteArray>()
  private var queuedBytes = 0
  private val executor = Executors.newSingleThreadExecutor()
  private val inputExecutor = Executors.newSingleThreadExecutor()
  private val draining = AtomicBoolean(false)
  @Volatile private var playbackGeneration = 0L
  @Volatile private var outputSampleRate = DEFAULT_SAMPLE_RATE
  @Volatile private var audioTrack: AudioTrack? = null
  @Volatile private var audioRecord: AudioRecord? = null
  @Volatile private var inputRunning = false
  private val audioManager = reactContext.getSystemService(android.content.Context.AUDIO_SERVICE) as AudioManager

  override fun getName() = NAME

  @ReactMethod
  fun enqueue(base64Pcm: String, sampleRate: Int) {
    if (sampleRate !in setOf(8_000, 16_000, 24_000, 48_000)) return

    val pcm = try {
      Base64.decode(base64Pcm, Base64.DEFAULT)
    } catch (_: IllegalArgumentException) {
      return
    }

    if (pcm.isEmpty() || pcm.size % BYTES_PER_PCM_FRAME != 0) return

    if (pcm.size > maxBufferBytes(sampleRate)) {
      Log.w(TAG, "Ignoring oversized PCM frame: ${pcm.size} bytes at $sampleRate Hz")
      return
    }

    configureOutputSampleRate(sampleRate)

    synchronized(queueLock) {
      while (queuedBytes + pcm.size > maxBufferBytes()) {
        try {
          queueLock.wait()
        } catch (_: InterruptedException) {
          Thread.currentThread().interrupt()
          return
        }
      }
      queue.addLast(pcm)
      queuedBytes += pcm.size
      queueLock.notifyAll()
    }
    scheduleDrain()
  }

  @ReactMethod
  fun clear() {
    playbackGeneration += 1
    synchronized(queueLock) {
      queue.clear()
      queuedBytes = 0
      queueLock.notifyAll()
    }
    withCurrentTrack("clear") { track ->
      track.pause()
      track.flush()
      track.play()
    }
  }

  @ReactMethod
  fun release() {
    playbackGeneration += 1
    synchronized(queueLock) {
      queue.clear()
      queuedBytes = 0
      queueLock.notifyAll()
    }
    releaseTrack()
    stopInput()
  }

  /**
   * Captures microphone audio on Sanbot S1-B2 robot hardware.
   * Uses MODE_NORMAL and VOICE_RECOGNITION audio source to engage Sanbot mic array.
   */
  @ReactMethod
  fun startInput() {
    if (inputRunning) return

    // Keep MODE_NORMAL so Sanbot robot mic & body speaker hardware route correctly
    audioManager.mode = AudioManager.MODE_NORMAL
    @Suppress("DEPRECATION")
    audioManager.isSpeakerphoneOn = true

    // Ensure STREAM_MUSIC volume is audible on Sanbot main body speakers
    try {
      val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
      val currentVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
      if (currentVol < maxVol / 3) {
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (maxVol * 0.85).toInt(), 0)
        Log.i(TAG, "🔊 Unmuted Sanbot STREAM_MUSIC volume to ${(maxVol * 0.85).toInt()}/$maxVol")
      }
    } catch (e: Exception) {
      Log.w(TAG, "Unable to adjust STREAM_MUSIC volume", e)
    }

    val sampleRate = 16_000
    val minBuffer = AudioRecord.getMinBufferSize(
      sampleRate,
      AudioFormat.CHANNEL_IN_MONO,
      AudioFormat.ENCODING_PCM_16BIT
    )
    if (minBuffer <= 0) {
      Log.e(TAG, "AudioRecord.getMinBufferSize failed: $minBuffer")
      return
    }

    val bufferSize = maxOf(minBuffer * 4, 6_400)

    // Try VOICE_RECOGNITION first (activates Sanbot hardware mic array), then MIC, then DEFAULT
    val sources = intArrayOf(
      MediaRecorder.AudioSource.VOICE_RECOGNITION,
      MediaRecorder.AudioSource.MIC,
      MediaRecorder.AudioSource.DEFAULT
    )

    var record: AudioRecord? = null
    for (source in sources) {
      try {
        val candidate = AudioRecord(
          source,
          sampleRate,
          AudioFormat.CHANNEL_IN_MONO,
          AudioFormat.ENCODING_PCM_16BIT,
          bufferSize
        )
        if (candidate.state == AudioRecord.STATE_INITIALIZED) {
          record = candidate
          Log.i(TAG, "🎤 AudioRecord initialized successfully with audioSource=$source")
          break
        } else {
          candidate.release()
        }
      } catch (e: Exception) {
        Log.w(TAG, "Failed AudioRecord init for source=$source: ${e.message}")
      }
    }

    if (record == null) {
      Log.e(TAG, "❌ Failed to initialize AudioRecord with any audio source!")
      return
    }

    audioRecord = record
    inputRunning = true
    record.startRecording()

    inputExecutor.execute {
      val buffer = ByteArray(3_200) // 100 ms at 16kHz PCM16 mono
      var consecutiveSilenceCount = 0
      while (inputRunning && audioRecord === record) {
        val bytesRead = record.read(buffer, 0, buffer.size)
        if (bytesRead > 0) {
          var allZeros = true
          for (i in 0 until bytesRead) {
            if (buffer[i].toInt() != 0) {
              allZeros = false
              break
            }
          }
          if (allZeros) {
            consecutiveSilenceCount++
            if (consecutiveSilenceCount % 50 == 1) {
              Log.w(TAG, "🎤 Mic capturing zeros (silence count: $consecutiveSilenceCount)")
            }
          } else {
            if (consecutiveSilenceCount > 0) {
              Log.i(TAG, "🎤 Voice audio detected! Silence reset.")
            }
            consecutiveSilenceCount = 0
          }

          val event = Arguments.createMap().apply {
            putString("data", Base64.encodeToString(buffer.copyOf(bytesRead), Base64.NO_WRAP))
            putInt("bytes", bytesRead)
          }
          reactApplicationContext
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
            .emit("voicePcmInput", event)
        }
      }
    }
  }

  @ReactMethod
  fun stopInput() {
    inputRunning = false
    audioRecord?.let { record ->
      try {
        record.stop()
      } catch (_: IllegalStateException) {
      }
      record.release()
    }
    audioRecord = null
    audioManager.mode = AudioManager.MODE_NORMAL
  }

  @ReactMethod
  fun addListener(eventName: String) = Unit

  @ReactMethod
  fun removeListeners(count: Int) = Unit

  private fun scheduleDrain() {
    if (!draining.compareAndSet(false, true)) return
    executor.execute {
      val generation = playbackGeneration
      try {
        val track = getOrCreateTrack() ?: run {
          synchronized(queueLock) {
            queue.clear()
            queuedBytes = 0
            queueLock.notifyAll()
          }
          return@execute
        }
        waitForInitialBuffer(generation)
        if (generation != playbackGeneration) return@execute
        if (!startPlayback(track, generation)) return@execute
        while (true) {
          val pcm = takeNextPcm(generation) ?: break
          if (generation != playbackGeneration) continue
          var offset = 0
          while (offset < pcm.size && generation == playbackGeneration) {
            val written = writePcm(track, pcm, offset, pcm.size - offset, generation)
            if (written <= 0) break
            offset += written
          }
        }
      } catch (error: RuntimeException) {
        Log.e(TAG, "PCM playback failed", error)
      } catch (error: LinkageError) {
        Log.e(TAG, "PCM playback API linkage failed", error)
      } finally {
        draining.set(false)
        synchronized(queueLock) {
          if (queue.isNotEmpty()) scheduleDrain()
        }
      }
    }
  }

  private fun waitForInitialBuffer(generation: Long) {
    synchronized(queueLock) {
      while (
        generation == playbackGeneration &&
        queue.isNotEmpty() &&
        queuedBytes < startBufferBytes()
      ) {
        try {
          queueLock.wait()
        } catch (_: InterruptedException) {
          Thread.currentThread().interrupt()
          return
        }
      }
    }
  }

  private fun takeNextPcm(generation: Long): ByteArray? = synchronized(queueLock) {
    while (generation == playbackGeneration && queue.isEmpty()) {
      try {
        queueLock.wait()
      } catch (_: InterruptedException) {
        Thread.currentThread().interrupt()
        return@synchronized null
      }
    }

    if (generation != playbackGeneration) return@synchronized null

    queue.removeFirst().also { pcm ->
      queuedBytes -= pcm.size
      queueLock.notifyAll()
    }
  }

  private fun getOrCreateTrack(): AudioTrack? {
    audioTrack?.let { return it }
    synchronized(this) {
      audioTrack?.let { return it }
      val sampleRate = outputSampleRate
      val minBuffer = AudioTrack.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_OUT_MONO,
        AudioFormat.ENCODING_PCM_16BIT
      )
      if (minBuffer <= 0) {
        Log.w(TAG, "Unsupported PCM output: sampleRate=$sampleRate, minBuffer=$minBuffer")
        return null
      }

      val bufferSize = maxOf(minBuffer * 8, startBufferBytes(sampleRate))
      val track = try {
        // Use USAGE_MEDIA on Sanbot S1-B2 robot to route sound to main body speakers (STREAM_MUSIC)
        AudioTrack(
          AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build(),
          AudioFormat.Builder()
            .setSampleRate(sampleRate)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .build(),
          bufferSize,
          AudioTrack.MODE_STREAM,
          AudioManager.AUDIO_SESSION_ID_GENERATE
        )
      } catch (error: IllegalArgumentException) {
        Log.e(TAG, "Invalid PCM output configuration: sampleRate=$sampleRate, bufferSize=$bufferSize", error)
        return null
      } catch (error: UnsupportedOperationException) {
        Log.e(TAG, "PCM output is unsupported by this device", error)
        return null
      }

      if (track.state != AudioTrack.STATE_INITIALIZED) {
        Log.w(TAG, "AudioTrack failed to initialize: sampleRate=$sampleRate, bufferSize=$bufferSize")
        track.release()
        return null
      }

      synchronized(trackLock) {
        audioTrack = track
      }
      return track
    }
  }

  private fun startPlayback(track: AudioTrack, generation: Long): Boolean = synchronized(trackLock) {
    if (audioTrack !== track || generation != playbackGeneration) return@synchronized false
    try {
      if (track.playState != AudioTrack.PLAYSTATE_PLAYING) track.play()
      true
    } catch (error: IllegalStateException) {
      Log.e(TAG, "Unable to start PCM playback", error)
      false
    }
  }

  private fun writePcm(
    track: AudioTrack,
    pcm: ByteArray,
    offset: Int,
    size: Int,
    generation: Long
  ): Int = synchronized(trackLock) {
    if (audioTrack !== track || generation != playbackGeneration) return@synchronized 0
    try {
      track.write(pcm, offset, size)
    } catch (error: IllegalStateException) {
      Log.e(TAG, "Unable to write PCM data", error)
      0
    }
  }

  private fun withCurrentTrack(operation: String, block: (AudioTrack) -> Unit) {
    synchronized(trackLock) {
      val track = audioTrack ?: return
      try {
        block(track)
      } catch (error: IllegalStateException) {
        Log.w(TAG, "AudioTrack $operation failed", error)
      }
    }
  }

  private fun releaseTrack() {
    synchronized(trackLock) {
      val track = audioTrack ?: return
      audioTrack = null
      try {
        track.release()
      } catch (error: IllegalStateException) {
        Log.w(TAG, "AudioTrack release failed", error)
      }
    }
  }

  private fun configureOutputSampleRate(sampleRate: Int) {
    if (sampleRate == outputSampleRate) return

    synchronized(this) {
      if (sampleRate == outputSampleRate) return

      playbackGeneration += 1
      synchronized(queueLock) {
        queue.clear()
        queuedBytes = 0
        queueLock.notifyAll()
      }
      withCurrentTrack("reconfigure") { track ->
        track.pause()
        track.flush()
      }
      releaseTrack()
      outputSampleRate = sampleRate
    }
  }

  private fun startBufferBytes(sampleRate: Int = outputSampleRate) =
    sampleRate * BYTES_PER_PCM_FRAME * START_BUFFER_MS / 1_000

  private fun maxBufferBytes(sampleRate: Int = outputSampleRate) =
    sampleRate * BYTES_PER_PCM_FRAME * MAX_BUFFER_MS / 1_000
}
