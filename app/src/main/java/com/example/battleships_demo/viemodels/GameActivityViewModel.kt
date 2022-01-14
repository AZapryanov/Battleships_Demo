package com.example.battleships_demo.viemodels

import androidx.lifecycle.ViewModel

class GameActivityViewModel : ViewModel() {

    var myAttacksPositionsFromPreviousRound: Array<Array<Int>> = Array(10) { Array(10) { 0 } }
    var myShipsPositionsFromPreviousRound: Array<Array<Int>> = Array(10) { Array(10) { 0 } }
}