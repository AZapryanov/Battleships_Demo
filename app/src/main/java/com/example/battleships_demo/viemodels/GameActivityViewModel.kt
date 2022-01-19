package com.example.battleships_demo.viemodels

import androidx.lifecycle.ViewModel

class GameActivityViewModel : ViewModel() {

    companion object {
        private const val INITIAL_ARRAY_VALUE = 15
        private const val INITIAL_ARRAY_SIZE = 3
    }

    var myAttacksPositionsFromPreviousRound: Array<Array<Int>> = Array(10) { Array(10) { 0 } }
    var myShipsPositionsFromPreviousRound: Array<Array<Int>> = Array(10) { Array(10) { 0 } }
    var opponentAttackCoordinates = Array(INITIAL_ARRAY_SIZE) { INITIAL_ARRAY_VALUE }
}