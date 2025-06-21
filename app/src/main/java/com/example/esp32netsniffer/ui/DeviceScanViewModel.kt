package com.example.esp32netsniffer.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esp32netsniffer.bluetooth.BluetoothService
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DeviceScanViewModel(
    private val bluetoothService: BluetoothService
) : ViewModel() {
    val discoveredDevices: StateFlow<List<android.bluetooth.BluetoothDevice>> = bluetoothService.discoveredDevices
    val connectionState: StateFlow<BluetoothService.ConnectionState> = bluetoothService.connectionState

    fun startScan() {
        bluetoothService.startScan()
    }

    fun stopScan() {
        bluetoothService.stopScan()
    }

    fun connectToDevice(device: android.bluetooth.BluetoothDevice) {
        bluetoothService.connectToDevice(device)
    }

    fun disconnect() {
        bluetoothService.disconnect()
    }
} 