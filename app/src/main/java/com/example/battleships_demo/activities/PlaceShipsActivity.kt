package com.example.battleships_demo.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.example.battleships_demo.R
import com.example.battleships_demo.bluetooth.BluetoothService
import com.example.battleships_demo.bluetooth.BtEvents
import com.example.battleships_demo.customviews.EditableBoard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.*

class PlaceShipsActivity : AppCompatActivity(), BluetoothService.BtListener {

    companion object {
        private const val TAG = "PlaceShipsActivity"
        // Intent extras
        const val EXTRA_MY_SHIPS = "myShips"
        const val EXTRA_OPPONENT_SHIPS = "enemyShips"
        const val EXTRA_IS_PLAYER_ONE = "isPlayerOneOrTwo"
    }

    private lateinit var mBoard: EditableBoard
    private var otherPlayerReady = false
    private var mHasClickedReady = false
    private lateinit var mMyBoard: Array<Array<Int>>
    private var mEnemyBoard: Array<Array<Int>>? = null
    private var mIsPlayer1 = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_place_ships)

        mBoard = findViewById(R.id.editable_board)
        BluetoothService.register(this)

        findViewById<Button>(R.id.btn_ready).setOnClickListener {
            if (mHasClickedReady){
                return@setOnClickListener
            }
            
            mBoard.finishEditing()
            
            mHasClickedReady = true
            mMyBoard = mBoard.getBoardState()

            val boardAsByteArray = ByteArray(100)
            mMyBoard.forEachIndexed { i, nums ->
                nums.forEachIndexed { j, num ->
                    // "i * nums.size + j" turns 2d coords into 1d indices
                    // example: for 4x7 2d arr, coords [2, 4] would be 2*7+4 = 18
                    boardAsByteArray[i * nums.size + j] = num.toByte()
                }
            }

            // Send your board to the other player
            BluetoothService.write(boardAsByteArray)

            CoroutineScope(Dispatchers.IO).launch {
                startGameActivity()
            }
        }
    }

    override fun onReceiveEvent(eventType: Int, message: Any?) {
        when(eventType){
            BtEvents.EVENT_LISTENING -> {
                BluetoothService.unregister(this)
                finish()
            }
            BtEvents.EVENT_WRITE -> {
                Log.d(TAG, "onReceiveMessage: sending board to the enemy")
                if(!otherPlayerReady){
                    mIsPlayer1 = true
                }
            }
            BtEvents.EVENT_READ -> {
                val buffer = (message as Bundle).getByteArray(BtEvents.BYTES) ?: return
                val byteCnt = message.getInt(BtEvents.BYTE_COUNT)
                mEnemyBoard = bytesToSquareGrid(buffer, byteCnt, 10)
                otherPlayerReady = true
            }
        }
    }

    private suspend fun startGameActivity(){
        Log.d(TAG, "startGameActivity():")

        val intent = Intent(this, GameActivity::class.java)
        intent.putExtra(EXTRA_MY_SHIPS, mMyBoard)
        intent.putExtra(EXTRA_IS_PLAYER_ONE, mIsPlayer1)

        waitForPlayer()

        intent.putExtra(EXTRA_OPPONENT_SHIPS, mEnemyBoard)

        BluetoothService.unregister(this)
        startActivity(intent)
    }

    private suspend fun waitForPlayer(){
        // Wait for the other player to press ready
        while(!otherPlayerReady){
            continue
        }
    }

    private fun bytesToSquareGrid(buffer: ByteArray, byteCnt: Int, gridSize: Int): Array<Array<Int>>?{
        if (gridSize * gridSize > byteCnt) {
            Log.d(TAG, "bytesToGrid: cant turn byteArray of size $byteCnt into a ${gridSize}x$gridSize grid")
            return null
        }

        val grid = Array(gridSize) { Array(gridSize) {0} }
        buffer.forEachIndexed { index, byte ->
            if (index >= byteCnt) return@forEachIndexed
            grid[index / 10][index % 10] = byte.toInt()
        }

        return grid
    }
}