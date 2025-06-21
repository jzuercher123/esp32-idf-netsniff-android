package com.example.esp32netsniffer.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "packets")
data class PacketData(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val channel: Int,
    val rssi: Int,
    val packetType: PacketType,
    val packetLength: Int,
    val rawData: ByteArray,
    val sourceMac: String? = null,
    val destinationMac: String? = null,
    val ssid: String? = null,
    val frameControl: Int? = null,
    val sequenceNumber: Int? = null
) {
    enum class PacketType(val displayName: String) {
        MANAGEMENT("Management"),
        CONTROL("Control"),
        DATA("Data"),
        UNKNOWN("Unknown")
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PacketData

        if (id != other.id) return false
        if (timestamp != other.timestamp) return false
        if (channel != other.channel) return false
        if (rssi != other.rssi) return false
        if (packetType != other.packetType) return false
        if (packetLength != other.packetLength) return false
        if (!rawData.contentEquals(other.rawData)) return false
        if (sourceMac != other.sourceMac) return false
        if (destinationMac != other.destinationMac) return false
        if (ssid != other.ssid) return false
        if (frameControl != other.frameControl) return false
        if (sequenceNumber != other.sequenceNumber) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + channel
        result = 31 * result + rssi
        result = 31 * result + packetType.hashCode()
        result = 31 * result + packetLength
        result = 31 * result + rawData.contentHashCode()
        result = 31 * result + (sourceMac?.hashCode() ?: 0)
        result = 31 * result + (destinationMac?.hashCode() ?: 0)
        result = 31 * result + (ssid?.hashCode() ?: 0)
        result = 31 * result + (frameControl ?: 0)
        result = 31 * result + (sequenceNumber ?: 0)
        return result
    }
}

data class NetworkStatistics(
    val totalPackets: Int = 0,
    val packetsByType: Map<PacketData.PacketType, Int> = emptyMap(),
    val packetsByChannel: Map<Int, Int> = emptyMap(),
    val averageRssi: Double = 0.0,
    val uniqueSsids: Set<String> = emptySet(),
    val uniqueMacs: Set<String> = emptySet(),
    val captureStartTime: Long = System.currentTimeMillis(),
    val lastPacketTime: Long = System.currentTimeMillis()
)

data class PacketFilter(
    val packetTypes: Set<PacketData.PacketType> = emptySet(),
    val channels: Set<Int> = emptySet(),
    val minRssi: Int? = null,
    val maxRssi: Int? = null,
    val ssidFilter: String? = null,
    val macFilter: String? = null,
    val timeRange: Pair<Long, Long>? = null
) 