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
        private const val PHASE_MARK_ATTACK = "doAttack"
        private const val PHASE_TOUCH_INPUTS_LOCKED = "lock"
        private const val NUMBER_OF_DESTROYED_SHIPS_FOR_ENDGAME = 17
        private const val SECOND_ATTACK_AFTER_HIT = "It's a hit! Do another attack."
        private const val WINNER_MESSAGE = "GG, You have won!"
        private const val DEFEATED_MESSAGE = "GG, You have lost."
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

    private lateinit var mOpponentShipsPositions: Array<Array<Int>>
    private lateinit var mOpponentAttackCoordinates: Array<Int>

    private lateinit var mMyAttacksPositionsFromPreviousRound: Array<Array<Int>>
    private lateinit var mMyShipsPositionsFromPreviousRound: Array<Array<Int>>

    private var mReceivedAttackThroughBt = ""
    private val mReceivedAttack = Array(2) { 0 }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        BluetoothService.clearReceivedMessage()

        //The following block is executed only one time at the start of the game,
        // in order to set everything up for the first turn
        //-----------------------------------------------------------------------------------------------------------------------
        cvMyShips.setBoardState(transformStringToIntMatrix(intent.getStringExtra(PlaceShipsActivity.EXTRA_MY_SHIPS)))
        mMyShipsPositionsFromPreviousRound =
            transformStringToIntMatrix(intent.getStringExtra(PlaceShipsActivity.EXTRA_MY_SHIPS))
        mMyAttacksPositionsFromPreviousRound = cvMyAttacks.getBoardState()

        mOpponentShipsPositions =
            transformStringToIntMatrix(intent.getStringExtra(PlaceShipsActivity.EXTRA_OPPONENT_SHIPS))
        mOpponentAttackCoordinates = Array(2) { 0 }

        mIsPlayerOne = intent.getBooleanExtra(PlaceShipsActivity.EXTRA_IS_PLAYER_ONE, false)
        Log.d(TAG, "I am player one = $mIsPlayerOne.")
        buttonEndTurn.visibility = View.GONE

        cvMyShips.setPhase(PHASE_TOUCH_INPUTS_LOCKED)
        cvMyAttacks.setPhase(PHASE_TOUCH_INPUTS_LOCKED)

        if (mIsPlayerOne) {
            mIsMyTurn.value = true
        } else {
            mIsNotFirstTurn = true
            mShouldWaitForOpponentAttack.value = mShouldWaitForOpponentAttack.value != true
        }
        //-----------------------------------------------------------------------------------------------------------------------

        //If the value of mIsMyTurn changes due to received opponent attack coordinates through BT,
        //meaning that he finished his turn, the following code executes
        mIsMyTurn.observe(this, {
            Log.d(TAG, "My turn starts.")
            cvMyShips.setPhase(PHASE_TOUCH_INPUTS_LOCKED)
            cvMyAttacks.setPhase(PHASE_MARK_ATTACK)

            //My ships are updated based on the received attack coordinated from the opponent
            if (mIsNotFirstTurn) {
                val opponentAttackCoordinates = mOpponentAttackCoordinates
                val updatedBoardState =
                    updateMyShips(opponentAttackCoordinates, mMyShipsPositionsFromPreviousRound)
                cvMyShips.setBoardState(updatedBoardState)
                Log.d(TAG, "My ships updated with opponent attack.")

                mMyShipsPositionsFromPreviousRound = cvMyShips.getBoardState()
                mIsEndgame = checkIfGameHasEnded(cvMyShips.getBoardState())

                if (mIsEndgame) {
                    doEndgameProcedure(DEFEATED_MESSAGE)
                }
            }

            if (!mIsEndgame) {
                buttonEndTurn.visibility = View.VISIBLE
            }
            mIsNotFirstTurn = true
        })

        buttonEndTurn.setOnClickListener {

            //Check is an attack has been marked before allowing the End Turn button to be pressed
            if (cvMyAttacks.getTouchCounter() >= 1) {
                Log.d(TAG, "End turn button clicked.")
                cvMyAttacks.resetBoardTouchCounter()
                cvMyAttacks.setPhase(PHASE_TOUCH_INPUTS_LOCKED)
                buttonEndTurn.visibility = View.GONE

                //Gets the last touch input on the interactive game board
                val myAttackCoordinates = cvMyAttacks.getLastTouchInput()
                var coordinatesToSend = ""
                coordinatesToSend += myAttackCoordinates[0].toString()
                coordinatesToSend += myAttackCoordinates[1].toString()

                //My attack board is updated based on my attack coordinates and whether it is a hit or miss
                val updatedMyAttacksPositions =
                    updateMyAttacks(
                        myAttackCoordinates,
                        mMyAttacksPositionsFromPreviousRound,
                        mOpponentShipsPositions
                    )

                cvMyAttacks.setBoardState(updatedMyAttacksPositions)
                Log.d(TAG, "My attacks updated after check for hit.")
                mMyAttacksPositionsFromPreviousRound = cvMyAttacks.getBoardState()

                mIsEndgame = checkIfGameHasEnded(cvMyAttacks.getBoardState())

                if (mIsEndgame) {
                    doEndgameProcedure(WINNER_MESSAGE)

                    //Send my attack coordinates to the other player through BT
                    //so that his game ends too and he gets a message that he is defeated
                    BluetoothService.write(coordinatesToSend.toByteArray())
                }

                if (!mIsEndgame) {
                    cvMyAttacks.resetBoardTouchCounter()

                    //If I have hit an enemy ship on my turn I get an extra turn
                    if (checkIfAttackIsAHit(myAttackCoordinates)) {
                        Toast.makeText(this, SECOND_ATTACK_AFTER_HIT, Toast.LENGTH_SHORT).show()
                        Log.d(TAG, "The attack is a hit. Will do another attack")
                        mIsMyTurn.value = !mIsMyTurn.equals(true)

                    } else {
                        //Send my attack coordinates to the other player through BT
                        BluetoothService.write(coordinatesToSend.toByteArray())
                        Log.d(TAG, "Attack sent to opponent.")

                        //This object is observed => Switching its value starts a coroutine
                        // in which I wait to receive the opponent's attack and to start my next turn
                        mShouldWaitForOpponentAttack.value =
                            mShouldWaitForOpponentAttack.value != true
                    }
                }
            }
        }

        mShouldWaitForOpponentAttack.observe(this, {
            lifecycleScope.launch(Dispatchers.Default) {
                BluetoothService.clearReceivedMessage()
                Log.d(TAG, "Waiting for opponent attack.")

                //Waiting to receive opponent attack coordinates and to start my next turn
                while (true) {
                    mReceivedAttackThroughBt = BluetoothService.mReceivedMessage

                    if (mReceivedAttackThroughBt.length > 1) {
                        mReceivedAttack[0] = mReceivedAttackThroughBt[0].digitToInt()
                        mReceivedAttack[1] = mReceivedAttackThroughBt[1].digitToInt()
                        Log.d(TAG, "Opponent attack received.")
                        break
                    }
                }

                launch(Dispatchers.Main) {
                    mOpponentAttackCoordinates = mReceivedAttack

                    //When attack coordinates are received from the other player through BT,
                    //by switching the value of mIsMyTurn (it is observed), my next turn is started
                    mIsMyTurn.value = !mIsMyTurn.equals(true)
                }
            }
        })
    }

    private fun updateMyAttacks(
        myAttackCoordinates: Array<Int>,
        myAttacksPositions: Array<Array<Int>>,
        opponentShipsPositions: Array<Array<Int>>
    ): Array<Array<Int>> {
        val opponentAttackX = myAttackCoordinates[0]
        val opponentAttackY = myAttackCoordinates[1]

        if (opponentShipsPositions[opponentAttackX][opponentAttackY] == Board.EMPTY_BOX) {
            myAttacksPositions[opponentAttackX][opponentAttackY] = Board.CROSS

        } else if (opponentShipsPositions[opponentAttackX][opponentAttackY] == Board.SHIP_PART) {
            myAttacksPositions[opponentAttackX][opponentAttackY] = Board.SHIP_PART_HIT
        }
        return myAttacksPositions
    }

    private fun updateMyShips(
        opponentAttackCoordinates: Array<Int>,
        myShipsPositions: Array<Array<Int>>,
    ): Array<Array<Int>> {
        val opponentAttackX = opponentAttackCoordinates[0]
        val opponentAttackY = opponentAttackCoordinates[1]

        if (myShipsPositions[opponentAttackX][opponentAttackY] == Board.EMPTY_BOX) {
            myShipsPositions[opponentAttackX][opponentAttackY] = Board.CROSS

        } else if (myShipsPositions[opponentAttackX][opponentAttackY] == Board.SHIP_PART) {
            myShipsPositions[opponentAttackX][opponentAttackY] = Board.SHIP_PART_HIT
        }
        return myShipsPositions
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

    private fun checkIfAttackIsAHit(attackCoordinates: Array<Int>): Boolean {
        if (mOpponentShipsPositions[attackCoordinates[0]][attackCoordinates[1]] == 1) {
            return true
        }
        return false
    }

    private fun doEndgameProcedure(messageToShow: String) {
        //Lock all inputs
        //----------------------------------------------
        Log.d(TAG, "Game has ended.")
        cvMyAttacks.setPhase(PHASE_TOUCH_INPUTS_LOCKED)
        buttonEndTurn.visibility = View.GONE
        //----------------------------------------------

        //The remaining enemy ships are shown if game is lost
        cvMyAttacks.visualizeRemainingOpponentShips(mOpponentShipsPositions)
        Toast.makeText(this, messageToShow, Toast.LENGTH_LONG).show()
    }

    private fun transformStringToIntMatrix(inputString: String?): Array<Array<Int>> {
        var counter = 0
        val outputMatrix = Array(10) { Array(10) { 0 } }
        for (i in outputMatrix.indices) {
            for (j in outputMatrix.indices) {
                outputMatrix[i][j] = inputString!![counter].digitToInt()
                counter++
            }
        }
        return outputMatrix
    }
}