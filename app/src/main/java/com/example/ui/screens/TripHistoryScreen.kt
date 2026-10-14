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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
fun TripHistoryScreen(
    viewModel: MainViewModel,
    currentUser: User,
    loans: List<LoanRequest>,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf<LoanStatus?>(null) }

    val filteredLoans = if (selectedFilter == null) {
        loans
    } else {
        loans.filter { it.status == selectedFilter }
    }

    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID")) }

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
                        text = "Riwayat Perjalanan Armada 📜",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = GameDarkNavy
                    )
                    Text(
                        text = "Total ${loans.size} catatan peminjaman unit",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }
        }

        // Filter chips
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            FilterBadge(
                label = "Semua (${loans.size})",
                isSelected = selectedFilter == null,
                color = GameSkyBlue,
                onClick = { selectedFilter = null }
            )

            FilterBadge(
                label = "Aktif",
                isSelected = selectedFilter == LoanStatus.APPROVED_IN_TRIP,
                color = GameSkyBlue,
                onClick = { selectedFilter = LoanStatus.APPROVED_IN_TRIP }
            )

            FilterBadge(
                label = "Selesai 🏡",
                isSelected = selectedFilter == LoanStatus.RETURNED_COMPLETED,
                color = GameMintGreen,
                onClick = { selectedFilter = LoanStatus.RETURNED_COMPLETED }
            )

            FilterBadge(
                label = "Menunggu ⏳",
                isSelected = selectedFilter == LoanStatus.PENDING_KEAMANAN,
                color = GameAmber,
                onClick = { selectedFilter = LoanStatus.PENDING_KEAMANAN }
            )
        }

        if (filteredLoans.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "📜", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Belum Ada Catatan Riwayat",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = GameDarkNavy
                    )
                    Text(
                        text = "Catatan perjalanan akan otomatis tersimpan di sini saat peminjaman disetujui atau selesai.",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredLoans, key = { it.id }) { loan ->
                    TripItemCard(
                        loan = loan,
                        dateFormat = dateFormat,
                        onTrackClick = if (loan.status == LoanStatus.APPROVED_IN_TRIP) {
                            { viewModel.openTrackingMap(loan) }
                        } else null
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterBadge(
    label: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) color else Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) color else Color(0xFFCBD5E1)),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color.White else GameDarkNavy,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
        )
    }
}

@Composable
private fun TripItemCard(
    loan: LoanRequest,
    dateFormat: SimpleDateFormat,
    onTrackClick: (() -> Unit)?
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("trip_item_${loan.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Vehicle name & Plate
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = loan.vehicleName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = GameDarkNavy
                    )
                    Text(
                        text = "Plat: ${loan.vehiclePlate}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }

                // Status chip
                val (badgeBg, badgeText, badgeColor) = when (loan.status) {
                    LoanStatus.PENDING, LoanStatus.PENDING_KEAMANAN -> Triple(
                        GameAmber.copy(alpha = 0.15f),
                        "Menunggu Persetujuan ⏳",
                        GameAmber
                    )
                    LoanStatus.APPROVED, LoanStatus.APPROVED_IN_TRIP -> Triple(
                        GameSkyBlue.copy(alpha = 0.15f),
                        "Dalam Perjalanan 🚀",
                        GameSkyBlue
                    )
                    LoanStatus.REJECTED, LoanStatus.REJECTED_BY_KEAMANAN -> Triple(
                        GameCoralRed.copy(alpha = 0.15f),
                        "Ditolak ❌",
                        GameCoralRed
                    )
                    LoanStatus.RETURNED_COMPLETED -> Triple(
                        GameMintGreen.copy(alpha = 0.15f),
                        "Kembali di Garasi 🏡",
                        GameMintGreen
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = badgeBg
                ) {
                    Text(
                        text = badgeText,
                        color = badgeColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Details
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF8FAFC),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "👤 Peminjam: ${loan.staffName}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GameDarkNavy
                    )
                    Text(
                        text = "📍 Tujuan: ${loan.destination}",
                        fontSize = 11.sp,
                        color = Color.DarkGray
                    )
                    Text(
                        text = "📝 Keperluan: ${loan.purpose}",
                        fontSize = 11.sp,
                        color = Color.DarkGray
                    )
                    if (loan.verifiedBySecurityName != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "🛡️ Diverifikasi Pos: ${loan.verifiedBySecurityName}",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                    if (loan.securityNotes != null) {
                        Text(
                            text = "Catatan Gerbang: \"${loan.securityNotes}\"",
                            fontSize = 10.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            color = Color.Gray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Waktu: ${dateFormat.format(Date(loan.requestTimeMillis))}",
                    fontSize = 10.sp,
                    color = Color.Gray
                )

                if (onTrackClick != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = GameSkyBlue,
                        modifier = Modifier.clickable(onClick = onTrackClick)
                    ) {
                        Text(
                            text = "Buka Radar Map ➔",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
