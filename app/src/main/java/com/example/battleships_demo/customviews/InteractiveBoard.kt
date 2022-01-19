package com.example.battleships_demo.customviews

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import com.example.battleships_demo.activities.GameActivity

class InteractiveBoard(context: Context, attrs: AttributeSet) : Board(context, attrs) {
    companion object {
        private const val TAG = "InteractiveBoard"
        private const val DRAW_CROSS = "drawCross"
        private const val DRAW_RED_SHIP_PART_WITH_CROSS = "drawRedShipPartWithCross"
        private const val NUMBER_OF_DESTROYED_SHIPS_FOR_ENDGAME = 17
        private const val EMPTY_BOX = 0
        private const val SHIP_PART = 1
        private const val CROSS = 2
        private const val SHIP_PART_HIT = 3
    }

    private var mOpponentShipsPositions = Array(mBoardSize) { Array(mBoardSize) { 0 } }

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

                    //If there isalready a Cross or Hit ship part - do nothing
                    if (mBoardState[cellY][cellX] ==  CROSS || mBoardState[cellY][cellX] == SHIP_PART_HIT) {
                        return false

                    } else {
                        //During my attack turn if I want to change my attack location, when I click again,
                        // the previously clicked box will become blank when the view redraws itself
                        if (mTouchCounter >= 1 && mCurrentPhase == "doAttack") {
                            mBoardState[mLastRecordedTouchInput[0]][mLastRecordedTouchInput[1]] = 0
                        }

                        //Check whether to fill the box with red and cross(set a ship part that is hit)
                        // or put a cross (attack)
                        when (mWhatToDoOnTouch) {
                            DRAW_CROSS -> {
                                mBoardState[cellY][cellX] = CROSS
                            }
                            DRAW_RED_SHIP_PART_WITH_CROSS -> {
                                mBoardState[cellY][cellX] = SHIP_PART_HIT
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

    fun getTouchCounter(): Int {
        return mTouchCounter
    }

    fun resetBoardTouchCounter() {
        mTouchCounter = 0
    }

    fun getLastTouchInput(): Array<Int> {
        return mLastRecordedTouchInput
    }

    fun setOpponentShipsPositions(opponentShips: Array<Array<Int>>) {
        mOpponentShipsPositions = opponentShips
    }

    fun updateMyAttacks(
        myAttackCoordinates: Array<Int>,
        myAttacksPositions: Array<Array<Int>>,
    ) {
        val opponentAttackX = myAttackCoordinates[0]
        val opponentAttackY = myAttackCoordinates[1]

        if (mOpponentShipsPositions[opponentAttackX][opponentAttackY] == EMPTY_BOX) {
            myAttacksPositions[opponentAttackX][opponentAttackY] = CROSS

        } else if (mOpponentShipsPositions[opponentAttackX][opponentAttackY] == SHIP_PART) {
            myAttacksPositions[opponentAttackX][opponentAttackY] = SHIP_PART_HIT
        }
        mBoardState = myAttacksPositions
        invalidate()
    }

    fun updateMyShips(
        opponentAttackCoordinates: Array<Int>,
        myShipsPositions: Array<Array<Int>>,
    ) {
        val opponentAttackX = opponentAttackCoordinates[0]
        val opponentAttackY = opponentAttackCoordinates[1]

        if (myShipsPositions[opponentAttackX][opponentAttackY] == EMPTY_BOX) {
            myShipsPositions[opponentAttackX][opponentAttackY] = CROSS

        } else if (myShipsPositions[opponentAttackX][opponentAttackY] == SHIP_PART) {
            myShipsPositions[opponentAttackX][opponentAttackY] = SHIP_PART_HIT
        }

        mBoardState = myShipsPositions
        invalidate()
    }

    fun checkIfAttackIsAHit(attackCoordinates: Array<Int>): Boolean {
        if (mOpponentShipsPositions[attackCoordinates[0]][attackCoordinates[1]] == SHIP_PART) {
            return true
        }
        return false
    }

    fun checkIfGameHasEnded(): Boolean {
        var counter = 0
        for (i in mBoardState.indices) {
            for (j in mBoardState.indices) {
                if (mBoardState[i][j] == SHIP_PART_HIT) {
                    counter++
                }
            }
        }
        return counter >= NUMBER_OF_DESTROYED_SHIPS_FOR_ENDGAME
    }

    fun visualizeRemainingOpponentShips() {
        for (i in 0 until mBoardSize) {
            for (j in 0 until mBoardSize) {
                if (mOpponentShipsPositions[i][j] == SHIP_PART && mBoardState[i][j] == EMPTY_BOX) {
                    mBoardState[i][j] = SHIP_PART
                }
            }
        }
        invalidate()
    }
}