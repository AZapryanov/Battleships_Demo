package com.example.battleships_demo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import com.example.battleships_demo.common.Constants.PLAYER_ONE
import com.example.battleships_demo.ships_placement_round.ShipsPlacementActivity
import kotlinx.android.synthetic.main.activity_main.*

import android.widget.Button


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btn_start_connect_activity).setOnClickListener {
            startActivity(Intent(this, ConnectActivity::class.java))

        }
    }
}