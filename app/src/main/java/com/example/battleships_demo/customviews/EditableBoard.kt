package com.example.battleships_demo.customviews

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.core.view.GestureDetectorCompat
import com.example.battleships_demo.Ship

class EditableBoard(context: Context, attrs: AttributeSet) : Board(context, attrs) {
    companion object{
        private const val TAG = "EditableBoard"
    }

    private val mBoardRect = RectF()
    private val mShips = arrayOf(Ship(5), Ship(4), Ship(3), Ship(3), Ship(2))
    private var mTmpRect = RectF()
    private val mMarginTop = 60
    private val mMarginLeft = 30
    private val mDetector: GestureDetectorCompat

    init {
        mDetector = GestureDetectorCompat(this.context, MyGestureListener())
    }

    private inner class MyGestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent?): Boolean {
            Log.d(TAG, "onSingleTapUp: called")
            for(ship in mShips)
                if(ship.isTouched){
                    ship.isHorizontal = !ship.isHorizontal
                    ship.isTouched = false
                }
            return super.onSingleTapUp(e)
        }
    }

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
        var value = super.onTouchEvent(event)

        val x = event?.x
        val y = event?.y

        when(event?.action){
            // Once player touches the view
            MotionEvent.ACTION_DOWN -> {
//                Log.d(TAG, "onTouchEvent: action down triggered")
//                Log.d(TAG, "onTouchEvent: touch coords: x = $x, y = $y")
                value = false

                // Determine which ship has been touched
                for(ship in mShips){
                    if (ship.rect.contains(x!!, y!!)){
                        Log.d(TAG, "onTouchEvent: ship of size ${ship.size}")
                        ship.isTouched = true
                        value = true
                    } else {
                        ship.isTouched = false
                    }
                }
                if (value) mDetector.onTouchEvent(event)
            }
            // If ACTION_DOWN returned true then do this
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
                        // If ship is outside of the board it has invalid position
                        ship.hasInvalidPos = !mBoardRect.contains(ship.rect)
                        // If ship intersects any other ship it has invalid position
                        if(!ship.hasInvalidPos)
                            for(otherShip in mShips){
                                if(otherShip == ship) continue
                                if(RectF.intersects(ship.rect, otherShip.rect))
                                    ship.hasInvalidPos = true
                            }
                        value = true
                    }
            }
            MotionEvent.ACTION_UP -> {
                val xInBoardSpace = (x!! / mCellWidth).toInt()
                val yInBoardSpace = (y!! / mCellHeight).toInt()

                for(ship in mShips)
                    if (ship.isTouched) {
                        if (ship.hasInvalidPos) {
                            Log.d(TAG, "onTouchEvent: ${ship.rect}")
                            // Return ship to original position and break out of this event
                            ship.rect = RectF(ship.initialPos)
                            ship.hasInvalidPos = false  // Ship now has a valid position
                            ship.isTouched = false
                            invalidate()
                            return true
                        } else {
                            mTmpRect.apply {
                                left = xInBoardSpace * mCellWidth
                                top = yInBoardSpace * mCellHeight
                                right = left + ship.rect.width()
                                bottom = top + ship.rect.height()
                            }
                            Log.d(TAG, "action up: pos on release - ${ship.rect}")
                            ship.rect.set(RectF(mTmpRect))
                            Log.d(TAG, "action up: pos after snap - ${ship.rect}")
                            ship.isTouched = false
                        }
                        value = true
                    }
            }
        }

        invalidate()
        return value
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

    fun finishEditing(){
        // Set the board state according to where the ships are placed
        for(ship in mShips)
            for(i in 0 until ship.size){
                // Convert ship coords to board coords
                val xInBoardSpace = (ship.rect.left / mCellWidth).toInt() + i
                val yInBoardSpace = (ship.rect.top / mCellHeight).toInt()
                mBoardState[xInBoardSpace][yInBoardSpace] = 1
            }
    }
}