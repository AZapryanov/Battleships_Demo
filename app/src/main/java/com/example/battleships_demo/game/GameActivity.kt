package com.example.battleships_demo.game

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.example.battleships_demo.R
import com.example.battleships_demo.common.Constants
import kotlinx.android.synthetic.main.activity_game.*

class GameActivity : AppCompatActivity() {

    private var isPlayerOne = true
    private val isMyTurn: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }
    private var isFirstLaunch = true
    private lateinit var enemyShipsPositions: Array<Array<Int>>
    lateinit var myAttacksPositionsFromPreviousRound: Array<Array<Int>>
    private lateinit var myShipsPositionsFromPreviousRound: Array<Array<Int>>
    lateinit var opponentAttackCoordinates: Array<Int>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        //The following block is executed only one time at the start of the game
        if (isFirstLaunch) {
            val myShipsPositionsFromIntent = intent.getStringExtra("myShips")
            enemyShipsPositions = transformStringToIntMatrix(intent.getStringExtra("enemyShips"))
            myShipsPositionsFromPreviousRound =
                transformStringToIntMatrix(myShipsPositionsFromIntent)
            myAttacksPositionsFromPreviousRound = cvMyAttacks.getBoardState()

            isPlayerOne = intent.getBooleanExtra("isPlayerOneOrTwo", false)

            cvMyShips.setBoardState(myShipsPositionsFromPreviousRound)
            isFirstLaunch = false

            if (isPlayerOne) {
                isMyTurn.value = true
            }
        }

        //isMyTurn changes to true when a BT intent is received from the opponent that he finished his turn and the following code executes
        isMyTurn.observe(this, {
            cvMyShips.setPhase(Constants.PHASE_TOUCH_INPUTS_LOCKED)
            cvMyAttacks.setPhase(Constants.PHASE_MARK_ATTACK)

            //My ships are updated based on the received attack coordinated from the opponent
            val updatedMyShips = updateMyShips()
            cvMyShips.setBoardState(updatedMyShips)

            val isEndgame = checkIfGameHasEnded(cvMyShips.getBoardState())
            if (isEndgame) {
                Toast.makeText(this, "GG, You have lost!", Toast.LENGTH_SHORT).show()
            }

            buttonEndTurn.setOnClickListener {
                val myAttackCoordinates = cvMyAttacks.getLastTouchInput()

                //My attack board is updated based on my attack coordinates and whether is a hit or miss
                val updatedMyAttacks = updateMyAttacks(myAttackCoordinates)
                cvMyAttacks.setBoardState(updatedMyAttacks)
                myAttacksPositionsFromPreviousRound = cvMyAttacks.getBoardState()

                val isEndgame = checkIfGameHasEnded(cvMyAttacks.getBoardState())
                if (isEndgame) {
                    //Send my attack coordinates to opponent so that his board can update to the final state and also register Endgame
                    Toast.makeText(this, "GG, You have won!", Toast.LENGTH_SHORT).show()
                }

                //Send my attack coordinates as array to other player through BT
            }
        })
    }

    private fun updateMyAttacks(myAttackCoordinates: Array<Int>): Array<Array<Int>> {
        val updatedCvMyAttacks = myAttacksPositionsFromPreviousRound
        val currentAttackX = myAttackCoordinates[0]
        val currentAttackY = myAttackCoordinates[1]

        if (enemyShipsPositions[currentAttackX][currentAttackY] == 0) {
            updatedCvMyAttacks[currentAttackX][currentAttackY] = 1

        } else if (enemyShipsPositions[currentAttackX][currentAttackY] == 2) {
            updatedCvMyAttacks[currentAttackX][currentAttackY] = 3
        }

        return updatedCvMyAttacks
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