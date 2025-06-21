package com.example.esp32netsniffer.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esp32netsniffer.data.PacketData
import com.example.esp32netsniffer.data.PacketFilter
import com.example.esp32netsniffer.repository.PacketRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PacketListViewModel(
    private val repository: PacketRepository
) : ViewModel() {
    private val _filter = MutableStateFlow(PacketFilter())
    val filter: StateFlow<PacketFilter> = _filter

    val packets: StateFlow<List<PacketData>> = _filter
        .flatMapLatest { repository.getFilteredPackets(it) }
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.Lazily, emptyList())

    fun setFilter(newFilter: PacketFilter) {
        _filter.value = newFilter
    }

    fun clearFilter() {
        _filter.value = PacketFilter()
    }
} 