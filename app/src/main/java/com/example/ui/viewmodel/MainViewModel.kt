package com.example.ui.viewmodel

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.auth.FirebaseAuthRepository
import com.example.data.model.LoanRequest
import com.example.data.model.LoanStatus
import com.example.data.model.PromoBanner
import com.example.data.model.SchoolLocation
import com.example.data.model.User
import com.example.data.model.UserApprovalStatus
import com.example.data.model.UserRole
import com.example.data.model.Vehicle
import com.example.data.repository.AppRepository
import com.example.data.repository.FirestoreLoanRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class ScreenState {
    AUTH,
    DASHBOARD,
    PINJAM_FORM,
    TRACKING_MAP,
    TRIP_HISTORY,
    ADMIN_PANEL,
    SECURITY_VERIFY
}

class MainViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository: AppRepository = AppRepository.getInstance()
    val firebaseAuthRepo: FirebaseAuthRepository = FirebaseAuthRepository.getInstance(application)
    val firestoreLoanRepo: FirestoreLoanRepository = FirestoreLoanRepository.getInstance(application)

    val currentUser: StateFlow<User?> = repository.currentUser
    val users: StateFlow<List<User>> = repository.users
    val vehicles: StateFlow<List<Vehicle>> = repository.vehicles
    val loans: StateFlow<List<LoanRequest>> = repository.loans
    val schoolLocation: StateFlow<SchoolLocation> = repository.schoolLocation
    val promoBanner: StateFlow<PromoBanner> = repository.promoBanner

    // Firebase Auth states
    val firebaseUser: StateFlow<FirebaseUser?> = firebaseAuthRepo.firebaseUser
    val firebaseUserProfile: StateFlow<User?> = firebaseAuthRepo.userProfile
    val isRegisteringNewRole: StateFlow<Boolean> = firebaseAuthRepo.isRegisteringNewRole

    // Firestore pending loan requests
    private val _firestorePendingLoans = MutableStateFlow<List<LoanRequest>>(emptyList())
    val firestorePendingLoans: StateFlow<List<LoanRequest>> = _firestorePendingLoans.asStateFlow()

    // Firestore all loan requests (history & pending)
    private val _firestoreAllLoans = MutableStateFlow<List<LoanRequest>>(emptyList())
    val firestoreAllLoans: StateFlow<List<LoanRequest>> = _firestoreAllLoans.asStateFlow()

    private val _currentScreen = MutableStateFlow(ScreenState.DASHBOARD)
    val currentScreen: StateFlow<ScreenState> = _currentScreen.asStateFlow()

    private val _selectedVehicleForLoan = MutableStateFlow<Vehicle?>(null)
    val selectedVehicleForLoan: StateFlow<Vehicle?> = _selectedVehicleForLoan.asStateFlow()

    private val _selectedLoanForTracking = MutableStateFlow<LoanRequest?>(null)
    val selectedLoanForTracking: StateFlow<LoanRequest?> = _selectedLoanForTracking.asStateFlow()

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    // Map animated route progress (0f..1f)
    private val _mapRouteProgress = MutableStateFlow(0.35f)
    val mapRouteProgress: StateFlow<Float> = _mapRouteProgress.asStateFlow()

    init {
        // Observe Firebase User Profile updates
        viewModelScope.launch {
            firebaseAuthRepo.userProfile.collect { profile ->
                if (profile != null) {
                    if (profile.status == UserApprovalStatus.APPROVED) {
                        repository.switchUser(profile)
                        _currentScreen.value = ScreenState.DASHBOARD
                    }
                }
            }
        }

        // Real-time observation of pending loans from Firestore
        viewModelScope.launch {
            firestoreLoanRepo.observePendingLoanRequests().collect { firestoreList ->
                _firestorePendingLoans.value = firestoreList
            }
        }

        // Real-time observation of all loans from Firestore
        viewModelScope.launch {
            firestoreLoanRepo.observeAllLoanRequests().collect { list ->
                _firestoreAllLoans.value = list
            }
        }

        // Start simulated live motion for active trips
        viewModelScope.launch {
            while (isActive) {
                delay(1200)
                val activeLoan = loans.value.find { it.status == LoanStatus.APPROVED_IN_TRIP }
                if (activeLoan != null) {
                    _mapRouteProgress.value = (_mapRouteProgress.value + 0.05f)
                    if (_mapRouteProgress.value > 1.0f) {
                        _mapRouteProgress.value = 0.05f
                    }
                }
            }
        }
    }

    fun navigateTo(screen: ScreenState) {
        _currentScreen.value = screen
    }

    fun selectVehicleForLoan(vehicle: Vehicle) {
        _selectedVehicleForLoan.value = vehicle
        _currentScreen.value = ScreenState.PINJAM_FORM
    }

    fun openTrackingMap(loan: LoanRequest?) {
        _selectedLoanForTracking.value = loan ?: loans.value.firstOrNull { it.status == LoanStatus.APPROVED_IN_TRIP }
        _currentScreen.value = ScreenState.TRACKING_MAP
    }

    fun openSecurityVerification() {
        _currentScreen.value = ScreenState.SECURITY_VERIFY
    }

    fun openAdminPanel() {
        _currentScreen.value = ScreenState.ADMIN_PANEL
    }

    fun openTripHistory() {
        _currentScreen.value = ScreenState.TRIP_HISTORY
    }

    // Google Sign-In with Firebase Auth
    fun signInWithGoogle(activity: Activity) {
        firebaseAuthRepo.signInWithGoogle(
            activity = activity,
            scope = viewModelScope,
            onSuccess = {
                viewModelScope.launch {
                    _userMessage.emit("Login Google Berhasil! Memeriksa profil peran...")
                }
            },
            onError = { errorMsg ->
                viewModelScope.launch {
                    _userMessage.emit(errorMsg)
                }
            }
        )
    }

    // Register Role in Firebase Auth profile
    fun registerFirebaseRole(role: UserRole, fullName: String, phone: String) {
        firebaseAuthRepo.registerUserProfile(
            role = role,
            fullName = fullName,
            phone = phone,
            onSuccess = {
                viewModelScope.launch {
                    _userMessage.emit("Profil $role terdaftar! Menunggu persetujuan Admin.")
                }
            },
            onError = { err ->
                viewModelScope.launch {
                    _userMessage.emit(err)
                }
            }
        )
    }

    fun signOutFirebase() {
        firebaseAuthRepo.signOut(viewModelScope) {
            repository.logout()
            _currentScreen.value = ScreenState.AUTH
            viewModelScope.launch {
                _userMessage.emit("Berhasil keluar dari akun Firebase.")
            }
        }
    }

    fun quickSwitchAccount(user: User) {
        repository.switchUser(user)
        _currentScreen.value = ScreenState.DASHBOARD
        viewModelScope.launch {
            _userMessage.emit("Masuk sebagai: ${user.fullName} (${user.role.displayName})")
        }
    }

    fun logout() {
        repository.logout()
        firebaseAuthRepo.signOut(viewModelScope) {}
        _currentScreen.value = ScreenState.AUTH
        viewModelScope.launch {
            _userMessage.emit("Anda telah keluar.")
        }
    }

    fun approveUser(userId: String) {
        repository.setApprovalStatus(userId, isApproved = true)
        viewModelScope.launch {
            _userMessage.emit("Akun disetujui! Pengguna kini dapat mengakses sistem ✅")
        }
    }

    fun rejectUser(userId: String) {
        repository.setApprovalStatus(userId, isApproved = false)
        viewModelScope.launch {
            _userMessage.emit("Akun telah ditolak ❌")
        }
    }

    fun submitLoan(vehicleId: String, purpose: String, destination: String) {
        val user = currentUser.value
        if (user == null || user.role != UserRole.STAFF) {
            viewModelScope.launch {
                _userMessage.emit("Hanya Staff yang dapat mengajukan peminjaman!")
            }
            return
        }

        val result = repository.requestVehicleLoan(user, vehicleId, purpose, destination)
        result.onSuccess { newLoan ->
            // Sync to Firestore
            firestoreLoanRepo.saveLoanRequest(newLoan)
            _currentScreen.value = ScreenState.DASHBOARD
            viewModelScope.launch {
                _userMessage.emit("Pengajuan terkirim! Notifikasi telah masuk ke Pos Keamanan & Firestore 🔔")
            }
        }.onFailure {
            viewModelScope.launch {
                _userMessage.emit(it.message ?: "Gagal mengajukan")
            }
        }
    }

    // Admin updates LoanRequest status in Firestore (Approve or Reject)
    fun adminApproveLoan(loanId: String, notes: String) {
        val admin = currentUser.value
        val reviewerName = "Admin: ${admin?.fullName ?: "Pak Bambang"}"
        val finalNotes = notes.ifBlank { "Disetujui oleh Admin Sekolah Rakyat" }

        // Update in Firestore with status 'approved'
        firestoreLoanRepo.updateLoanStatus(
            loanId = loanId,
            status = LoanStatus.APPROVED_IN_TRIP,
            reviewerName = reviewerName,
            notes = finalNotes
        ) { success ->
            if (success) {
                viewModelScope.launch {
                    _userMessage.emit("Status dokumen di Firestore berhasil diupdate: 'approved' ✅")
                }
            }
        }

        // Also update local state & vehicle status
        val adminUser = admin ?: users.value.first { it.role == UserRole.ADMIN }
        repository.verifyLoan(loanId, adminUser, approve = true, notes = finalNotes)

        viewModelScope.launch {
            _userMessage.emit("Peminjaman berhasil DISETUJUI oleh Admin! 🚀 Mobil keluar garasi.")
        }
    }

    fun adminRejectLoan(loanId: String, notes: String) {
        val admin = currentUser.value
        val reviewerName = "Admin: ${admin?.fullName ?: "Pak Bambang"}"
        val finalNotes = notes.ifBlank { "Ditolak oleh Admin Sekolah Rakyat" }

        // Update in Firestore with status 'rejected'
        firestoreLoanRepo.updateLoanStatus(
            loanId = loanId,
            status = LoanStatus.REJECTED_BY_KEAMANAN,
            reviewerName = reviewerName,
            notes = finalNotes
        ) { success ->
            if (success) {
                viewModelScope.launch {
                    _userMessage.emit("Status dokumen di Firestore berhasil diupdate: 'rejected' ❌")
                }
            }
        }

        // Also update local state
        val adminUser = admin ?: users.value.first { it.role == UserRole.ADMIN }
        repository.verifyLoan(loanId, adminUser, approve = false, notes = finalNotes)

        viewModelScope.launch {
            _userMessage.emit("Peminjaman telah DITOLAK oleh Admin ❌")
        }
    }

    fun createSampleFirestoreLoan(
        vehicleId: String,
        vehicleName: String,
        vehiclePlate: String,
        staffName: String,
        purpose: String,
        destination: String
    ) {
        firestoreLoanRepo.createSampleLoanRequest(
            vehicleId = vehicleId,
            vehicleName = vehicleName,
            vehiclePlate = vehiclePlate,
            staffName = staffName,
            purpose = purpose,
            destination = destination
        ) { success ->
            viewModelScope.launch {
                if (success) {
                    _userMessage.emit("Dokumen pengajuan simulasi baru berhasil dibuat di Firestore! 🚗")
                } else {
                    _userMessage.emit("Gagal membuat dokumen di Firestore.")
                }
            }
        }
    }

    fun verifyLoanBySecurity(loanId: String, approve: Boolean, notes: String) {
        val security = currentUser.value
        if (security == null || security.role != UserRole.KEAMANAN) {
            viewModelScope.launch {
                _userMessage.emit("Hanya Keamanan yang berhak memverifikasi pintu keluar!")
            }
            return
        }

        val result = repository.verifyLoan(loanId, security, approve, notes)
        result.onSuccess {
            val message = if (approve) {
                "Gerbang dibuka! Mobil keluar dan Tracking Map dimulai 🚀"
            } else {
                "Peminjaman unit telah ditolak di gerbang."
            }
            viewModelScope.launch {
                _userMessage.emit(message)
            }
            _currentScreen.value = ScreenState.DASHBOARD
        }.onFailure {
            viewModelScope.launch {
                _userMessage.emit(it.message ?: "Gagal memverifikasi")
            }
        }
    }

    fun returnVehicle(loanId: String) {
        val result = repository.returnVehicleToGarage(loanId)
        result.onSuccess {
            viewModelScope.launch {
                _userMessage.emit("Mobil berhasil kembali ke Garasi Sekolah Rakyat! 🏡")
            }
            _currentScreen.value = ScreenState.DASHBOARD
        }.onFailure {
            viewModelScope.launch {
                _userMessage.emit(it.message ?: "Gagal mengembalikan mobil")
            }
        }
    }

    fun updateSchoolLocation(name: String, address: String, lat: Double, lng: Double, phone: String) {
        repository.updateSchoolLocation(name, address, lat, lng, phone)
        viewModelScope.launch {
            _userMessage.emit("Titik lokasi Sekolah Rakyat berhasil disimpan! 📍")
        }
    }

    fun updatePromoBanner(title: String, subtitle: String, badge: String) {
        repository.updatePromoBanner(title, subtitle, badge)
        viewModelScope.launch {
            _userMessage.emit("Banner promosi berhasil diperbarui! 📢")
        }
    }
}
