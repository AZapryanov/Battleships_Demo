package com.example.battleships_demo.customviews

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import androidx.core.view.marginTop

class EditableBoard(context: Context, attrs: AttributeSet) : Board(context, attrs) {
    companion object{
        private const val TAG = "EditableBoard"
    }

    private val mShip5 = RectF()
    private val mShip4 = RectF()
    private val mShip3 = RectF()
    private val mShip2 = RectF()
    private var mTmpRect = RectF()

    private var mShipTouched = 0

    private val mMarginTop = 60
    private val mMarginLeft = 30


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        mCellWidth = (w.toFloat() / mBoardSize)
        mCellHeight = mCellWidth

        // Initialize all ships here after getting the cell width and height
        // first row
        var x = 0f
        var y = (mBoardSize*mCellHeight)+mMarginTop
        mShip5.set(x, y, x+(mCellWidth*5), y+mCellHeight)

        x = mShip5.right + mMarginLeft
        mShip4.set(x, y, x+(mCellWidth*4), y+mCellHeight)

        // Second row
        x = 0f
        y = mShip5.top + mShip5.height() + mMarginTop
        mShip3.set(x, y, x+(mCellWidth*3), y+mCellHeight)

        x = mShip3.right + mMarginLeft
        mShip2.set(x, y, x+(mCellWidth*2), y+mCellHeight)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        var value = super.onTouchEvent(event)

        val x = event?.x
        val y = event?.y

        when(event?.action){
            // Once player touches the view
            MotionEvent.ACTION_DOWN -> {
                Log.d(TAG, "onTouchEvent: action down triggered")
                Log.d(TAG, "onTouchEvent: touch coords: x = $x, y = $y")
                if (mShip5.contains(x!!, y!!)) {
                    Log.d(TAG, "onTouchEvent: ship5 touched")
                    mShipTouched = 5
                    value = true
                } else if (mShip4.contains(x, y)) {
                    Log.d(TAG, "onTouchEvent: ship4 touched")
                    mShipTouched = 4
                    value = true
                } else if (mShip3.contains(x, y)) {
                    Log.d(TAG, "onTouchEvent: ship3 touched")
                    mShipTouched = 3
                    value = true
                } else if (mShip2.contains(x, y)) {
                    Log.d(TAG, "onTouchEvent: ship2 touched")
                    mShipTouched = 2
                    value = true
                }
                else {
                    value = false
                }
            }
            // If ACTION_DOWN returned true then do this
            MotionEvent.ACTION_MOVE -> {
                Log.d(TAG, "onTouchEvent: action move triggered")
                Log.d(TAG, "onTouchEvent: touch coords x = $x, y = $y")

                mTmpRect.left = x!! - mCellWidth/2
                mTmpRect.top = y!! - mCellHeight/2
                when(mShipTouched){
                    5 -> {
                        mTmpRect.right = mTmpRect.left + mShip5.width()
                        mTmpRect.bottom = mTmpRect.top + mShip5.height()

                        mShip5.set(mTmpRect)
                        value = true
                    }
                    4 -> {
                        mTmpRect.right = mTmpRect.left + mShip4.width()
                        mTmpRect.bottom = mTmpRect.top + mShip4.height()

                        mShip4.set(mTmpRect)
                        value = true
                    }
                    3 -> {
                        mTmpRect.right = mTmpRect.left + mShip3.width()
                        mTmpRect.bottom = mTmpRect.top + mShip3.height()

                        mShip3.set(mTmpRect)
                        value = true
                    }
                    2 -> {
                        mTmpRect.right = mTmpRect.left + mShip2.width()
                        mTmpRect.bottom = mTmpRect.top + mShip2.height()

                        mShip2.set(mTmpRect)
                        value = true
                    }
                    else -> {
                        value = false
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                val xInBoardSpace = (x!! / mCellWidth).toInt()
                val yInBoardSpace = (y!! / mCellHeight).toInt()

                for(i in 0 until mShipTouched){
                    mBoardState[xInBoardSpace + i][yInBoardSpace] = 1
                }

                when(mShipTouched){
                    5 -> {mShip5.setEmpty()}
                    4 -> {mShip4.setEmpty()}
                    3 -> {mShip3.setEmpty()}
                    2 -> {mShip2.setEmpty()}
                }

                value = true
            }
        }

        invalidate()

        return value
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.drawRect(mShip5, mGreenPaint)
        canvas?.drawRect(mShip4, mGreenPaint)
        canvas?.drawRect(mShip3, mGreenPaint)
        canvas?.drawRect(mShip2, mGreenPaint)
    }
}