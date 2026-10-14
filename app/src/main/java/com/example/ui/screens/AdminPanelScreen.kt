package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.LoanRequest
import com.example.data.model.LoanStatus
import com.example.data.model.PromoBanner
import com.example.data.model.SchoolLocation
import com.example.data.model.User
import com.example.data.model.UserApprovalStatus
import com.example.data.model.UserRole
import com.example.ui.components.GameButton
import com.example.ui.theme.GameAmber
import com.example.ui.theme.GameCoralRed
import com.example.ui.theme.GameDarkNavy
import com.example.ui.theme.GameMintGreen
import com.example.ui.theme.GameSkyBlue
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.ScreenState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminPanelScreen(
    viewModel: MainViewModel,
    currentUser: User,
    users: List<User>,
    schoolLocation: SchoolLocation,
    promoBanner: PromoBanner,
    loans: List<LoanRequest> = emptyList(),
    modifier: Modifier = Modifier
) {
    // Collect pending loans from Firestore live stream
    val firestorePendingLoans by viewModel.firestorePendingLoans.collectAsStateWithLifecycle()
    val allPendingLoans = remember(firestorePendingLoans, loans) {
        (firestorePendingLoans + loans.filter { it.status == LoanStatus.PENDING_KEAMANAN })
            .distinctBy { it.id }
    }

    var selectedTab by remember { mutableStateOf(0) } // 0 = Pinjam Mobil (Firestore), 1 = Persetujuan Akun, 2 = Titik Lokasi, 3 = Banner Promosi

    // Titik Lokasi State
    var locName by remember(schoolLocation) { mutableStateOf(schoolLocation.name) }
    var locAddress by remember(schoolLocation) { mutableStateOf(schoolLocation.address) }
    var locLat by remember(schoolLocation) { mutableStateOf(schoolLocation.latitude.toString()) }
    var locLng by remember(schoolLocation) { mutableStateOf(schoolLocation.longitude.toString()) }
    var locPhone by remember(schoolLocation) { mutableStateOf(schoolLocation.contactPhone) }

    // Banner Promosi State
    var bannerTitle by remember(promoBanner) { mutableStateOf(promoBanner.title) }
    var bannerSubtitle by remember(promoBanner) { mutableStateOf(promoBanner.subtitle) }
    var bannerBadge by remember(promoBanner) { mutableStateOf(promoBanner.badge) }

    val pendingUsers = users.filter { it.status == UserApprovalStatus.PENDING }
    val approvedUsers = users.filter { it.status == UserApprovalStatus.APPROVED && it.role != UserRole.ADMIN }
    val dateFormat = remember { SimpleDateFormat("HH:mm, dd MMM yyyy", Locale("id", "ID")) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F9))
    ) {
        // App bar
        Surface(
            color = Color.White,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp)
            ) {
                IconButton(onClick = { viewModel.navigateTo(ScreenState.DASHBOARD) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint = GameDarkNavy
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Column {
                    Text(
                        text = "Panel Kontrol Admin Sekolah 👑",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = GameDarkNavy
                    )
                    Text(
                        text = "Kelola Permintaan Pinjam Mobil, Akun & Titik Sekolah",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }
        }

        // Navigation Tabs (4 Sections)
        Surface(
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceAround,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 6.dp)
            ) {
                AdminTabItem(
                    label = "Pinjam Mobil (${allPendingLoans.size}) 🚗",
                    isSelected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    testTag = "tab_admin_loan_requests"
                )
                AdminTabItem(
                    label = "Akun (${pendingUsers.size}) 👥",
                    isSelected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    testTag = "tab_admin_approval"
                )
                AdminTabItem(
                    label = "Lokasi 📍",
                    isSelected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    testTag = "tab_admin_location"
                )
                AdminTabItem(
                    label = "Banner 📢",
                    isSelected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    testTag = "tab_admin_banner"
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            when (selectedTab) {
                0 -> {
                    // TAB 0: Persetujuan LoanRequest Dokumen Firestore
                    var loanVehicleFilter by remember { mutableStateOf<String?>(null) }
                    var showProcessedHistory by remember { mutableStateOf(false) }
                    val firestoreAllLoans by viewModel.firestoreAllLoans.collectAsStateWithLifecycle()

                    val displayedPendingLoans = remember(allPendingLoans, loanVehicleFilter) {
                        if (loanVehicleFilter == null) allPendingLoans
                        else allPendingLoans.filter { it.vehicleId.contains(loanVehicleFilter!!, ignoreCase = true) || it.vehicleName.contains(loanVehicleFilter!!, ignoreCase = true) }
                    }

                    val processedLoans = remember(firestoreAllLoans) {
                        firestoreAllLoans.filter {
                            it.status == LoanStatus.APPROVED_IN_TRIP ||
                            it.status == LoanStatus.APPROVED ||
                            it.status == LoanStatus.REJECTED_BY_KEAMANAN ||
                            it.status == LoanStatus.REJECTED
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Permohonan Pinjam Mobil (Firestore)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = GameDarkNavy
                            )
                            Text(
                                text = "Admin dapat melihat dokumen 'pending' dari Firestore dan update status ke 'approved' atau 'rejected'.",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = GameMintGreen.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "Firestore Live 🟢",
                                color = GameMintGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick Stats Bar
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = GameAmber.copy(alpha = 0.15f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(8.dp)
                            ) {
                                Text(text = "${allPendingLoans.size}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = GameAmber)
                                Text(text = "Menunggu (Pending)", fontSize = 10.sp, color = GameDarkNavy)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = GameMintGreen.copy(alpha = 0.15f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(8.dp)
                            ) {
                                Text(text = "${processedLoans.count { it.status == LoanStatus.APPROVED_IN_TRIP || it.status == LoanStatus.APPROVED }}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = GameMintGreen)
                                Text(text = "Disetujui (Approved)", fontSize = 10.sp, color = GameDarkNavy)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = GameCoralRed.copy(alpha = 0.15f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(8.dp)
                            ) {
                                Text(text = "${processedLoans.count { it.status == LoanStatus.REJECTED_BY_KEAMANAN || it.status == LoanStatus.REJECTED }}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = GameCoralRed)
                                Text(text = "Ditolak (Rejected)", fontSize = 10.sp, color = GameDarkNavy)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Vehicle Filter Chips & Action Toggle
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        FilterPill(
                            label = "Semua (${allPendingLoans.size})",
                            isSelected = loanVehicleFilter == null,
                            onClick = { loanVehicleFilter = null }
                        )
                        FilterPill(
                            label = "Gran Max 🚐",
                            isSelected = loanVehicleFilter == "granmax",
                            onClick = { loanVehicleFilter = "granmax" }
                        )
                        FilterPill(
                            label = "Avanza 🚗",
                            isSelected = loanVehicleFilter == "avanza",
                            onClick = { loanVehicleFilter = "avanza" }
                        )
                        FilterPill(
                            label = "N-Max 🛵",
                            isSelected = loanVehicleFilter == "nmax",
                            onClick = { loanVehicleFilter = "nmax" }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (displayedPendingLoans.isEmpty()) {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp)
                            ) {
                                Text(text = "🎉", fontSize = 38.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Tidak Ada Permohonan Pending",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = GameDarkNavy
                                )
                                Text(
                                    text = "Semua dokumen LoanRequest dari Firestore sudah ditinjau oleh Admin.",
                                    fontSize = 11.sp,
                                    color = Color.Gray,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                GameButton(
                                    text = "Buat Pengajuan Simulasi di Firestore 🚗✨",
                                    onClick = {
                                        viewModel.createSampleFirestoreLoan(
                                            vehicleId = "granmax",
                                            vehicleName = "Daihatsu Gran Max",
                                            vehiclePlate = "L 1945 SR",
                                            staffName = "Bu Dewi, S.Pd",
                                            purpose = "Kunjungan Siswa Sekolah Rakyat ke Balai Kota",
                                            destination = "Balai Pemuda Surabaya"
                                        )
                                    },
                                    buttonColor = GameSkyBlue,
                                    fontSize = 11.sp,
                                    height = 40.dp,
                                    modifier = Modifier.fillMaxWidth(0.9f),
                                    testTag = "btn_create_sample_firestore_loan"
                                )
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            displayedPendingLoans.forEach { loan ->
                                AdminLoanApprovalCard(
                                    loan = loan,
                                    dateFormat = dateFormat,
                                    onApprove = { notes ->
                                        viewModel.adminApproveLoan(loan.id, notes)
                                    },
                                    onReject = { notes ->
                                        viewModel.adminRejectLoan(loan.id, notes)
                                    }
                                )
                            }
                        }
                    }

                    // Button to toggle previously processed history
                    if (processedLoans.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showProcessedHistory = !showProcessedHistory }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text(
                                    text = "Riwayat Dokumen yang Sudah Diproses (${processedLoans.size}) 📋",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = GameDarkNavy
                                )
                                Text(
                                    text = if (showProcessedHistory) "Tutup ▲" else "Lihat ▼",
                                    fontSize = 11.sp,
                                    color = GameSkyBlue,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (showProcessedHistory) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                processedLoans.forEach { loan ->
                                    ProcessedLoanCard(loan = loan, dateFormat = dateFormat)
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // TAB 1: Persetujuan Akun Staff & Keamanan
                    Text(
                        text = "Persetujuan Akun Pengguna Baru",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = GameDarkNavy
                    )
                    Text(
                        text = "Staff & Keamanan yang baru mendaftar harus disetujui Admin sebelum dapat login.",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (pendingUsers.isEmpty()) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp)
                            ) {
                                Text(text = "🎉", fontSize = 36.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Tidak Ada Akun Pending",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = GameDarkNavy
                                )
                                Text(
                                    text = "Semua pendaftaran Staff dan Keamanan sudah ditinjau.",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            pendingUsers.forEach { user ->
                                PendingUserCard(
                                    user = user,
                                    onApprove = { viewModel.approveUser(user.id) },
                                    onReject = { viewModel.rejectUser(user.id) }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Akun Aktif yang Sudah Disetujui (${approvedUsers.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = GameDarkNavy
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        approvedUsers.forEach { user ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color.White,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    Text(text = user.avatarEmoji, fontSize = 24.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = user.fullName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = GameDarkNavy
                                        )
                                        Text(
                                            text = "${user.role.displayName} • @${user.username}",
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = GameMintGreen.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "Aktif ✅",
                                            color = GameMintGreen,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // TAB 2: Menentukan Titik Lokasi Sekolah Rakyat
                    Text(
                        text = "Atur Titik Lokasi Sekolah Rakyat 📍",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = GameDarkNavy
                    )
                    Text(
                        text = "Titik ini menjadi titik awal (Home Base) keberangkatan dan kepulangan armada mobil.",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            OutlinedTextField(
                                value = locName,
                                onValueChange = { locName = it },
                                label = { Text("Nama Lokasi / Kampus") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = locAddress,
                                onValueChange = { locAddress = it },
                                label = { Text("Alamat Lengkap") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = locLat,
                                    onValueChange = { locLat = it },
                                    label = { Text("Latitude") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                OutlinedTextField(
                                    value = locLng,
                                    onValueChange = { locLng = it },
                                    label = { Text("Longitude") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(14.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = locPhone,
                                onValueChange = { locPhone = it },
                                label = { Text("Kontak Pos Sekolah") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp)
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "Preset Cepat:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                PresetChip(title = "Surabaya Pusat") {
                                    locName = "Sekolah Rakyat Kampus Surabaya"
                                    locAddress = "Jl. Pendidikan Kerakyatan No. 12, Surabaya"
                                    locLat = "-7.2575"
                                    locLng = "112.7521"
                                }
                                PresetChip(title = "Jakarta Pusat") {
                                    locName = "Sekolah Rakyat Merdeka Jakarta"
                                    locAddress = "Jl. Merdeka Rakyat No. 45, Jakarta"
                                    locLat = "-6.2088"
                                    locLng = "106.8456"
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            GameButton(
                                text = "Simpan Titik Lokasi 📍",
                                onClick = {
                                    val lat = locLat.toDoubleOrNull() ?: -7.2575
                                    val lng = locLng.toDoubleOrNull() ?: 112.7521
                                    viewModel.updateSchoolLocation(locName, locAddress, lat, lng, locPhone)
                                },
                                buttonColor = GameSkyBlue,
                                modifier = Modifier.fillMaxWidth(),
                                testTag = "btn_save_location"
                            )
                        }
                    }
                }

                3 -> {
                    // TAB 3: Kelola Banner Promosi
                    Text(
                        text = "Kelola Banner Promosi Sekolah 📢",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = GameDarkNavy
                    )
                    Text(
                        text = "Banner promosi tampil di halaman utama aplikasi peminjaman mobil.",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            OutlinedTextField(
                                value = bannerBadge,
                                onValueChange = { bannerBadge = it },
                                label = { Text("Label Badge") },
                                placeholder = { Text("Contoh: INFO OPERASIONAL / PENGUMUMAN") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = bannerTitle,
                                onValueChange = { bannerTitle = it },
                                label = { Text("Judul Banner") },
                                placeholder = { Text("Contoh: Armada Sekolah Rakyat Ceria!") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = bannerSubtitle,
                                onValueChange = { bannerSubtitle = it },
                                label = { Text("Pesan / Subtitle Banner") },
                                minLines = 3,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp)
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            GameButton(
                                text = "Perbarui Banner Promosi 📢",
                                onClick = {
                                    viewModel.updatePromoBanner(bannerTitle, bannerSubtitle, bannerBadge)
                                },
                                buttonColor = GameAmber,
                                modifier = Modifier.fillMaxWidth(),
                                testTag = "btn_save_banner"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminLoanApprovalCard(
    loan: LoanRequest,
    dateFormat: SimpleDateFormat,
    onApprove: (notes: String) -> Unit,
    onReject: (notes: String) -> Unit
) {
    var adminNotes by remember { mutableStateOf("Surat tugas lengkap, disetujui Admin Sekolah") }

    val vehicleEmoji = when {
        loan.vehicleName.contains("N-Max", ignoreCase = true) -> "🛵"
        loan.vehicleName.contains("Gran Max", ignoreCase = true) -> "🚐"
        else -> "🚗"
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        border = BorderStroke(2.dp, GameSkyBlue.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_loan_${loan.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Vehicle Name, Plate & Document ID
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(GameSkyBlue.copy(alpha = 0.15f))
                    ) {
                        Text(text = vehicleEmoji, fontSize = 22.sp)
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = loan.vehicleName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = GameDarkNavy
                        )
                        Text(
                            text = "Plat: ${loan.vehiclePlate} • Unit ID: ${loan.vehicleId}",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = GameAmber.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "⏳ Menunggu Admin",
                        color = GameAmber,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Document & User IDs Bar
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFF1F5F9),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "ID Dokumen: #${loan.id.take(10)}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = GameSkyBlue
                    )
                    Text(
                        text = "ID User: ${loan.userId}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.DarkGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Details Box
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF8FAFC),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "👤 Pemohon: ${loan.staffName}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GameDarkNavy
                    )
                    Text(
                        text = "📍 Lokasi Tujuan: ${loan.destination}",
                        fontSize = 11.sp,
                        color = Color.DarkGray
                    )
                    Text(
                        text = "📝 Keperluan: ${loan.purpose}",
                        fontSize = 11.sp,
                        color = Color.DarkGray
                    )
                    Text(
                        text = "⏰ Timestamp: ${dateFormat.format(Date(loan.requestTimeMillis))}",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Preset Quick Notes
            Text(
                text = "Preset Catatan Cepat:",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                PresetChip(title = "Kegiatan Sekolah ✅") {
                    adminNotes = "Disetujui untuk kegiatan operasional Sekolah Rakyat."
                }
                PresetChip(title = "Surat Tugas Siap 📄") {
                    adminNotes = "Surat tugas lengkap, armada dan bensin standby."
                }
                PresetChip(title = "Servis Unit 🔧") {
                    adminNotes = "Ditolak: Armada sedang dijadwalkan servis rutin."
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Catatan Admin
            OutlinedTextField(
                value = adminNotes,
                onValueChange = { adminNotes = it },
                label = { Text("Catatan Persetujuan Admin") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_admin_loan_notes_${loan.id}"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GameSkyBlue,
                    unfocusedBorderColor = Color(0xFFCBD5E1)
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons: Tolak (rejected) vs Setujui (approved)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GameButton(
                    text = "Tolak (Rejected) ❌",
                    onClick = { onReject(adminNotes) },
                    buttonColor = GameCoralRed,
                    height = 42.dp,
                    fontSize = 11.sp,
                    modifier = Modifier.weight(1f),
                    testTag = "btn_admin_reject_${loan.id}"
                )

                GameButton(
                    text = "Setujui (Approved) 🚀✅",
                    onClick = { onApprove(adminNotes) },
                    buttonColor = GameMintGreen,
                    height = 42.dp,
                    fontSize = 11.sp,
                    modifier = Modifier.weight(1.3f),
                    testTag = "btn_admin_approve_${loan.id}"
                )
            }
        }
    }
}

@Composable
private fun FilterPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) GameSkyBlue else Color.White,
        border = BorderStroke(1.dp, if (isSelected) GameSkyBlue else Color(0xFFCBD5E1)),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color.White else GameDarkNavy,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Composable
private fun ProcessedLoanCard(
    loan: LoanRequest,
    dateFormat: SimpleDateFormat
) {
    val isApproved = loan.status == LoanStatus.APPROVED_IN_TRIP || loan.status == LoanStatus.APPROVED
    val statusColor = if (isApproved) GameMintGreen else GameCoralRed
    val statusLabel = if (isApproved) "Disetujui (Approved) ✅" else "Ditolak (Rejected) ❌"

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = loan.vehicleName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = GameDarkNavy
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• ${loan.staffName}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
                Text(
                    text = "Tujuan: ${loan.destination} (${loan.purpose})",
                    fontSize = 11.sp,
                    color = Color.DarkGray
                )
                if (!loan.securityNotes.isNullOrBlank()) {
                    Text(
                        text = "Catatan: ${loan.securityNotes}",
                        fontSize = 10.sp,
                        color = Color.Gray,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = statusColor.copy(alpha = 0.15f)
            ) {
                Text(
                    text = statusLabel,
                    color = statusColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun AdminTabItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) GameSkyBlue else Color.Transparent,
        modifier = Modifier
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            fontSize = 10.5.sp,
            color = if (isSelected) Color.White else GameDarkNavy,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp)
        )
    }
}

@Composable
private fun PresetChip(
    title: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFF1F5F9),
        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = title,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = GameSkyBlue,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun PendingUserCard(
    user: User,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(1.5.dp, GameAmber.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(GameAmber.copy(alpha = 0.15f))
                ) {
                    Text(text = user.avatarEmoji, fontSize = 22.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user.fullName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = GameDarkNavy
                    )
                    Text(
                        text = "Role Diajukan: ${user.role.displayName}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GameAmber
                    )
                    Text(
                        text = "Username: @${user.username} • HP: ${user.phone}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GameButton(
                    text = "Tolak ❌",
                    onClick = onReject,
                    buttonColor = GameCoralRed,
                    height = 38.dp,
                    fontSize = 11.sp,
                    modifier = Modifier.weight(1f),
                    testTag = "reject_user_${user.id}"
                )

                GameButton(
                    text = "Setujui Akun ✅",
                    onClick = onApprove,
                    buttonColor = GameMintGreen,
                    height = 38.dp,
                    fontSize = 11.sp,
                    modifier = Modifier.weight(1f),
                    testTag = "approve_user_${user.id}"
                )
            }
        }
    }
}
