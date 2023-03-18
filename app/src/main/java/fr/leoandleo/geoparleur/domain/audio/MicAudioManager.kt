package fr.leoandleo.geoparleur.domain.audio

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.*
import android.util.Log
import androidx.core.content.ContextCompat


// reference: https://dolby.io/blog/recording-audio-on-android-with-examples
// reference: https://twigstechtips.blogspot.com/2013/07/android-enable-noise-cancellation-in.html

// manage microphone recording
class MicAudioManager(ctx: Context) {
    private val TAG: String = "MicAM"

    private var recorder: AudioRecord
    private var audioTrack: AudioTrack
    private var stopped: Boolean = true

    private var buffer : ByteArray
    var ix = 0
    var channelConfig = AudioFormat.CHANNEL_IN_STEREO
    var audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val sampleRate = 44100
    private var minBuffer = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

        init {
            // check microphone
            require(ctx.packageManager.hasSystemFeature(PackageManager.FEATURE_MICROPHONE)) {
                "Microphone is not detected on this device"
            }
            require(
                ContextCompat.checkSelfPermission(
                    ctx,
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                "Microphone recording is not permitted"
            }

            buffer = ByteArray(minBuffer)

            recorder = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                minBuffer
            )

            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()


           val audioFormat: AudioFormat = AudioFormat.Builder()
                .setSampleRate(sampleRate)
                .setEncoding(audioFormat)
                .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                .build()


            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(audioAttributes)
                .setAudioFormat(audioFormat)
                .setBufferSizeInBytes(minBuffer)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .setPerformanceMode(AudioTrack.PERFORMANCE_MODE_LOW_LATENCY)
                .build()

            audioTrack.playbackRate = sampleRate;
        }

    // start recording
    fun start() {
        stopped = false
        recorder.startRecording()

        audioTrack.play();

        // ... loop
        while (!stopped) {
             recorder.read(buffer, 0, buffer.size)
             audioTrack.write(buffer, 0, buffer.size);
        }
    }

    // stop recording
    fun stop() {
        stopped = true
        recorder.stop()
        Log.d(TAG, "stop")
    }

}