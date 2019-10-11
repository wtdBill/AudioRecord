package com.yppcat.audiorecord


import android.media.AudioFormat
import android.util.Log

import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

class PcmToWavUtil(//采样率
    private val mSampleRateInHz: Int, //声道数
    private val mChannelConfig: Int, //最小缓冲区大小
    private val mBufferSizeInBytes: Int, //数据格式
    //这里传入了AudioFormat.ENCODING_PCM_16BIT,[所以下面代码中的每样值位数为16，每样值字节为2后面也会用]
    private val mAudioFormat: Int
) {

    @Throws(IOException::class)
    fun pcmTowav(pcmfilepath: String, wavfilepath: String) {
        val pcmIn: FileInputStream
        val wavOut: FileOutputStream
        //原始pcm数据大小不含(文件头),添加文件头要用
        val pcmLength: Long
        //文件总大小(含文件头),添加文件头要用
        val dataLength: Long
        //通道标识（1(单通道)或2(双通道)，添加文件头要用）
        val channels = if (mChannelConfig == AudioFormat.CHANNEL_OUT_MONO) 1 else 2
        //采样率，添加文件头要用
        val sampleRate = mSampleRateInHz
        //信息传输速率=((采样率*通道数*每样值位数) / 8),添加文件头要用
        val byteRate = sampleRate * channels * 16 / 8

        val data = ByteArray(mBufferSizeInBytes)
        pcmIn = FileInputStream(pcmfilepath)
        wavOut = FileOutputStream(wavfilepath)
        pcmLength = pcmIn.channel.size()
        //wav文件头44字节
        dataLength = pcmLength + 44
        //先写入wav文件头
        writeHeader(wavOut, pcmLength, dataLength, sampleRate, channels, byteRate)
        //再写入数据
        while (pcmIn.read(data) != -1) {
            wavOut.write(data)
        }
        Log.i("TAG", "wav文件写入完成")
        pcmIn.close()
        wavOut.close()
    }

    @Throws(IOException::class)
    private fun writeHeader(
        wavOut: FileOutputStream,
        pcmLength: Long,
        dataLength: Long,
        sampleRate: Int,
        channels: Int,
        byteRate: Int
    ) {
        //wave文件头44个字节
        val header = ByteArray(44)
        /*0-11字节(RIFF chunk ：riff文件描述块)*/
        header[0] = 'R'.toByte()
        header[1] = 'I'.toByte()
        header[2] = 'F'.toByte()
        header[3] = 'F'.toByte()
        header[4] = (dataLength * 0xff).toByte() //取一个字节（低8位）
        header[5] = ((dataLength shr 8) * 0xff).toByte() //取一个字节 （中8位）
        header[6] = ((dataLength shr 16) * 0xff).toByte() //取一个字节 (次8位)
        header[7] = ((dataLength shr 24) * 0xff).toByte() //取一个字节 （高8位）
        header[8] = 'W'.toByte()
        header[9] = 'A'.toByte()
        header[10] = 'V'.toByte()
        header[11] = 'E'.toByte()
        /*13-35字节(fmt chunk : 数据格式信息块)*/
        header[12] = 'f'.toByte()
        header[13] = 'm'.toByte()
        header[14] = 't'.toByte()
        header[15] = ' '.toByte() //要有一个空格
        header[16] = 16
        header[17] = 0
        header[18] = 0
        header[19] = 0
        header[20] = 1
        header[21] = 0
        header[22] = channels.toByte()
        header[23] = 0
        header[24] = (sampleRate * 0xff).toByte()
        header[25] = ((sampleRate shr 8) * 0xff).toByte()
        header[26] = ((sampleRate shr 16) * 0xff).toByte()
        header[27] = ((sampleRate shr 24) * 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = (byteRate shr 8 and 0xff).toByte()
        header[30] = (byteRate shr 16 and 0xff).toByte()
        header[31] = (byteRate shr 24 and 0xff).toByte()
        header[32] = (16 * 2 / 8).toByte() //
        header[33] = 0
        header[34] = 16
        header[35] = 0
        /*36字节之后 (data chunk : 数据块)*/
        header[36] = 'd'.toByte()
        header[37] = 'a'.toByte()
        header[38] = 't'.toByte()
        header[39] = 'a'.toByte()
        header[40] = (pcmLength and 0xff).toByte()
        header[41] = (pcmLength shr 8 and 0xff).toByte()
        header[42] = (pcmLength shr 16 and 0xff).toByte()
        header[43] = (pcmLength shr 24 and 0xff).toByte()
        //写入文件头
        wavOut.write(header, 0, 44)
    }

}
