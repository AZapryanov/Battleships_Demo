package com.example.battleships_demo.customviews

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.example.battleships_demo.R

class Board(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private var mBoardSize: Int
    private var mCellWidth: Float = 0f
    private var mCellHeight: Float = 0f

    // This creates a 2D array initialized with 0s
    private var mBoardState: Array<Array<Int>>
    private val mEmptyPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mClickedPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var mCellRect: RectF

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

        mEmptyPaint.style = Paint.Style.STROKE
        mEmptyPaint.color = Color.BLACK
        mEmptyPaint.strokeWidth = 5f

        mClickedPaint.style = Paint.Style.FILL_AND_STROKE
        mClickedPaint.color = Color.GRAY
        mClickedPaint.strokeWidth = 5f

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

                if (mBoardState[i][j] == 0) {
                    canvas?.drawRect(mCellRect, mEmptyPaint)
                }
            }
        }
    }
}