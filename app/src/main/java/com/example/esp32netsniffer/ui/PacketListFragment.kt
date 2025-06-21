package com.example.esp32netsniffer.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.esp32netsniffer.databinding.FragmentPacketListBinding
import kotlinx.coroutines.launch

class PacketListFragment : Fragment() {
    private var _binding: FragmentPacketListBinding? = null
    private val binding get() = _binding!!
    
    // TODO: Replace with proper dependency injection
    private val viewModel: PacketListViewModel by viewModels {
        object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                // TODO: Inject PacketRepository properly
                val database = com.example.esp32netsniffer.data.PacketDatabase.getDatabase(requireContext())
                val repository = com.example.esp32netsniffer.repository.PacketRepository(database.packetDao())
                return PacketListViewModel(repository) as T
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPacketListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        setupClickListeners()
        setupFilterDrawer()
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.packets.collect { packets ->
                // TODO: Update RecyclerView adapter with packets
                updatePacketList(packets)
            }
        }
    }

    private fun setupClickListeners() {
        binding.fabStats.setOnClickListener {
            findNavController().navigate(R.id.action_packetList_to_stats)
        }
    }

    private fun setupFilterDrawer() {
        binding.filterDrawer.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.filter_packet_type -> {
                    // TODO: Show packet type filter dialog
                    true
                }
                R.id.filter_channel -> {
                    // TODO: Show channel filter dialog
                    true
                }
                R.id.filter_rssi -> {
                    // TODO: Show RSSI filter dialog
                    true
                }
                R.id.filter_ssid -> {
                    // TODO: Show SSID filter dialog
                    true
                }
                R.id.filter_mac -> {
                    // TODO: Show MAC filter dialog
                    true
                }
                else -> false
            }
        }
    }

    private fun updatePacketList(packets: List<com.example.esp32netsniffer.data.PacketData>) {
        // TODO: Update RecyclerView adapter
        // For now, just show count
        // This will be replaced with actual adapter implementation
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 