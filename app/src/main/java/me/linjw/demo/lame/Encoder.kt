package me.linjw.demo.lame

import me.linjw.lib.lame.LameMp3
import java.io.File
import java.io.FileOutputStream

class Encoder {
    private var fos: FileOutputStream? = null
    private val buffer = ByteArray(1024 * 256)

    private lateinit var lameMp3: LameMp3
    private var numChannel: Int = 0

    fun start(outputMp3: File, sampleRate: Int, numChannel: Int) {
        fos = FileOutputStream(outputMp3)
        this.numChannel = numChannel
        lameMp3 = LameMp3.Builder()
            .setInSampleRate(sampleRate)
            .setNumChannels(numChannel)
            .build()
    }

    fun stop() {
        fos?.flush()
        fos?.close()
    }

    fun encode(pcmData: ByteArray, dataLen: Int) {
        val encodeSize = if (numChannel == 1) {
            lameMp3.encode(pcmData, null, dataLen / 2, buffer)
        } else {
            lameMp3.encodeInterleaved(pcmData, dataLen / 4, buffer)
        }
        fos?.write(buffer, 0, encodeSize)
    }
}