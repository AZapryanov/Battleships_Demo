package com.example.battleships_demo.customviews

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.core.view.GestureDetectorCompat
import com.example.battleships_demo.Ship

class EditableBoard(context: Context, attrs: AttributeSet) :
            Board(context, attrs),
            GestureDetector.OnGestureListener {

    companion object{
        private const val TAG = "EditableBoard"
    }

    private val mBoardRect = RectF()
    private val mShips = arrayOf(Ship(5), Ship(4), Ship(3), Ship(3), Ship(2))
    private var mTmpRect = RectF()
    private val mMarginTop = 60
    private val mMarginLeft = 30

    private val mDetector = GestureDetectorCompat(this.context, this)

    /**
     * Gesture detector override functions
     */
    override fun onDown(e: MotionEvent?): Boolean {
        // Set
        var value = false

        // Determine which ship has been touched
        for(ship in mShips){
            if (ship.rect.contains(e!!.x, e.y)){
                Log.d(TAG, "onDown: ship of size ${ship.size}")
                ship.isTouched = true
                value = true
            } else {
                ship.isTouched = false
            }
        }
        return value
    }

    override fun onSingleTapUp(e: MotionEvent?): Boolean {
        Log.d(TAG, "onSingleTapUp: $e")
        for(ship in mShips)
            if(ship.isTouched){
                ship.turn()
                evaluatePos(ship)
                if (ship.hasInvalidPos) {
                    // If the touched ship had invalid position upon releasing finger
                    // Return ship to original position and break out of this event
                    ship.returnToInitPos()
                    ship.hasInvalidPos = false  // Ship now has a valid position
                } else {
                    val xInBoardSpace = (e!!.x / mCellWidth).toInt()
                    val yInBoardSpace = (e.y / mCellHeight).toInt()
                    mTmpRect.apply {
                        left = xInBoardSpace * mCellWidth
                        top = yInBoardSpace * mCellHeight
                        right = left + ship.rect.width()
                        bottom = top + ship.rect.height()
                    }
                    ship.rect.set(RectF(mTmpRect))
                }
            }
        invalidate()
        return true
    }

    override fun onShowPress(event: MotionEvent) {
        Log.d(TAG, "onShowPress: $event")
    }

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent?,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        Log.d(TAG, "onScroll: $e1\n$e2")
        return false
    }

    override fun onLongPress(e: MotionEvent?) {
        Log.d(TAG, "onLongPress: $e")
    }

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent?,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        Log.d(TAG, "onFling: $e1\n$e2")
        return false
    }

    /**
     * View override functions
     */
    // May be better to override onMeasure() here?!
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        // Cell height is equal to cell width for cells to be square
        mCellWidth = (w.toFloat() / mBoardSize)
        mCellHeight = mCellWidth

        mBoardRect.set(0f, 0f, mCellWidth*mBoardSize, mCellHeight*mBoardSize)

        /** Initialize all ships here after getting the cell width and height
         *  Ships array(index -> ship size): 0 -> 5, 1 -> 4, 2 -> 3, 3 -> 3, 4 -> 2 */
        // First row
        var x = 0f
        var y = mBoardRect.bottom + mMarginTop
        mShips[0].rect.set(x, y, x+(mCellWidth*mShips[0].size), y+mCellHeight)
        mShips[0].initialPos.set(RectF(mShips[0].rect))

        x = mShips[0].rect.right + mMarginLeft
        mShips[1].rect.set(x, y, x+(mCellWidth*mShips[1].size), y+mCellHeight)
        mShips[1].initialPos.set(RectF(mShips[1].rect))

        // Second row
        x = 0f
        y = mShips[0].rect.bottom + mMarginTop
        mShips[2].rect.set(x, y, x+(mCellWidth*mShips[2].size), y+mCellHeight)
        mShips[2].initialPos.set(RectF(mShips[2].rect))

        x = mShips[2].rect.right + mMarginLeft
        mShips[3].rect.set(x, y, x+(mCellWidth*mShips[3].size), y+mCellHeight)
        mShips[3].initialPos.set(RectF(mShips[3].rect))

        x = mShips[3].rect.right + mMarginLeft
        mShips[4].rect.set(x, y, x+(mCellWidth*mShips[4].size), y+mCellHeight)
        mShips[4].initialPos.set(RectF(mShips[4].rect))
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return if (mDetector.onTouchEvent(event)) {
            true
        } else {
            var value = super.onTouchEvent(event)
            val x = event?.x
            val y = event?.y

            when(event?.action){
                MotionEvent.ACTION_MOVE -> {
                    value = false
                    // Change the ship's rect accordingly
                    for(ship in mShips)
                        if(ship.isTouched){
                            // left and top are always gonna be where the touch coords are
                            // "- mCell../2" makes your finger be in the center of the ship's first square
                            mTmpRect.left = x!! - mCellWidth/2
                            mTmpRect.top = y!! - mCellHeight/2
                            mTmpRect.right = mTmpRect.left + ship.rect.width()
                            mTmpRect.bottom = mTmpRect.top + ship.rect.height()

                            ship.rect.set(RectF(mTmpRect))
                            evaluatePos(ship)
                            value = true
                        }
                }
                MotionEvent.ACTION_UP -> {
                    Log.d(TAG, "onTouchEvent: ACTION UP triggered")
                    val xInBoardSpace = (x!! / mCellWidth).toInt()
                    val yInBoardSpace = (y!! / mCellHeight).toInt()

                    for(ship in mShips)
                        if (ship.isTouched) {
                            if (ship.hasInvalidPos) {
                                ship.returnToInitPos()
                                ship.hasInvalidPos = false
                                invalidate()
                                return true
                            } else {
                                // If the ship has valid position upon releasing finger
                                // snap it to the cell where finger is
                                mTmpRect.apply {
                                    left = xInBoardSpace * mCellWidth
                                    top = yInBoardSpace * mCellHeight
                                    right = left + ship.rect.width()
                                    bottom = top + ship.rect.height()
                                }
                                ship.rect.set(RectF(mTmpRect))
                            }
                            value = true
                        }
                }
            }

            invalidate()
            return value
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        for(ship in mShips)
            if(ship.hasInvalidPos){
                canvas?.drawRect(ship.rect, mRedPaint)
                canvas?.drawRect(ship.rect, mDefRectPaint)
            } else {
                canvas?.drawRect(ship.rect, mGreenPaint)
                canvas?.drawRect(ship.rect, mDefRectPaint)
            }
    }


    /**
     * My functions
     */
    fun finishEditing(){
        // Set the board state according to where the ships are placed
        for(ship in mShips) {
            if (ship.isHorizontal) {
                for (i in 0 until ship.size) {
                    // Convert ship coords to board coords
                    val xInBoardSpace = (ship.rect.left / mCellWidth).toInt() + i
                    val yInBoardSpace = (ship.rect.top / mCellHeight).toInt()
                    mBoardState[xInBoardSpace][yInBoardSpace] = 1
                }
            } else {
                for (i in 0 until ship.size) {
                    val xInBoardSpace = (ship.rect.left / mCellWidth).toInt()
                    val yInBoardSpace = (ship.rect.top / mCellHeight).toInt() + i
                    mBoardState[xInBoardSpace][yInBoardSpace] = 1
                }
            }
        }
    }


    // Takes a ship and changes it's hasValidPosition
    private fun evaluatePos(ship: Ship){
        // If ship is outside of the board it has invalid position
        ship.hasInvalidPos = !mBoardRect.contains(ship.rect)
        // If ship intersects any other ship it has invalid position
        if(!ship.hasInvalidPos)
            // Check collisions
            for(otherShip in mShips){
                if(otherShip == ship) continue
                if(RectF.intersects(ship.rect, otherShip.rect))
                    ship.hasInvalidPos = true
            }
    }
}