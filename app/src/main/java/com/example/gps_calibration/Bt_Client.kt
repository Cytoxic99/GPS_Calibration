package com.example.gps_calibration

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import java.io.IOException
import java.util.*

class BluetoothClient(private val macAddress: String) {

    private val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // SPP UUID
    private var socket: BluetoothSocket? = null

    fun connect(): Boolean {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        val device: BluetoothDevice? = adapter.getRemoteDevice(macAddress)

        try {
            socket = device?.createRfcommSocketToServiceRecord(uuid)
            socket?.connect()
            return true
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return false
    }

    fun send_message(message: String) {
        val outputStream = socket?.outputStream
        try {
            outputStream?.write(message.toByteArray())
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun disconnect() {
        try {
            socket?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
