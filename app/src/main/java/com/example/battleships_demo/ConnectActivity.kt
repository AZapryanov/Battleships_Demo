package com.example.battleships_demo

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.getSystemService

class ConnectActivity : AppCompatActivity() {

    private var mConnectedDeviceName: String? = null
    private var mOutStringBuffer: StringBuffer? = null
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var mService: BluetoothService? = null
    private var mMessages: ArrayList<String> = ArrayList()

    private lateinit var mTextName: TextView

    companion object {
        private const val TAG = "ConnectActivity"
        private const val REQUEST_ENABLE_BT = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connect)

        mTextName = findViewById<TextView>(R.id.text_device_name)

        val manager = this.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        mBluetoothAdapter = manager.adapter

        val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK){
                connectDevice(result.data)
            }
        }

        findViewById<Button>(R.id.btn_connect).setOnClickListener {
            val intent = Intent(this, DeviceListActivity::class.java)
            resultLauncher.launch(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        if(mBluetoothAdapter == null) return

        if (!mBluetoothAdapter!!.isEnabled) {
            val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(
                enableIntent,
                REQUEST_ENABLE_BT
            )
        } else if (mService == null){
            // Initialize the BluetoothChatService to perform bluetooth connections
            mService = BluetoothService(this, mHandler)
            // Initialize the buffer for outgoing messages
            mOutStringBuffer = StringBuffer()
        }
    }

    private val mHandler = Handler(Looper.getMainLooper()) { msg ->
        when(msg.what){
            Constants.MESSAGE_WRITE -> {
                val writeBuf = msg.obj as ByteArray
                // construct a string from the buffer
                val writeMessage = String(writeBuf)
                mMessages.add(writeMessage)
                true
            }
            Constants.MESSAGE_READ -> {
                val readBuf = msg.obj as ByteArray
                val readMessage = String(readBuf, offset = 0, msg.arg1)
                mMessages.add(readMessage)
                true
            }
            Constants.MESSAGE_TOAST -> {
                Toast.makeText(this,
                    msg.data.getString(Constants.TOAST),
                    Toast.LENGTH_SHORT)
                    .show()

                true
            }
            else -> false
        }
    }

    private fun connectDevice(data: Intent?){
        if (data == null) return
        // Get the device MAC address
        val address = data.extras?.getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS)
        mConnectedDeviceName = data.extras?.getString(DeviceListActivity.EXTRA_DEVICE_NAME)
        // Get the BluetoothDevice object
        val device = mBluetoothAdapter!!.getRemoteDevice(address)
        // Attempt to connect to the device
        mService!!.connect(device)
    }
}