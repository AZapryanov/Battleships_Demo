package com.example.battleships_demo

import android.graphics.RectF

class Ship(val size: Int) {
    var rect = RectF()
    var initialPos = RectF()
    var hasInvalidPos = false
    var isTouched = false
    var isHorizontal = true

    fun turn(){
        val tmpRect = RectF()
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
        rect = RectF(initialPos)
        isHorizontal = true
    }
}