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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.getSystemService

class ConnectActivity : AppCompatActivity() {

    private var mConnectedDeviceName: String? = null
    private var mOutStringBuffer: StringBuffer? = null
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var mService: BluetoothService? = null

    companion object {
        private const val TAG = "ConnectActivity"
        private const val REQUEST_ENABLE_BT = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connect)

        val textName = findViewById<TextView>(R.id.text_device_name)
        val textAddress = findViewById<TextView>(R.id.text_device_address)

        val manager = this.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        mBluetoothAdapter = manager.adapter

        if (!mBluetoothAdapter!!.isEnabled) {
            val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(
                enableIntent,
                REQUEST_ENABLE_BT
            )
        }

        var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK){

                // Get the device MAC address
                val intent: Intent = result.data ?: return@registerForActivityResult

                val address = intent.extras!!.getString("com.example.battleships_demo.device_address")
                // Get the BluetoothDevice object
                val device = mBluetoothAdapter!!.getRemoteDevice(address)
                // Attempt to connect to the device
                mService!!.connect(device)
            }
        }

        findViewById<Button>(R.id.btn_connect).setOnClickListener {
            val intent = Intent(this, DeviceListActivity::class.java)
            resultLauncher.launch(intent)
        }
    }

    private val mHandler = Handler(Looper.getMainLooper()) { msg ->
        when(msg.what){
            Constants.MESSAGE_DEVICE_NAME -> {
                mConnectedDeviceName = msg.data.getString(Constants.DEVICE_NAME)
                true
            }
            else -> false
        }
    }
}