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
import com.example.battleships_demo.customviews.InteractiveBoard
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
        private const val WINNER_MESSAGE = "GG, You have won!"
        private const val DEFEATED_MESSAGE = "GG, You have lost."
        private const val NUMBER_OF_DESTROYED_SHIPS_FOR_ENDGAME = 17
        private const val INITIAL_ARRAY_VALUE = 15
        private const val INITIAL_ARRAY_SIZE = 50
        private const val SWAPPABLE_ONE = 1
        private const val SWAPPABLE_TWO = 2
    }

    private val mShouldStartMyNextTurn: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    private var mIsPlayerOne = true
    private var mIsNotFirstTurn = false
    private var mIsEndgame = false
    private var mIsAttackAfterHit = false
    private var mIsWaitingForOpponentTurn = false
    private var mIsActivityPaused = false

    private lateinit var mOpponentShipsPositions: Array<Array<Int>>
    private lateinit var mOpponentAttackCoordinates: Array<Int>

    private lateinit var gameActivityViewModel: GameActivityViewModel

    private var mCoordinatesToSend = ""
    private var mReceivedBluetoothMessage = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        gameActivityViewModel = ViewModelProvider(this)[GameActivityViewModel::class.java]
        BluetoothService.register(this)

        //The following three functions are executed only one time at the start of the game,
        // in order to set everything up for the first turn
        setViewModelData()
        getExtrasFromIntent()
        initializeTheGame()

        //If the value of mIsMyTurn changes due to received opponent attack coordinates through BT,
        //meaning that he finished his turn, the following code executes
        mShouldStartMyNextTurn.observe(this, {
            Log.d(TAG, "My turn starts.")
            cvMyShips.setPhase(PHASE_TOUCH_INPUTS_LOCKED)
            cvMyAttacks.setPhase(PHASE_MARK_ATTACK)

            if (!mIsAttackAfterHit) {
                //My ships are updated based on the received attack coordinated from the opponent
                if (mIsNotFirstTurn) {
                    val opponentAttackCoordinates = mOpponentAttackCoordinates
                    val updatedBoardState =
                        updateMyShips(
                            opponentAttackCoordinates,
                            gameActivityViewModel.myShipsPositionsFromPreviousRound
                        )
                    cvMyShips.setBoardState(updatedBoardState)
                    Log.d(TAG, "My ships updated with opponent attack.")
                    mOpponentAttackCoordinates = Array(INITIAL_ARRAY_SIZE) { INITIAL_ARRAY_VALUE }

                    gameActivityViewModel.myShipsPositionsFromPreviousRound =
                        cvMyShips.getState()
                    mIsEndgame = checkIfGameHasEnded(cvMyShips.getState())

                    if (mIsEndgame) {
                        doEndgameProcedure(DEFEATED_MESSAGE)
                    }
                }
            }
            mIsAttackAfterHit = false

            if (!mIsEndgame) {
                buttonEndTurn.visibility = View.VISIBLE
            }
            mIsNotFirstTurn = true
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
                val updatedMyAttacksPositions =
                    updateMyAttacks(
                        myAttackCoordinates,
                        gameActivityViewModel.myAttacksPositionsFromPreviousRound
                    )

                cvMyAttacks.setBoardState(updatedMyAttacksPositions)
                Log.d(TAG, "My attacks updated after check for hit.")
                gameActivityViewModel.myAttacksPositionsFromPreviousRound =
                    cvMyAttacks.getState()

                mIsEndgame = checkIfGameHasEnded(cvMyAttacks.getState())

                if (mIsEndgame) {
                    doEndgameProcedure(WINNER_MESSAGE)

                    //Send my attack coordinates to the other player through BT
                    //so that his game ends too and he gets a message that he is defeated
                    BluetoothService.write(mCoordinatesToSend.toByteArray())
                }

                if (!mIsEndgame) {
                    cvMyAttacks.resetBoardTouchCounter()

                    //If I have hit an enemy ship on my turn I get an extra turn
                    if (checkIfAttackIsAHit(myAttackCoordinates)) {
                        Toast.makeText(this, SECOND_ATTACK_AFTER_HIT, Toast.LENGTH_SHORT).show()
                        mIsAttackAfterHit = true
                        Log.d(TAG, "The attack is a hit. Will do another attack")
                        startNextTurn()

                    } else {
                        //Send my attack coordinates to the other player through BT
                        BluetoothService.write(mCoordinatesToSend.toByteArray())
                        Log.d(TAG, "Attack sent to opponent.")
                        
                        startWaitingForOpponentAttack()
                    }
                }
            }
        }
    }

    override fun onRestart() {
        super.onRestart()
        Log.d(TAG, "Entered onRestart")
    }

    override fun onStart() {
        super.onStart()
        if (mIsActivityPaused) {
            executeOnStartIfWasOnPause()
        }
        Log.d(TAG, "Entered onStart")
        Log.d(TAG, "Restored the state of the game - $mIsActivityPaused")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "Entered onResume")
    }

    override fun onPause() {
        super.onPause()
        mIsActivityPaused = true
        Log.d(TAG, "Entered onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "Entered onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        BluetoothService.unregister(this)
        Log.d(TAG, "Entered onDestroy")
    }

    private fun setViewModelData() {
        gameActivityViewModel.myShipsPositionsFromPreviousRound = intent.extras!!.get(PlaceShipsActivity.EXTRA_MY_SHIPS) as Array<Array<Int>>
        gameActivityViewModel.myAttacksPositionsFromPreviousRound = cvMyAttacks.getState()
    }

    private fun getExtrasFromIntent() {
        cvMyShips.setBoardState(intent.extras!!.get(PlaceShipsActivity.EXTRA_MY_SHIPS) as Array<Array<Int>>)
        mOpponentShipsPositions = intent.extras!!.get(PlaceShipsActivity.EXTRA_OPPONENT_SHIPS) as Array<Array<Int>>
        mIsPlayerOne = intent.getBooleanExtra(PlaceShipsActivity.EXTRA_IS_PLAYER_ONE, false)
        Log.d(TAG, "I am player one = $mIsPlayerOne.")
    }

    private fun initializeTheGame() {
            mOpponentAttackCoordinates = Array(INITIAL_ARRAY_SIZE) { INITIAL_ARRAY_VALUE }
            buttonEndTurn.visibility = View.GONE

            cvMyShips.setPhase(PHASE_TOUCH_INPUTS_LOCKED)
            cvMyAttacks.setPhase(PHASE_TOUCH_INPUTS_LOCKED)

            if (mIsPlayerOne) {
                Log.d(TAG, "Starting my first turn.")
                startNextTurn()
            } else {
                Log.d(TAG, "Waiting for opponents first turn.")
                mIsNotFirstTurn = true
                startWaitingForOpponentAttack()
            }
    }

    private fun checkIfAttackIsAHit(attackCoordinates: Array<Int>): Boolean {
        if (mOpponentShipsPositions[attackCoordinates[0]][attackCoordinates[1]] == 1) {
            return true
        }
        return false
    }

    private fun updateMyAttacks(
        myAttackCoordinates: Array<Int>,
        myAttacksPositions: Array<Array<Int>>,
    ): Array<Array<Int>> {
        val opponentAttackX = myAttackCoordinates[0]
        val opponentAttackY = myAttackCoordinates[1]

        if (mOpponentShipsPositions[opponentAttackX][opponentAttackY] == InteractiveBoard.EMPTY_BOX) {
            myAttacksPositions[opponentAttackX][opponentAttackY] = InteractiveBoard.CROSS

        } else if (mOpponentShipsPositions[opponentAttackX][opponentAttackY] == InteractiveBoard.SHIP_PART) {
            myAttacksPositions[opponentAttackX][opponentAttackY] = InteractiveBoard.SHIP_PART_HIT
        }
        return myAttacksPositions
    }

    private fun updateMyShips(
        opponentAttackCoordinates: Array<Int>,
        myShipsPositions: Array<Array<Int>>,
    ): Array<Array<Int>> {

        //Updates my ships board with one or more opponent attacks
        for (i in opponentAttackCoordinates.indices step 2) {
            if (opponentAttackCoordinates[i] == INITIAL_ARRAY_VALUE) {
                break
            }

            val opponentAttackX = opponentAttackCoordinates[i]
            val opponentAttackY = opponentAttackCoordinates[i + 1]

            if (myShipsPositions[opponentAttackX][opponentAttackY] == InteractiveBoard.EMPTY_BOX) {
                myShipsPositions[opponentAttackX][opponentAttackY] = InteractiveBoard.CROSS

            } else if (myShipsPositions[opponentAttackX][opponentAttackY] == InteractiveBoard.SHIP_PART) {
                myShipsPositions[opponentAttackX][opponentAttackY] = InteractiveBoard.SHIP_PART_HIT
            }
        }

        return myShipsPositions
    }

    private fun startNextTurn() = if (mShouldStartMyNextTurn.value == SWAPPABLE_ONE
        || mShouldStartMyNextTurn.value == null
    ) {
        mShouldStartMyNextTurn.value = SWAPPABLE_TWO
    } else {
        mShouldStartMyNextTurn.value = SWAPPABLE_ONE
    }

    private fun startWaitingForOpponentAttack() {
        mIsWaitingForOpponentTurn = true
        mCoordinatesToSend = ""
        lifecycleScope.launch(Dispatchers.Default) {
            Log.d(TAG, "Waiting for opponent attack.")

            //Waiting to receive opponent attack coordinates and to start my next turn
            while (true) {
                if (mReceivedBluetoothMessage.length > 1) {

                    for (i in mReceivedBluetoothMessage.indices) {
                        mOpponentAttackCoordinates[i] = mReceivedBluetoothMessage[i].digitToInt()
                    }
                    Log.d(TAG, "Opponent attack received. $mReceivedBluetoothMessage")
                    break
                }
            }

            launch(Dispatchers.Main) {
                mIsWaitingForOpponentTurn = false
                mReceivedBluetoothMessage = ""
                //When attack coordinates are received from the other player through BT,
                //by switching the value of mIsMyTurn (it is observed), my next turn is started
                startNextTurn()
            }
        }
    }

    private fun checkIfGameHasEnded(shipsBoard: Array<Array<Int>>): Boolean {
        var counter = 0
        for (i in shipsBoard.indices) {
            for (j in shipsBoard.indices) {
                if (shipsBoard[i][j] == 3) {
                    counter++
                }
            }
        }
        return counter >= NUMBER_OF_DESTROYED_SHIPS_FOR_ENDGAME
    }

    private fun doEndgameProcedure(messageToShow: String) {
        //Lock all inputs
        //----------------------------------------------
        Log.d(TAG, "Game has ended.")
        cvMyAttacks.setPhase(PHASE_TOUCH_INPUTS_LOCKED)
        buttonEndTurn.visibility = View.GONE
        //----------------------------------------------

        //Show appropriate pop-up message and toast for Winner or Defeated
        showWinnerOrDefeatedImage(messageToShow)

        //The remaining enemy ships are shown if game is lost
        cvMyAttacks.visualizeRemainingOpponentShips(mOpponentShipsPositions)
    }

    private fun showWinnerOrDefeatedImage(messageToShow: String) {
        if (messageToShow == WINNER_MESSAGE) {
            ivWinner.visibility = View.VISIBLE

        } else if (messageToShow == DEFEATED_MESSAGE) {
            ivDefeated.visibility = View.VISIBLE
        }
        Toast.makeText(this, messageToShow, Toast.LENGTH_LONG).show()
    }

    private fun executeOnStartIfWasOnPause() {
        //Restore my Ships and my Attacks states as they were before the activity went onPause
        restoreShipsAndAttacksBoardStates()

        //Check whether the activity was paused in the middle of my turn
        // or while waiting for opponent attack and restore the game state accordingly
        if (mIsWaitingForOpponentTurn) {
            cvMyShips.setPhase(PHASE_TOUCH_INPUTS_LOCKED)
            cvMyAttacks.setPhase(PHASE_TOUCH_INPUTS_LOCKED)
            buttonEndTurn.visibility = View.GONE
            setLifecycleRelatedBooleansToFalse()

            startWaitingForOpponentAttack()
        } else {
            //Reset to false the values of the booleans which give information
            // in what state was the game before the activity went onPause
            setLifecycleRelatedBooleansToFalse()
            startNextTurn()
        }
    }

    private fun restoreShipsAndAttacksBoardStates() {
        cvMyAttacks.setBoardState(gameActivityViewModel.myAttacksPositionsFromPreviousRound)
        cvMyShips.setBoardState(gameActivityViewModel.myShipsPositionsFromPreviousRound)
    }

    private fun setLifecycleRelatedBooleansToFalse() {
        mIsWaitingForOpponentTurn = false
        mIsActivityPaused = false
    }

    override fun onReceiveEvent(messageType: Int, message: Any?) {
        when(messageType){
            BtEvents.EVENT_WRITE -> {
                Log.d(TAG, "onReceiveMessage: sending attack to the opponent")
            }
            BtEvents.EVENT_READ -> {
                val bytes = (message as Bundle).getByteArray(BtEvents.BYTES) ?: return
                val byteCnt = message.getInt(BtEvents.BYTE_COUNT)
                mReceivedBluetoothMessage = String(bytes, 0, byteCnt)
            }
        }
    }
}