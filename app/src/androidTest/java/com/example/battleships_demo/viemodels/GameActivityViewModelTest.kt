package com.example.battleships_demo.viemodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth
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
    fun triggerForNextTurnWorksCorrectlyWithInitialValue() {
        gameActivityViewModel.mShouldStartMyNextTurn.value = null
        gameActivityViewModel.startNextTurn()
        val result = gameActivityViewModel.mShouldStartMyNextTurn.value
        Truth.assertThat(result).isEqualTo(GameActivityViewModel.SWAPPABLE_TWO)
    }

    @Test
    fun triggerForNextTurnWorksCorrectlyWithFirstSwappableValue() {
        gameActivityViewModel.mShouldStartMyNextTurn.value = GameActivityViewModel.SWAPPABLE_ONE
        gameActivityViewModel.startNextTurn()
        val result = gameActivityViewModel.mShouldStartMyNextTurn.value
        Truth.assertThat(result).isEqualTo(GameActivityViewModel.SWAPPABLE_TWO)
    }

    @Test
    fun triggerForNextTurnWorksCorrectlyWithSecondSwappableValue() {
        gameActivityViewModel.mShouldStartMyNextTurn.value = GameActivityViewModel.SWAPPABLE_TWO
        gameActivityViewModel.startNextTurn()
        val result = gameActivityViewModel.mShouldStartMyNextTurn.value
        Truth.assertThat(result).isEqualTo(GameActivityViewModel.SWAPPABLE_ONE)
    }
}