package com.example.battleships_demo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.battleships_demo.bluetooth.BluetoothService
import com.example.battleships_demo.customviews.EditableBoard

class PlaceShipsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_place_ships)

        intent.extras

        findViewById<Button>(R.id.btn_ready).setOnClickListener {


            if (BluetoothService.player1Ready && BluetoothService.player2Ready){
                startActivity(Intent(this, PlayActivity::class.java))
            }
        }
    }
}