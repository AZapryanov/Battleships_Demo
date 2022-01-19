package com.example.battleships_demo.viemodels

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.battleships_demo.bluetooth.BluetoothService
import com.example.battleships_demo.bluetooth.BtEvents

class PlaceShipsViewModel : ViewModel(), BluetoothService.BtListener{
    var mMyBoardState: Array<Array<Int>>? = null
    var mEnemyBoardState: Array<Array<Int>>? = null
    var mIsDisconnectedLiveData: MutableLiveData<Boolean> = MutableLiveData()
    var mIsPlayer1 = false
    private var mOtherPlayerReady = false

    companion object {
        private const val TAG = "PlaceShipsViewModel"
    }

    init {
        mIsDisconnectedLiveData.value = false
    }

    override fun onReceiveEvent(eventType: Int, message: Any?) {
        when(eventType){
            BtEvents.EVENT_LISTENING -> {
                BluetoothService.unregister(this)
                mIsDisconnectedLiveData.value = true
            }
            BtEvents.EVENT_WRITE -> {
                Log.d(TAG, "onReceiveMessage: sending board to the enemy")
                if(!mOtherPlayerReady){
                    mIsPlayer1 = true
                }
            }
            BtEvents.EVENT_READ -> {
                val buffer = (message as Bundle).getByteArray(BtEvents.BYTES) ?: return
                val byteCnt = message.getInt(BtEvents.BYTE_COUNT)
                mEnemyBoardState = bytesToSquareGrid(buffer, byteCnt, 10)
                Log.d(TAG, "onReceiveEvent: other player is ready")
                mOtherPlayerReady = true
            }
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

    suspend fun waitForPlayer(){
        Log.d(TAG, "waitForPlayer: called")
        // Wait for the other player to press ready
        while(!mOtherPlayerReady){
            continue
        }
    }

    fun sendBoardState(){
        if (mMyBoardState == null) {
            Log.d(TAG, "sendBoardState: myBoardState is null!")
            return
        }
        val boardAsByteArray = ByteArray(100)
        mMyBoardState!!.forEachIndexed { i, nums ->
            nums.forEachIndexed { j, num ->
                // "i * nums.size + j" turns 2d coords into 1d indices
                // example: for 4x7 2d arr, coords [2, 4] would be 2*7+4 = 18
                boardAsByteArray[i * nums.size + j] = num.toByte()
            }
        }

        // Send your board to the other player
        BluetoothService.write(boardAsByteArray)
    }
}