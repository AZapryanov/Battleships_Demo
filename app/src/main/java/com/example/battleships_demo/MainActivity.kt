package com.example.battleships_demo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btn_start_connect_activity).setOnClickListener {
            startActivity(Intent(this, ConnectActivity::class.java))
        }

        findViewById<Button>(R.id.btn_test_ships).setOnClickListener {
            startActivity(Intent(this, PlaceShipsActivity::class.java))
        }
    }
}