package com.example.battleships_demo.customviews

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.example.battleships_demo.R

open class Board(context: Context, attrs: AttributeSet) : View(context, attrs) {
    protected var mBoardSize: Int
    protected var mCellWidth: Float = 0f
    protected var mCellHeight: Float = 0f

    // This creates a 2D array initialized with 0s
    protected var mBoardState: Array<Array<Int>>
    protected val mDefRectPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    protected val mGreenPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Used to fill boxes with already attacked ship parts in them
    private val mRedPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    protected var mCellRect: RectF

    companion object {
        private const val TAG = "Board"
    }

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.Board,
            0,
            0
        ).apply {
            try {
                mBoardSize = getInt(R.styleable.Board_size, 0)
            } finally {
                recycle()
            }
        }

        mBoardState = Array(mBoardSize) { Array(mBoardSize) { 0 } }

        mDefRectPaint.style = Paint.Style.STROKE
        mDefRectPaint.color = Color.BLACK
        mDefRectPaint.strokeWidth = 5f

        mGreenPaint.style = Paint.Style.FILL
        mGreenPaint.color = Color.GREEN

        mRedPaint.style = Paint.Style.FILL
        mRedPaint.color = Color.RED

        mCellRect = RectF()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        mCellWidth = (w.toFloat() / mBoardSize)
        mCellHeight = (h.toFloat() / mBoardSize)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        for (i in 0 until mBoardSize) {
            for (j in 0 until mBoardSize) {
                val left = mCellWidth * i
                val top = mCellHeight * j
                val right = left + mCellWidth
                val bottom = top + mCellHeight
                mCellRect.set(left, top, right, bottom)

                when {
                    mBoardState[i][j] == 0 -> {
                        canvas?.drawRect(mCellRect, mDefRectPaint)
                    }
                    mBoardState[i][j] == 1 -> {
                        canvas?.drawRect(mCellRect, mGreenPaint)
                    }
                    mBoardState[i][j] == 2 -> {
                        canvas?.drawRect(mCellRect,mDefRectPaint)
                        drawCross(canvas)
                    }
                    mBoardState[i][j] == 3 -> {
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
            mCellRect.left + 15,
            mCellRect.top + 15,
            mCellRect.right - 15,
            mCellRect.bottom - 15,
            mDefRectPaint
        )
        canvas?.drawLine(
            mCellRect.left + 15,
            mCellRect.bottom - 15,
            mCellRect.right - 15,
            mCellRect.top + 15,
            mDefRectPaint
        )
    }

    fun getBoardState(): Array<Array<Int>> {
        return mBoardState
    }
}