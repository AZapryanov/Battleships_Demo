package com.example.battleships_demo.customviews

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import com.example.battleships_demo.common.GameActivityConstants

class InteractiveBoard(context: Context, attrs: AttributeSet) : Board(context, attrs) {
    companion object{
        private const val TAG = "InteractiveBoard"
    }

    private var mWhatToDoOnTouch: String = ""
    private var mCurrentPhase: String = ""
    private var mLastRecordedTouchInput = Array(2) {0}
    private var mTouchCounter: Int = 0

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (mCurrentPhase == "doAttack") {
            mWhatToDoOnTouch = "drawCross"

        } else if (mCurrentPhase == "placeShips") {
            mWhatToDoOnTouch = "drawShipPart"
        }

        if (mTouchCounter >= 1 && mCurrentPhase == "doAttack") {
            return false

        } else if (mTouchCounter >= 17 && mCurrentPhase == "placeShips") {
            return false

        } else if (mCurrentPhase == "receiveAttack" || mCurrentPhase == "lock") {
            return false

        } else {
            val value = super.onTouchEvent(event)

            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Converts the tapped position to board coords
                    val cellX = (event.x / mCellWidth).toInt()
                    val cellY = (event.y / mCellHeight).toInt()

                    mLastRecordedTouchInput[0] = cellX
                    mLastRecordedTouchInput[1] = cellY

                    //Check whether to fill the box (set a ship) or put a cross (attack)
                    when (mWhatToDoOnTouch) {
                        GameActivityConstants.DRAW_CROSS -> {
                            mBoardState[cellX][cellY] = 1
                        }
                        GameActivityConstants.DRAW_SHIP_PART -> {
                            mBoardState[cellX][cellY] = 2
                        }
                        GameActivityConstants.DRAW_RED_SHIP_PART_WITH_CROSS -> {
                            mBoardState[cellX][cellY] = 3
                        }
                    }
                    invalidate()
                    mTouchCounter++
                    return true
                }
            }
            return value
        }
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

    fun getBoardStateAsString(): String? {
        var boardState = ""
        for (i in 0 until mBoardSize) {
            for (j in 0 until mBoardSize) {
                boardState += mBoardState[i][j].toString()
            }
        }
        return boardState
    }

    fun getLastTouchInput(): Array<Int> {
        return  mLastRecordedTouchInput
    }

    fun resetBoardTouchCounter() {
        mTouchCounter = 0
    }
}