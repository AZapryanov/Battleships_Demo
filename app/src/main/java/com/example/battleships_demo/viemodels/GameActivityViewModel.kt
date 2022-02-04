package com.example.battleships_demo.viemodels

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.battleships_demo.activities.GameActivity
import com.example.battleships_demo.bluetooth.BluetoothService
import com.example.battleships_demo.bluetooth.BtEvents
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class GameActivityViewModel : ViewModel(), BluetoothService.BtListener {

    companion object {
        private const val INITIAL_ARRAY_VALUE = 15
        private const val INITIAL_ARRAY_SIZE = 3
        const val SWAPPABLE_ONE = 1
        const val SWAPPABLE_TWO = 2
    }

    val mShouldStartMyNextTurn: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    var mIsDisconnected: MutableLiveData<Boolean> = MutableLiveData()

    var myAttacksPositionsFromPreviousRound: Array<Array<Int>> = Array(10) { Array(10) { 0 } }
    var myShipsPositionsFromPreviousRound: Array<Array<Int>> = Array(10) { Array(10) { 0 } }
    var mOpponentAttackCoordinates: Array<Int> = Array(INITIAL_ARRAY_SIZE) { INITIAL_ARRAY_VALUE }
    var mCoordinatesToSend: String = ""
    var mIsWaitingForOpponentTurn: Boolean = false
    var mIsShipHitByOpponent: Boolean = false
    var mIsToDoAnotherAttackAfterHit: Boolean = false
    private var mReceivedBluetoothMessage: String = ""

    init {
        mIsDisconnected.value = false
    }

    override fun onReceiveEvent(messageType: Int, message: Any?) {
        when (messageType) {
            BtEvents.EVENT_LISTENING -> {
                BluetoothService.unregister(this)
                mIsDisconnected.value = true
            }

            BtEvents.EVENT_READ -> {
                val bytes = (message as Bundle).getByteArray(BtEvents.BYTES) ?: return
                val byteCnt = message.getInt(BtEvents.BYTE_COUNT)
                mReceivedBluetoothMessage = String(bytes, 0, byteCnt)
            }
        }
    }

    @DelicateCoroutinesApi
    fun startWaitingForOpponentAttack() {
        mIsWaitingForOpponentTurn = true
        mCoordinatesToSend = ""

        GlobalScope.launch(Dispatchers.Default) {
            Log.d(TAG, "Waiting for response from opponent.")

            //Waiting to receive opponent attack coordinates and to start my next turn
            while (true) {
                if (mReceivedBluetoothMessage.length > 1) {
                    interpretBluetoothMessage()
                    break
                }
            }

            launch (Dispatchers.Main){
                //If the value at index 2 is set to 1 it means that the opponent's attack was successful
                // and after it is visualized on my ships board, he will attack again
                if (mOpponentAttackCoordinates[2] == 1) {
                    mIsShipHitByOpponent = true
                }

                mIsWaitingForOpponentTurn = false
                mReceivedBluetoothMessage = ""
                startNextTurn()
            }
        }
    }

    private fun interpretBluetoothMessage() {
        //My previous attack was a hit and I can attack again immediately
        if (mReceivedBluetoothMessage == GameActivity.DO_ANOTHER_ATTACK) {
            mIsToDoAnotherAttackAfterHit = true

        } else {
            for (i in mReceivedBluetoothMessage.indices) {
                mOpponentAttackCoordinates[i] =
                    mReceivedBluetoothMessage[i].digitToInt()
            }
            Log.d(TAG, "Opponent attack received. $mReceivedBluetoothMessage")
        }
    }
    
    fun startNextTurn() = if (mShouldStartMyNextTurn.value == SWAPPABLE_ONE
        || mShouldStartMyNextTurn.value == null
    ) {
        mShouldStartMyNextTurn.value = SWAPPABLE_TWO
    } else {
        mShouldStartMyNextTurn.value = SWAPPABLE_ONE
    }
}