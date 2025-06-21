package com.example.esp32netsniffer.ui

import androidx.lifecycle.ViewModel
import com.example.esp32netsniffer.data.PacketData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PacketDetailViewModel : ViewModel() {
    private val _selectedPacket = MutableStateFlow<PacketData?>(null)
    val selectedPacket: StateFlow<PacketData?> = _selectedPacket

    fun setPacket(packet: PacketData) {
        _selectedPacket.value = packet
    }

    fun clearPacket() {
        _selectedPacket.value = null
    }
} 