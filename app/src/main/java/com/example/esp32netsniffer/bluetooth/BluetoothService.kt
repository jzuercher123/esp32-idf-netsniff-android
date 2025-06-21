package com.example.esp32netsniffer.bluetooth

import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.util.Log
import com.example.esp32netsniffer.data.PacketData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*

class BluetoothService(private val context: Context) {
    
    companion object {
        private const val TAG = "BluetoothService"
        
        // ESP32 Sniffer device names
        private val ESP32_DEVICE_NAMES = listOf("ESP32_Sniffer", "ESP32_Sniffer_BT")
        
        // GATT Service and Characteristic UUIDs (based on ESP32 implementation)
        private val GENERIC_ACCESS_SERVICE_UUID = UUID.fromString("1800-0000-1000-8000-00805F9B34FB")
        private val DEVICE_NAME_CHARACTERISTIC_UUID = UUID.fromString("2A00-0000-1000-8000-00805F9B34FB")
        
        // Custom service UUID for packet data (if ESP32 uses custom service)
        private val PACKET_DATA_SERVICE_UUID = UUID.fromString("12345678-1234-1234-1234-123456789ABC")
        private val PACKET_DATA_CHARACTERISTIC_UUID = UUID.fromString("87654321-4321-4321-4321-CBA987654321")
    }
    
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val bluetoothLeScanner: BluetoothLeScanner? = bluetoothAdapter?.bluetoothLeScanner
    private var bluetoothGatt: BluetoothGatt? = null
    
    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()
    
    private val _discoveredDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val discoveredDevices: StateFlow<List<BluetoothDevice>> = _discoveredDevices.asStateFlow()
    
    private val _receivedPackets = MutableStateFlow<List<PacketData>>(emptyList())
    val receivedPackets: StateFlow<List<PacketData>> = _receivedPackets.asStateFlow()
    
    private var packetCallback: ((PacketData) -> Unit)? = null
    
    enum class ConnectionState {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        DISCOVERING_SERVICES,
        READY
    }
    
    fun startScan() {
        if (bluetoothLeScanner == null) {
            Log.e(TAG, "Bluetooth LE Scanner not available")
            return
        }
        
        val scanFilter = ScanFilter.Builder()
            .build()
        
        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
        
        bluetoothLeScanner.startScan(listOf(scanFilter), scanSettings, scanCallback)
        Log.d(TAG, "Started BLE scan")
    }
    
    fun stopScan() {
        bluetoothLeScanner?.stopScan(scanCallback)
        Log.d(TAG, "Stopped BLE scan")
    }
    
    fun connectToDevice(device: BluetoothDevice) {
        _connectionState.value = ConnectionState.CONNECTING
        bluetoothGatt = device.connectGatt(context, false, gattCallback)
        Log.d(TAG, "Connecting to device: ${device.name}")
    }
    
    fun disconnect() {
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
        _connectionState.value = ConnectionState.DISCONNECTED
        Log.d(TAG, "Disconnected from device")
    }
    
    fun setPacketCallback(callback: (PacketData) -> Unit) {
        packetCallback = callback
    }
    
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            val deviceName = device.name
            
            if (deviceName != null && ESP32_DEVICE_NAMES.any { deviceName.contains(it) }) {
                Log.d(TAG, "Found ESP32 Sniffer device: $deviceName")
                val currentDevices = _discoveredDevices.value.toMutableList()
                if (!currentDevices.contains(device)) {
                    currentDevices.add(device)
                    _discoveredDevices.value = currentDevices
                }
            }
        }
        
        override fun onScanFailed(errorCode: Int) {
            Log.e(TAG, "Scan failed with error: $errorCode")
        }
    }
    
    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.d(TAG, "Connected to GATT server")
                    _connectionState.value = ConnectionState.CONNECTED
                    gatt.discoverServices()
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.d(TAG, "Disconnected from GATT server")
                    _connectionState.value = ConnectionState.DISCONNECTED
                }
            }
        }
        
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Services discovered")
                _connectionState.value = ConnectionState.DISCOVERING_SERVICES
                
                // Look for packet data service
                val packetService = gatt.getService(PACKET_DATA_SERVICE_UUID)
                if (packetService != null) {
                    val packetCharacteristic = packetService.getCharacteristic(PACKET_DATA_CHARACTERISTIC_UUID)
                    if (packetCharacteristic != null) {
                        // Enable notifications
                        gatt.setCharacteristicNotification(packetCharacteristic, true)
                        val descriptor = packetCharacteristic.getDescriptor(
                            UUID.fromString("00002902-0000-1000-8000-00805F9B34FB")
                        )
                        descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                        gatt.writeDescriptor(descriptor)
                        Log.d(TAG, "Enabled packet data notifications")
                    }
                }
                
                // Also check generic access service for device name
                val genericService = gatt.getService(GENERIC_ACCESS_SERVICE_UUID)
                if (genericService != null) {
                    val nameCharacteristic = genericService.getCharacteristic(DEVICE_NAME_CHARACTERISTIC_UUID)
                    if (nameCharacteristic != null) {
                        gatt.readCharacteristic(nameCharacteristic)
                    }
                }
                
                _connectionState.value = ConnectionState.READY
            } else {
                Log.e(TAG, "Service discovery failed: $status")
            }
        }
        
        override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                when (characteristic.uuid) {
                    DEVICE_NAME_CHARACTERISTIC_UUID -> {
                        val deviceName = String(characteristic.value)
                        Log.d(TAG, "Device name: $deviceName")
                    }
                }
            }
        }
        
        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            when (characteristic.uuid) {
                PACKET_DATA_CHARACTERISTIC_UUID -> {
                    val data = characteristic.value
                    processPacketData(data)
                }
            }
        }
    }
    
    private fun processPacketData(data: ByteArray) {
        try {
            // Parse packet data based on ESP32 format
            // Format: [channel][rssi][packet_type][length][raw_data...]
            if (data.size < 4) return
            
            val channel = data[0].toInt()
            val rssi = data[1].toInt()
            val packetTypeByte = data[2].toInt()
            val packetLength = data[3].toInt()
            
            val packetType = when (packetTypeByte) {
                0 -> PacketData.PacketType.MANAGEMENT
                1 -> PacketData.PacketType.CONTROL
                2 -> PacketData.PacketType.DATA
                else -> PacketData.PacketType.UNKNOWN
            }
            
            val rawData = if (data.size > 4) {
                data.copyOfRange(4, minOf(4 + packetLength, data.size))
            } else {
                ByteArray(0)
            }
            
            val packet = PacketData(
                channel = channel,
                rssi = rssi,
                packetType = packetType,
                packetLength = packetLength,
                rawData = rawData
            )
            
            // Parse additional packet information if available
            val parsedPacket = parsePacketDetails(packet)
            
            // Update received packets
            val currentPackets = _receivedPackets.value.toMutableList()
            currentPackets.add(0, parsedPacket)
            if (currentPackets.size > 1000) { // Keep only last 1000 packets
                currentPackets.removeAt(currentPackets.size - 1)
            }
            _receivedPackets.value = currentPackets
            
            // Call callback
            packetCallback?.invoke(parsedPacket)
            
            Log.d(TAG, "Processed packet: Channel=$channel, RSSI=$rssi, Type=$packetType, Length=$packetLength")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error processing packet data", e)
        }
    }
    
    private fun parsePacketDetails(packet: PacketData): PacketData {
        // Basic 802.11 frame parsing
        if (packet.rawData.size < 24) return packet
        
        try {
            // Extract MAC addresses (bytes 4-9: destination, 10-15: source)
            val destMac = packet.rawData.copyOfRange(4, 10).joinToString(":") { 
                String.format("%02X", it) 
            }
            val srcMac = packet.rawData.copyOfRange(10, 16).joinToString(":") { 
                String.format("%02X", it) 
            }
            
            // Extract frame control (bytes 0-1)
            val frameControl = (packet.rawData[1].toInt() shl 8) or packet.rawData[0].toInt()
            
            // Extract sequence number (bytes 22-23)
            val sequenceNumber = (packet.rawData[23].toInt() shl 8) or packet.rawData[22].toInt()
            
            // Extract SSID from beacon frames (management frames)
            var ssid: String? = null
            if (packet.packetType == PacketData.PacketType.MANAGEMENT && packet.rawData.size > 36) {
                val ssidLength = packet.rawData[36].toInt()
                if (ssidLength > 0 && packet.rawData.size > 36 + ssidLength) {
                    ssid = String(packet.rawData.copyOfRange(37, 37 + ssidLength))
                }
            }
            
            return packet.copy(
                sourceMac = srcMac,
                destinationMac = destMac,
                ssid = ssid,
                frameControl = frameControl,
                sequenceNumber = sequenceNumber
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing packet details", e)
            return packet
        }
    }
} 