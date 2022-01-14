package com.example.battleships_demo.customviews

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.example.battleships_demo.R

open class Board(context: Context, attrs: AttributeSet) : View(context, attrs) {
    protected var mBoardSize: Int
    protected var mCellWidth: Int = 0
    protected var mCellHeight: Int = 0

    // This creates a 2D array initialized with 0s
    protected var mBoardState: Array<Array<Int>>
    protected val mDefRectPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    protected val mGreenPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Used to fill boxes with already attacked ship parts in them
    protected val mRedPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    protected var mCellRect: Rect

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

        mBoardState = Array(mBoardSize) { Array(mBoardSize) {0} }

        mDefRectPaint.style = Paint.Style.STROKE
        mDefRectPaint.color = Color.BLACK
        mDefRectPaint.strokeWidth = 5f

        mGreenPaint.style = Paint.Style.FILL
        mGreenPaint.color = Color.GREEN

        mRedPaint.style = Paint.Style.FILL
        mRedPaint.color = Color.RED

        mCellRect = Rect()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        mCellWidth = (w / mBoardSize)
        mCellHeight = (h / mBoardSize)
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

                canvas?.drawRect(mCellRect, mDefRectPaint)
            }
        }
    }

    fun getBoardStateAsString(): String {
        var boardState = ""
        for (i in 0 until mBoardSize) {
            for (j in 0 until mBoardSize) {
                boardState += mBoardState[i][j].toString()
            }
        }
        return boardState
    }

    fun getBoardState(): Array<Array<Int>> {
        return mBoardState
    }
}