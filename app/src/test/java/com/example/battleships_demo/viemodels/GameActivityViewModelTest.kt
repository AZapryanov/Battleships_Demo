package com.example.battleships_demo.viemodels


import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule

import org.junit.Test
import org.junit.rules.TestRule

class GameActivityViewModelTest {
    private lateinit var gameActivityViewModel: GameActivityViewModel

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        gameActivityViewModel = GameActivityViewModel()
    }

    @Test
    fun `if value is null should become 2`() {
        gameActivityViewModel.mShouldStartMyNextTurn.value = null
        gameActivityViewModel.startNextTurn()
        val result = gameActivityViewModel.mShouldStartMyNextTurn.value
        assertThat(result).isEqualTo(2)
    }

    @Test
    fun `if value is 1 should become 2`() {
        gameActivityViewModel.mShouldStartMyNextTurn.value = 1
        gameActivityViewModel.startNextTurn()
        val result = gameActivityViewModel.mShouldStartMyNextTurn.value
        assertThat(result).isEqualTo(2)
    }

    @Test
    fun `if value is 2 should become 1`() {
        gameActivityViewModel.mShouldStartMyNextTurn.value = 2
        gameActivityViewModel.startNextTurn()
        val result = gameActivityViewModel.mShouldStartMyNextTurn.value
        assertThat(result).isEqualTo(1)
    }

    @Test
    fun `if message is specified should change boolean to true`() {
        var value = ""
        value += 0
        value += 0
        value += 1
        gameActivityViewModel.startWaitingForOpponentAttack()
        gameActivityViewModel.setMReceivedBluetoothMessage(value)
        val result = gameActivityViewModel.mIsShipHitByOpponent
        assertThat(result).isEqualTo(true)
    }

}