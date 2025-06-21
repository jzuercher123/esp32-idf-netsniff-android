package com.example.esp32netsniffer.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PacketDao {
    @Query("SELECT * FROM packets ORDER BY timestamp DESC")
    fun getAllPackets(): Flow<List<PacketData>>
    
    @Query("SELECT * FROM packets ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentPackets(limit: Int): Flow<List<PacketData>>
    
    @Query("SELECT * FROM packets WHERE id = :id")
    suspend fun getPacketById(id: Long): PacketData?
    
    @Query("SELECT * FROM packets WHERE channel = :channel ORDER BY timestamp DESC")
    fun getPacketsByChannel(channel: Int): Flow<List<PacketData>>
    
    @Query("SELECT * FROM packets WHERE packetType = :type ORDER BY timestamp DESC")
    fun getPacketsByType(type: PacketData.PacketType): Flow<List<PacketData>>
    
    @Query("SELECT * FROM packets WHERE rssi >= :minRssi ORDER BY timestamp DESC")
    fun getPacketsByMinRssi(minRssi: Int): Flow<List<PacketData>>
    
    @Query("SELECT * FROM packets WHERE ssid LIKE '%' || :ssid || '%' ORDER BY timestamp DESC")
    fun getPacketsBySsid(ssid: String): Flow<List<PacketData>>
    
    @Query("SELECT * FROM packets WHERE sourceMac LIKE '%' || :mac || '%' OR destinationMac LIKE '%' || :mac || '%' ORDER BY timestamp DESC")
    fun getPacketsByMac(mac: String): Flow<List<PacketData>>
    
    @Query("SELECT * FROM packets WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    fun getPacketsByTimeRange(startTime: Long, endTime: Long): Flow<List<PacketData>>
    
    @Query("SELECT COUNT(*) FROM packets")
    suspend fun getTotalPacketCount(): Int
    
    @Query("SELECT COUNT(*) FROM packets WHERE packetType = :type")
    suspend fun getPacketCountByType(type: PacketData.PacketType): Int
    
    @Query("SELECT COUNT(*) FROM packets WHERE channel = :channel")
    suspend fun getPacketCountByChannel(channel: Int): Int
    
    @Query("SELECT AVG(rssi) FROM packets")
    suspend fun getAverageRssi(): Double?
    
    @Query("SELECT DISTINCT ssid FROM packets WHERE ssid IS NOT NULL")
    suspend fun getUniqueSsids(): List<String>
    
    @Query("SELECT DISTINCT sourceMac FROM packets WHERE sourceMac IS NOT NULL UNION SELECT DISTINCT destinationMac FROM packets WHERE destinationMac IS NOT NULL")
    suspend fun getUniqueMacs(): List<String>
    
    @Insert
    suspend fun insertPacket(packet: PacketData)
    
    @Insert
    suspend fun insertPackets(packets: List<PacketData>)
    
    @Delete
    suspend fun deletePacket(packet: PacketData)
    
    @Query("DELETE FROM packets")
    suspend fun deleteAllPackets()
    
    @Query("DELETE FROM packets WHERE timestamp < :timestamp")
    suspend fun deletePacketsOlderThan(timestamp: Long)
}

@Database(entities = [PacketData::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class PacketDatabase : RoomDatabase() {
    abstract fun packetDao(): PacketDao
    
    companion object {
        @Volatile
        private var INSTANCE: PacketDatabase? = null
        
        fun getDatabase(context: android.content.Context): PacketDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    PacketDatabase::class.java,
                    "packet_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class Converters {
    @TypeConverter
    fun fromByteArray(value: ByteArray): String {
        return value.joinToString(",") { it.toString() }
    }
    
    @TypeConverter
    fun toByteArray(value: String): ByteArray {
        return value.split(",").map { it.toByte() }.toByteArray()
    }
    
    @TypeConverter
    fun fromPacketType(value: PacketData.PacketType): String {
        return value.name
    }
    
    @TypeConverter
    fun toPacketType(value: String): PacketData.PacketType {
        return PacketData.PacketType.valueOf(value)
    }
} 