package com.example.battleships_demo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.battleships_demo.bluetooth.BluetoothService
import com.example.battleships_demo.customviews.EditableBoard

class PlaceShipsActivity : AppCompatActivity() {

    private var mPlayerNum: Int = 0
    private lateinit var mBoard: EditableBoard
    private var hasClickedReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_place_ships)

        mPlayerNum = intent.getIntExtra(ConnectActivity.EXTRA_PLAYER_NUMBER, 0)
        mBoard = findViewById(R.id.editable_board)

        findViewById<Button>(R.id.btn_ready).setOnClickListener {
            if (hasClickedReady){
                return@setOnClickListener
            }

            BluetoothService.write(mBoard.getBoardStateAsString()!!.toByteArray())
            when(mPlayerNum){
                1 -> {
                    BluetoothService.player1Ready = true
                }
                2 -> {
                    BluetoothService.player2Ready = true
                }
            }

            while(!(BluetoothService.player1Ready && BluetoothService.player2Ready))

            startActivity(Intent(this, GameActivity::class.java))
        }
    }
}