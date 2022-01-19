package com.example.battleships_demo.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.battleships_demo.R
import com.example.battleships_demo.bluetooth.BluetoothService
import com.example.battleships_demo.customviews.EditableBoard
import com.example.battleships_demo.viemodels.PlaceShipsViewModel
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
    }

    private lateinit var mViewModel: PlaceShipsViewModel
    private lateinit var mBoard: EditableBoard
    private var mIsReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_place_ships)
        Log.d(TAG, "onCreate: Entered")

        mViewModel = ViewModelProvider(this)[PlaceShipsViewModel::class.java]
        mBoard = findViewById(R.id.editable_board)
        BluetoothService.register(mViewModel)

        mViewModel.mIsDisconnectedLiveData.observe(this) { isDisconnected ->
            if(isDisconnected) finish()
        }

        findViewById<Button>(R.id.btn_ready).setOnClickListener {
            if (mIsReady) return@setOnClickListener
            if(!mBoard.finishEditing()){
                Toast.makeText(this, "Place all your ships", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            mIsReady = true
            mViewModel.mMyBoardState = mBoard.getState()
            mViewModel.sendBoardState()

            CoroutineScope(Dispatchers.IO).launch {
                startGameActivity()
            }
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy: called")
        super.onDestroy()
        BluetoothService.unregister(mViewModel)
    }

    private suspend fun startGameActivity(){
        Log.d(TAG, "startGameActivity():")

        val intent = Intent(this, GameActivity::class.java)
        intent.putExtra(EXTRA_MY_SHIPS, mViewModel.mMyBoardState)
        intent.putExtra(EXTRA_IS_PLAYER_ONE, mViewModel.mIsPlayer1)

        mViewModel.waitForPlayer()

        intent.putExtra(EXTRA_OPPONENT_SHIPS, mViewModel.mEnemyBoardState)

        startActivity(intent)
        BluetoothService.unregister(mViewModel)
        finish()
    }
}