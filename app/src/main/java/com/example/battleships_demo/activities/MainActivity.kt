package com.example.battleships_demo.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.battleships_demo.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_Battleships_Demo)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btn_start_connect_activity).setOnClickListener {
            startActivity(Intent(this, ConnectActivity::class.java))
        }
    }
}