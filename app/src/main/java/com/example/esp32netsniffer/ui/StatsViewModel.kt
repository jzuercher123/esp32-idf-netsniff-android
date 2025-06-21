package com.example.esp32netsniffer.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esp32netsniffer.data.NetworkStatistics
import com.example.esp32netsniffer.repository.PacketRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class StatsViewModel(
    private val repository: PacketRepository
) : ViewModel() {
    private val _stats = MutableStateFlow<NetworkStatistics?>(null)
    val stats: StateFlow<NetworkStatistics?> = _stats

    fun loadStats() {
        viewModelScope.launch {
            _stats.value = repository.getNetworkStatistics()
        }
    }
} 