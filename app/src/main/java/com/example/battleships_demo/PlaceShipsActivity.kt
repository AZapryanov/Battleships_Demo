package com.example.battleships_demo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.example.battleships_demo.bluetooth.BluetoothService
import com.example.battleships_demo.customviews.EditableBoard

class PlaceShipsActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "PlaceShipsActivity"
        const val EXTRA_MY_SHIPS = "myShips"
        const val EXTRA_ENEMY_SHIPS = "enemyShips"
        const val EXTRA_IS_PLAYER_ONE = "isPlayerOneOrTwo"
    }

    private var mPlayerNum: Int = 0
    private lateinit var mBoard: EditableBoard

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_place_ships)

        mPlayerNum = intent.getIntExtra(ConnectActivity.EXTRA_PLAYER_NUMBER, 0)
        mBoard = findViewById(R.id.editable_board)
        Log.d(TAG, "onCreate: Player: $mPlayerNum")

        val myShipsAsString = mBoard.getBoardStateAsString()


        findViewById<Button>(R.id.btn_ready).setOnClickListener {

            BluetoothService.write(mBoard.getBoardStateAsString()!!.toByteArray())

            val intent = Intent(this, GameActivity::class.java)
            intent.putExtra(EXTRA_MY_SHIPS, myShipsAsString)
            intent.putExtra(EXTRA_ENEMY_SHIPS, myShipsAsString)
            when(mPlayerNum){
                1 -> intent.putExtra(EXTRA_IS_PLAYER_ONE, true)
                2 -> intent.putExtra(EXTRA_IS_PLAYER_ONE, false)
            }
            startActivity(intent)
        }
    }
}