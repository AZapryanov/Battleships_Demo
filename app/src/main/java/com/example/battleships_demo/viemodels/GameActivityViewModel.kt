package com.example.battleships_demo.viemodels

import androidx.lifecycle.ViewModel
import com.example.battleships_demo.activities.GameActivity

class GameActivityViewModel : ViewModel() {

    var myAttacksPositionsFromPreviousRound: Array<Array<Int>> = Array(10) { Array(10) { 0 } }
    var myShipsPositionsFromPreviousRound: Array<Array<Int>> = Array(10) { Array(10) { 0 } }
    var opponentAttackCoordinates = Array(GameActivity.INITIAL_ARRAY_SIZE) { GameActivity.INITIAL_ARRAY_VALUE }
}