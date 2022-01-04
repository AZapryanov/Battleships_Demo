package com.example.battleships_demo.customviews

import android.content.Context
import android.graphics.*
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import com.example.battleships_demo.Ship

class EditableBoard(context: Context, attrs: AttributeSet) : Board(context, attrs) {
    companion object{
        private const val TAG = "EditableBoard"
    }

    private val mBoardRect = RectF()

    private val mShip5 = Ship(5)
    private val mShip4 = Ship(4)
    private val mShip3 = Ship(3)
    private val m2ndShip3 = Ship(3)
    private val mShip2 = Ship(2)
    private var mTmpRect = RectF()

    private var mShipTouched = 0
    private var m3shipsLeft = 2
    private var mTextPos = PointF()

    private val mMarginTop = 60
    private val mMarginLeft = 30

    private val mTextPaint = Paint(ANTI_ALIAS_FLAG)

    init {
        mTextPaint.style = Paint.Style.FILL
        mTextPaint.color = Color.BLACK
        mTextPaint.textSize = 70f
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        // Cell height is equal to cell width for cells to be square
        mCellWidth = (w.toFloat() / mBoardSize)
        mCellHeight = mCellWidth

        mBoardRect.set(0f, 0f, mCellWidth*mBoardSize, mCellHeight*mBoardSize)

        // Initialize all ships here after getting the cell width and height
        // first row
        var x = 0f
        var y = mBoardRect.bottom + mMarginTop
        mShip5.rect.set(x, y, x+(mCellWidth*mShip5.size), y+mCellHeight)
        mShip5.initialPos.set(RectF(mShip5.rect))

        x = mShip5.rect.right + mMarginLeft
        mShip4.rect.set(x, y, x+(mCellWidth*mShip4.size), y+mCellHeight)
        mShip4.initialPos.set(RectF(mShip4.rect))

        // Second row
        x = 0f
        y = mShip5.rect.bottom + mMarginTop
        mShip3.rect.set(x, y, x+(mCellWidth*mShip3.size), y+mCellHeight)
        mShip3.initialPos.set(RectF(mShip3.rect))
        m2ndShip3.rect.set(x, y, x+(mCellWidth*m2ndShip3.size), y+mCellHeight)
        m2ndShip3.initialPos.set(RectF(m2ndShip3.rect))

        // Set text relative to the length 3 ships
        mTextPos.set(mShip3.rect.right + 20, mShip3.rect.bottom)

        x = mShip4.rect.left
        mShip2.rect.set(x, y, x+(mCellWidth*mShip2.size), y+mCellHeight)
        mShip2.initialPos.set(RectF(mShip2.rect))
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

                // Determine if which ship has been touched
                if (mShip5.rect.contains(x!!, y!!)) {
                    Log.d(TAG, "onTouchEvent: ship5 touched, initPos ${mShip5.initialPos}")
                    mShipTouched = 5
                    value = true
                } else if (mShip4.rect.contains(x, y)) {
                    Log.d(TAG, "onTouchEvent: ship4 touched")
                    mShipTouched = 4
                    value = true
                } else if (mShip3.rect.contains(x, y)) {
                    Log.d(TAG, "onTouchEvent: ship3 touched")
                    mShipTouched = 3
                    value = true
                } else if(m2ndShip3.rect.contains(x, y)) {
                    Log.d(TAG, "onTouchEvent: 2ndShip3 touched")
                    if(!mShip3.rect.contains(m2ndShip3.rect)) {
                        // Decided to give the 2nd ship of length 3 the number 3_2 for readability
                        // in actuality it's just the number 32
                        mShipTouched = 3_2
                        value = true
                    }
                } else if (mShip2.rect.contains(x, y)) {
                    Log.d(TAG, "onTouchEvent: ship2 touched")
                    mShipTouched = 2
                    value = true
                } else if (mShip1.contains(x, y)) {
                    Log.d(TAG, "onTouchEvent: ship1 touched")
                    mShipTouched = 1
                    value = true
                } else {
                    value = false
                }
            }
            // If ACTION_DOWN returned true then do this
            MotionEvent.ACTION_MOVE -> {
                //Log.d(TAG, "onTouchEvent: action move triggered")
                //Log.d(TAG, "onTouchEvent: touch coords x = $x, y = $y")

                // left and top are always gonna be where the touch coords are
                // "- mCell../2" makes your finger be in the center of the ship's first square
                mTmpRect.left = x!! - mCellWidth/2
                mTmpRect.top = y!! - mCellHeight/2

                // Change the ship's rect accordingly
                when(mShipTouched){
                    5 -> {
                        mTmpRect.right = mTmpRect.left + mShip5.rect.width()
                        mTmpRect.bottom = mTmpRect.top + mShip5.rect.height()

                        mShip5.rect.set(mTmpRect)
                        // If ship is outside of the board it has invalid position
                        mShip5.invalidPos = !mBoardRect.contains(mShip5.rect)
                        value = true
                    }
                    4 -> {
                        mTmpRect.right = mTmpRect.left + mShip4.rect.width()
                        mTmpRect.bottom = mTmpRect.top + mShip4.rect.height()

                        mShip4.rect.set(mTmpRect)
                        mShip4.invalidPos = !mBoardRect.contains(mShip4.rect)
                        value = true
                    }
                    3 -> {
                        mTmpRect.right = mTmpRect.left + mShip3.rect.width()
                        mTmpRect.bottom = mTmpRect.top + mShip3.rect.height()

                        mShip3.rect.set(mTmpRect)
                        mShip3.invalidPos = !mBoardRect.contains(mShip3.rect)
                        value = true
                    }
                    3_2 -> {
                        mTmpRect.right = mTmpRect.left + m2ndShip3.rect.width()
                        mTmpRect.bottom = mTmpRect.top + m2ndShip3.rect.height()

                        m2ndShip3.rect.set(mTmpRect)
                        m2ndShip3.invalidPos = !mBoardRect.contains(m2ndShip3.rect)
                        value = true
                    }
                    2 -> {
                        mTmpRect.right = mTmpRect.left + mShip2.rect.width()
                        mTmpRect.bottom = mTmpRect.top + mShip2.rect.height()

                        mShip2.rect.set(mTmpRect)
                        mShip2.invalidPos = !mBoardRect.contains(mShip2.rect)
                        value = true
                    }
                    1 -> {
                        mTmpRect.right = mTmpRect.left + mShip1.width()
                        mTmpRect.bottom = mTmpRect.top + mShip1.height()

                        mShip1.set(mTmpRect)
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

                mTmpRect.left = xInBoardSpace*mCellWidth
                mTmpRect.top = yInBoardSpace*mCellHeight

                when(mShipTouched) {
                    5 -> {
                        if(mShip5.invalidPos) {
                            // Return ship to original position and break out of this event
                            mShip5.apply {
                                rect = RectF(initialPos)
                                invalidPos = false
                            }
                            invalidate()
                            return true
                        } else {
                            // Snap the ship to the cell where your finger is
                            mTmpRect.apply {
                                right = left + mShip5.rect.width()
                                bottom = top + mShip5.rect.height()
                            }
                            mShip5.rect.set(mTmpRect)
                        }
                    }
                    4 -> {
                        if(mShip4.invalidPos) {
                            mShip4.apply {
                                rect = RectF(initialPos)
                                invalidPos = false
                            }
                            invalidate()
                            return true
                        } else {
                            mTmpRect.apply {
                                right = left + mShip4.rect.width()
                                bottom = top + mShip4.rect.height()
                            }
                            mShip4.rect.set(mTmpRect)
                        }
                    }
                    3 -> {
                        if(mShip3.invalidPos) {
                            mShip3.apply {
                                rect = RectF(initialPos)
                                invalidPos = false
                            }
                            invalidate()
                            return true
                        } else {
                            mTmpRect.apply {
                                right = left + mShip3.rect.width()
                                bottom = top + mShip3.rect.height()
                            }
                            mShip3.rect.set(mTmpRect)
                            if(m3shipsLeft != 0) m3shipsLeft--
                        }
                    }
                    3_2 -> {
                        if(m2ndShip3.invalidPos) {
                            m2ndShip3.apply {
                                rect = RectF(initialPos)
                                invalidPos = false
                            }
                            invalidate()
                            return true
                        } else {
                            mTmpRect.apply {
                                right = left + m2ndShip3.rect.width()
                                bottom = top + m2ndShip3.rect.height()
                            }
                            m2ndShip3.rect.set(mTmpRect)
                            if(m3shipsLeft != 0) m3shipsLeft--
                        }
                    }
                    2 -> {
                        if(mShip2.invalidPos) {
                            mShip2.apply {
                                rect = RectF(initialPos)
                                invalidPos = false
                            }
                            invalidate()
                            return true
                        } else {
                            mTmpRect.apply {
                                right = left + mShip2.rect.width()
                                bottom = top + mShip2.rect.height()
                            }
                            mShip2.rect.set(mTmpRect)
                        }
                    }
                }

                // 3_2 number is actually 32 and that messes up the for loop
                // so I just change it to 3
//                if(mShipTouched == 3_2) {
//                    mShipTouched = 3
//                    m2ndShip3.rect.setEmpty()
//                }
//                for(i in 0 until mShipTouched){
//                    mBoardState[xInBoardSpace + i][yInBoardSpace] = 1
//                }

                value = true
            }
        }

        invalidate()

        return value
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.apply {
            if(mShip5.invalidPos) {
                drawRect(mShip5.rect, mRedPaint)
            }
            else drawRect(mShip5.rect, mGreenPaint)

            if(mShip4.invalidPos) {
                drawRect(mShip4.rect, mRedPaint)
            }
            else drawRect(mShip4.rect, mGreenPaint)

            if(mShip3.invalidPos) {
                drawRect(mShip3.rect, mRedPaint)
            }
            else drawRect(mShip3.rect, mGreenPaint)

            if(m2ndShip3.invalidPos) {
                drawRect(m2ndShip3.rect, mRedPaint)
            }
            else drawRect(m2ndShip3.rect, mGreenPaint)

            if(mShip2.invalidPos) {
                drawRect(mShip2.rect, mRedPaint)
            }
            else drawRect(mShip2.rect, mGreenPaint)
        }

        canvas?.drawText("${m3shipsLeft}x", mTextPos.x, mTextPos.y, mTextPaint)
    }

    fun finishEditing(){
        // Set the board state according to where the ships are placed
        // I'll clean this up later!
        for(i in 0 until mShip5.size)
            mBoardState[(mShip5.rect.left / mCellWidth).toInt() + i][(mShip5.rect.top / mCellHeight).toInt()] = 1
        for(i in 0 until mShip4.size)
            mBoardState[(mShip4.rect.left / mCellWidth).toInt() + i][(mShip4.rect.top / mCellHeight).toInt()] = 1
        for(i in 0 until mShip3.size)
            mBoardState[(mShip3.rect.left / mCellWidth).toInt() + i][(mShip3.rect.top / mCellHeight).toInt()] = 1
        for(i in 0 until m2ndShip3.size)
            mBoardState[(m2ndShip3.rect.left / mCellWidth).toInt() + i][(m2ndShip3.rect.top / mCellHeight).toInt()] = 1
        for(i in 0 until mShip2.size)
            mBoardState[(mShip2.rect.left / mCellWidth).toInt() + i][(mShip2.rect.top / mCellHeight).toInt()] = 1
    }
}