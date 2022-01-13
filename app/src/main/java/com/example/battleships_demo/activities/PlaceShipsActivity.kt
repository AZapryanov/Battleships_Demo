package com.example.battleships_demo.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.example.battleships_demo.R
import com.example.battleships_demo.bluetooth.BluetoothService
import com.example.battleships_demo.bluetooth.Constants
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

        var otherPlayerReady = false
    }

    private lateinit var mBoard: EditableBoard
    private var mHasClickedReady = false
    private lateinit var mMyBoard: Array<Array<Int>>
    private var mEnemyBoard: Array<Array<Int>>? = null

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
            Log.d(TAG, "onCreate: ${mBoard.getBoardState()}")
            
            mHasClickedReady = true
            mMyBoard = mBoard.getBoardState()

            val boardAsByteArray = ByteArray(1024)
            mMyBoard.forEachIndexed { row, nums ->
                nums.forEachIndexed { col, num ->
                    // "row * nums.size + col" turns 2d coords into 1d indices
                    // example: for 4x7 2d arr, coords [2, 4] would be 2*7+4 = 18
                    boardAsByteArray[row * nums.size + col] = num.toByte()
                }
            }
            Log.d(TAG, "onCreate: $boardAsByteArray")
            // Send your board to the other player
            BluetoothService.write(boardAsByteArray)

            CoroutineScope(Dispatchers.IO).launch {
                startGameActivity()
            }
        }
    }

    override fun onReceiveMessage(messageType: Int, message: Any?) {
        when(messageType){
            Constants.MESSAGE_LISTENING -> {
                BluetoothService.unregister(this)
                finish()
            }
            Constants.MESSAGE_WRITE -> {
                Log.d(TAG, "onReceiveMessage: sending board to the enemy")
            }
            Constants.MESSAGE_READ -> {
                val bytes = (message as Bundle).getByteArray(Constants.BYTES) ?: return
                mEnemyBoard = bytesToSquareGrid(bytes, 10)
                otherPlayerReady = true
            }
        }
    }

    private suspend fun startGameActivity(){
        Log.d(TAG, "startGameActivity():")

        val intent = Intent(this, GameActivity::class.java)
        intent.putExtra(EXTRA_MY_SHIPS, mMyBoard)
        val isPlayer1 = BluetoothService.determinePlayer1()
        intent.putExtra(EXTRA_IS_PLAYER_ONE, isPlayer1)

        waitForPlayer()

        intent.putExtra(EXTRA_OPPONENT_SHIPS, mEnemyBoard)

        var boardStr = ""
        mMyBoard.forEach { row ->
            row.forEach { num ->
                boardStr += num.toString()
            }
            boardStr += "\n"
        }
        Log.d(TAG, "startGameActivity: own board\n$boardStr")
        boardStr = ""
        mEnemyBoard?.forEach { row ->
            row.forEach { num ->
                boardStr += num.toString()
            }
            boardStr += "\n"
        }
        Log.d(TAG, "startGameActivity: enemy board\n$boardStr")

        BluetoothService.unregister(this)
        startActivity(intent)
    }

    private suspend fun waitForPlayer(){
        // Wait for the other player to press ready
        while(!otherPlayerReady){
            continue
        }
    }

    private fun bytesToSquareGrid(bytes: ByteArray, gridSize: Int): Array<Array<Int>>?{
        if (gridSize * gridSize > bytes.size) {
            Log.d(TAG, "bytesToGrid: cant turn byteArray of size ${bytes.size} into a ${gridSize}x$gridSize grid")
            return null
        } else if(gridSize >= 100){
            return null
        }

        val grid = Array(gridSize) { Array(gridSize) {0} }
        bytes.forEachIndexed { index, byte ->
            if (index >= 100) return@forEachIndexed
            grid[index % 10][index / 10] = byte.toInt()
        }

        return grid
    }
}