package com.example.battleships_demo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.example.battleships_demo.bluetooth.BluetoothService
import com.example.battleships_demo.customviews.EditableBoard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.*

class PlaceShipsActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "PlaceShipsActivity"
        // Intent extras
        const val EXTRA_MY_SHIPS = "myShips"
        const val EXTRA_OPPONENT_SHIPS = "enemyShips"
        const val EXTRA_IS_PLAYER_ONE = "isPlayerOneOrTwo"

        var otherPlayerReady = false
    }

    private var mPlayerNum: Int = 0
    private lateinit var mBoard: EditableBoard
    private var mHasClickedReady = false
    private lateinit var mMyShipsAsString: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_place_ships)

        mPlayerNum = intent.getIntExtra(ConnectActivity.EXTRA_PLAYER_NUMBER, 0)
        mBoard = findViewById(R.id.editable_board)
        Log.d(TAG, "onCreate: Player: $mPlayerNum")

        findViewById<Button>(R.id.btn_ready).setOnClickListener {
            if (mHasClickedReady){
                return@setOnClickListener
            }
            mHasClickedReady = true
            mMyShipsAsString = mBoard.getBoardStateAsString()!!

            // Send your board to the other player
            BluetoothService.write(mBoard.getBoardStateAsString()!!.toByteArray())

            CoroutineScope(Dispatchers.IO).launch {
                startGameActivity()
            }
        }
    }

    private suspend fun startGameActivity(){
        Log.d(TAG, "startGameActivity():")
        waitForPlayer()

        val intent = Intent(this, GameActivity::class.java)
        intent.putExtra(EXTRA_MY_SHIPS, mMyShipsAsString)
        intent.putExtra(EXTRA_OPPONENT_SHIPS, BluetoothService.mEnemyBoard)
        when(mPlayerNum){
            1 -> intent.putExtra(EXTRA_IS_PLAYER_ONE, true)
            2 -> intent.putExtra(EXTRA_IS_PLAYER_ONE, false)
        }

        startActivity(intent)
    }

    private suspend fun waitForPlayer(){
        // Wait for the other player to press ready
        while(!otherPlayerReady){
            continue
        }
    }
}