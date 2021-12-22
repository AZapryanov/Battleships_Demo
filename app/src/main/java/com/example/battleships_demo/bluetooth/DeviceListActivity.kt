package com.example.battleships_demo.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import com.example.battleships_demo.R

class DeviceListActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "DeviceListActivity"
        const val EXTRA_DEVICE_NAME = "com.example.battleships_demo.device_name"
        const val EXTRA_DEVICE_ADDRESS = "com.example.battleships_demo.device_address"
    }

    private lateinit var mBtAdapter: BluetoothAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_list)

        // In case the user backs out
        setResult(RESULT_CANCELED)

        val pairedDevicesArrayAdapter = ArrayAdapter<String>(
            this, R.layout.device_info
        )
        val pairedListView = findViewById<ListView>(R.id.paired_devices)
        pairedListView.adapter = pairedDevicesArrayAdapter
        pairedListView.onItemClickListener = mDeviceClickListener

        val manager = this.getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        mBtAdapter = manager.adapter

        val pairedDevices: Set<BluetoothDevice> = mBtAdapter.bondedDevices

        if(pairedDevices.isNotEmpty()){
            for(device in pairedDevices){
                pairedDevicesArrayAdapter.add("${device.name} \n ${device.address}")
            }
        }
    }

    private val mDeviceClickListener =
        OnItemClickListener { av, v, arg2, arg3 ->

            val info = (v as TextView).text.toString()
            val name = info.substring(0, info.length - 18)
            val address = info.substring(info.length - 17)

            // Create the result Intent and include the MAC address
            val intent = Intent()
            intent.putExtra(EXTRA_DEVICE_NAME, name)
            intent.putExtra(EXTRA_DEVICE_ADDRESS, address)

            // Set result and finish this Activity
            setResult(RESULT_OK, intent)
            finish()
        }
}