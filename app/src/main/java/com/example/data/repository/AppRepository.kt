package com.example.data.repository

import com.example.data.model.LoanRequest
import com.example.data.model.LoanStatus
import com.example.data.model.PromoBanner
import com.example.data.model.SchoolLocation
import com.example.data.model.User
import com.example.data.model.UserApprovalStatus
import com.example.data.model.UserRole
import com.example.data.model.Vehicle
import com.example.data.model.VehicleStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

class AppRepository {

    // Initial Users
    private val _users = MutableStateFlow<List<User>>(
        listOf(
            User(
                id = "admin-1",
                username = "admin",
                password = "123",
                fullName = "Pak Bambang (Admin)",
                role = UserRole.ADMIN,
                status = UserApprovalStatus.APPROVED,
                avatarEmoji = "👑",
                phone = "0811223344"
            ),
            User(
                id = "staff-1",
                username = "staff",
                password = "123",
                fullName = "Bu Dewi (Guru SD)",
                role = UserRole.STAFF,
                status = UserApprovalStatus.APPROVED,
                avatarEmoji = "👩‍🏫",
                phone = "0812345678"
            ),
            User(
                id = "keamanan-1",
                username = "keamanan",
                password = "123",
                fullName = "Pak Slamet (Pos Gerbang)",
                role = UserRole.KEAMANAN,
                status = UserApprovalStatus.APPROVED,
                avatarEmoji = "👮‍♂️",
                phone = "0819876543"
            ),
            // Pending users to showcase Admin Approval workflow
            User(
                id = "staff-pending-1",
                username = "budi_guru",
                password = "123",
                fullName = "Pak Budi Santoso (Staff Olahraga)",
                role = UserRole.STAFF,
                status = UserApprovalStatus.PENDING,
                avatarEmoji = "🏃‍♂️",
                phone = "0813334445"
            ),
            User(
                id = "keamanan-pending-1",
                username = "agus_satpam",
                password = "123",
                fullName = "Pak Agus (Keamanan Pintu Timur)",
                role = UserRole.KEAMANAN,
                status = UserApprovalStatus.PENDING,
                avatarEmoji = "🛡️",
                phone = "0817778889"
            )
        )
    )
    val users: StateFlow<List<User>> = _users.asStateFlow()

    // 3 Specified Vehicles
    private val _vehicles = MutableStateFlow<List<Vehicle>>(
        listOf(
            Vehicle(
                id = "nmax",
                name = "Yamaha N-Max",
                licensePlate = "SR 1234 NM",
                category = "Motor Operasional Cepat",
                status = VehicleStatus.GARASI,
                fuelPercent = 90,
                emojiIcon = "🛵",
                lastOdometerKm = 4820
            ),
            Vehicle(
                id = "granmax",
                name = "Daihatsu Gran Max",
                licensePlate = "SR 5678 GM",
                category = "Mobil Angkutan Logistik & Siswa",
                status = VehicleStatus.GARASI,
                fuelPercent = 75,
                emojiIcon = "🚐",
                lastOdometerKm = 34500
            ),
            Vehicle(
                id = "avanza",
                name = "Toyota Avanza Veloz",
                licensePlate = "SR 9999 VZ",
                category = "Mobil Dinas & Kunjungan Tamu",
                status = VehicleStatus.GARASI,
                fuelPercent = 85,
                emojiIcon = "🚗",
                lastOdometerKm = 21900
            )
        )
    )
    val vehicles: StateFlow<List<Vehicle>> = _vehicles.asStateFlow()

    // Loan Requests & History
    private val _loans = MutableStateFlow<List<LoanRequest>>(
        listOf(
            LoanRequest(
                id = "loan-demo-completed",
                staffId = "staff-1",
                staffName = "Bu Dewi (Guru SD)",
                vehicleId = "granmax",
                vehicleName = "Daihatsu Gran Max",
                vehiclePlate = "SR 5678 GM",
                purpose = "Distribusi Paket Buku & Seragam",
                destination = "Panti Binaan Tunjungan",
                requestTimeMillis = System.currentTimeMillis() - 86400000L,
                approvedTimeMillis = System.currentTimeMillis() - 82800000L,
                returnTimeMillis = System.currentTimeMillis() - 72000000L,
                status = LoanStatus.RETURNED_COMPLETED,
                securityNotes = "Surat tugas lengkap, KM dicek",
                verifiedBySecurityName = "Pak Slamet (Pos Gerbang)",
                currentDistanceKm = 18.4f,
                estimatedTotalKm = 18.4f
            )
        )
    )
    val loans: StateFlow<List<LoanRequest>> = _loans.asStateFlow()

    // School Base Location (Can be set by Admin)
    private val _schoolLocation = MutableStateFlow(
        SchoolLocation(
            name = "Sekolah Rakyat Pusat",
            address = "Jl. Pendidikan Kerakyatan No. 12, Surabaya",
            latitude = -7.2575,
            longitude = 112.7521,
            contactPhone = "0812-3456-7890"
        )
    )
    val schoolLocation: StateFlow<SchoolLocation> = _schoolLocation.asStateFlow()

    // Promo Banner (Can be updated by Admin)
    private val _promoBanner = MutableStateFlow(
        PromoBanner(
            id = "banner-1",
            title = "Armada Sekolah Rakyat Ceria! 🌟",
            subtitle = "Pastikan izin gerbang keamanan lengkap & selalu cek keselamatan unit sebelum berkendara.",
            badge = "INFO OPERASIONAL"
        )
    )
    val promoBanner: StateFlow<PromoBanner> = _promoBanner.asStateFlow()

    // Current Logged In User
    private val _currentUser = MutableStateFlow<User?>(_users.value.first { it.role == UserRole.ADMIN })
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // Register new user (Staff or Keamanan)
    fun register(
        username: String,
        password: String,
        fullName: String,
        role: UserRole,
        phone: String
    ): Result<User> {
        val trimmedUsername = username.trim().lowercase()
        if (_users.value.any { it.username.lowercase() == trimmedUsername }) {
            return Result.failure(Exception("Username sudah terdaftar! Gunakan username lain."))
        }

        val avatar = when (role) {
            UserRole.ADMIN -> "👑"
            UserRole.STAFF -> "👨‍🏫"
            UserRole.KEAMANAN -> "👮‍♂️"
        }

        val newUser = User(
            id = UUID.randomUUID().toString(),
            username = trimmedUsername,
            password = password,
            fullName = fullName.trim(),
            role = role,
            status = UserApprovalStatus.PENDING, // Must be approved by Admin
            avatarEmoji = avatar,
            phone = phone.ifBlank { "0812345678" }
        )

        _users.update { it + newUser }
        return Result.success(newUser)
    }

    // Login user
    fun login(username: String, password: String): Result<User> {
        val user = _users.value.find { it.username.equals(username.trim(), ignoreCase = true) }
            ?: return Result.failure(Exception("Username tidak ditemukan!"))

        if (user.password != password) {
            return Result.failure(Exception("Password salah!"))
        }

        if (user.status == UserApprovalStatus.PENDING) {
            return Result.failure(Exception("Akun Anda sedang menunggu persetujuan Admin! Hubungi Admin."))
        }

        if (user.status == UserApprovalStatus.REJECTED) {
            return Result.failure(Exception("Akun Anda telah ditolak oleh Admin."))
        }

        _currentUser.value = user
        return Result.success(user)
    }

    fun switchUser(user: User) {
        _currentUser.value = user
    }

    fun logout() {
        _currentUser.value = null
    }

    // Admin approves/rejects user
    fun setApprovalStatus(userId: String, isApproved: Boolean) {
        val newStatus = if (isApproved) UserApprovalStatus.APPROVED else UserApprovalStatus.REJECTED
        _users.update { list ->
            list.map { if (it.id == userId) it.copy(status = newStatus) else it }
        }
    }

    // Staff submits loan request
    fun requestVehicleLoan(
        staff: User,
        vehicleId: String,
        purpose: String,
        destination: String
    ): Result<LoanRequest> {
        val vehicle = _vehicles.value.find { it.id == vehicleId }
            ?: return Result.failure(Exception("Kendaraan tidak ditemukan!"))

        if (vehicle.status != VehicleStatus.GARASI) {
            return Result.failure(Exception("Kendaraan saat ini sedang dipakai di luar garasi!"))
        }

        val newLoan = LoanRequest(
            id = UUID.randomUUID().toString(),
            userId = staff.id,
            staffId = staff.id,
            staffName = staff.fullName,
            vehicleId = vehicle.id,
            vehicleName = vehicle.name,
            vehiclePlate = vehicle.licensePlate,
            purpose = purpose.trim(),
            destination = destination.trim(),
            requestTimeMillis = System.currentTimeMillis(),
            status = LoanStatus.PENDING_KEAMANAN
        )

        _loans.update { listOf(newLoan) + it }
        return Result.success(newLoan)
    }

    // Keamanan verifies loan request (Approve or Reject at Exit Gate)
    fun verifyLoan(
        loanId: String,
        securityUser: User,
        approve: Boolean,
        notes: String
    ): Result<Unit> {
        val loan = _loans.value.find { it.id == loanId }
            ?: return Result.failure(Exception("Permintaan peminjaman tidak ditemukan!"))

        if (approve) {
            // Update loan status to APPROVED_IN_TRIP
            _loans.update { list ->
                list.map {
                    if (it.id == loanId) {
                        it.copy(
                            status = LoanStatus.APPROVED_IN_TRIP,
                            approvedTimeMillis = System.currentTimeMillis(),
                            verifiedBySecurityName = securityUser.fullName,
                            securityNotes = notes.ifBlank { "Diizinkan keluar gerbang sekolah" },
                            currentDistanceKm = 0.5f
                        )
                    } else it
                }
            }

            // Update Vehicle to SEDANG_DIPAKAI
            _vehicles.update { list ->
                list.map {
                    if (it.id == loan.vehicleId) {
                        it.copy(
                            status = VehicleStatus.SEDANG_DIPAKAI,
                            currentBorrowerName = loan.staffName,
                            currentPurpose = loan.purpose,
                            currentDestination = loan.destination
                        )
                    } else it
                }
            }
        } else {
            // Reject loan
            _loans.update { list ->
                list.map {
                    if (it.id == loanId) {
                        it.copy(
                            status = LoanStatus.REJECTED_BY_KEAMANAN,
                            verifiedBySecurityName = securityUser.fullName,
                            securityNotes = notes.ifBlank { "Ditolak oleh petugas keamanan gerbang" }
                        )
                    } else it
                }
            }
        }

        return Result.success(Unit)
    }

    // Return vehicle back to Sekolah Rakyat garage
    fun returnVehicleToGarage(loanId: String): Result<Unit> {
        val loan = _loans.value.find { it.id == loanId }
            ?: return Result.failure(Exception("Data perjalanan tidak ditemukan!"))

        _loans.update { list ->
            list.map {
                if (it.id == loanId) {
                    it.copy(
                        status = LoanStatus.RETURNED_COMPLETED,
                        returnTimeMillis = System.currentTimeMillis(),
                        currentDistanceKm = it.estimatedTotalKm
                    )
                } else it
            }
        }

        // Return vehicle back to GARASI
        _vehicles.update { list ->
            list.map {
                if (it.id == loan.vehicleId) {
                    it.copy(
                        status = VehicleStatus.GARASI,
                        currentBorrowerName = null,
                        currentPurpose = null,
                        currentDestination = null,
                        lastOdometerKm = it.lastOdometerKm + loan.estimatedTotalKm.toInt()
                    )
                } else it
            }
        }

        return Result.success(Unit)
    }

    // Admin updates school base location
    fun updateSchoolLocation(name: String, address: String, lat: Double, lng: Double, phone: String) {
        _schoolLocation.value = SchoolLocation(
            name = name.trim(),
            address = address.trim(),
            latitude = lat,
            longitude = lng,
            contactPhone = phone.trim()
        )
    }

    // Admin updates promo banner
    fun updatePromoBanner(title: String, subtitle: String, badge: String) {
        _promoBanner.value = _promoBanner.value.copy(
            title = title.trim(),
            subtitle = subtitle.trim(),
            badge = badge.trim(),
            dateUpdated = "Baru saja"
        )
    }

    companion object {
        @Volatile
        private var instance: AppRepository? = null

        fun getInstance(): AppRepository {
            return instance ?: synchronized(this) {
                instance ?: AppRepository().also { instance = it }
            }
        }
    }
}
