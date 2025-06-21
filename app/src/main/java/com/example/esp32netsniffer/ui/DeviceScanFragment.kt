package com.example.esp32netsniffer.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.esp32netsniffer.databinding.FragmentDeviceScanBinding
import kotlinx.coroutines.launch

class DeviceScanFragment : Fragment() {
    private var _binding: FragmentDeviceScanBinding? = null
    private val binding get() = _binding!!
    
    // TODO: Replace with proper dependency injection
    private val viewModel: DeviceScanViewModel by viewModels {
        // Temporary ViewModelFactory - will be replaced with proper DI
        object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                // TODO: Inject BluetoothService properly
                return DeviceScanViewModel(com.example.esp32netsniffer.bluetooth.BluetoothService(requireContext())) as T
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDeviceScanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        setupClickListeners()
        startScanning()
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.discoveredDevices.collect { devices ->
                // TODO: Update RecyclerView adapter with devices
                updateDeviceList(devices)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.connectionState.collect { state ->
                updateConnectionState(state)
            }
        }
    }

    private fun setupClickListeners() {
        binding.connectButton.setOnClickListener {
            // TODO: Get selected device from adapter
            // For now, just navigate to packet list
            findNavController().navigate(R.id.action_deviceScan_to_packetList)
        }
    }

    private fun startScanning() {
        viewModel.startScan()
    }

    private fun updateDeviceList(devices: List<android.bluetooth.BluetoothDevice>) {
        // TODO: Update RecyclerView adapter
        // For now, just show count
        binding.connectButton.text = "Connect (${devices.size} devices found)"
    }

    private fun updateConnectionState(state: com.example.esp32netsniffer.bluetooth.BluetoothService.ConnectionState) {
        when (state) {
            com.example.esp32netsniffer.bluetooth.BluetoothService.ConnectionState.CONNECTING -> {
                binding.progressBar.visibility = View.VISIBLE
                binding.connectButton.isEnabled = false
            }
            com.example.esp32netsniffer.bluetooth.BluetoothService.ConnectionState.READY -> {
                binding.progressBar.visibility = View.GONE
                binding.connectButton.isEnabled = true
                // Navigate to packet list when connected
                findNavController().navigate(R.id.action_deviceScan_to_packetList)
            }
            else -> {
                binding.progressBar.visibility = View.GONE
                binding.connectButton.isEnabled = true
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.stopScan()
        _binding = null
    }
} 