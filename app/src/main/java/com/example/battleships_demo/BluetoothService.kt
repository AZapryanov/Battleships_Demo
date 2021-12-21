package com.example.battleships_demo

import android.bluetooth.*
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

class BluetoothService(context: Context, handler: Handler) {
    companion object {
        private const val TAG = "BluetoothService"
        private val MY_UUID = UUID.fromString("5e51c389-e382-4acf-9089-c4d6d1d4c31b")

        private const val CONNECTION_NAME = "MyBluetoothService"

        const val STATE_NONE = 0
        const val STATE_LISTEN = 1
        const val STATE_CONNECTING = 2
        const val STATE_CONNECTED = 3
    }

    private val mAdapter: BluetoothAdapter?
    private val mHandler: Handler
    private var mAcceptThread: AcceptThread?
    private var mConnectThread: ConnectThread?
    private var mConnectedThread: ConnectedThread?
    private var mState: Int

    init {
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        mAdapter = manager.adapter
        mHandler = handler
        mAcceptThread = null
        mConnectThread = null
        mConnectedThread = null
        mState = STATE_NONE
    }

    @Synchronized
    fun start() {
        Log.d(TAG, "start")

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread!!.cancel()
            mConnectThread = null
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread!!.cancel()
            mConnectedThread = null
        }

        // Start the thread to listen on a BluetoothServerSocket
        if (mAcceptThread == null) {
            mAcceptThread = AcceptThread()
            mAcceptThread!!.start()
        }
    }

    @Synchronized
    fun connect(device: BluetoothDevice) {
        Log.d(TAG, "connect to: $device")

        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread!!.cancel()
                mConnectThread = null
            }
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread!!.cancel()
            mConnectedThread = null
        }

        // Start the thread to connect with the given device
        mConnectThread = ConnectThread(device)
        mConnectThread!!.start()
    }

    @Synchronized
    fun connected(socket: BluetoothSocket, device: BluetoothDevice){
        Log.d(TAG, "connected")

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread!!.cancel()
            mConnectThread = null
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread!!.cancel()
            mConnectedThread = null
        }

        // Cancel the accept thread because we only want to connect to one device
        if (mAcceptThread != null) {
            mAcceptThread!!.cancel()
            mAcceptThread = null
        }


        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = ConnectedThread(socket)
        mConnectedThread!!.start()

        // Send a message through the handler with the device name
        val msg: Message = mHandler.obtainMessage(Constants.MESSAGE_DEVICE_NAME)
        val bundle = Bundle()
        bundle.putString(Constants.DEVICE_NAME, device.name)
        msg.data = bundle
        mHandler.sendMessage(msg)
    }

    @Synchronized
    fun stop(){
        Log.d(TAG, "stop")

        if (mConnectThread != null) {
            mConnectThread!!.cancel()
            mConnectThread = null
        }

        if (mConnectedThread != null) {
            mConnectedThread!!.cancel()
            mConnectedThread = null
        }

        if (mAcceptThread != null) {
            mAcceptThread!!.cancel()
            mAcceptThread = null
        }
        mState = STATE_NONE

    }

    fun write(out: ByteArray){
        // Temp object
        val r: ConnectedThread

        synchronized(this){
            if (mState != STATE_CONNECTED) return
            r = mConnectedThread!!
        }

        r.write(out)
    }

    private fun connectionFailed() {
        // Send a failure message back to the Activity
        val msg = mHandler.obtainMessage(Constants.MESSAGE_TOAST)
        val bundle = Bundle()
        bundle.putString(Constants.TOAST, "Unable to connect device")
        msg.data = bundle
        mHandler.sendMessage(msg)
        mState = STATE_NONE

        // Start the service over to restart listening mode
        this@BluetoothService.start()
    }

    private fun connectionLost() {
        // Send a failure message back to the Activity
        val msg = mHandler.obtainMessage(Constants.MESSAGE_TOAST)
        val bundle = Bundle()
        bundle.putString(Constants.TOAST, "Device connection was lost")
        msg.data = bundle
        mHandler.sendMessage(msg)
        mState = STATE_NONE

        // Start the service over to restart listening mode
        this@BluetoothService.start()
    }

    private inner class AcceptThread : Thread() {
        private val mmServerSocket: BluetoothServerSocket?

        init {
            var tmp: BluetoothServerSocket? = null

            try {
                tmp = mAdapter?.listenUsingRfcommWithServiceRecord(CONNECTION_NAME, MY_UUID)
            } catch (e: IOException) {
                Log.e(TAG, "init: listen() failed", e)
            }

            mmServerSocket = tmp
            mState = STATE_LISTEN
        }

        override fun run() {
            Log.d(TAG, "START mAcceptThread $this")
            name = "AcceptThread"

            var socket: BluetoothSocket?

            while (mState != STATE_CONNECTED) {
                try {
                    socket = mmServerSocket?.accept()
                } catch (e: IOException) {
                    Log.e(TAG, "run: accept() failed", e)
                    break
                }


                // If a connection was accepted
                if (socket != null) {
                    synchronized(this@BluetoothService) {
                        when (mState) {
                            STATE_LISTEN, STATE_CONNECTING ->
                                connected(socket, socket.remoteDevice)
                            STATE_NONE, STATE_CONNECTED ->
                                try {
                                    socket.close()
                                } catch (e: IOException) {
                                    Log.e(TAG, "run: could not close socket", e)
                                }
                            else -> Log.d(TAG, "run: invalid mState")
                        }
                    }
                }
            }
            Log.i(TAG, "run: End mAcceptThread")
        }

        fun cancel() {
            Log.d(TAG, "cancel: $this")
            try {
                mmServerSocket!!.close()
            } catch (e: IOException){
                Log.d(TAG, "cancel: Failed to close server socket")
            }
        }
    }

    private inner class ConnectThread(device: BluetoothDevice) : Thread() {
        private val mmSocket: BluetoothSocket?
        private val mmDevice: BluetoothDevice?

        init {
            mmDevice = device
            var tmp: BluetoothSocket? = null

            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID)
            } catch (e: IOException) {
                Log.e(TAG, "init: create() failed", e)
            }

            mmSocket = tmp
            mState = STATE_CONNECTING
        }

        override fun run() {
            Log.d(TAG, "run: Start connect thread")
            name = "ConnectThread"

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket!!.connect()
            } catch (e: IOException) {
                // Close the socket
                try {
                    mmSocket!!.close()
                } catch (e2: IOException) {
                    Log.e(TAG, "unable to close()", e2)
                }
                connectionFailed()
                return
            }

            // Reset the ConnectThread because we're done
            synchronized(this@BluetoothService) { mConnectThread = null }

            // Start the connected thread
            connected(mmSocket, mmDevice!!)
        }

        fun cancel(){
            try{
                mmSocket!!.close()
            } catch (e: IOException){
                Log.e(TAG, "cancel: close() failed", e)
            }
        }
    }


    private inner class ConnectedThread(socket: BluetoothSocket) : Thread() {
        private val mmSocket: BluetoothSocket?
        private val mmInStream: InputStream?
        private val mmOutStream: OutputStream?

        init {
            Log.d(TAG, "init: connected thread")
            mmSocket = socket
            var tmpIn: InputStream? = null
            var tmpOut: OutputStream? = null

            try {
                tmpIn = socket.inputStream
                tmpOut = socket.outputStream
            } catch (e: IOException){
                Log.e(TAG, "connected thread init: temp sockets failed to create", e)
            }

            mmInStream = tmpIn
            mmOutStream = tmpOut
            mState = STATE_CONNECTED
        }

        override fun run() {
            Log.i(TAG, "run: START mConnectedThread")
            val buffer = ByteArray(1024)
            var bytes: Int

            while (mState == STATE_CONNECTED) {
                try {
                    bytes = mmInStream!!.read(buffer)

                    mHandler.obtainMessage(Constants.MESSAGE_READ, bytes, -1, buffer)
                } catch (e: IOException){
                    Log.e(TAG, "disconnected", e)
                    connectionLost()
                    break
                }
            }
        }

        fun write(buffer: ByteArray){
            try{
                mmOutStream!!.write(buffer)

                mHandler.obtainMessage(Constants.MESSAGE_WRITE, -1, -1, buffer)
                    .sendToTarget()
            } catch (e: IOException){
                Log.e(TAG, "wirte: failed", e)
            }
        }

        fun cancel() {
            try {
                mmSocket!!.close()
            } catch (e: IOException) {
                Log.e(TAG, "close() of connect socket failed", e)
            }
        }
    }
}
