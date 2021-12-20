package com.example.battleships_demo

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView

class DeviceListActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "DeviceListActivity"
        private const val EXTRA_DEVICE_ADDRESS = "com.example.battleships_demo.device_address"
    }

    private lateinit var mBtAdapter: BluetoothAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_list)

        setResult(RESULT_CANCELED)

        val pairedDevicesArrayAdapter = ArrayAdapter<String>(
            this, R.layout.device_info)
        val pairedListView = findViewById<ListView>(R.id.paired_devices)
        pairedListView.adapter = pairedDevicesArrayAdapter
        pairedListView.onItemClickListener = mDeviceClickListener
    }

    private val mDeviceClickListener =
        OnItemClickListener { av, v, arg2, arg3 ->

            val info = (v as TextView).text.toString()
            val address = info.substring(info.length - 17)

            // Create the result Intent and include the MAC address
            val intent = Intent()
            intent.putExtra(EXTRA_DEVICE_ADDRESS, address)

            // Set result and finish this Activity
            setResult(RESULT_OK, intent)
            finish()
        }
}