package com.example.battleships_demo.customviews

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent

class InteractiveBoard(context: Context, attrs: AttributeSet) : Board(context, attrs) {
    companion object {
        private const val TAG = "InteractiveBoard"
        const val DRAW_CROSS = "drawCross"
        const val DRAW_RED_SHIP_PART_WITH_CROSS = "drawRedShipPartWithCross"
        const val EMPTY_BOX = 0
        const val SHIP_PART = 1
        const val CROSS = 2
        const val SHIP_PART_HIT = 3
    }

    private var mWhatToDoOnTouch: String = ""
    private var mCurrentPhase: String = ""
    private var mLastRecordedTouchInput = Array(2) { 0 }
    private var mTouchCounter: Int = 0

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        mWhatToDoOnTouch = "drawCross"

        //Checks if the touch input should be allowed depending on the phase of the game
        if (mCurrentPhase == "lock") {
            return false

        } else {
            val value = super.onTouchEvent(event)

            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {

                    // Converts the tapped position to board coordinates
                    val cellX = (event.x / mCellWidth).toInt()
                    val cellY = (event.y / mCellHeight).toInt()

                    //Check whether to fill the box (set a ship) or put a cross (attack)
                    if (mBoardState[cellY][cellX] == 2 || mBoardState[cellY][cellX] == 3) {
                        return false

                    } else {
                        //During my attack turn if I want to change my attack location, when I click again,
                        // the previously clicked box will become blank again when the view redraws itself,
                        // because here its value gets set to 0
                        if (mTouchCounter >= 1 && mCurrentPhase == "doAttack") {
                            mBoardState[mLastRecordedTouchInput[0]][mLastRecordedTouchInput[1]] = 0
                        }

                        //Check whether to fill the box with red and cross(set a ship part that is hit)
                        // or put a cross (attack)
                        when (mWhatToDoOnTouch) {
                            DRAW_CROSS -> {
                                mBoardState[cellY][cellX] = 2
                            }
                            DRAW_RED_SHIP_PART_WITH_CROSS -> {
                                mBoardState[cellY][cellX] = 3
                            }
                        }

                        invalidate()
                        mTouchCounter++
                        mLastRecordedTouchInput[0] = cellY
                        mLastRecordedTouchInput[1] = cellX
                        return true
                    }
                }
            }
            return value
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        for (i in 0 until mBoardSize) {
            for (j in 0 until mBoardSize) {
                val left = mCellWidth * j
                val top = mCellHeight * i
                val right = left + mCellWidth
                val bottom = top + mCellHeight
                mCellRect.set(left, top, right, bottom)

                when {
                    mBoardState[i][j] == EMPTY_BOX -> {
                        canvas?.drawRect(mCellRect, mDefRectPaint)
                    }
                    mBoardState[i][j] == SHIP_PART -> {
                        canvas?.drawRect(mCellRect, mGreenPaint)
                        canvas?.drawRect(mCellRect, mDefRectPaint)
                    }
                    mBoardState[i][j] == CROSS -> {
                        canvas?.drawRect(mCellRect, mDefRectPaint)
                        drawCross(canvas)
                    }
                    mBoardState[i][j] == SHIP_PART_HIT -> {
                        canvas?.drawRect(mCellRect, mRedPaint)
                        canvas?.drawRect(mCellRect, mDefRectPaint)
                        drawCross(canvas)
                    }
                }
            }
        }
    }

    private fun drawCross(canvas: Canvas?) {
        canvas?.drawLine(
            (mCellRect.left   + 15).toFloat(),
            (mCellRect.top    + 15).toFloat(),
            (mCellRect.right  - 15).toFloat(),
            (mCellRect.bottom - 15).toFloat(),
            mDefRectPaint
        )
        canvas?.drawLine(
            (mCellRect.left   + 15).toFloat(),
            (mCellRect.bottom - 15).toFloat(),
            (mCellRect.right  - 15).toFloat(),
            (mCellRect.top    + 15).toFloat(),
            mDefRectPaint
        )
    }

    fun setPhase(phase: String) {
        mCurrentPhase = phase
    }

    fun setBoardState(inputState: Array<Array<Int>>) {
        for (i in 0 until mBoardSize) {
            for (j in 0 until mBoardSize) {
                mBoardState[i][j] = inputState[i][j]
            }
        }
        invalidate()
    }

    fun getLastTouchInput(): Array<Int> {
        return mLastRecordedTouchInput
    }

    fun visualizeRemainingOpponentShips(opponentShips: Array<Array<Int>>) {
        for (i in 0 until mBoardSize) {
            for (j in 0 until mBoardSize) {
                if (opponentShips[i][j] == 1 && mBoardState[i][j] == 0) {
                    mBoardState[i][j] = 1
                }
            }
        }
        invalidate()
    }

    fun getTouchCounter(): Int {
        return mTouchCounter
    }

    fun resetBoardTouchCounter() {
        mTouchCounter = 0
    }
}