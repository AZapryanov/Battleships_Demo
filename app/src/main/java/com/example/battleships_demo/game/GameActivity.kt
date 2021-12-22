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

    private var isPlayerOne = true
    private val isMyTurn: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

//    private val isEndTurnButtonVisible: MutableLiveData<Boolean> by lazy {
//        MutableLiveData<Boolean>()
//    }

    private val shouldReceiveOpponentAttack: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    private var isNotFirstTurn = false
    private var isEndgame = false
    private lateinit var enemyShipsPositions: Array<Array<Int>>
    private lateinit var myAttacksPositionsFromPreviousRound: Array<Array<Int>>
    private lateinit var myShipsPositionsFromPreviousRound: Array<Array<Int>>
    private var opponentAttackCoordinates = Array(2) { 0 }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        //The following block is executed only one time at the start of the game
//        isMyTurn.value = false
        val myShipsPositionsFromIntent = intent.getStringExtra("myShips")
        enemyShipsPositions = transformStringToIntMatrix(intent.getStringExtra("enemyShips"))
        myShipsPositionsFromPreviousRound = transformStringToIntMatrix(myShipsPositionsFromIntent)
        myAttacksPositionsFromPreviousRound = cvMyAttacks.getBoardState()

        isPlayerOne = intent.getBooleanExtra("isPlayerOneOrTwo", false)
        cvMyShips.setBoardState(myShipsPositionsFromPreviousRound)

        if (isPlayerOne) {
            isMyTurn.value = true
        }

        //isMyTurn changes to true when a BT intent is received from the opponent that he finished his turn and the following code executes
        isMyTurn.observe(this, {
            cvMyShips.setPhase(Constants.PHASE_TOUCH_INPUTS_LOCKED)
            cvMyAttacks.setPhase(Constants.PHASE_MARK_ATTACK)

            //My ships are updated based on the received attack coordinated from the opponent
            if (isNotFirstTurn) {
                val updatedMyShips = updateMyShips()
                cvMyShips.setBoardState(updatedMyShips)

                isEndgame = checkIfGameHasEnded(cvMyShips.getBoardState())
                if (isEndgame) {
                    cvMyAttacks.setPhase(Constants.PHASE_TOUCH_INPUTS_LOCKED)
                    buttonEndTurn.visibility = View.GONE
                    Toast.makeText(this, "GG, You have lost!", Toast.LENGTH_LONG).show()
                }
            }
            if (!isEndgame) {
                buttonEndTurn.visibility = View.VISIBLE
            }
            isNotFirstTurn = true
        })

//        isEndTurnButtonVisible.observe(this, {
//
//        })

        buttonEndTurn.setOnClickListener {
            cvMyAttacks.resetBoardTouchCounter()
            buttonEndTurn.visibility = View.GONE

            val myAttackCoordinates = cvMyAttacks.getLastTouchInput()

            //My attack board is updated based on my attack coordinates and whether is a hit or miss
            val updatedMyAttacks = updateMyAttacks(myAttackCoordinates)
            cvMyAttacks.setBoardState(updatedMyAttacks)
            myAttacksPositionsFromPreviousRound = cvMyAttacks.getBoardState()

            isEndgame = checkIfGameHasEnded(cvMyAttacks.getBoardState())
            if (isEndgame) {
                //Send my attack coordinates to opponent so that his board can update to the final state and also register Endgame
                buttonEndTurn.visibility = View.GONE
                cvMyAttacks.setPhase(Constants.PHASE_TOUCH_INPUTS_LOCKED)
                Toast.makeText(this, "GG, You have won!", Toast.LENGTH_LONG).show()
            }

            //Send my attack coordinates as array to other player through BT

            //For test purposes:
            if (!isEndgame) {
                shouldReceiveOpponentAttack.value = !shouldReceiveOpponentAttack.equals(true)
            }

        }

        shouldReceiveOpponentAttack.observe(this, {
            opponentAttackCoordinates = generateRandomOpponentAttack()
            isMyTurn.value = !isMyTurn.equals(true)
        })
    }

    private fun generateRandomOpponentAttack(): Array<Int> {
        val coordinates = Array(2) { 0 }
        val xCoordinateOpponent = (0..9).random()
        val yCoordinateOpponent = (0..9).random()
        coordinates[0] = xCoordinateOpponent
        coordinates[1] = yCoordinateOpponent
        return coordinates
    }

    private fun updateMyAttacks(myAttackCoordinates: Array<Int>): Array<Array<Int>> {
        val updatedMyAttacks = myAttacksPositionsFromPreviousRound
        val currentAttackX = myAttackCoordinates[0]
        val currentAttackY = myAttackCoordinates[1]

        if (enemyShipsPositions[currentAttackX][currentAttackY] == 0) {
            updatedMyAttacks[currentAttackX][currentAttackY] = 1

        } else if (enemyShipsPositions[currentAttackX][currentAttackY] == 2) {
            updatedMyAttacks[currentAttackX][currentAttackY] = 3
        }

        return updatedMyAttacks
    }

    private fun updateMyShips(): Array<Array<Int>> {
        val updatedCvMyShips = myShipsPositionsFromPreviousRound
        val currentAttackX = opponentAttackCoordinates[0]
        val currentAttackY = opponentAttackCoordinates[1]

        if (myShipsPositionsFromPreviousRound[currentAttackX][currentAttackY] == 0) {
            updatedCvMyShips[currentAttackX][currentAttackY] = 1

        } else if (myShipsPositionsFromPreviousRound[currentAttackX][currentAttackY] == 2) {
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