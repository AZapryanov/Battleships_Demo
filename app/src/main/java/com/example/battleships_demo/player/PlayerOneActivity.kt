package com.example.battleships_demo.player

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.battleships_demo.R
import kotlinx.android.synthetic.main.activity_player_one.*

class PlayerOneActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player_one)

        var isSetupRound = false
        val updatedPlayerOneShips: Array<Array<Int>>

        val receivedAction = intent.getStringExtra("placeShipsOrRegisterAttacks")
        val receivedPlayerOneShips = intent.getStringExtra("playerOneShips")
        val receivedPlayerTwoAttacks = intent.getStringExtra("playerTwoAttacks")

        if (receivedAction == "placeShips") {
            cvPlayerOneShips.setPhase("playerOnePlaceShips")
            cvPlayerOneShips.setWhatToDoOnTouch(2)
            isSetupRound = true

        } else if (receivedAction == "registerAttacks") {
            cvPlayerOneShips.setPhase("playerOneLookAtDyingShips^^")
            updatedPlayerOneShips = updateShipsWithAttacks(
                transformStringToIntMatrix(receivedPlayerOneShips),
                transformStringToIntMatrix(receivedPlayerTwoAttacks)
            )

            cvPlayerOneShips.setBoardState(updatedPlayerOneShips)
        }

        buttonGoToPlayerTwo.setOnClickListener {
            val boardState = cvPlayerOneShips.getBoardStateAsString()
            val intent = Intent(this, PlayerTwoActivity::class.java)
            intent.putExtra("playerOneShips", boardState)

            if (isSetupRound) {
                intent.putExtra("playerTwoAttacks", "wasSetupRound")
            } else {
                intent.putExtra("playerTwoAttacks", receivedPlayerTwoAttacks)
            }

            startActivity(intent)
        }
    }

    private fun updateShipsWithAttacks(
        playerOneShips: Array<Array<Int>>,
        playerTwoAttacks: Array<Array<Int>>
    ): Array<Array<Int>> {
        val updatedPlayerOneShips = Array(10) { Array(10) { 0 } }
        for (i in updatedPlayerOneShips.indices) {
            for (j in updatedPlayerOneShips.indices) {

                if (playerTwoAttacks[i][j] == 1 && playerOneShips[i][j] == 0) {
                    updatedPlayerOneShips[i][j] = 1

                } else if (playerTwoAttacks[i][j] == 1 && playerOneShips[i][j] == 2) {
                    updatedPlayerOneShips[i][j] = 3

                } else {
                    updatedPlayerOneShips[i][j] = playerOneShips[i][j]
                }
            }
        }
        return updatedPlayerOneShips
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