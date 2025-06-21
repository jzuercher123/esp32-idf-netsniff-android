package com.example.esp32netsniffer.repository

import com.example.esp32netsniffer.data.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PacketRepository @Inject constructor(
    private val packetDao: PacketDao
) {
    
    fun getAllPackets(): Flow<List<PacketData>> = packetDao.getAllPackets()
    
    fun getRecentPackets(limit: Int): Flow<List<PacketData>> = packetDao.getRecentPackets(limit)
    
    suspend fun getPacketById(id: Long): PacketData? = packetDao.getPacketById(id)
    
    fun getFilteredPackets(filter: PacketFilter): Flow<List<PacketData>> {
        return packetDao.getAllPackets().map { packets ->
            packets.filter { packet ->
                var matches = true
                
                // Filter by packet type
                if (filter.packetTypes.isNotEmpty()) {
                    matches = matches && packet.packetType in filter.packetTypes
                }
                
                // Filter by channel
                if (filter.channels.isNotEmpty()) {
                    matches = matches && packet.channel in filter.channels
                }
                
                // Filter by RSSI range
                if (filter.minRssi != null) {
                    matches = matches && packet.rssi >= filter.minRssi
                }
                if (filter.maxRssi != null) {
                    matches = matches && packet.rssi <= filter.maxRssi
                }
                
                // Filter by SSID
                if (filter.ssidFilter != null && filter.ssidFilter.isNotEmpty()) {
                    matches = matches && packet.ssid?.contains(filter.ssidFilter, ignoreCase = true) == true
                }
                
                // Filter by MAC address
                if (filter.macFilter != null && filter.macFilter.isNotEmpty()) {
                    val macMatches = packet.sourceMac?.contains(filter.macFilter, ignoreCase = true) == true ||
                                   packet.destinationMac?.contains(filter.macFilter, ignoreCase = true) == true
                    matches = matches && macMatches
                }
                
                // Filter by time range
                if (filter.timeRange != null) {
                    val (startTime, endTime) = filter.timeRange
                    matches = matches && packet.timestamp in startTime..endTime
                }
                
                matches
            }
        }
    }
    
    suspend fun insertPacket(packet: PacketData) {
        packetDao.insertPacket(packet)
    }
    
    suspend fun insertPackets(packets: List<PacketData>) {
        packetDao.insertPackets(packets)
    }
    
    suspend fun deletePacket(packet: PacketData) {
        packetDao.deletePacket(packet)
    }
    
    suspend fun deleteAllPackets() {
        packetDao.deleteAllPackets()
    }
    
    suspend fun deletePacketsOlderThan(timestamp: Long) {
        packetDao.deletePacketsOlderThan(timestamp)
    }
    
    suspend fun getNetworkStatistics(): NetworkStatistics {
        val totalPackets = packetDao.getTotalPacketCount()
        val averageRssi = packetDao.getAverageRssi() ?: 0.0
        val uniqueSsids = packetDao.getUniqueSsids().toSet()
        val uniqueMacs = packetDao.getUniqueMacs().toSet()
        
        val packetsByType = PacketData.PacketType.values().associateWith { type ->
            packetDao.getPacketCountByType(type)
        }
        
        val packetsByChannel = (1..13).associateWith { channel ->
            packetDao.getPacketCountByChannel(channel)
        }
        
        return NetworkStatistics(
            totalPackets = totalPackets,
            packetsByType = packetsByType,
            packetsByChannel = packetsByChannel,
            averageRssi = averageRssi,
            uniqueSsids = uniqueSsids,
            uniqueMacs = uniqueMacs,
            captureStartTime = System.currentTimeMillis() - (24 * 60 * 60 * 1000), // Last 24 hours
            lastPacketTime = System.currentTimeMillis()
        )
    }
    
    suspend fun exportToCsv(): String {
        val packets = packetDao.getAllPackets().map { it.first() }
        
        val csvBuilder = StringBuilder()
        csvBuilder.append("Timestamp,Channel,RSSI,Packet Type,Length,Source MAC,Destination MAC,SSID,Frame Control,Sequence Number\n")
        
        packets.forEach { packet ->
            csvBuilder.append("${packet.timestamp},")
            csvBuilder.append("${packet.channel},")
            csvBuilder.append("${packet.rssi},")
            csvBuilder.append("${packet.packetType.displayName},")
            csvBuilder.append("${packet.packetLength},")
            csvBuilder.append("\"${packet.sourceMac ?: ""}\",")
            csvBuilder.append("\"${packet.destinationMac ?: ""}\",")
            csvBuilder.append("\"${packet.ssid ?: ""}\",")
            csvBuilder.append("${packet.frameControl ?: ""},")
            csvBuilder.append("${packet.sequenceNumber ?: ""}\n")
        }
        
        return csvBuilder.toString()
    }
} 