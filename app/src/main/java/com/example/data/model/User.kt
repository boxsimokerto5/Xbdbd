package com.example.data.model

enum class UserRole(val displayName: String, val badgeEmoji: String) {
    ADMIN("Admin Sekolah", "👑"),
    STAFF("Staff / Pengajar", "👨‍🏫"),
    KEAMANAN("Keamanan Pintu", "👮‍♂️")
}

enum class UserApprovalStatus(val title: String) {
    PENDING("Menunggu Persetujuan"),
    APPROVED("Disetujui"),
    REJECTED("Ditolak")
}

data class User(
    val id: String,
    val username: String,
    val password: String = "123456",
    val fullName: String,
    val role: UserRole,
    val status: UserApprovalStatus = UserApprovalStatus.PENDING,
    val avatarEmoji: String = "🎒",
    val phone: String = "08123456789",
    val registrationDateMillis: Long = System.currentTimeMillis()
)
