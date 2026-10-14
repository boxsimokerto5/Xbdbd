package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class LoanStatus(val label: String, val badgeColorHex: Long) {
    PENDING("Menunggu Persetujuan", 0xFFFB8500),
    PENDING_KEAMANAN("Menunggu Verifikasi Keamanan", 0xFFFB8500),
    APPROVED("Disetujui - Dalam Perjalanan", 0xFF3A86FF),
    APPROVED_IN_TRIP("Disetujui - Dalam Perjalanan", 0xFF3A86FF),
    REJECTED("Ditolak", 0xFFEF476F),
    REJECTED_BY_KEAMANAN("Ditolak Pintu Keamanan", 0xFFEF476F),
    RETURNED_COMPLETED("Selesai & Kembali di Garasi", 0xFF06D6A0);

    companion object {
        fun fromString(value: String?): LoanStatus {
            return when (value?.uppercase()?.trim()) {
                "PENDING" -> PENDING
                "PENDING_KEAMANAN" -> PENDING_KEAMANAN
                "APPROVED" -> APPROVED
                "APPROVED_IN_TRIP" -> APPROVED_IN_TRIP
                "REJECTED" -> REJECTED
                "REJECTED_BY_KEAMANAN" -> REJECTED_BY_KEAMANAN
                "RETURNED", "RETURNED_COMPLETED" -> RETURNED_COMPLETED
                else -> PENDING
            }
        }
    }
}

/**
 * Data class untuk pengajuan peminjaman mobil Sekolah Rakyat ('LoanRequest')
 * yang terintegrasi secara type-safe dengan Firebase Firestore.
 */
@IgnoreExtraProperties
data class LoanRequest(
    @DocumentId
    val id: String = "",

    // ID user pemohon (Staff)
    val userId: String = "",
    val staffId: String = userId,
    val staffName: String = "",

    // ID unit mobil (nmax, granmax, avanza)
    val vehicleId: String = "",
    val vehicleName: String = "",
    val vehiclePlate: String = "",

    // Keperluan & Lokasi
    val purpose: String = "",
    val destination: String = "",

    // Status: PENDING_KEAMANAN (pending), APPROVED_IN_TRIP (approved), REJECTED_BY_KEAMANAN (rejected), RETURNED_COMPLETED
    val status: LoanStatus = LoanStatus.PENDING_KEAMANAN,

    // Timestamp server untuk integrasi Firestore
    @ServerTimestamp
    val timestamp: Timestamp? = null,

    @ServerTimestamp
    val createdAt: Timestamp? = null,

    // Catatan waktu lokal & verifikasi pos gerbang
    val requestTimeMillis: Long = System.currentTimeMillis(),
    val approvedTimeMillis: Long? = null,
    val returnTimeMillis: Long? = null,
    val securityNotes: String? = null,
    val verifiedBySecurityName: String? = null,
    val currentDistanceKm: Float = 0f,
    val estimatedTotalKm: Float = 14.5f
)
