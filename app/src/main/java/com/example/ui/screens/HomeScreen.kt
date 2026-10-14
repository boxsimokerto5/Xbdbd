package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.LoanRequest
import com.example.data.model.LoanStatus
import com.example.data.model.PromoBanner
import com.example.data.model.SchoolLocation
import com.example.data.model.User
import com.example.data.model.UserRole
import com.example.data.model.Vehicle
import com.example.data.model.VehicleStatus
import com.example.ui.components.CheerfulActionCard
import com.example.ui.components.GameActionCard
import com.example.ui.components.GameButton
import com.example.ui.components.VehicleItemCard
import com.example.ui.theme.GameAmber
import com.example.ui.theme.GameCoralRed
import com.example.ui.theme.GameDarkNavy
import com.example.ui.theme.GameMintGreen
import com.example.ui.theme.GamePurple
import com.example.ui.theme.GameSkyBlue
import com.example.ui.viewmodel.MainViewModel

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    currentUser: User,
    vehicles: List<Vehicle>,
    loans: List<LoanRequest>,
    promoBanner: PromoBanner,
    schoolLocation: SchoolLocation,
    modifier: Modifier = Modifier
) {
    // Count of pending loans waiting for security
    val pendingSecurityCount = loans.count { it.status == LoanStatus.PENDING_KEAMANAN }
    val activeTripCount = loans.count { it.status == LoanStatus.APPROVED_IN_TRIP }

    // Count in garage vs in use
    val inGarageCount = vehicles.count { it.status == VehicleStatus.GARASI }
    val inUseCount = vehicles.count { it.status == VehicleStatus.SEDANG_DIPAKAI }

    var vehicleFilter by remember { mutableStateOf<VehicleStatus?>(null) }
    val filteredVehicles = if (vehicleFilter == null) {
        vehicles
    } else {
        vehicles.filter { it.status == vehicleFilter }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F9)),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // TOP BAR: Akun, Nama, dan Judul Aplikasi
        item {
            TopHeaderSection(
                currentUser = currentUser,
                onLogout = { viewModel.logout() },
                onSwitchRole = {
                    viewModel.navigateTo(com.example.ui.viewmodel.ScreenState.AUTH)
                }
            )
        }

        // ROLE SPECIFIC HIGHLIGHTS
        if (currentUser.role == UserRole.KEAMANAN && pendingSecurityCount > 0) {
            item {
                SecurityAlertBanner(
                    pendingCount = pendingSecurityCount,
                    onOpenVerification = { viewModel.openSecurityVerification() }
                )
            }
        }

        if (currentUser.role == UserRole.ADMIN) {
            item {
                val firestorePendingLoans by viewModel.firestorePendingLoans.collectAsStateWithLifecycle()
                val totalPending = (firestorePendingLoans + loans.filter { it.status == LoanStatus.PENDING_KEAMANAN })
                    .distinctBy { it.id }.size
                AdminQuickBar(
                    onOpenAdminPanel = { viewModel.openAdminPanel() },
                    schoolLocation = schoolLocation,
                    pendingLoansCount = totalPending
                )
            }
        }

        // BANNER PROMOSI YANG DIBUAT OLEH ADMIN
        item {
            PromoBannerSection(
                promoBanner = promoBanner,
                isAdmin = currentUser.role == UserRole.ADMIN,
                onEditBanner = { viewModel.openAdminPanel() }
            )
        }

        // 3 KOTAK AKSI UTAMA (PINJAM MOBIL, PANTAU MOBIL, RIWAYAT PERJALANAN)
        item {
            QuickActionsThreeBoxes(
                onPinjamClick = {
                    val availableVehicle = vehicles.firstOrNull { it.status == VehicleStatus.GARASI }
                    if (availableVehicle != null) {
                        viewModel.selectVehicleForLoan(availableVehicle)
                    } else {
                        // All vehicles in use
                        viewModel.selectVehicleForLoan(vehicles.first())
                    }
                },
                onPantauClick = {
                    val activeLoan = loans.firstOrNull { it.status == LoanStatus.APPROVED_IN_TRIP }
                    viewModel.openTrackingMap(activeLoan)
                },
                onRiwayatClick = {
                    viewModel.openTripHistory()
                },
                availableVehiclesCount = inGarageCount,
                activeTripsCount = activeTripCount,
                totalHistoryCount = loans.size
            )
        }

        // SECTION: STATUS ARMADA MOBIL (SEDANG DIGARASI MAUPUN SEDANG DIPAKAI)
        item {
            FleetStatusHeader(
                totalCount = vehicles.size,
                inGarageCount = inGarageCount,
                inUseCount = inUseCount,
                selectedFilter = vehicleFilter,
                onSelectFilter = { vehicleFilter = it }
            )
        }

        // 3 UNIT KENDARAAN (N-MAX, GRAN MAX, AVANZA VELOZ)
        items(filteredVehicles, key = { it.id }) { vehicle ->
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                VehicleItemCard(
                    vehicle = vehicle,
                    onPinjamClick = if (currentUser.role == UserRole.STAFF) {
                        { viewModel.selectVehicleForLoan(vehicle) }
                    } else null
                )
            }
        }
    }
}

@Composable
private fun TopHeaderSection(
    currentUser: User,
    onLogout: () -> Unit,
    onSwitchRole: () -> Unit
) {
    Surface(
        color = Color.White,
        shadowElevation = 3.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Title and App Name
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "SEKOLAH RAKYAT",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = GameSkyBlue,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "🚗", fontSize = 18.sp)
                    }
                    Text(
                        text = "Peminjaman Mobil & Keamanan",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Switch Role / Logout button
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF1F5F9),
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                        modifier = Modifier.clickable(onClick = onSwitchRole)
                    ) {
                        Text(
                            text = "Ganti Role 🔄",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GameDarkNavy,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Keluar Akun",
                            tint = GameCoralRed
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // User Profile Row (Akun & Nama)
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = when (currentUser.role) {
                    UserRole.ADMIN -> GameSkyBlue.copy(alpha = 0.1f)
                    UserRole.STAFF -> GameAmber.copy(alpha = 0.12f)
                    UserRole.KEAMANAN -> GameMintGreen.copy(alpha = 0.15f)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    ) {
                        Text(text = currentUser.avatarEmoji, fontSize = 20.sp)
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentUser.fullName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = GameDarkNavy
                        )
                        Text(
                            text = "@${currentUser.username} • Telp: ${currentUser.phone}",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }

                    // Role Badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when (currentUser.role) {
                            UserRole.ADMIN -> GameSkyBlue
                            UserRole.STAFF -> GameAmber
                            UserRole.KEAMANAN -> GameMintGreen
                        }
                    ) {
                        Text(
                            text = "${currentUser.role.badgeEmoji} ${currentUser.role.displayName}",
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

@Composable
private fun PromoBannerSection(
    promoBanner: PromoBanner,
    isAdmin: Boolean,
    onEditBanner: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column {
            // Illustrated Promo Image Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_promo_banner),
                    contentDescription = "Banner Promosi Sekolah Rakyat",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Dark gradient overlay for text readability
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.65f))
                            )
                        )
                )

                // Badge top left
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = GameSunYellow,
                    modifier = Modifier
                        .padding(10.dp)
                        .align(Alignment.TopStart)
                ) {
                    Text(
                        text = "📢 ${promoBanner.badge}",
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp,
                        color = Color(0xFF78350F),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                if (isAdmin) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier
                            .padding(10.dp)
                            .align(Alignment.TopEnd)
                            .clickable(onClick = onEditBanner)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Banner",
                                tint = GameDarkNavy,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Edit Banner",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = GameDarkNavy
                            )
                        }
                    }
                }

                // Banner title overlay at bottom of image
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                ) {
                    Text(
                        text = promoBanner.title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }

            // Subtitle text underneath
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = promoBanner.subtitle,
                    fontSize = 12.sp,
                    color = Color.DarkGray,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

val GameSunYellow = Color(0xFFFFB703)

@Composable
private fun QuickActionsThreeBoxes(
    onPinjamClick: () -> Unit,
    onPantauClick: () -> Unit,
    onRiwayatClick: () -> Unit,
    availableVehiclesCount: Int,
    activeTripsCount: Int,
    totalHistoryCount: Int
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Menu Cepat Armada ⚡",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = GameDarkNavy
            )
            Text(
                text = "Aksi Interaktif",
                fontSize = 11.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Row of 3 Cheerful Action Cards side by side
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Kotak Pinjam Mobil
            CheerfulActionCard(
                title = "Pinjam Mobil",
                subtitle = "Ajukan Unit",
                primaryIcon = Icons.Default.DirectionsCar,
                secondaryEmoji = "🔑",
                gradientColors = listOf(Color(0xFF2563EB), Color(0xFF60A5FA)),
                accentColor = GameSkyBlue,
                onClick = onPinjamClick,
                badgeText = if (availableVehiclesCount > 0) "$availableVehiclesCount Siap" else "Penuh",
                badgeColor = if (availableVehiclesCount > 0) GameMintGreen else Color.Gray,
                modifier = Modifier.weight(1f),
                testTag = "card_action_pinjam"
            )

            // Kotak Pantau Mobil
            CheerfulActionCard(
                title = "Pantau Mobil",
                subtitle = "Radar Map",
                primaryIcon = Icons.Default.Explore,
                secondaryEmoji = "🛰️",
                gradientColors = listOf(Color(0xFF059669), Color(0xFF34D399)),
                accentColor = GameMintGreen,
                onClick = onPantauClick,
                badgeText = if (activeTripsCount > 0) "$activeTripsCount Jalan" else "Standby",
                badgeColor = if (activeTripsCount > 0) GameCoralRed else Color(0xFF64748B),
                modifier = Modifier.weight(1f),
                testTag = "card_action_pantau"
            )

            // Kotak Riwayat Perjalanan
            CheerfulActionCard(
                title = "Riwayat",
                subtitle = "Log & Status",
                primaryIcon = Icons.Default.ReceiptLong,
                secondaryEmoji = "⭐",
                gradientColors = listOf(Color(0xFFD97706), Color(0xFFFBBF24)),
                accentColor = GameAmber,
                onClick = onRiwayatClick,
                badgeText = "$totalHistoryCount Trip",
                badgeColor = GameAmber,
                modifier = Modifier.weight(1f),
                testTag = "card_action_riwayat"
            )
        }
    }
}

@Composable
private fun FleetStatusHeader(
    totalCount: Int,
    inGarageCount: Int,
    inUseCount: Int,
    selectedFilter: VehicleStatus?,
    onSelectFilter: (VehicleStatus?) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 22.dp, bottom = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Status Armada Kendaraan ($totalCount)",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = GameDarkNavy
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Filter chips: Semua, Di Garasi, Sedang Dipakai
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                label = "Semua ($totalCount)",
                isSelected = selectedFilter == null,
                color = GameSkyBlue,
                onClick = { onSelectFilter(null) }
            )

            FilterChip(
                label = "Di Garasi ($inGarageCount) 🏡",
                isSelected = selectedFilter == VehicleStatus.GARASI,
                color = GameMintGreen,
                onClick = { onSelectFilter(VehicleStatus.GARASI) }
            )

            FilterChip(
                label = "Sedang Dipakai ($inUseCount) 🚀",
                isSelected = selectedFilter == VehicleStatus.SEDANG_DIPAKAI,
                color = GameAmber,
                onClick = { onSelectFilter(VehicleStatus.SEDANG_DIPAKAI) }
            )
        }
    }
}

@Composable
private fun FilterChip(
    label: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) color else Color.White,
        border = BorderStroke(1.dp, if (isSelected) color else Color(0xFFCBD5E1)),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color.White else GameDarkNavy,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun SecurityAlertBanner(
    pendingCount: Int,
    onOpenVerification: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
        border = BorderStroke(1.5.dp, GameAmber),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onOpenVerification)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(14.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(GameAmber.copy(alpha = 0.2f))
            ) {
                Text(text = "🚨", fontSize = 22.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Perhatian Keamanan Pintu!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFF92400E)
                )
                Text(
                    text = "Ada $pendingCount permohonan pinjam mobil menunggu verifikasi keluar.",
                    fontSize = 11.sp,
                    color = Color(0xFFB45309)
                )
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = GameAmber
            ) {
                Text(
                    text = "Periksa ➔",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun AdminQuickBar(
    onOpenAdminPanel: () -> Unit,
    schoolLocation: SchoolLocation,
    pendingLoansCount: Int = 0
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
        border = BorderStroke(1.dp, GameSkyBlue.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onOpenAdminPanel)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(GameSkyBlue.copy(alpha = 0.15f))
            ) {
                Text(text = "👑", fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Panel Kontrol Admin Sekolah",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = GameSkyBlue
                    )
                    if (pendingLoansCount > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = CircleShape,
                            color = GameAmber
                        ) {
                            Text(
                                text = "$pendingLoansCount",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    text = if (pendingLoansCount > 0) "Ada $pendingLoansCount permohonan pinjam mobil menunggu persetujuan!" else "Lokasi Titik: ${schoolLocation.name}",
                    fontSize = 10.sp,
                    color = if (pendingLoansCount > 0) Color(0xFFB45309) else Color.Gray,
                    fontWeight = if (pendingLoansCount > 0) FontWeight.SemiBold else FontWeight.Normal
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = GameSkyBlue
            ) {
                Text(
                    text = "Buka Panel ⚙️",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
