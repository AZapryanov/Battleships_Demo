package com.example.battleships_demo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.battleships_demo.player.PlayerOneActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        buttonConnectDevices.setOnClickListener {
            val intent = Intent(this, PlayerOneActivity::class.java)
            intent.putExtra("placeShipsOrRegisterAttacks", "placeShips")
            startActivity(intent)
        }
    }
}