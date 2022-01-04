package com.example.battleships_demo

import android.graphics.RectF

class Ship(val size: Int) {
    var rect = RectF()
    var initialPos = RectF()
    var invalidPos = false
}