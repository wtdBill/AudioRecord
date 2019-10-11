package com.yppcat.audiorecord

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_audio_record.*
import java.io.*
import java.lang.Exception


class AudioRecordActivity : AppCompatActivity() {

    private lateinit var audioRecord: AudioRecord
    private var recordBufSize = 0
    private val frequency = 44100
    private var isRecording = false
    private val fileName =
        Environment.getExternalStorageDirectory().absolutePath + File.separator + "audiorecordtest.pcm"
    private val wavFilename =
        Environment.getExternalStorageDirectory().absolutePath + File.separator + "test.wav"
    private lateinit var mRecordingFile: File
    private val mChannelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO //单声道
    private lateinit var mDataOutputStream: DataOutputStream
    private lateinit var mThread: Thread

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_record)

        start_record.setOnClickListener {
            requestPermission()
        }
        stop_record.setOnClickListener {
            stopRecord()
        }
        add_head.setOnClickListener {
            addHead()
        }
        play_audio.setOnClickListener {
            val intent = Intent(this@AudioRecordActivity,AudioTrackPlayActivity::class.java)
            startActivity(intent)
        }
    }

    private fun threadStart() {
        mThread = Thread {
            try {
                startRecord()
            } catch (e: Exception) {
                e.printStackTrace()
                stopRecord()
            }
        }
        mThread.start()
    }

    private fun initAudioRecord() {
        recordBufSize = AudioRecord.getMinBufferSize(
            frequency,
            AudioFormat.CHANNEL_IN_DEFAULT,
            AudioFormat.ENCODING_PCM_16BIT
        )
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            frequency,
            AudioFormat.CHANNEL_IN_DEFAULT,
            AudioFormat.ENCODING_PCM_16BIT,
            recordBufSize
        )
        mRecordingFile = File(fileName)
        if (mRecordingFile.exists()) {
            mRecordingFile.delete()
        }
        mRecordingFile.createNewFile()
        mDataOutputStream = DataOutputStream(BufferedOutputStream(FileOutputStream(mRecordingFile)))

        val data = ByteArray(recordBufSize)
    }

    @Throws(IOException::class)
    private fun startRecord() {
        initAudioRecord()
        if (recordBufSize == AudioRecord.ERROR_BAD_VALUE || recordBufSize == AudioRecord.ERROR) {
            Log.i("AudioRecord", "Unable To getMinBufferSize")
        } else {
            if (audioRecord.state == AudioRecord.STATE_INITIALIZED) {
                val buffer = ByteArray(recordBufSize)
                audioRecord.startRecording()
                isRecording = true
                while (audioRecord.recordingState == AudioRecord.RECORDSTATE_RECORDING && isRecording) {
                    Log.i("AudioRecord", "start record")
                    val bufferReadResult = audioRecord.read(buffer, 0, recordBufSize)
                    if (AudioRecord.ERROR_INVALID_OPERATION != bufferReadResult) {
                        mDataOutputStream.write(buffer)
                    }
                }
                mDataOutputStream.close()
            }
        }
    }

    private fun stopRecord() {
        isRecording = false
        if (audioRecord.state == AudioRecord.STATE_INITIALIZED) {
            audioRecord.stop()
            Log.i("TAG", "录音停止")
        }
        if (audioRecord != null) {
            audioRecord.release()
        }
    }

    private fun addHead() {
        val wavFile = File(wavFilename)
        if (wavFile.exists()) {
            wavFile.delete()
        }
        wavFile.createNewFile()
        recordBufSize = AudioRecord.getMinBufferSize(
            frequency,
            AudioFormat.CHANNEL_IN_DEFAULT,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val pcmToWavUtil = PcmToWavUtil(
            frequency,
            AudioFormat.CHANNEL_IN_DEFAULT,
            recordBufSize,
            AudioFormat.ENCODING_PCM_16BIT
        )
        pcmToWavUtil.pcmTowav(fileName, wavFilename)
    }

    private fun requestPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                this, Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            val permission = arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO
            )
            this.requestPermissions(permission, 0)
        } else {
            threadStart()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                threadStart()
            }
        }
    }
}
