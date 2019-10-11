package com.yppcat.audiorecord

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_record.setOnClickListener {
            val intent = Intent(this@MainActivity, AudioRecordActivity::class.java)
            startActivity(intent)
        }

        btn_exit.setOnClickListener {
            finish()
        }
    }
}