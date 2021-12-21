package com.example.battleships_demo.player

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.battleships_demo.R
import com.example.battleships_demo.common.Constants
import kotlinx.android.synthetic.main.activity_player_two.*

class PlayerTwoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player_two)

        val receivedPlayerOneShips = intent.getStringExtra("playerOneShips")
        val receivedPlayerTwoAttacks = intent.getStringExtra("playerTwoAttacks")
        cvPlayerTwoAttacks.setPhase("playerTwoAttack")

        cvPlayerTwoAttacks.setWhatToDoOnTouch(Constants.DRAW_CROSS)

        if (receivedPlayerTwoAttacks != "wasSetupRound") {
            cvPlayerTwoAttacks.setBoardState(transformStringToIntMatrix(receivedPlayerTwoAttacks))
        }

        buttonGoToPlayerOne.setOnClickListener {
            val boardState = cvPlayerTwoAttacks.getBoardStateAsString()
            val intent = Intent(this, PlayerOneActivity::class.java)
            intent.putExtra("placeShipsOrRegisterAttacks", "registerAttacks")
            intent.putExtra("playerOneShips", receivedPlayerOneShips)
            intent.putExtra("playerTwoAttacks", boardState)
            startActivity(intent)
        }
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