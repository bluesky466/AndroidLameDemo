package me.linjw.demo.lame

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import java.io.File
import java.io.FileOutputStream


class Recorder(private val listener: IRecordListener) {
    private var recorder: AudioRecord? = null
    private var recorderThread: RecordThread? = null
    private lateinit var buffer: ByteArray

    @SuppressLint("MissingPermission")
    fun start(audioSource: Int, sampleRate: Int, channelConfig: Int, audioFormat: Int): Boolean {
        if (recorder != null) {
            return false
        }

        val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

        buffer = ByteArray(bufferSize)
        recorder = AudioRecord(audioSource, sampleRate, channelConfig, audioFormat, bufferSize)

        recorderThread = RecordThread()
        recorderThread?.start()
        return true
    }

    fun stop() {
        recorder?.stop()
        recorderThread?.join()
        recorderThread = null
    }

    inner class RecordThread : Thread() {
        override fun run() {
            super.run()

            var readLen: Int
            recorder?.startRecording()
            while (recorder?.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                readLen = recorder?.read(buffer, 0, buffer.size) ?: break
                listener.onRecord(buffer, readLen)
            }
            recorder = null
        }
    }

    interface IRecordListener {
        fun onRecord(pcm: ByteArray, dataLen: Int)
    }
}