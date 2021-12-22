package com.example.battleships_demo.game

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.example.battleships_demo.R
import com.example.battleships_demo.common.Constants
import kotlinx.android.synthetic.main.activity_game.*

private const val TAG = "GameActivity"

class GameActivity : AppCompatActivity() {
    private val mIsMyTurn: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }
    private val mShouldReceiveOpponentAttack: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    private var mIsPlayerOne = true
    private var mIsNotFirstTurn = false
    private var mIsEndgame = false
    private lateinit var mEnemyShipsPositions: Array<Array<Int>>
    private lateinit var mMyAttacksPositionsFromPreviousRound: Array<Array<Int>>
    private lateinit var mMyShipsPositionsFromPreviousRound: Array<Array<Int>>
    private var mOpponentAttackCoordinates = Array(2) { 0 }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        //The following block is executed only one time at the start of the game
        //------------------------------------
        val myShipsPositionsFromIntent = intent.getStringExtra("myShips")
        mEnemyShipsPositions = transformStringToIntMatrix(intent.getStringExtra("enemyShips"))
        mMyShipsPositionsFromPreviousRound = transformStringToIntMatrix(myShipsPositionsFromIntent)
        mMyAttacksPositionsFromPreviousRound = cvMyAttacks.getBoardState()

        mIsPlayerOne = intent.getBooleanExtra("isPlayerOneOrTwo", false)
        cvMyShips.setBoardState(mMyShipsPositionsFromPreviousRound)

        if (mIsPlayerOne) {
            mIsMyTurn.value = true
        }
        //------------------------------------

        //If the value of mIsMyTurn changes due to received opponent attack coordinates through BT,
        //meaning that he finished his turn, the following code executes
        mIsMyTurn.observe(this, {
            cvMyShips.setPhase(Constants.PHASE_TOUCH_INPUTS_LOCKED)
            cvMyAttacks.setPhase(Constants.PHASE_MARK_ATTACK)

            //My ships are updated based on the received attack coordinated from the opponent
            if (mIsNotFirstTurn) {
                val updatedMyShips = updateMyShips()
                cvMyShips.setBoardState(updatedMyShips)

                mIsEndgame = checkIfGameHasEnded(cvMyShips.getBoardState())
                if (mIsEndgame) {
                    cvMyAttacks.setPhase(Constants.PHASE_TOUCH_INPUTS_LOCKED)
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
            cvMyAttacks.resetBoardTouchCounter()
            buttonEndTurn.visibility = View.GONE

            val myAttackCoordinates = cvMyAttacks.getLastTouchInput()

            //My attack board is updated based on my attack coordinates and whether is a hit or miss
            val updatedMyAttacks = updateMyAttacks(myAttackCoordinates)
            cvMyAttacks.setBoardState(updatedMyAttacks)
            mMyAttacksPositionsFromPreviousRound = cvMyAttacks.getBoardState()

            mIsEndgame = checkIfGameHasEnded(cvMyAttacks.getBoardState())
            if (mIsEndgame) {
                //Send my attack coordinates to opponent so that his board can update to the final state and also register Endgame
                buttonEndTurn.visibility = View.GONE
                cvMyAttacks.setPhase(Constants.PHASE_TOUCH_INPUTS_LOCKED)
                Toast.makeText(this, "GG, You have won!", Toast.LENGTH_LONG).show()
            }

            //Send my attack coordinates as array to other player through BT
            //For test purposes:
            if (!mIsEndgame) {
                mShouldReceiveOpponentAttack.value = !mShouldReceiveOpponentAttack.equals(true)
            }

        }

        mShouldReceiveOpponentAttack.observe(this, {
            mOpponentAttackCoordinates = generateRandomOpponentAttack()
            mIsMyTurn.value = !mIsMyTurn.equals(true)
        })
    }

    private fun generateRandomOpponentAttack(): Array<Int> {
        val coordinates = Array(2) { 0 }
        coordinates[0] = (0..9).random()
        coordinates[1] = (0..9).random()
        return coordinates
    }

    private fun updateMyAttacks(myAttackCoordinates: Array<Int>): Array<Array<Int>> {
        val updatedMyAttacks = mMyAttacksPositionsFromPreviousRound
        val currentAttackX = myAttackCoordinates[0]
        val currentAttackY = myAttackCoordinates[1]

        if (mEnemyShipsPositions[currentAttackX][currentAttackY] == 0) {
            updatedMyAttacks[currentAttackX][currentAttackY] = 1

        } else if (mEnemyShipsPositions[currentAttackX][currentAttackY] == 2) {
            updatedMyAttacks[currentAttackX][currentAttackY] = 3
        }

        return updatedMyAttacks
    }

    private fun updateMyShips(): Array<Array<Int>> {
        val updatedCvMyShips = mMyShipsPositionsFromPreviousRound
        val currentAttackX = mOpponentAttackCoordinates[0]
        val currentAttackY = mOpponentAttackCoordinates[1]

        if (mMyShipsPositionsFromPreviousRound[currentAttackX][currentAttackY] == 0) {
            updatedCvMyShips[currentAttackX][currentAttackY] = 1

        } else if (mMyShipsPositionsFromPreviousRound[currentAttackX][currentAttackY] == 2) {
            updatedCvMyShips[currentAttackX][currentAttackY] = 3
        }
        return updatedCvMyShips
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
        Log.d(TAG, "Hello: $counter")
        return counter >= Constants.NUMBER_OF_DESTROYED_SHIPS_FOR_ENDGAME
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