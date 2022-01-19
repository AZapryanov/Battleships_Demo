package com.example.battleships_demo.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.battleships_demo.R
import com.example.battleships_demo.bluetooth.BluetoothService
import com.example.battleships_demo.bluetooth.BtEvents
import com.example.battleships_demo.viemodels.GameActivityViewModel
import kotlinx.android.synthetic.main.activity_game.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GameActivity : AppCompatActivity(), BluetoothService.BtListener {

    companion object {
        private const val TAG = "GameActivity"
        private const val PHASE_MARK_ATTACK = "doAttack"
        private const val PHASE_TOUCH_INPUTS_LOCKED = "lock"
        private const val SECOND_ATTACK_AFTER_HIT = "It's a hit! Do another attack."
        private const val SHIP_HAS_BEEN_HIT_MESSAGE =
            "Your ship has been hit! Opponent will attack again."
        private const val DO_ANOTHER_ATTACK = "Do another Attack"
        private const val WINNER_MESSAGE = "GG, You have won!"
        private const val DEFEATED_MESSAGE = "GG, You have lost."
        private const val SWAPPABLE_ONE = 1
        private const val SWAPPABLE_TWO = 2
    }

    private val mShouldStartMyNextTurn: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    private var mIsPlayerOne = true
    private var mIsNotFirstTurn = false
    private var mIsEndgame = false
    private var mIsShipHitByOpponent = false
    private var mIsToDoAnotherAttackAfterHit = false
    private var mIsWaitingForOpponentTurn = false
    private var mIsActivityPaused = false

    private lateinit var gameActivityViewModel: GameActivityViewModel

    private var mCoordinatesToSend = ""
    private var mReceivedBluetoothMessage = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        gameActivityViewModel = ViewModelProvider(this)[GameActivityViewModel::class.java]
        BluetoothService.register(this)
        buttonEndTurn.visibility = View.GONE

        //The following four functions are executed only one time at the start of the game
        setViewModelData()
        getExtrasFromIntent()
        setInitialBoardPhases()
        initializePlayers()

        mShouldStartMyNextTurn.observe(this, {
            Log.d(TAG, "My turn starts.")
            cvMyShips.setPhase(PHASE_TOUCH_INPUTS_LOCKED)

            //If one of my ships is hit, the opponent attacks again and I do not enter phase - mark attack
            if (!mIsShipHitByOpponent) {
                cvMyAttacks.setPhase(PHASE_MARK_ATTACK)
            }

            if (mIsNotFirstTurn) {
                //My ships board is not updated with opponent attack if I have to attack again after a successful hit
                if (!mIsToDoAnotherAttackAfterHit) {
                    //My ships are updated based on the received attack coordinates from the opponent
                    cvMyShips.updateMyShips(
                        gameActivityViewModel.opponentAttackCoordinates,
                        gameActivityViewModel.myShipsPositionsFromPreviousRound
                    )
                    Log.d(TAG, "My ships updated with opponent attack.")
                    
                    gameActivityViewModel.myShipsPositionsFromPreviousRound =
                        cvMyShips.getBoardState()
                    mIsEndgame = cvMyShips.checkIfGameHasEnded()

                    if (mIsEndgame) {
                        doEndgameProcedure(DEFEATED_MESSAGE)
                    }
                }
            }
            mIsToDoAnotherAttackAfterHit = false
            mIsNotFirstTurn = true

            if (mIsShipHitByOpponent) {
                Toast.makeText(this, SHIP_HAS_BEEN_HIT_MESSAGE, Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Message to attack again sent to opponent.")
                mIsShipHitByOpponent = false

                //If the opponent's attack is successful, he receives a BT message that allows him to attack again immediately
                BluetoothService.write(DO_ANOTHER_ATTACK.toByteArray())

            } else {
                if (!mIsEndgame) {
                    buttonEndTurn.visibility = View.VISIBLE
                }
            }
        })

        buttonEndTurn.setOnClickListener {
            //Check is an attack has been marked before allowing the End Turn button to be pressed
            if (cvMyAttacks.getTouchCounter() >= 1) {
                Log.d(TAG, "End turn button clicked.")
                cvMyAttacks.resetBoardTouchCounter()
                cvMyAttacks.setPhase(PHASE_TOUCH_INPUTS_LOCKED)
                buttonEndTurn.visibility = View.GONE

                //Gets the last touch input on the interactive game board
                val myAttackCoordinates = cvMyAttacks.getLastTouchInput()
                mCoordinatesToSend += myAttackCoordinates[0].toString()
                mCoordinatesToSend += myAttackCoordinates[1].toString()

                //My attack board is updated based on my attack coordinates and whether it is a hit or miss
                cvMyAttacks.updateMyAttacks(
                    myAttackCoordinates,
                    gameActivityViewModel.myAttacksPositionsFromPreviousRound
                )

                Log.d(TAG, "My attacks updated after check for hit.")

                gameActivityViewModel.myAttacksPositionsFromPreviousRound =
                    cvMyAttacks.getBoardState()
                mIsEndgame = cvMyAttacks.checkIfGameHasEnded()

                if (mIsEndgame) {
                    doEndgameProcedure(WINNER_MESSAGE)

                } else {
                    cvMyAttacks.resetBoardTouchCounter()
                    mCoordinatesToSend += if (cvMyAttacks.checkIfAttackIsAHit(myAttackCoordinates)) {
                        Toast.makeText(this, SECOND_ATTACK_AFTER_HIT, Toast.LENGTH_SHORT).show()
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
                BluetoothService.write(mCoordinatesToSend.toByteArray())
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
        BluetoothService.unregister(this)
        Log.d(TAG, "Entered onDestroy")
    }

    private fun setViewModelData() {
        gameActivityViewModel.myShipsPositionsFromPreviousRound =
            intent.extras!!.get(PlaceShipsActivity.EXTRA_MY_SHIPS) as Array<Array<Int>>
        gameActivityViewModel.myAttacksPositionsFromPreviousRound = cvMyAttacks.getBoardState()
    }

    private fun getExtrasFromIntent() {
        cvMyShips.setBoardState(intent.extras!!.get(PlaceShipsActivity.EXTRA_MY_SHIPS) as Array<Array<Int>>)
        cvMyAttacks.setOpponentShipsPositions(intent.extras!!.get(PlaceShipsActivity.EXTRA_OPPONENT_SHIPS) as Array<Array<Int>>)
        mIsPlayerOne = intent.getBooleanExtra(PlaceShipsActivity.EXTRA_IS_PLAYER_ONE, false)
        Log.d(TAG, "I am player one = $mIsPlayerOne.")
    }

    private fun setInitialBoardPhases() {
        cvMyShips.setPhase(PHASE_TOUCH_INPUTS_LOCKED)
        cvMyAttacks.setPhase(PHASE_TOUCH_INPUTS_LOCKED)
    }

    private fun initializePlayers() {
        if (mIsPlayerOne) {
            Log.d(TAG, "Starting my first turn.")
            startNextTurn()
        } else {
            Log.d(TAG, "Waiting for opponents first turn.")
            mIsNotFirstTurn = true
            startWaitingForOpponentAttack()
        }
    }

    private fun startNextTurn() = if (mShouldStartMyNextTurn.value == SWAPPABLE_ONE
        || mShouldStartMyNextTurn.value == null
    ) {
        mShouldStartMyNextTurn.value = SWAPPABLE_TWO
    } else {
        mShouldStartMyNextTurn.value = SWAPPABLE_ONE
    }

    private fun enterStateWaitForOpponentAttack() {
        cvMyShips.setPhase(PHASE_TOUCH_INPUTS_LOCKED)
        cvMyAttacks.setPhase(PHASE_TOUCH_INPUTS_LOCKED)
        buttonEndTurn.visibility = View.GONE
        startWaitingForOpponentAttack()
    }

    private fun startWaitingForOpponentAttack() {
        mIsWaitingForOpponentTurn = true
        mCoordinatesToSend = ""

        lifecycleScope.launch(Dispatchers.Default) {
            Log.d(TAG, "Waiting for response from opponent.")

            //Waiting to receive opponent attack coordinates and to start my next turn
            while (true) {
                if (mReceivedBluetoothMessage.length > 1) {
                    interpretBluetoothMessage()
                    break
                }
            }

            launch(Dispatchers.Main) {
                //If the value at index 2 is set to 1 it means that the opponent's attack was successful
                // and after it is visualized on my ships board, he will attack again
                if (gameActivityViewModel.opponentAttackCoordinates[2] == 1) {
                    mIsShipHitByOpponent = true
                }

                mIsWaitingForOpponentTurn = false
                mReceivedBluetoothMessage = ""
                startNextTurn()
            }
        }
    }

    private fun interpretBluetoothMessage() {
        //My previous attack was a hit and I can attack again immediately
        if (mReceivedBluetoothMessage == DO_ANOTHER_ATTACK) {
            mIsToDoAnotherAttackAfterHit = true

        } else {
            for (i in mReceivedBluetoothMessage.indices) {
                gameActivityViewModel.opponentAttackCoordinates[i] =
                    mReceivedBluetoothMessage[i].digitToInt()
            }
            Log.d(TAG, "Opponent attack received. $mReceivedBluetoothMessage")
        }
    }

    private fun doEndgameProcedure(messageToShow: String) {
        //Lock all inputs-------------------------------
        Log.d(TAG, "Game has ended.")
        cvMyAttacks.setPhase(PHASE_TOUCH_INPUTS_LOCKED)
        buttonEndTurn.visibility = View.GONE
        //----------------------------------------------

        //Show appropriate pop-up message and toast for Winner or Defeated
        showWinnerOrDefeatedImage(messageToShow)

        //The remaining enemy ships are shown if the game is lost
        cvMyAttacks.visualizeRemainingOpponentShips()
    }

    private fun showWinnerOrDefeatedImage(messageToShow: String) {
        if (messageToShow == WINNER_MESSAGE) {
            ivWinner.visibility = View.VISIBLE

        } else if (messageToShow == DEFEATED_MESSAGE) {
            ivDefeated.visibility = View.VISIBLE
        }
        Toast.makeText(this, messageToShow, Toast.LENGTH_LONG).show()
    }

    override fun onReceiveEvent(messageType: Int, message: Any?) {
        when (messageType) {
            BtEvents.EVENT_LISTENING -> {
                BluetoothService.unregister(this)
                finish()
            }

            BtEvents.EVENT_WRITE -> {
                startWaitingForOpponentAttack()
            }

            BtEvents.EVENT_READ -> {
                val bytes = (message as Bundle).getByteArray(BtEvents.BYTES) ?: return
                val byteCnt = message.getInt(BtEvents.BYTE_COUNT)
                mReceivedBluetoothMessage = String(bytes, 0, byteCnt)
            }
        }
    }

    private fun executeOnStartIfWasOnPause() {
        //Restore my Ships and my Attacks states as they were before the activity went onPause
        restoreShipsAndAttacksBoardStates()

        //Check whether the activity was paused, in the middle of my turn
        // or while waiting for opponent attack and restore the game state accordingly
        if (mIsWaitingForOpponentTurn) {
            enterStateWaitForOpponentAttack()
        } else {
            startNextTurn()
        }
        resetGameStateRelatedBooleans()
    }

    private fun restoreShipsAndAttacksBoardStates() {
        cvMyAttacks.setBoardState(gameActivityViewModel.myAttacksPositionsFromPreviousRound)
        cvMyShips.setBoardState(gameActivityViewModel.myShipsPositionsFromPreviousRound)
    }

    private fun resetGameStateRelatedBooleans() {
        mIsWaitingForOpponentTurn = false
        mIsActivityPaused = false
    }
}