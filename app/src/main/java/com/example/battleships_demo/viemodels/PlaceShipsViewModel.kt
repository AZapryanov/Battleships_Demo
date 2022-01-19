package com.example.battleships_demo.viemodels

import androidx.lifecycle.ViewModel

class PlaceShipsViewModel : ViewModel(){
    var mMyBoardState: Array<Array<Int>>? = null
    var mEnemyBoardState: Array<Array<Int>>? = null
}