package com.example.data.model

enum class VehicleStatus(val label: String, val chipEmoji: String) {
    GARASI("Di Garasi", "🏡"),
    SEDANG_DIPAKAI("Sedang Dipakai", "🚀")
}

data class Vehicle(
    val id: String,
    val name: String,
    val licensePlate: String,
    val category: String,
    val status: VehicleStatus = VehicleStatus.GARASI,
    val fuelPercent: Int = 85,
    val emojiIcon: String = "🚗",
    val currentBorrowerName: String? = null,
    val currentPurpose: String? = null,
    val currentDestination: String? = null,
    val lastOdometerKm: Int = 12450
)
