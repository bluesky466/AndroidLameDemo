package me.linjw.demo.lame

import android.Manifest
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.storage.StorageManager
import android.util.Log
import android.view.MotionEvent
import android.widget.Button
import android.widget.TextView
import me.linjw.demo.lame.databinding.ActivityMainBinding
import java.io.File

class MainActivity : AppCompatActivity(), Recorder.IRecordListener {
    companion object {
        init {
            System.loadLibrary("lame")
        }

        private const val TAG = "MainActivity"

        private const val AUDIO_SOURCE = MediaRecorder.AudioSource.MIC
        private const val SAMPLE_RATE = 44100
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO // 单通道
//        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_STEREO // 双通道

        val CHANNEL_COUNT = AudioFormat.Builder()
            .setChannelMask(CHANNEL_CONFIG)
            .build()
            .channelCount
    }

    private lateinit var binding: ActivityMainBinding
    private val recorder = Recorder(this)
    private val encoder = Encoder()

    private val player = MediaPlayer()

    private lateinit var outputFile: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        outputFile = File(filesDir, "output.mp3")
        Log.d(TAG,"outputFile = ${outputFile.absolutePath}")

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        findViewById<Button>(R.id.play).setOnClickListener { playMp3() }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M
            || PERMISSION_GRANTED == checkSelfPermission(Manifest.permission.RECORD_AUDIO)
        ) {
            initRecorder()
        } else {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 0x01)
        }
    }

    private fun playMp3() {
        Log.d(TAG, "playMp3")
        player.reset()
        player.setDataSource(outputFile.absolutePath)
        player.isLooping = false
        player.prepare()
        player.seekTo(0)
        player.start()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissions.forEachIndexed { index, permission ->
            if (permission == Manifest.permission.RECORD_AUDIO
                && grantResults[index] == PERMISSION_GRANTED
            ) {
                initRecorder()
            }
        }
    }

    private fun initRecorder() {
        Log.d(TAG, "initRecorder")
        binding.record.setOnTouchListener { _, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                Log.d(TAG, "start record")
                encoder.start(outputFile, SAMPLE_RATE, CHANNEL_COUNT)
                recorder.start(AUDIO_SOURCE, SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
            } else if (motionEvent.action != MotionEvent.ACTION_MOVE) {
                Log.d(TAG, "stop record")
                recorder.stop()
                encoder.stop()
            }
            false
        }
    }

    override fun onRecord(pcm: ByteArray, dataLen: Int) {
        encoder.encode(pcm, dataLen)
    }
}