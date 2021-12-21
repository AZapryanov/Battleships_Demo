package com.example.battleships_demo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.battleships_demo.common.Constants.PLAYER_ONE
import com.example.battleships_demo.player.PlayerOneActivity
import com.example.battleships_demo.ships_placement_round.ShipsPlacementActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        buttonConnectDevices.setOnClickListener {
            val intent = Intent(this, ShipsPlacementActivity::class.java)
            intent.putExtra("isPlayerOneOrTwo", PLAYER_ONE)
            startActivity(intent)
        }
    }
}