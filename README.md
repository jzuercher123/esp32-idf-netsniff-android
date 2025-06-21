# ESP32 Network Sniffer Android App

A Wireshark-like Android application for analyzing WiFi packets captured by an ESP32 network sniffer device via Bluetooth Low Energy (BLE).

## Features

### ✅ Implemented
- **BLE Device Management**: Scan for and connect to ESP32 sniffer devices
- **Packet Data Model**: Complete data structures for WiFi packets (802.11 frames)
- **Database Layer**: Room database for storing and querying packet data
- **Repository Pattern**: Clean architecture for data access
- **Navigation**: Modern Android Navigation component with fragments
- **UI Framework**: Material Design 3 with view binding
- **Statistics Dashboard**: Charts for packet analysis (MPAndroidChart integration)

### 🚧 In Progress
- **Real-time Packet Display**: RecyclerView adapters for packet list
- **Filter System**: Advanced filtering by packet type, channel, RSSI, SSID, MAC
- **Packet Detail View**: Hex/ASCII viewer with parsed fields
- **BLE Communication**: ESP32 packet data parsing and processing

### 📋 Planned
- **Export Functionality**: CSV/PCAP export
- **Advanced Analytics**: Network topology, device tracking
- **Real-time Monitoring**: Live packet capture and analysis
- **Custom Filters**: User-defined filter rules

## Architecture

```
app/
├── data/           # Data models and database
├── bluetooth/      # BLE communication service
├── repository/     # Data access layer
├── ui/            # ViewModels and Fragments
└── res/           # Layouts, resources, navigation
```

### Key Components

1. **BluetoothService**: Manages BLE scanning, connection, and packet reception
2. **PacketRepository**: Handles data operations and filtering
3. **ViewModels**: Business logic and state management for each screen
4. **Fragments**: UI components with navigation between screens

## Screens

1. **Device Scan**: BLE device discovery and connection
2. **Packet List**: Wireshark-style packet list with filters
3. **Packet Detail**: Hex view, parsed fields, packet analysis
4. **Statistics**: Charts and network analytics

## Dependencies

- **AndroidX**: Core Android libraries
- **Room**: Database persistence
- **Navigation**: Fragment navigation
- **Material Design**: UI components
- **MPAndroidChart**: Data visualization
- **Coroutines**: Asynchronous programming

## Setup

1. Clone the repository
2. Open in Android Studio
3. Build and run on Android device (API 24+)
4. Grant Bluetooth and Location permissions
5. Connect to ESP32 sniffer device

## ESP32 Integration

The app expects ESP32 devices to:
- Advertise with names containing "ESP32_Sniffer"
- Provide packet data via BLE characteristics
- Send packet data in format: `[channel][rssi][type][length][raw_data...]`

## Development Status

- **UI Layer**: ✅ Complete
- **ViewModels**: ✅ Complete  
- **Navigation**: ✅ Complete
- **BLE Service**: 🚧 Basic implementation
- **Database**: ✅ Complete
- **Repository**: ✅ Complete

## Next Steps

1. Implement RecyclerView adapters for device and packet lists
2. Complete BLE packet parsing and processing
3. Add filter dialogs and advanced filtering
4. Implement real-time packet capture
5. Add export functionality
6. Polish UI/UX and add animations

## Contributing

This project is designed to work with the ESP32 IDF Network Sniffer firmware. See the ESP32 repository for hardware setup and firmware details. 