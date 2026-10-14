package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LoanRequest
import com.example.data.model.LoanStatus
import com.example.data.model.User
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
fun SecurityVerificationScreen(
    viewModel: MainViewModel,
    currentUser: User,
    loans: List<LoanRequest>,
    modifier: Modifier = Modifier
) {
    val pendingLoans = loans.filter { it.status == LoanStatus.PENDING_KEAMANAN }
    val activeLoans = loans.filter { it.status == LoanStatus.APPROVED_IN_TRIP }
    val dateFormat = remember { SimpleDateFormat("HH:mm, dd MMM", Locale("id", "ID")) }

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
                        text = "Pos Gerbang: Verifikasi Pintu Keluar 👮‍♂️",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = GameDarkNavy
                    )
                    Text(
                        text = "Petugas: ${currentUser.fullName} (${currentUser.role.displayName})",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header: Pending requests
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Menunggu Buka Gerbang (${pendingLoans.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = GameDarkNavy
                    )
                    if (pendingLoans.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = GameCoralRed
                        ) {
                            Text(
                                text = "NOTIF MASUK 🔔",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            if (pendingLoans.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            Text(text = "🛡️", fontSize = 38.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Gerbang Aman & Terkendali",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = GameDarkNavy
                            )
                            Text(
                                text = "Tidak ada mobil yang sedang menunggu izin keluar saat ini.",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            } else {
                items(pendingLoans, key = { it.id }) { loan ->
                    SecurityPendingLoanItem(
                        loan = loan,
                        dateFormat = dateFormat,
                        onVerify = { approve, notes ->
                            viewModel.verifyLoanBySecurity(loan.id, approve, notes)
                        }
                    )
                }
            }

            // Section: Currently Out in Trip
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Unit Sedang di Luar Gerbang (${activeLoans.size}) 🚀",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = GameDarkNavy
                )
            }

            if (activeLoans.isEmpty()) {
                item {
                    Text(
                        text = "Semua mobil saat ini berada di garasi sekolah.",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            } else {
                items(activeLoans, key = { it.id }) { loan ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White,
                        shadowElevation = 2.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(GameSkyBlue.copy(alpha = 0.15f))
                            ) {
                                Text(text = "🚗", fontSize = 20.sp)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${loan.vehicleName} (${loan.vehiclePlate})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = GameDarkNavy
                                )
                                Text(
                                    text = "Peminjam: ${loan.staffName} • Tujuan: ${loan.destination}",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = GameSkyBlue,
                                modifier = Modifier.clickable { viewModel.openTrackingMap(loan) }
                            ) {
                                Text(
                                    text = "Radar ➔",
                                    color = Color.White,
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
    }
}

@Composable
private fun SecurityPendingLoanItem(
    loan: LoanRequest,
    dateFormat: SimpleDateFormat,
    onVerify: (approve: Boolean, notes: String) -> Unit
) {
    var securityNotes by remember { mutableStateOf("STNK & kondisi fisik unit lengkap. Izin gerbang keluar diberikan.") }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        border = androidx.compose.foundation.BorderStroke(2.dp, GameAmber),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("security_loan_${loan.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Vehicle Name & Plate
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = loan.vehicleName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = GameDarkNavy
                    )
                    Text(
                        text = "Plat: ${loan.vehiclePlate}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = GameAmber.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "Pintu Gerbang 🚪",
                        color = GameAmber,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Details Box
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFFFFBEB),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "👤 Pemohon: ${loan.staffName}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF92400E)
                    )
                    Text(
                        text = "📍 Tujuan: ${loan.destination}",
                        fontSize = 11.sp,
                        color = Color(0xFFB45309)
                    )
                    Text(
                        text = "📝 Keperluan: ${loan.purpose}",
                        fontSize = 11.sp,
                        color = Color(0xFFB45309)
                    )
                    Text(
                        text = "⏰ Jam Pengajuan: ${dateFormat.format(Date(loan.requestTimeMillis))}",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Catatan Keamanan
            OutlinedTextField(
                value = securityNotes,
                onValueChange = { securityNotes = it },
                label = { Text("Catatan Pemeriksaan Gerbang") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_security_notes"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GameSkyBlue,
                    unfocusedBorderColor = Color(0xFFCBD5E1)
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons: Tolak vs Setujui & Buka Gerbang
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GameButton(
                    text = "Tolak Peminjaman ❌",
                    onClick = { onVerify(false, securityNotes.ifBlank { "Ditolak petugas keamanan" }) },
                    buttonColor = GameCoralRed,
                    height = 44.dp,
                    fontSize = 12.sp,
                    modifier = Modifier.weight(1f),
                    testTag = "btn_reject_loan_${loan.id}"
                )

                GameButton(
                    text = "Setujui: Buka Gerbang 🚪🟢",
                    onClick = { onVerify(true, securityNotes) },
                    buttonColor = GameMintGreen,
                    height = 44.dp,
                    fontSize = 12.sp,
                    modifier = Modifier.weight(1.3f),
                    testTag = "btn_approve_loan_${loan.id}"
                )
            }
        }
    }
}
