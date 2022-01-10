package com.example.battleships_demo.activities

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.battleships_demo.R
import com.example.battleships_demo.bluetooth.BluetoothService
import com.example.battleships_demo.bluetooth.Constants

class ConnectActivity : AppCompatActivity(), BluetoothService.BtListener {

    private var mConnectedDeviceName: String? = null
    private var mOutStringBuffer: StringBuffer? = null
    private var mBluetoothAdapter: BluetoothAdapter? = null

    // The person who initiates a connection gets this variable set to 1
    // which makes him player 1
    private var mPlayerNum = 2

    private lateinit var mTextName: TextView

    companion object {
        private const val TAG = "ConnectActivity"
        private const val REQUEST_ENABLE_BT = 1
        const val EXTRA_PLAYER_NUMBER = "playerNumber"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connect)

        mTextName = findViewById(R.id.text_device_name)

        // This is a higher order function that runs a callback after we set the result
        // from DeviceListActivity and finish() it
        val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            // Callback function:
            if (result.resultCode == RESULT_OK){
                connectDevice(result.data, true)
            } else if(result.resultCode == RESULT_CANCELED) {
                BluetoothService.stop()
                BluetoothService.start()
            }
        }

        findViewById<Button>(R.id.btn_connect).setOnClickListener {
            val intent = Intent(this, DeviceListActivity::class.java)
            resultLauncher.launch(intent)
        }
    }

    override fun onStart() {
        super.onStart()

        val manager = this.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        mBluetoothAdapter = manager.adapter
        if(mBluetoothAdapter == null) return

        if (!mBluetoothAdapter!!.isEnabled) {
            val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            // I'll fix this don't worry
            startActivityForResult(
                enableIntent,
                REQUEST_ENABLE_BT
            )
        }
        // Initialize the BluetoothChatService to perform bluetooth connections
        BluetoothService.init(this, this)
        // Initialize the buffer for outgoing messages
        mOutStringBuffer = StringBuffer()

        BluetoothService.start()
    }

    override fun onAccept() {
        TODO("Not yet implemented")
    }

    override fun onConnect() {
        TODO("Not yet implemented")
    }

    override fun onConnected() {
        TODO("Not yet implemented")
    }

    override fun onRead(bytes: ByteArray?) {
        TODO("Not yet implemented")
    }

    override fun onWrite(bytes: ByteArray?) {
        TODO("Not yet implemented")
    }

    /**
     *  Init private variable handler
     */
    private val mHandler = Handler(Looper.getMainLooper()) { msg ->
        when (msg.what) {
            Constants.MESSAGE_STATE_CHANGE ->
                // I pass mState as arg1 in BluetoothService
                when (msg.arg1) {
                BluetoothService.STATE_CONNECTED -> {
                    setStatus("Connected to $mConnectedDeviceName")
                    val intent = Intent(this, PlaceShipsActivity::class.java)
                    intent.putExtra(EXTRA_PLAYER_NUMBER, mPlayerNum)
                    startActivity(intent)
                    true
                }
                BluetoothService.STATE_CONNECTING -> {
                    setStatus("Connecting...")
                    true
                }
                BluetoothService.STATE_LISTEN,
                BluetoothService.STATE_NONE -> {
                    setStatus("Not connected")
                    true
                }
                else -> false
            }
            Constants.MESSAGE_FIRST_PLAYER -> {
                mPlayerNum = msg.arg1
                true
            }
            Constants.MESSAGE_WRITE -> {
                val writeBuf = msg.obj as ByteArray
                // construct a string from the buffer
                val writeMessage = String(writeBuf)

                BluetoothService.mMyBoard = writeMessage
                true
            }
            Constants.MESSAGE_READ -> {
                val readBuf = msg.obj as ByteArray
                // construct a string from the valid bytes in the buffer
                val readMessage = String(readBuf, 0, msg.arg1)

                Log.d(TAG, "reading message: $readMessage")

                PlaceShipsActivity.otherPlayerReady = true

                // This gets single coordinates from an attack
                BluetoothService.mReceivedMessage = readMessage
                // This is used once for transferring initial boards
                BluetoothService.mEnemyBoard = readMessage
                true
            }
            Constants.MESSAGE_DEVICE -> {
                // save the connected device's name
                mConnectedDeviceName = msg.data.getString(Constants.DEVICE_NAME)
                Toast.makeText(
                    this, "Connected to $mConnectedDeviceName", Toast.LENGTH_SHORT
                ).show()
                true
            }
            Constants.MESSAGE_TOAST -> {
                Toast.makeText(
                    this, msg.data.getString(Constants.TOAST), Toast.LENGTH_SHORT).show()
                true
            }
            else -> false
        }
    }

    private fun connectDevice(data: Intent?, secure: Boolean){
        if (data == null) return
        // Get the device MAC address
        val address = data.extras?.getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS)
        mConnectedDeviceName = data.extras?.getString(DeviceListActivity.EXTRA_DEVICE_NAME)
        // Get the BluetoothDevice object
        val device = mBluetoothAdapter!!.getRemoteDevice(address)
        // Attempt to connect to the device
        BluetoothService.connect(device, secure)
    }

    private fun setStatus(str: String){
        mTextName.text = str
    }
}