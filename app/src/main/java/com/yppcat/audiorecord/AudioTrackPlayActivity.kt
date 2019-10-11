package com.yppcat.audiorecord

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_audio_track_play.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException


class AudioTrackPlayActivity : AppCompatActivity() {
    companion object {
        private val TAG = "AudioTrackPlayActivity"
    }

    private val fileName =
        Environment.getExternalStorageDirectory().absolutePath + File.separator + "audiorecordtest.pcm"
    private lateinit var asyncTask: AsyncTask<Void, Void, Void>
    private var audioData: ByteArray? = null
    private lateinit var audioTrack: AudioTrack
    private val frequency = 44100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_track_play)

        mode_static.setOnClickListener {
            it.isEnabled = false
            releaseAudioTrack()
            audioTrack = AudioTrack(
                AudioManager.STREAM_MUSIC,
                frequency,
                AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT,
                audioData?.size!!,
                AudioTrack.MODE_STATIC
            )
            Log.d(TAG, "Writing audio data...");
            audioTrack.write(audioData!!, 0, audioData!!.size)
            Log.d(TAG, "Starting playback")
            audioTrack.play()
            Log.d(TAG, "Playing")
            it.isEnabled = true
        }
    }

    private fun initStaticMode() {
        asyncTask = @SuppressLint("StaticFieldLeak")
        object : AsyncTask<Void, Void, Void>() {

            override fun doInBackground(vararg params: Void?): Void? {
                try {
                    val inputStream = File(fileName).inputStream()
                    try {
                        val outputStream = ByteArrayOutputStream(284848)
                        val b: Int = inputStream.read()
                        while (b != -1) {
                            outputStream.write(b)
                        }
                        Log.d(TAG, "Got the Data")
                        audioData = outputStream.toByteArray()
                    } finally {
                        inputStream.close()
                    }
                } catch (e: IOException) {
                    Log.d(TAG, "Failed to red data")
                    e.printStackTrace()
                }
                return null
            }

            override fun onPostExecute(result: Void?) {
                Log.d(TAG, "Creating track...")
                mode_static.isEnabled = true
                Log.d(TAG, "Enabled button")
            }
        }

    }

    private fun releaseAudioTrack() {
        audioTrack.let {
            Log.d(TAG, "Stopping")
            it.stop()
            Log.d(TAG, "Releasing")
            it.release()
            Log.d(TAG, "Nulling")
        }
    }

    override fun onPause() {
        super.onPause()
        releaseAudioTrack()
    }
}
