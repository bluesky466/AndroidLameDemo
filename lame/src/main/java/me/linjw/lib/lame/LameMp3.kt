package me.linjw.lib.lame

class LameMp3 {
    private var lameClientPtr: Long = createLameMp3Client()

    fun getLibVersion(): String = getLameMp3Version()

    fun destroy(): Int {
        val ret = close(lameClientPtr)
        lameClientPtr = 0
        return ret
    }

    fun encode(
        leftBuff: ByteArray,
        rightBuff: ByteArray?,
        numSamples: Int,
        resultBuff: ByteArray
    ): Int {
        return encode(lameClientPtr, leftBuff, rightBuff, numSamples, resultBuff)
    }

    fun encodeInterleaved(
        pcmBuffer: ByteArray,
        numSamples: Int,
        resultBuff: ByteArray
    ): Int {
        return encodeInterleaved(lameClientPtr, pcmBuffer, numSamples, resultBuff)
    }

    private external fun getLameMp3Version(): String
    private external fun createLameMp3Client(): Long
    private external fun close(client: Long): Int
    private external fun setQuality(client: Long, quality: Int): Int
    private external fun setInSampleRate(client: Long, sampleRate: Int): Int
    private external fun setOutSampleRate(client: Long, sampleRate: Int): Int
    private external fun setBitRate(client: Long, bitRate: Int): Int
    private external fun setNumChannels(client: Long, numChannels: Int): Int
    private external fun initParams(client: Long): Int

    private external fun encode(
        client: Long,
        leftBuff: ByteArray,
        rightBuff: ByteArray?,
        numSamples: Int,
        resultBuff: ByteArray
    ): Int

    private external fun encodeInterleaved(
        client: Long,
        pcmBuff: ByteArray,
        numSamples: Int,
        resultBuff: ByteArray
    ): Int

    companion object {
        init {
            System.loadLibrary("lame")
        }
    }

    class Builder {
        private var inSampleRate: Int = 44100
        private var outSampleRate: Int = inSampleRate
        private var numChannels: Int = 2
        private var bitRate: Int = 128
        private var quality: Int = 2

        fun setInSampleRate(inSampleRate: Int): Builder {
            this.inSampleRate = inSampleRate
            return this
        }

        fun setOutSampleRate(outSampleRate: Int): Builder {
            this.outSampleRate = outSampleRate
            return this
        }

        fun setNumChannels(numChannels: Int): Builder {
            this.numChannels = numChannels
            return this
        }

        fun setBitRate(bitRate: Int): Builder {
            this.bitRate = bitRate
            return this
        }

        fun setQuality(quality: Int): Builder {
            this.quality = quality
            return this
        }

        fun build(): LameMp3 {
            val lameMp3 = LameMp3()
            lameMp3.setInSampleRate(lameMp3.lameClientPtr, inSampleRate)
            lameMp3.setOutSampleRate(lameMp3.lameClientPtr, outSampleRate)
            lameMp3.setNumChannels(lameMp3.lameClientPtr, numChannels)
            lameMp3.setBitRate(lameMp3.lameClientPtr, bitRate)
            lameMp3.setQuality(lameMp3.lameClientPtr, quality)
            lameMp3.initParams(lameMp3.lameClientPtr)
            return lameMp3
        }
    }
}