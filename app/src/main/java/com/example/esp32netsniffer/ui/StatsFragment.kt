package com.example.esp32netsniffer.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.esp32netsniffer.databinding.FragmentStatsBinding
import com.github.mikephil.charting.data.*
import kotlinx.coroutines.launch

class StatsFragment : Fragment() {
    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!
    
    // TODO: Replace with proper dependency injection
    private val viewModel: StatsViewModel by viewModels {
        object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                val database = com.example.esp32netsniffer.data.PacketDatabase.getDatabase(requireContext())
                val repository = com.example.esp32netsniffer.repository.PacketRepository(database.packetDao())
                return StatsViewModel(repository) as T
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        setupClickListeners()
        setupCharts()
        loadStats()
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.stats.collect { stats ->
                stats?.let { displayStats(it) }
            }
        }
    }

    private fun setupClickListeners() {
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupCharts() {
        // Configure pie chart for packet types
        binding.typePieChart.apply {
            description.isEnabled = false
            setUsePercentValues(true)
            legend.isEnabled = true
        }

        // Configure bar chart for channels
        binding.channelBarChart.apply {
            description.isEnabled = false
            legend.isEnabled = true
            setDrawGridBackground(false)
        }

        // Configure line chart for RSSI
        binding.rssiLineChart.apply {
            description.isEnabled = false
            legend.isEnabled = true
            setDrawGridBackground(false)
        }
    }

    private fun loadStats() {
        viewModel.loadStats()
    }

    private fun displayStats(stats: com.example.esp32netsniffer.data.NetworkStatistics) {
        // Update summary text
        binding.summaryStats.text = """
            Total Packets: ${stats.totalPackets}
            Average RSSI: ${String.format("%.1f", stats.averageRssi)} dBm
            Unique SSIDs: ${stats.uniqueSsids.size}
            Unique MACs: ${stats.uniqueMacs.size}
            Capture Duration: ${(stats.lastPacketTime - stats.captureStartTime) / 1000} seconds
        """.trimIndent()

        // Update packet type pie chart
        val pieEntries = stats.packetsByType.map { (type, count) ->
            PieEntry(count.toFloat(), type.displayName)
        }
        val pieDataSet = PieDataSet(pieEntries, "Packet Types")
        pieDataSet.colors = listOf(
            android.graphics.Color.rgb(255, 99, 132),
            android.graphics.Color.rgb(54, 162, 235),
            android.graphics.Color.rgb(255, 205, 86),
            android.graphics.Color.rgb(75, 192, 192)
        )
        binding.typePieChart.data = PieData(pieDataSet)
        binding.typePieChart.invalidate()

        // Update channel bar chart
        val barEntries = stats.packetsByChannel.map { (channel, count) ->
            BarEntry(channel.toFloat(), count.toFloat())
        }
        val barDataSet = BarDataSet(barEntries, "Packets by Channel")
        barDataSet.color = android.graphics.Color.rgb(54, 162, 235)
        binding.channelBarChart.data = BarData(barDataSet)
        binding.channelBarChart.invalidate()

        // Update RSSI line chart (simplified - just show average)
        val lineEntries = listOf(
            Entry(0f, stats.averageRssi.toFloat())
        )
        val lineDataSet = LineDataSet(lineEntries, "Average RSSI")
        lineDataSet.color = android.graphics.Color.rgb(255, 99, 132)
        lineDataSet.setCircleColor(android.graphics.Color.rgb(255, 99, 132))
        binding.rssiLineChart.data = LineData(lineDataSet)
        binding.rssiLineChart.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 