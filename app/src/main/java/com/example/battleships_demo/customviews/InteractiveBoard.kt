package com.example.battleships_demo.customviews

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent

class InteractiveBoard(context: Context, attrs: AttributeSet) : Board(context, attrs) {
    companion object {
        private const val TAG = "InteractiveBoard"
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
                    if (mBoardState[cellX][cellY] == 2 || mBoardState[cellX][cellY] == 3) {
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
                                mBoardState[cellX][cellY] = 2
                            }
                            DRAW_RED_SHIP_PART_WITH_CROSS -> {
                                mBoardState[cellX][cellY] = 3
                            }
                        }

                        invalidate()
                        mTouchCounter++
                        mLastRecordedTouchInput[0] = cellX
                        mLastRecordedTouchInput[1] = cellY
                        return true
                    }
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

    fun getLastTouchInput(): Array<Int> {
        return mLastRecordedTouchInput
    }

    fun resetBoardTouchCounter() {
        mTouchCounter = 0
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
}