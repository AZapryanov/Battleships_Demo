package com.example.battleships_demo.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.battleships_demo.bluetooth.BluetoothService
import com.example.battleships_demo.databinding.ActivityGameBinding
import com.example.battleships_demo.databinding.ShipHitToastBinding
import com.example.battleships_demo.databinding.SuccessfulAttackToastBinding
import com.example.battleships_demo.viemodels.GameActivityViewModel

class GameActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "GameActivity"
        private const val PHASE_MARK_ATTACK = "doAttack"
        private const val PHASE_TOUCH_INPUTS_LOCKED = "lock"
        const val DO_ANOTHER_ATTACK = "Do another Attack"
        private const val WINNER_MESSAGE = "GG, You have won!"
        private const val DEFEATED_MESSAGE = "GG, You have lost."
    }

    private var mIsPlayerOne = true
    private var mIsNotFirstTurn = false
    private var mIsEndgame = false
    private var mIsActivityPaused = false

    private lateinit var gameActivityViewModel: GameActivityViewModel
    private lateinit var binding: ActivityGameBinding
    private lateinit var shipHitToastBinding: ShipHitToastBinding
    private lateinit var successfulAttackToastBinding: SuccessfulAttackToastBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        val contentView = binding.root
        setContentView(contentView)

        gameActivityViewModel = ViewModelProvider(this)[GameActivityViewModel::class.java]

        shipHitToastBinding = ShipHitToastBinding.inflate(layoutInflater)
        successfulAttackToastBinding = SuccessfulAttackToastBinding.inflate(layoutInflater)

        BluetoothService.register(gameActivityViewModel)
        binding.buttonEndTurn.visibility = View.GONE

        //The following four functions are executed only one time at the start of the game
        setViewModelData()
        getExtrasFromIntent()
        setInitialBoardPhases()
        initializePlayers()

        gameActivityViewModel.mIsDisconnected.observe(this) { isDisconnected ->
            if (isDisconnected) finish()
        }

        gameActivityViewModel.mShouldStartMyNextTurn.observe(this, {
            Log.d(TAG, "My turn starts.")
            binding.cvMyShips.setPhase(PHASE_TOUCH_INPUTS_LOCKED)

            //If one of my ships is hit, the opponent attacks again and I do not enter phase - mark attack
            if (!gameActivityViewModel.mIsShipHitByOpponent) {
                binding.cvMyAttacks.setPhase(PHASE_MARK_ATTACK)
            }

            if (mIsNotFirstTurn) {
                //My ships board is not updated with opponent attack if I have to attack again after a successful hit
                if (!gameActivityViewModel.mIsToDoAnotherAttackAfterHit) {
                    //My ships are updated based on the received attack coordinates from the opponent
                    binding.cvMyShips.updateMyShips(
                        gameActivityViewModel.mOpponentAttackCoordinates,
                        gameActivityViewModel.myShipsPositionsFromPreviousRound
                    )
                    Log.d(TAG, "My ships updated with opponent attack.")
                    
                    gameActivityViewModel.myShipsPositionsFromPreviousRound =
                        binding.cvMyShips.getState()
                    mIsEndgame = binding.cvMyShips.checkIfGameHasEnded()

                    if (mIsEndgame) {
                        doEndgameProcedure(DEFEATED_MESSAGE)
                    }
                }
            }

            gameActivityViewModel.mIsToDoAnotherAttackAfterHit = false
            mIsNotFirstTurn = true

            if (gameActivityViewModel.mIsShipHitByOpponent) {
                Toast(this).apply {
                    duration = Toast.LENGTH_SHORT
                    view = shipHitToastBinding.clShipHitToast
                    show()
                }
                Log.d(TAG, "Message to attack again sent to opponent.")
                gameActivityViewModel.mIsShipHitByOpponent = false

                //If the opponent's attack is successful, he receives a BT message that allows him to attack again immediately
                BluetoothService.write(DO_ANOTHER_ATTACK.toByteArray())
                gameActivityViewModel.startWaitingForOpponentAttack()

            } else {
                if (!mIsEndgame) {
                    binding.buttonEndTurn.visibility = View.VISIBLE
                }
            }
        })

        binding.buttonEndTurn.setOnClickListener {
            //Check is an attack has been marked before allowing the End Turn button to be pressed
            if (binding.cvMyAttacks.getTouchCounter() >= 1) {
                Log.d(TAG, "End turn button clicked.")
                binding.cvMyAttacks.resetBoardTouchCounter()
                binding.cvMyAttacks.setPhase(PHASE_TOUCH_INPUTS_LOCKED)
                binding.buttonEndTurn.visibility = View.GONE

                //Gets the last touch input on the interactive game board
                val myAttackCoordinates = binding.cvMyAttacks.getLastTouchInput()
                gameActivityViewModel.mCoordinatesToSend += myAttackCoordinates[0].toString()
                gameActivityViewModel.mCoordinatesToSend += myAttackCoordinates[1].toString()

                //My attack board is updated based on my attack coordinates and whether it is a hit or miss
                binding.cvMyAttacks.updateMyAttacks(
                    myAttackCoordinates,
                    gameActivityViewModel.myAttacksPositionsFromPreviousRound
                )

                Log.d(TAG, "My attacks updated after check for hit.")

                gameActivityViewModel.myAttacksPositionsFromPreviousRound =
                    binding.cvMyAttacks.getState()
                mIsEndgame = binding.cvMyAttacks.checkIfGameHasEnded()

                if (mIsEndgame) {
                    doEndgameProcedure(WINNER_MESSAGE)

                } else {
                    binding.cvMyAttacks.resetBoardTouchCounter()
                    gameActivityViewModel.mCoordinatesToSend += if (binding.cvMyAttacks.checkIfAttackIsAHit(myAttackCoordinates)) {
                        Toast(this).apply {
                            duration = Toast.LENGTH_SHORT
                            view = successfulAttackToastBinding.clSuccessfulAttackToast
                            show()
                        }
                        Log.d(TAG, "My attack was successful, will attack again.")
                        //Gives information to the opponent that my attack has hit one of his ships
                        1
                    } else {
                        //Gives information to the opponent that my attack missed
                        0
                    }
                }

                Log.d(TAG, "Attack sent to opponent.")
                //Send my attack coordinates to the other player through BT so that his game ends too and he gets a message that he is defeated
                BluetoothService.write(gameActivityViewModel.mCoordinatesToSend.toByteArray())
                gameActivityViewModel.startWaitingForOpponentAttack()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (mIsActivityPaused) {
            executeOnStartIfWasOnPause()
        }
        Log.d(TAG, "Entered onStart")
        Log.d(TAG, "Restored the state of the game - $mIsActivityPaused")
    }

    override fun onPause() {
        super.onPause()
        mIsActivityPaused = true
        Log.d(TAG, "Entered onPause")
    }

    override fun onDestroy() {
        super.onDestroy()
        BluetoothService.unregister(gameActivityViewModel)
        Log.d(TAG, "Entered onDestroy")
    }

    private fun setViewModelData() {
        gameActivityViewModel.myShipsPositionsFromPreviousRound =
            intent.extras!!.get(PlaceShipsActivity.EXTRA_MY_SHIPS) as Array<Array<Int>>
        gameActivityViewModel.myAttacksPositionsFromPreviousRound = binding.cvMyAttacks.getState()
    }

    private fun getExtrasFromIntent() {
        binding.cvMyShips.setBoardState(intent.extras!!.get(PlaceShipsActivity.EXTRA_MY_SHIPS) as Array<Array<Int>>)
        binding.cvMyAttacks.setOpponentShipsPositions(intent.extras!!.get(PlaceShipsActivity.EXTRA_OPPONENT_SHIPS) as Array<Array<Int>>)
        mIsPlayerOne = intent.getBooleanExtra(PlaceShipsActivity.EXTRA_IS_PLAYER_ONE, false)
        Log.d(TAG, "I am player one = $mIsPlayerOne.")
    }

    private fun setInitialBoardPhases() {
        binding.cvMyShips.setPhase(PHASE_TOUCH_INPUTS_LOCKED)
        binding.cvMyAttacks.setPhase(PHASE_TOUCH_INPUTS_LOCKED)
    }

    private fun initializePlayers() {
        if (mIsPlayerOne) {
            Log.d(TAG, "Starting my first turn.")
            gameActivityViewModel.startNextTurn()
        } else {
            Log.d(TAG, "Waiting for opponents first turn.")
            mIsNotFirstTurn = true
            gameActivityViewModel.startWaitingForOpponentAttack()
        }
    }

    private fun doEndgameProcedure(messageToShow: String) {
        //Lock all inputs-------------------------------
        Log.d(TAG, "Game has ended.")
        binding.cvMyAttacks.setPhase(PHASE_TOUCH_INPUTS_LOCKED)
        binding.buttonEndTurn.visibility = View.GONE
        //----------------------------------------------

        //Show appropriate pop-up message and toast for Winner or Defeated
        showWinnerOrDefeatedImage(messageToShow)

        //The remaining enemy ships are shown if the game is lost
        binding.cvMyAttacks.visualizeRemainingOpponentShips()
    }

    private fun showWinnerOrDefeatedImage(messageToShow: String) {
        if (messageToShow == WINNER_MESSAGE) {
            binding.ivWinner.visibility = View.VISIBLE

        } else if (messageToShow == DEFEATED_MESSAGE) {
            binding.ivDefeated.visibility = View.VISIBLE
        }
        Toast.makeText(this, messageToShow, Toast.LENGTH_LONG).show()
    }

    private fun executeOnStartIfWasOnPause() {
        //Restore my Ships and my Attacks states as they were before the activity went onPause
        restoreShipsAndAttacksBoardStates()

        //Check whether the activity was paused, in the middle of my turn
        // or while waiting for opponent attack and restore the game state accordingly
        if (gameActivityViewModel.mIsWaitingForOpponentTurn) {
            enterStateWaitForOpponentAttack()
        } else {
            gameActivityViewModel.startNextTurn()
        }

        resetGameStateRelatedBooleans()
    }

    private fun enterStateWaitForOpponentAttack() {
        binding.cvMyShips.setPhase(PHASE_TOUCH_INPUTS_LOCKED)
        binding.cvMyAttacks.setPhase(PHASE_TOUCH_INPUTS_LOCKED)
        binding.buttonEndTurn.visibility = View.GONE
        gameActivityViewModel.startWaitingForOpponentAttack()
    }

    private fun restoreShipsAndAttacksBoardStates() {
        binding.cvMyAttacks.setBoardState(gameActivityViewModel.myAttacksPositionsFromPreviousRound)
        binding.cvMyShips.setBoardState(gameActivityViewModel.myShipsPositionsFromPreviousRound)
    }

    private fun resetGameStateRelatedBooleans() {
        gameActivityViewModel.mIsWaitingForOpponentTurn = false
        mIsActivityPaused = false
    }
}