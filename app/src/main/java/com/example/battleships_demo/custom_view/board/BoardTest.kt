package com.example.battleships_demo.custom_view.board

import android.content.Context
import android.graphics.*
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.battleships_demo.R
import com.example.battleships_demo.common.Constants

class BoardTest @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var mBoardSize: Int
    private var mCellWidth: Float = 0f
    private var mCellHeight: Float = 0f

    // This creates a 2D array initialized with 0s
    private var mBoardState: Array<Array<Int>>
    private val mEmptyPaint = Paint(ANTI_ALIAS_FLAG)

    // Used to fill boxes with ship parts in them
    private val mGreenPaint = Paint(ANTI_ALIAS_FLAG)

    // Used to fill boxes with already attacked ship parts in them
    private val mRedPaint = Paint(ANTI_ALIAS_FLAG)
    private val mClickedPaint = Paint(ANTI_ALIAS_FLAG)

    // If this == 1 -> when there is an OnTouchEvent, the value of the touched box will be set to 1
    // and onDraw will draw a cross inside the box
    // If this == 2 -> when there is an OnTouchEvent, the value of the touched box will be set to 2
    // and onDraw will fill the box with green color instead of putting a cross inside
    // If this == 3 -> when there is an OnTouchEvent, the value of the touched box will be set to 3
    // and onDraw will fill the box with red color AND draw a cross inside
    private var whatToDoOnTouch: String? = null

    private var currentPhase: String = ""
    private var touchCounter: Int = 0

    private var mCellRect: RectF

    companion object {
        private const val TAG = "BoardTest"
    }

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.BoardTest,
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
                        canvas?.drawRect(mCellRect, mEmptyPaint)
                    }
                    mBoardState[i][j] == 1 -> {
                        canvas?.drawRect(mCellRect, mEmptyPaint)
                        drawCross(canvas)
                    }
                    mBoardState[i][j] == 2 -> {
                        canvas?.drawRect(mCellRect, mGreenPaint)
                        canvas?.drawRect(mCellRect, mEmptyPaint)
                    }
                    mBoardState[i][j] == 3 -> {
                        canvas?.drawRect(mCellRect, mRedPaint)
                        canvas?.drawRect(mCellRect, mEmptyPaint)
                        drawCross(canvas)
                    }
                }
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (touchCounter >= 1 && currentPhase == "playerTwoAttack") {
            return false

        } else if (touchCounter >= 17 && currentPhase == "playerOnePlaceShips") {
            return false

        } else if (currentPhase == "playerOneLookAtDyingShips^^") {
            return false

        } else {
            val value = super.onTouchEvent(event)

            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Converts the tapped position to board coords
                    val cellX = (event.x / mCellWidth).toInt()
                    val cellY = (event.y / mCellHeight).toInt()

                    //Check whether to fill the box (set a ship) or put a cross (attack)
                    when (whatToDoOnTouch) {
                        Constants.DRAW_CROSS -> {
                            mBoardState[cellX][cellY] = 1
                        }
                        Constants.DRAW_SHIP_PART -> {
                            mBoardState[cellX][cellY] = 2
                        }
                        Constants.DRAW_RED_SHIP_PART_WITH_CROSS -> {
                            mBoardState[cellX][cellY] = 3
                        }
                    }
                    invalidate()
                    touchCounter++
                    return true
                }
            }
            return value
        }
    }

    private fun drawCross(canvas: Canvas?) {
        canvas?.drawLine(
            mCellRect.left + 15,
            mCellRect.top + 15,
            mCellRect.right - 15,
            mCellRect.bottom - 15,
            mEmptyPaint
        )
        canvas?.drawLine(
            mCellRect.left + 15,
            mCellRect.bottom - 15,
            mCellRect.right - 15,
            mCellRect.top + 15,
            mEmptyPaint
        )
    }

    fun setWhatToDoOnTouch(input: String) {
        whatToDoOnTouch = input
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

    fun setPhase(phase: String) {
        currentPhase = phase
    }
//    fun clearBoardState() {
//        mBoardState = Array(mBoardSize) { Array(mBoardSize) {0} }
//    }
}