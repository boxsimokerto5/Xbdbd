package com.example.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.User
import com.example.data.model.Vehicle
import com.example.data.model.VehicleStatus
import com.example.ui.components.GameButton
import com.example.ui.theme.GameDarkNavy
import com.example.ui.theme.GameMintGreen
import com.example.ui.theme.GameSkyBlue
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.ScreenState

@Composable
fun LoanRequestScreen(
    viewModel: MainViewModel,
    currentUser: User,
    vehicles: List<Vehicle>,
    initialVehicle: Vehicle?,
    modifier: Modifier = Modifier
) {
    var selectedVehicle by remember {
        mutableStateOf(initialVehicle ?: vehicles.firstOrNull { it.status == VehicleStatus.GARASI } ?: vehicles.first())
    }

    var purpose by remember { mutableStateOf("") }
    var destination by remember { mutableStateOf("") }

    val commonPurposes = listOf(
        "Kunjungan Belajar Siswa 🎒",
        "Logistik & Sembako Bantuan 📦",
        "Antar Jemput Guru Tamu 👨‍🏫",
        "Operasional Perlombaan 🏆"
    )

    val commonDestinations = listOf(
        "Pusat Kota / Tunjungan",
        "Kantor Dinas Pendidikan",
        "Panti Asuhan Binaan",
        "Gedung Olahraga Pemuda"
    )

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
                        text = "Form Pengajuan Pinjam Mobil",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = GameDarkNavy
                    )
                    Text(
                        text = "Pemohon: ${currentUser.fullName} (${currentUser.role.displayName})",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Step 1: Select Vehicle
            Text(
                text = "1. Pilih Unit Kendaraan:",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = GameDarkNavy
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                vehicles.forEach { vehicle ->
                    val isSelected = selectedVehicle.id == vehicle.id
                    val isAvailable = vehicle.status == VehicleStatus.GARASI

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) GameSkyBlue.copy(alpha = 0.12f) else Color.White,
                        border = androidx.compose.foundation.BorderStroke(
                            if (isSelected) 2.dp else 1.dp,
                            if (isSelected) GameSkyBlue else Color(0xFFCBD5E1)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = isAvailable) { selectedVehicle = vehicle }
                            .testTag("select_vehicle_${vehicle.id}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE2E8F0))
                            ) {
                                Text(text = vehicle.emojiIcon, fontSize = 24.sp)
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = vehicle.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (isAvailable) GameDarkNavy else Color.Gray
                                )
                                Text(
                                    text = "${vehicle.licensePlate} • Bensin: ${vehicle.fuelPercent}%",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isAvailable) GameMintGreen else Color.LightGray
                            ) {
                                Text(
                                    text = if (isAvailable) "Tersedia 🏡" else "Dipakai 🚀",
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

            Spacer(modifier = Modifier.height(18.dp))

            // Step 2: Keperluan Perjalanan
            Text(
                text = "2. Keperluan / Tujuan Perjalanan:",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = GameDarkNavy
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = purpose,
                onValueChange = { purpose = it },
                label = { Text("Tuliskan Keperluan") },
                placeholder = { Text("Contoh: Mengantar buku dan perlengkapan kelas") },
                leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_purpose"),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GameSkyBlue,
                    unfocusedBorderColor = Color(0xFFCBD5E1)
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Quick purpose tags
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                commonPurposes.take(2).forEach { p ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                        modifier = Modifier.clickable { purpose = p }
                    ) {
                        Text(
                            text = p,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = GameDarkNavy,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Step 3: Lokasi Tujuan
            Text(
                text = "3. Lokasi Tujuan:",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = GameDarkNavy
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = destination,
                onValueChange = { destination = it },
                label = { Text("Alamat / Lokasi Tujuan") },
                placeholder = { Text("Contoh: Panti Asuhan Tunjungan") },
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_destination"),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GameSkyBlue,
                    unfocusedBorderColor = Color(0xFFCBD5E1)
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Quick destination tags
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                commonDestinations.take(2).forEach { dest ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                        modifier = Modifier.clickable { destination = dest }
                    ) {
                        Text(
                            text = dest,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = GameDarkNavy,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Submit Button
            GameButton(
                text = "Kirim ke Pos Keamanan Pintu 🚪",
                onClick = {
                    viewModel.submitLoan(
                        vehicleId = selectedVehicle.id,
                        purpose = purpose.ifBlank { "Kegiatan Operasional Sekolah Rakyat" },
                        destination = destination.ifBlank { "Tujuan Kota" }
                    )
                },
                buttonColor = GameSkyBlue,
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedVehicle.status == VehicleStatus.GARASI && purpose.isNotBlank(),
                testTag = "btn_submit_loan"
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Setelah dikirim, notifikasi akan masuk ke Keamanan untuk verifikasi buka gerbang.",
                fontSize = 11.sp,
                color = Color.Gray
            )
        }
    }
}
