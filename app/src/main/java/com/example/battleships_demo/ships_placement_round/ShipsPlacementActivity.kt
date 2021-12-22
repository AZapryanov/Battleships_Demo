package com.example.battleships_demo.ships_placement_round

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.battleships_demo.R
import com.example.battleships_demo.common.Constants
import com.example.battleships_demo.game.GameActivity
import com.example.battleships_demo.player.PlayerTwoActivity
import kotlinx.android.synthetic.main.activity_player_one.*
import kotlinx.android.synthetic.main.activity_ships_placement.*

class ShipsPlacementActivity : AppCompatActivity() {

    var isPlayerOne = true
    private var isReceivedOtherPlayerShips = false
    var isSentMyShipsToOpponent = false
    var otherPlayerShipsPlacement: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ships_placement)

        //This intent is received from the DeviceListActivity, depending on the player who clicks on the other device to connect
        isPlayerOne = intent.getBooleanExtra("isPlayerOneOrTwo", true)
        cvPlaceShipsBoard.setPhase(Constants.PHASE_PLACE_SHIPS)

        buttonReady.setOnClickListener {
            val myBoardState = cvPlaceShipsBoard.getBoardStateAsString()
            val intent = Intent(this, GameActivity::class.java)
            intent.putExtra("myShips", myBoardState)
            intent.putExtra("isPlayerOneOrTwo", isPlayerOne)

            //Here my ships' placement needs to get sent to the enemy through BT
            //SEND SHIPS
            isSentMyShipsToOpponent = true

            //Here I wait until I receive the enemy's ships placement through BT
            isReceivedOtherPlayerShips = true
            //This is only for testing purposes - should be received from BT
            otherPlayerShipsPlacement =
                "2222200022220000002220000000222000000000220002200000000000000000000000000000000000000000000000000000"
            //The ready check can be whether I received the ships of the opponent and also sent mine then
            // the following check will pass and the game activity will be launched:
            if (isReceivedOtherPlayerShips && otherPlayerShipsPlacement != null && isSentMyShipsToOpponent) {
                intent.putExtra("enemyShips", otherPlayerShipsPlacement)
                startActivity(intent)
            }
        }
    }
}