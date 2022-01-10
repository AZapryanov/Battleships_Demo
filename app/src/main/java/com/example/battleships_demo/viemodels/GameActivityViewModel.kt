package com.example.battleships_demo.viemodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class GameActivityViewModel : ViewModel() {
    val myAttacksPositionsFromPreviousRound: MutableLiveData<Array<Array<Int>>> by lazy {
        MutableLiveData<Array<Array<Int>>>()
    }

    val myShipsPositionsFromPreviousRound: MutableLiveData<Array<Array<Int>>> by lazy {
        MutableLiveData<Array<Array<Int>>>()
    }
}