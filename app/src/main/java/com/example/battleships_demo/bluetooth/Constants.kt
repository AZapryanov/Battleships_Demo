package com.example.battleships_demo.bluetooth

interface Constants {
    companion object {
        // Message types sent from the BluetoothService Handler
        const val MESSAGE_LISTENING = 1
        const val MESSAGE_CONNECT = 2
        const val MESSAGE_CONNECTED = 3
        const val MESSAGE_WRITE = 4
        const val MESSAGE_READ = 5
        const val MESSAGE_TOAST = 6

        const val BYTE_COUNT = "byteCnt"
        const val BYTES = "theBytes"
    }
}