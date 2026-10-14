package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LoanRequest
import com.example.data.model.LoanStatus
import com.example.data.model.SchoolLocation
import com.example.data.model.User
import com.example.data.model.Vehicle
import com.example.ui.components.GameButton
import com.example.ui.theme.GameAmber
import com.example.ui.theme.GameCoralRed
import com.example.ui.theme.GameDarkNavy
import com.example.ui.theme.GameMintGreen
import com.example.ui.theme.GameSkyBlue
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.ScreenState
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun TrackingMapScreen(
    viewModel: MainViewModel,
    currentUser: User,
    schoolLocation: SchoolLocation,
    selectedLoan: LoanRequest?,
    vehicles: List<Vehicle>,
    routeProgress: Float,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val currentVehicle = vehicles.find { it.id == selectedLoan?.vehicleId }

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

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Pantau Perjalanan Armada 🛰️",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = GameDarkNavy
                    )
                    Text(
                        text = "Basis Titik: ${schoolLocation.name}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (selectedLoan != null && selectedLoan.status == LoanStatus.APPROVED_IN_TRIP)
                        GameMintGreen.copy(alpha = 0.15f)
                    else Color(0xFFE2E8F0)
                ) {
                    Text(
                        text = if (selectedLoan != null && selectedLoan.status == LoanStatus.APPROVED_IN_TRIP)
                            "GPS AKTIF 🟢" else "STANDBY ⏸️",
                        color = if (selectedLoan != null && selectedLoan.status == LoanStatus.APPROVED_IN_TRIP)
                            GameMintGreen else Color.Gray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Active Trip Info Header Card
        if (selectedLoan != null && selectedLoan.status == LoanStatus.APPROVED_IN_TRIP) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(12.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(GameSkyBlue.copy(alpha = 0.15f))
                    ) {
                        Text(text = currentVehicle?.emojiIcon ?: "🚗", fontSize = 24.sp)
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = selectedLoan.vehicleName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = GameDarkNavy
                        )
                        Text(
                            text = "Driver: ${selectedLoan.staffName} • Menuju: ${selectedLoan.destination}",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "38 km/jam",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = GameSkyBlue
                        )
                        Text(
                            text = "Bergerak 🚀",
                            fontSize = 10.sp,
                            color = GameMintGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Dynamic Interactive Radar / Map Canvas
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)), // Night/Radar game look
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("map_canvas")
                ) {
                    val width = size.width
                    val height = size.height

                    // Draw subtle grid lines (game map style)
                    val gridColor = Color(0xFF1E293B)
                    val stepX = width / 8
                    val stepY = height / 10

                    for (i in 0..8) {
                        drawLine(
                            color = gridColor,
                            start = Offset(i * stepX, 0f),
                            end = Offset(i * stepX, height),
                            strokeWidth = 1f
                        )
                    }
                    for (j in 0..10) {
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, j * stepY),
                            end = Offset(width, j * stepY),
                            strokeWidth = 1f
                        )
                    }

                    // Start Base Point (Sekolah Rakyat)
                    val startX = width * 0.22f
                    val startY = height * 0.75f

                    // Mid Waypoint
                    val midX = width * 0.65f
                    val midY = height * 0.50f

                    // Destination Point
                    val destX = width * 0.78f
                    val destY = height * 0.22f

                    // Draw curved route path
                    val routePath = Path().apply {
                        moveTo(startX, startY)
                        quadraticTo(midX, height * 0.85f, width * 0.5f, midY)
                        quadraticTo(width * 0.35f, height * 0.3f, destX, destY)
                    }

                    // Background track
                    drawPath(
                        path = routePath,
                        color = Color(0xFF334155),
                        style = Stroke(
                            width = 8f,
                            cap = StrokeCap.Round
                        )
                    )

                    // Active animated glow line
                    drawPath(
                        path = routePath,
                        color = Color(0xFF38BDF8),
                        style = Stroke(
                            width = 4f,
                            cap = StrokeCap.Round,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 15f), routeProgress * 40f)
                        )
                    )

                    // Return path (dotted arc back to Sekolah Rakyat)
                    val returnPath = Path().apply {
                        moveTo(destX, destY)
                        cubicTo(width * 0.9f, height * 0.45f, width * 0.45f, height * 0.95f, startX, startY)
                    }
                    drawPath(
                        path = returnPath,
                        color = Color(0xFFF59E0B).copy(alpha = 0.5f),
                        style = Stroke(
                            width = 3f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
                        )
                    )

                    // Pulsing radar signal at Base (Sekolah Rakyat)
                    drawCircle(
                        color = Color(0xFF3B82F6).copy(alpha = 0.25f),
                        radius = 35f * pulseScale,
                        center = Offset(startX, startY)
                    )
                    drawCircle(
                        color = Color(0xFF3B82F6),
                        radius = 14f,
                        center = Offset(startX, startY)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 6f,
                        center = Offset(startX, startY)
                    )

                    // Destination marker
                    drawCircle(
                        color = Color(0xFF10B981).copy(alpha = 0.3f),
                        radius = 28f * pulseScale,
                        center = Offset(destX, destY)
                    )
                    drawCircle(
                        color = Color(0xFF10B981),
                        radius = 12f,
                        center = Offset(destX, destY)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 5f,
                        center = Offset(destX, destY)
                    )

                    // Interpolate current vehicle position along the curve
                    val t = routeProgress.coerceIn(0.05f, 0.95f)
                    val carX = (1 - t) * (1 - t) * startX + 2 * (1 - t) * t * width * 0.5f + t * t * destX
                    val carY = (1 - t) * (1 - t) * startY + 2 * (1 - t) * t * midY + t * t * destY

                    // Vehicle glow aura
                    drawCircle(
                        color = Color(0xFFFBBF24).copy(alpha = 0.35f),
                        radius = 22f * pulseScale,
                        center = Offset(carX, carY)
                    )
                    drawCircle(
                        color = Color(0xFFFBBF24),
                        radius = 12f,
                        center = Offset(carX, carY)
                    )
                }

                // Overlay labels on top of Canvas
                // Start Base Label
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.75f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GameSkyBlue),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 24.dp, bottom = 40.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                        Text(
                            text = "🏫 ${schoolLocation.name}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "Titik Awal & Garasi",
                            color = GameSkyBlue,
                            fontSize = 9.sp
                        )
                    }
                }

                // Destination Label
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.75f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GameMintGreen),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 24.dp, top = 30.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                        Text(
                            text = "🏁 ${selectedLoan?.destination ?: "Lokasi Tujuan"}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "Target Kedatangan",
                            color = GameMintGreen,
                            fontSize = 9.sp
                        )
                    }
                }

                // Map Legend / GPS Indicator
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF1E293B).copy(alpha = 0.85f),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(text = "🛰️ Radar Satelit", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Bottom Controls: Action to return vehicle back to Sekolah Rakyat
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                if (selectedLoan != null && selectedLoan.status == LoanStatus.APPROVED_IN_TRIP) {
                    Text(
                        text = "Kontrol Selesai Perjalanan:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = GameDarkNavy
                    )
                    Text(
                        text = "Klik tombol di bawah jika unit telah kembali sampai di titik Sekolah Rakyat.",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    GameButton(
                        text = "Unit Tiba: Kembalikan ke Garasi 🏡",
                        onClick = { viewModel.returnVehicle(selectedLoan.id) },
                        buttonColor = GameMintGreen,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "btn_return_vehicle"
                    )
                } else {
                    Text(
                        text = "Status Armada: Siap di Garasi 🏡",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = GameDarkNavy
                    )
                    Text(
                        text = "Belum ada unit yang sedang dalam rute keluar. Ajukan pinjam mobil untuk memulai live tracking.",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    GameButton(
                        text = "Lihat Riwayat Perjalanan 📜",
                        onClick = { viewModel.openTripHistory() },
                        buttonColor = GameSkyBlue,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
