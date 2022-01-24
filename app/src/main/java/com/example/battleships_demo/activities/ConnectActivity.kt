package com.example.battleships_demo.activities

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.battleships_demo.R
import com.example.battleships_demo.bluetooth.BluetoothService
import com.example.battleships_demo.bluetooth.BtEvents
import com.example.battleships_demo.databinding.ActivityConnectBinding

class ConnectActivity : AppCompatActivity(), BluetoothService.BtListener {

    private var mConnectedDeviceName: String? = null
    private var mBluetoothAdapter: BluetoothAdapter? = null

    private lateinit var mStatusText: TextView
    private lateinit var binding: ActivityConnectBinding

    companion object {
        private const val TAG = "ConnectActivity"
        private const val REQUEST_ENABLE_BT = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConnectBinding.inflate(layoutInflater)
        val contentView = binding.root
        setContentView(contentView)

        mStatusText = findViewById(R.id.text_status)
        // Initialize the BluetoothChatService to perform bluetooth connections
        BluetoothService.init(this)
        BluetoothService.register(this)  // Register as listener for incoming messages

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

        BluetoothService.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        BluetoothService.unregister(this)
    }

    override fun onReceiveEvent(eventType: Int, message: Any?) {
        when(eventType){
            BtEvents.EVENT_LISTENING -> { setStatus("Listening for connections..") }
            BtEvents.EVENT_CONNECT -> { setStatus("Connecting..") }
            BtEvents.EVENT_CONNECTED -> {
                runOnUiThread {
                    setStatus("Connected to ${(message as BluetoothDevice).name}")
                }
                startActivity(Intent(this, PlaceShipsActivity::class.java))
            }
            BtEvents.EVENT_TOAST -> {
                runOnUiThread {
                    Toast.makeText(this, (message as String), Toast.LENGTH_SHORT).show()
                }
            }
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
        Log.d(TAG, "setStatus: changing status at $this")
        mStatusText.text = str
    }
}