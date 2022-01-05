package com.example.battleships_demo

import android.graphics.RectF

class Ship(val size: Int) {
    var rect = RectF()
    var initialPos = RectF()
    var hasInvalidPos = false
    var isTouched = false
    var isHorizontal = true
}