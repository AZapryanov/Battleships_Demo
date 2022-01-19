package com.example.battleships_demo

import android.graphics.Rect

class Ship(val size: Int) {
    var rect = Rect()
    var initialPos = Rect()
    var hasInvalidPos = false
    var isTouched = false
    var isHorizontal = true
    var isPlaced = false

    fun turn(){
        val tmpRect = Rect()
        if (isHorizontal){
            tmpRect.left   = rect.left
            tmpRect.top    = rect.top
            tmpRect.right  = rect.left + rect.height()
            tmpRect.bottom = rect.top + rect.width()
            rect.set(tmpRect)
            isHorizontal = false
        } else {
            tmpRect.left   = rect.left
            tmpRect.top    = rect.top
            tmpRect.right  = rect.left + rect.height()
            tmpRect.bottom = rect.top + rect.width()
            rect.set(tmpRect)
            isHorizontal = true
        }
    }

    fun returnToInitPos(){
        rect = Rect(initialPos)
        isHorizontal = true
        isPlaced = false
    }
}