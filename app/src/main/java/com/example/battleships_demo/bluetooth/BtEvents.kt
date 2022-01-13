package com.example.battleships_demo.bluetooth

interface BtEvents {
    companion object {
        // Message types sent from the BluetoothService Handler
        const val EVENT_LISTENING = 1
        const val EVENT_CONNECT = 2
        const val EVENT_CONNECTED = 3
        const val EVENT_WRITE = 4
        const val EVENT_READ = 5
        const val EVENT_TOAST = 6

        const val BYTE_COUNT = "byteCnt"
        const val BYTES = "theBytes"
    }
}