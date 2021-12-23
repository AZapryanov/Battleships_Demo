package com.example.battleships_demo.bluetooth

interface Constants {
    companion object {
        // Message types sent from the BluetoothChatService Handler
        const val MESSAGE_STATE_CHANGE = 1
        const val MESSAGE_READ = 2
        const val MESSAGE_WRITE = 3
        const val MESSAGE_DEVICE = 4
        const val MESSAGE_TOAST = 5
        const val MESSAGE_FIRST_PLAYER = 6

        // Key names received from the BluetoothChatService Handler
        const val DEVICE_NAME = "device_name"
        const val TOAST = "toast"
    }
}