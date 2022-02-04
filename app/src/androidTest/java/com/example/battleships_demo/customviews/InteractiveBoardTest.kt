package com.example.battleships_demo.customviews

import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth
import org.junit.Before
import org.junit.Test

class InteractiveBoardTest {
    companion object {
        private const val BOARD_SIZE = 10
    }

    private lateinit var testBoard: InteractiveBoard
    private lateinit var testMatrixOnlyZeros: Array<Array<Int>>

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        testBoard = InteractiveBoard(context, null)
        testMatrixOnlyZeros = Array(BOARD_SIZE) { Array(BOARD_SIZE) { InteractiveBoard.EMPTY_BOX } }
        testBoard.setBoardState(testMatrixOnlyZeros)
    }

    @Test
    fun attackOnBlankFieldChangesMatrixValueCorrectly() {
        val opponentShipsPositions = testMatrixOnlyZeros
        testBoard.setOpponentShipsPositions(opponentShipsPositions)

        val attackCoordinates = arrayOf(0, 0)
        val myAttackPositions = testMatrixOnlyZeros
        testBoard.updateMyAttacks(attackCoordinates, myAttackPositions)

        val result = testBoard.getState()[0][0]
        Truth.assertThat(result).isEqualTo(InteractiveBoard.CROSS)
    }

    @Test
    fun attackOnShipFieldChangesMatrixValueCorrectly() {
        val opponentShipsPositions = testMatrixOnlyZeros
        opponentShipsPositions[0][0] = InteractiveBoard.SHIP_PART
        testBoard.setOpponentShipsPositions(opponentShipsPositions)

        val attackCoordinates = arrayOf(0, 0)
        val myAttackPositions = testMatrixOnlyZeros
        testBoard.updateMyAttacks(attackCoordinates, myAttackPositions)

        val result = testBoard.getState()[0][0]
        Truth.assertThat(result).isEqualTo(InteractiveBoard.SHIP_PART_HIT)
    }

    @Test
    fun attackOnCrossFieldDoesNothing() {
        val opponentShipsPositions = testMatrixOnlyZeros
        opponentShipsPositions[0][0] = InteractiveBoard.CROSS
        testBoard.setOpponentShipsPositions(opponentShipsPositions)

        val attackCoordinates = arrayOf(0, 0)
        val myAttackPositions = testMatrixOnlyZeros
        testBoard.updateMyAttacks(attackCoordinates, myAttackPositions)

        val result = testBoard.getState()[0][0]
        Truth.assertThat(result).isEqualTo(InteractiveBoard.CROSS)
    }

    @Test
    fun attackOnHitShipFieldDoesNothing() {
        val opponentShipsPositions = testMatrixOnlyZeros
        opponentShipsPositions[0][0] = InteractiveBoard.SHIP_PART_HIT
        testBoard.setOpponentShipsPositions(opponentShipsPositions)

        val attackCoordinates = arrayOf(0, 0)
        val myAttackPositions = testMatrixOnlyZeros
        testBoard.updateMyAttacks(attackCoordinates, myAttackPositions)

        val result = testBoard.getState()[0][0]
        Truth.assertThat(result).isEqualTo(InteractiveBoard.SHIP_PART_HIT)
    }

    @Test
    fun successfulAttackReturnsTrue() {
        val opponentShipsPositions = testMatrixOnlyZeros
        opponentShipsPositions[0][0] = InteractiveBoard.SHIP_PART
        testBoard.setOpponentShipsPositions(opponentShipsPositions)

        val attackCoordinates = arrayOf(0, 0)
        val result = testBoard.checkIfAttackIsAHit(attackCoordinates)

        Truth.assertThat(result).isTrue()
    }

    @Test
    fun unsuccessfulAttackReturnsFalse() {
        val opponentShipsPositions = testMatrixOnlyZeros

        //The test should pass with any value different from the
        // one set in the constant: InteractiveBoard.SHIP_PART
        opponentShipsPositions[0][0] = InteractiveBoard.EMPTY_BOX
        testBoard.setOpponentShipsPositions(opponentShipsPositions)

        val attackCoordinates = arrayOf(0, 0)
        val result = testBoard.checkIfAttackIsAHit(attackCoordinates)

        Truth.assertThat(result).isFalse()
    }

    @Test
    fun correctValuesAreSetForRemainingOpponentShips() {
        val opponentShipsPositions = testMatrixOnlyZeros
        opponentShipsPositions[0][0] = InteractiveBoard.SHIP_PART
        opponentShipsPositions[4][5] = InteractiveBoard.SHIP_PART
        testBoard.setOpponentShipsPositions(opponentShipsPositions)

        val boardState = testMatrixOnlyZeros
        boardState[0][0] = InteractiveBoard.SHIP_PART_HIT
        testBoard.setBoardState(boardState)

        val result = testMatrixOnlyZeros
        result[0][0] = InteractiveBoard.SHIP_PART_HIT
        result[4][5] = InteractiveBoard.SHIP_PART

        Truth.assertThat(result).isEqualTo(testBoard.getState())
    }
}