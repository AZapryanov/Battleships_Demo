package com.example.battleships_demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.example.battleships_demo.bluetooth.BluetoothService
import com.example.battleships_demo.customviews.Board
import kotlinx.android.synthetic.main.activity_game.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GameActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "GameActivity"
        const val PHASE_MARK_ATTACK = "doAttack"
        const val PHASE_TOUCH_INPUTS_LOCKED = "lock"
        const val NUMBER_OF_DESTROYED_SHIPS_FOR_ENDGAME = 19
    }

    private val mIsMyTurn: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    private val mShouldWaitForOpponentAttack: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    private var mIsPlayerOne = true
    private var mIsNotFirstTurn = false
    private var mIsEndgame = false

    private lateinit var mEnemyShipsPositions: Array<Array<Int>>
    private lateinit var mOpponentAttackCoordinates: Array<Int>

    private lateinit var mMyAttacksPositionsFromPreviousRound: Array<Array<Int>>
    private lateinit var mMyShipsPositionsFromPreviousRound: Array<Array<Int>>

    private var receivedAttackThroughBt = ""
    private val receivedAttackAsArray = Array(2) { 0 }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        BluetoothService.clearMReceivedMessage()

        //The following block is executed only one time at the start of the game
        //------------------------------------
        val myShipsPositionsFromIntent = intent.getStringExtra(PlaceShipsActivity.EXTRA_MY_SHIPS)
        cvMyShips.setBoardState(transformStringToIntMatrix(myShipsPositionsFromIntent))

        mEnemyShipsPositions = transformStringToIntMatrix(intent.getStringExtra(PlaceShipsActivity.EXTRA_ENEMY_SHIPS))

        mMyShipsPositionsFromPreviousRound = transformStringToIntMatrix(myShipsPositionsFromIntent)
        mMyAttacksPositionsFromPreviousRound = cvMyAttacks.getBoardState()

        mIsPlayerOne = intent.getBooleanExtra(PlaceShipsActivity.EXTRA_IS_PLAYER_ONE, false)
        mOpponentAttackCoordinates = Array(2) {0}

        buttonEndTurn.visibility = View.GONE

        Log.d(TAG, "I am player one = $mIsPlayerOne.")

        if (mIsPlayerOne) {
            mIsMyTurn.value = true
        } else {
            mIsNotFirstTurn = true
            mShouldWaitForOpponentAttack.value = mShouldWaitForOpponentAttack.value != true
        }
        //------------------------------------

        //If the value of mIsMyTurn changes due to received opponent attack coordinates through BT,
        //meaning that he finished his turn, the following code executes
        mIsMyTurn.observe(this, {
            Log.d(TAG, "My turn starts.")
            cvMyShips.setPhase(PHASE_TOUCH_INPUTS_LOCKED)
            cvMyAttacks.setPhase(PHASE_MARK_ATTACK)

            //My ships are updated based on the received attack coordinated from the opponent
            if (mIsNotFirstTurn) {
                val opponentAttackCoordinates = mOpponentAttackCoordinates
                val updatedBoardState = updateState(opponentAttackCoordinates, mMyShipsPositionsFromPreviousRound)
                cvMyShips.setBoardState(updatedBoardState)
                Log.d(TAG, "My ships updated with opponent attack.")

                mMyShipsPositionsFromPreviousRound = cvMyShips.getBoardState()
                mIsEndgame = checkIfGameHasEnded(cvMyShips.getBoardState())

                if (mIsEndgame) {
                    Log.d(TAG, "Game has ended.")
                    cvMyAttacks.setPhase(PHASE_TOUCH_INPUTS_LOCKED)
                    buttonEndTurn.visibility = View.GONE
                    Toast.makeText(this, "GG, You have lost!", Toast.LENGTH_LONG).show()
                }
            }

            if (!mIsEndgame) {
                buttonEndTurn.visibility = View.VISIBLE
            }
            mIsNotFirstTurn = true
        })

        buttonEndTurn.setOnClickListener {
            Log.d(TAG, "End turn button clicked.")
            cvMyAttacks.resetBoardTouchCounter()
            cvMyAttacks.setPhase(PHASE_TOUCH_INPUTS_LOCKED)
            buttonEndTurn.visibility = View.GONE

            val myAttackCoordinates = cvMyAttacks.getLastTouchInput()

            //My attack board is updated based on my attack coordinates and whether is a hit or miss
            val updatedBoardState =
                updateState(myAttackCoordinates, mMyAttacksPositionsFromPreviousRound)
            cvMyAttacks.setBoardState(updatedBoardState)
            Log.d(TAG, "My attacks updated after check for hit.")
            mMyAttacksPositionsFromPreviousRound = cvMyAttacks.getBoardState()

            mIsEndgame = checkIfGameHasEnded(cvMyAttacks.getBoardState())

            if (mIsEndgame) {
                Log.d(TAG, "Game has ended.")
                //Send my attack coordinates to opponent so that
                // his board can update to the final state and also register Endgame
                buttonEndTurn.visibility = View.GONE
                cvMyAttacks.setPhase(PHASE_TOUCH_INPUTS_LOCKED)
                Toast.makeText(this, "GG, You have won!", Toast.LENGTH_LONG).show()
            }

            if (!mIsEndgame) {
                //Sending my attack coordinates as array to other player through BT
                var attackCoordinatesToSendThroughBt = ""
                attackCoordinatesToSendThroughBt += myAttackCoordinates[0].toString()
                attackCoordinatesToSendThroughBt += myAttackCoordinates[1].toString()
                BluetoothService.write(attackCoordinatesToSendThroughBt.toByteArray())
                Log.d(TAG, "Attack sent to opponent.")

                mShouldWaitForOpponentAttack.value = mShouldWaitForOpponentAttack.value != true
            }
        }

        mShouldWaitForOpponentAttack.observe(this, {
            lifecycleScope.launch(Dispatchers.Default) {
                BluetoothService.clearMReceivedMessage()
                Log.d(TAG, "Waiting for opponent attack.")
                //Waiting to receive opponent attack coordinates and to start my next turn
                while (true) {
                    receivedAttackThroughBt = BluetoothService.mReceivedMessage

                    if (receivedAttackThroughBt.length > 1) {
                        receivedAttackAsArray[0] = receivedAttackThroughBt[0].digitToInt()
                        receivedAttackAsArray[1] = receivedAttackThroughBt[1].digitToInt()
                        Log.d(TAG, "Opponent attack received.")
                        break
                    }
                }

                launch (Dispatchers.Main){
                    mOpponentAttackCoordinates = receivedAttackAsArray
                    //When attack coordinates are received from other player through BT
                    // by switching the value of mIsMyTurn, my next turn is started
                    mIsMyTurn.value = !mIsMyTurn.equals(true)
                }
            }
        })
    }

    private fun updateState(
        attackCoordinates: Array<Int>,
        updatedBoardState: Array<Array<Int>>
    ): Array<Array<Int>> {
        val currentAttackX = attackCoordinates[0]
        val currentAttackY = attackCoordinates[1]

        if (mEnemyShipsPositions[currentAttackX][currentAttackY] == Board.EMPTY_BOX) {
            updatedBoardState[currentAttackX][currentAttackY] = Board.CROSS

        } else if (mEnemyShipsPositions[currentAttackX][currentAttackY] == Board.SHIP_PART) {
            updatedBoardState[currentAttackX][currentAttackY] = Board.SHIP_PART_HIT
        }
        return updatedBoardState
    }

    private fun checkIfGameHasEnded(shipsBoard: Array<Array<Int>>): Boolean {
        var counter = 0
        for (i in shipsBoard.indices) {
            for (j in shipsBoard.indices) {
                if (shipsBoard[i][j] == 3) {
                    counter++
                }
            }
        }
        return counter >= NUMBER_OF_DESTROYED_SHIPS_FOR_ENDGAME
    }

    private fun transformStringToIntMatrix(input: String?): Array<Array<Int>> {
        var counter = 0
        val output = Array(10) { Array(10) { 0 } }
        for (i in output.indices) {
            for (j in output.indices) {
                output[i][j] = input!![counter].digitToInt()
                counter++
            }
        }
        return output
    }
}