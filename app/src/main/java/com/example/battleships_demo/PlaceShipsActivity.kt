package com.example.battleships_demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.battleships_demo.customviews.EditableBoard

class PlaceShipsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_place_ships)
    }

    override fun onDestroy() {
        super.onDestroy()
        val boardState = findViewById<EditableBoard>(R.id.editable_board).getBoardState()
    }
}