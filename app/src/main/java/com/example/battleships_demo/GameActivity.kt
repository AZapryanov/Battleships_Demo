package com.example.battleships_demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.example.battleships_demo.customviews.Board
import kotlinx.android.synthetic.main.activity_game.*

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
            cvMyShips.setPhase(PHASE_TOUCH_INPUTS_LOCKED)
            cvMyAttacks.setPhase(PHASE_MARK_ATTACK)

            //My ships are updated based on the received attack coordinated from the opponent
            if (mIsNotFirstTurn) {
                val opponentAttackCoordinates = mOpponentAttackCoordinates
                val updatedMyShips = updateState(opponentAttackCoordinates, mMyShipsPositionsFromPreviousRound)
                cvMyShips.setBoardState(updatedMyShips)

                mIsEndgame = checkIfGameHasEnded(cvMyShips.getBoardState())
                if (mIsEndgame) {
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
            cvMyAttacks.resetBoardTouchCounter()
            buttonEndTurn.visibility = View.GONE

            val myAttackCoordinates = cvMyAttacks.getLastTouchInput()

            //My attack board is updated based on my attack coordinates and whether is a hit or miss
            val updatedMyAttacks = updateState(myAttackCoordinates, mMyAttacksPositionsFromPreviousRound)
            cvMyAttacks.setBoardState(updatedMyAttacks)
            mMyAttacksPositionsFromPreviousRound = cvMyAttacks.getBoardState()

            mIsEndgame = checkIfGameHasEnded(cvMyAttacks.getBoardState())
            if (mIsEndgame) {
                //Send my attack coordinates to opponent so that his board can update to the final state and also register Endgame
                buttonEndTurn.visibility = View.GONE
                cvMyAttacks.setPhase(PHASE_TOUCH_INPUTS_LOCKED)
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

    private fun updateState(
        myAttackCoordinates: Array<Int>,
        stateFromPreviousRound: Array<Array<Int>>
    ): Array<Array<Int>> {
        val currentAttackX = myAttackCoordinates[0]
        val currentAttackY = myAttackCoordinates[1]

        if (mEnemyShipsPositions[currentAttackX][currentAttackY] == Board.EMPTY_BOX) {
            stateFromPreviousRound[currentAttackX][currentAttackY] = Board.CROSS

        } else if (mEnemyShipsPositions[currentAttackX][currentAttackY] == Board.SHIP_PART) {
            stateFromPreviousRound[currentAttackX][currentAttackY] = Board.SHIP_PART_HIT
        }

        return stateFromPreviousRound
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