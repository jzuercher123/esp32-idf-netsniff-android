package com.example.esp32netsniffer.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.esp32netsniffer.databinding.FragmentPacketDetailBinding
import kotlinx.coroutines.launch

class PacketDetailFragment : Fragment() {
    private var _binding: FragmentPacketDetailBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: PacketDetailViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPacketDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        setupClickListeners()
        
        // Get packet from arguments (passed from list)
        arguments?.getLong("packetId")?.let { packetId ->
            // TODO: Load packet by ID from repository
            loadPacketById(packetId)
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.selectedPacket.collect { packet ->
                packet?.let { displayPacket(it) }
            }
        }
    }

    private fun setupClickListeners() {
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun loadPacketById(packetId: Long) {
        // TODO: Load packet from repository and set in ViewModel
        // For now, create a dummy packet for testing
        val dummyPacket = com.example.esp32netsniffer.data.PacketData(
            id = packetId,
            channel = 6,
            rssi = -45,
            packetType = com.example.esp32netsniffer.data.PacketData.PacketType.MANAGEMENT,
            packetLength = 64,
            rawData = ByteArray(64) { it.toByte() },
            sourceMac = "AA:BB:CC:DD:EE:FF",
            destinationMac = "FF:EE:DD:CC:BB:AA",
            ssid = "TestNetwork"
        )
        viewModel.setPacket(dummyPacket)
    }

    private fun displayPacket(packet: com.example.esp32netsniffer.data.PacketData) {
        // Update summary
        binding.packetSummary.text = """
            Channel: ${packet.channel} | RSSI: ${packet.rssi} dBm
            Type: ${packet.packetType.displayName} | Length: ${packet.packetLength} bytes
            Source: ${packet.sourceMac ?: "N/A"}
            Destination: ${packet.destinationMac ?: "N/A"}
            SSID: ${packet.ssid ?: "N/A"}
        """.trimIndent()

        // Update hex view
        val hexString = packet.rawData.joinToString(" ") { 
            String.format("%02X", it) 
        }
        binding.packetHexView.text = hexString

        // Update ASCII view
        val asciiString = packet.rawData.joinToString("") { byte ->
            if (byte in 32..126) byte.toChar().toString() else "."
        }
        binding.packetAsciiView.text = asciiString

        // Update parsed fields
        val fieldsText = """
            Frame Control: ${packet.frameControl?.toString(16) ?: "N/A"}
            Sequence Number: ${packet.sequenceNumber ?: "N/A"}
            Timestamp: ${java.text.SimpleDateFormat("HH:mm:ss.SSS", java.util.Locale.getDefault()).format(java.util.Date(packet.timestamp))}
        """.trimIndent()
        binding.packetFields.text = fieldsText
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 