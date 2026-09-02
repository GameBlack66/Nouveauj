package com.example.game.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin
import kotlin.random.Random

class GameAudio(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Default)
    private var engineJob: Job? = null

    var isSoundEnabled: Boolean = true
    var isHapticsEnabled: Boolean = true

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    private fun playTone(
        frequencies: FloatArray,
        durationsMs: IntArray,
        volume: Float = 0.5f,
        type: String = "sine"
    ) {
        if (!isSoundEnabled) return
        scope.launch {
            try {
                val sampleRate = 22050
                var totalDuration = 0
                for (d in durationsMs) totalDuration += d
                val totalSamples = (sampleRate * totalDuration / 1000f).toInt()
                val audioData = ShortArray(totalSamples)

                var sampleIndex = 0
                for (step in frequencies.indices) {
                    val freq = frequencies[step]
                    val stepSamples = (sampleRate * durationsMs[step] / 1000f).toInt()
                    var phase = 0.0
                    val phaseIncrement = 2.0 * Math.PI * freq / sampleRate

                    for (i in 0 until stepSamples) {
                        if (sampleIndex >= audioData.size) break
                        val sampleVal = when (type) {
                            "square" -> if (sin(phase) >= 0) 0.8 else -0.8
                            "noise" -> (Random.nextFloat() * 2f - 1f).toDouble()
                            else -> sin(phase)
                        }
                        // Envelope to prevent clicks
                        val fadeFactor = if (i < 100) i / 100.0 else if (i > stepSamples - 200) (stepSamples - i) / 200.0 else 1.0
                        audioData[sampleIndex++] = (sampleVal * Short.MAX_VALUE * volume * fadeFactor).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                        phase += phaseIncrement
                    }
                }

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(audioData.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(audioData, 0, audioData.size)
                track.play()
                delay(totalDuration.toLong() + 50)
                track.release()
            } catch (_: Exception) {
                // Ignore audio errors gracefully
            }
        }
    }

    fun playCoin() {
        playTone(floatArrayOf(987f, 1318f), intArrayOf(70, 120), volume = 0.45f)
        vibrate(30)
    }

    fun playNitro() {
        playTone(floatArrayOf(300f, 550f, 850f, 1100f), intArrayOf(50, 60, 80, 120), volume = 0.6f, type = "square")
        vibrate(80)
    }

    fun playCrash() {
        playTone(floatArrayOf(120f, 80f, 50f), intArrayOf(150, 200, 250), volume = 0.9f, type = "noise")
        vibratePattern(longArrayOf(0, 100, 50, 200))
    }

    fun playNearMiss() {
        playTone(floatArrayOf(600f, 900f), intArrayOf(50, 70), volume = 0.4f)
        vibrate(25)
    }

    fun playUpgrade() {
        playTone(floatArrayOf(440f, 554f, 659f, 880f), intArrayOf(70, 70, 70, 160), volume = 0.5f)
        vibrate(40)
    }

    fun vibrate(durationMs: Long) {
        if (!isHapticsEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (_: Exception) {}
    }

    private fun vibratePattern(pattern: LongArray) {
        if (!isHapticsEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, -1)
            }
        } catch (_: Exception) {}
    }

    fun release() {
        engineJob?.cancel()
    }
}
