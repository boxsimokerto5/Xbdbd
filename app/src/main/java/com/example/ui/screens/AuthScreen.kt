package com.example.ui.screens

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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

@Composable
fun AuthScreen(
    viewModel: MainViewModel,
    users: List<User>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity

    val firebaseUser by viewModel.firebaseUser.collectAsStateWithLifecycle()
    val userProfile by viewModel.firebaseUserProfile.collectAsStateWithLifecycle()
    val isRegisteringRole by viewModel.isRegisteringNewRole.collectAsStateWithLifecycle()

    var selectedRole by remember { mutableStateOf(UserRole.STAFF) }
    var fullNameInput by remember(firebaseUser) {
        mutableStateOf(firebaseUser?.displayName ?: "")
    }
    var phoneInput by remember { mutableStateOf("") }
    var isSigningInGoogle by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F9))
            .padding(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Cheerful Game Mascot & App Branding
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(86.dp)
                    .clip(CircleShape)
                    .background(GameSkyBlue)
            ) {
                Text(text = "🚗", fontSize = 44.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "SEKOLAH RAKYAT",
                fontWeight = FontWeight.Black,
                fontSize = 22.sp,
                color = GameSkyBlue,
                letterSpacing = 1.sp
            )

            Text(
                text = "Autentikasi Aman & Navigasi Peran 🛡️",
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(18.dp))

            // STATE 1: PENDING APPROVAL SCREEN
            if (firebaseUser != null && userProfile != null && userProfile?.status == UserApprovalStatus.PENDING) {
                PendingApprovalCard(
                    user = userProfile!!,
                    onCheckStatus = {
                        viewModel.firebaseAuthRepo.loadUserProfile(firebaseUser!!.uid)
                    },
                    onSignOut = {
                        viewModel.signOutFirebase()
                    }
                )
            }
            // STATE 2: NEW GOOGLE USER MUST PICK ROLE (REGISTRATION)
            else if (firebaseUser != null && isRegisteringRole) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Lengkapi Pendaftaran Peran",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = GameDarkNavy
                        )
                        Text(
                            text = "Akun Google: ${firebaseUser?.email ?: ""}",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Pilih Peran Anda di Sekolah Rakyat:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = GameDarkNavy,
                            modifier = Modifier.align(Alignment.Start)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // 3 Role Options: Admin, Staff, Keamanan
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(UserRole.STAFF, UserRole.KEAMANAN, UserRole.ADMIN).forEach { role ->
                                val isSelected = selectedRole == role
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isSelected) GameSkyBlue.copy(alpha = 0.12f) else Color(0xFFF8FAFC),
                                    border = BorderStroke(
                                        if (isSelected) 2.dp else 1.dp,
                                        if (isSelected) GameSkyBlue else Color(0xFFCBD5E1)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedRole = role }
                                        .testTag("role_option_${role.name}")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(12.dp)
                                    ) {
                                        Text(text = role.badgeEmoji, fontSize = 24.sp)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = role.displayName,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = if (isSelected) GameSkyBlue else GameDarkNavy
                                            )
                                            Text(
                                                text = when (role) {
                                                    UserRole.ADMIN -> "Persetujuan akun, koordinat sekolah & banner"
                                                    UserRole.STAFF -> "Mengajukan pinjam mobil (N-Max, Gran Max, Avanza)"
                                                    UserRole.KEAMANAN -> "Verifikasi buka gerbang pintu keluar/masuk"
                                                },
                                                fontSize = 10.sp,
                                                color = Color.Gray
                                            )
                                        }
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = GameSkyBlue,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = fullNameInput,
                            onValueChange = { fullNameInput = it },
                            label = { Text("Nama Lengkap") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = phoneInput,
                            onValueChange = { phoneInput = it },
                            label = { Text("Nomor WhatsApp / HP") },
                            placeholder = { Text("08123456789") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        GameButton(
                            text = "Simpan & Daftarkan Profil 📋",
                            onClick = {
                                viewModel.registerFirebaseRole(
                                    role = selectedRole,
                                    fullName = fullNameInput.ifBlank { firebaseUser?.displayName ?: "Pengguna" },
                                    phone = phoneInput.ifBlank { "0812345678" }
                                )
                            },
                            buttonColor = GameMintGreen,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = fullNameInput.isNotBlank(),
                            testTag = "btn_submit_firebase_role"
                        )
                    }
                }
            }
            // STATE 3: LOGIN SCREEN (GOOGLE SIGN-IN)
            else {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Masuk ke Sekolah Rakyat",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = GameDarkNavy
                        )
                        Text(
                            text = "Gunakan Akun Google resmi Anda yang terdaftar",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Google Sign-In Interactive Button
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White,
                            border = BorderStroke(1.5.dp, Color(0xFFCBD5E1)),
                            shadowElevation = 3.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .clickable(enabled = !isSigningInGoogle && activity != null) {
                                    if (activity != null) {
                                        isSigningInGoogle = true
                                        viewModel.signInWithGoogle(activity)
                                    }
                                }
                                .testTag("btn_google_sign_in")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                if (isSigningInGoogle) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(22.dp),
                                        strokeWidth = 2.dp,
                                        color = GameSkyBlue
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Menghubungkan ke Google...",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = GameDarkNavy
                                    )
                                } else {
                                    Text(text = "🌐", fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Sign in with Google",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = GameDarkNavy
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = GameMintGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Firebase Auth & Firestore Zero-Trust Security",
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Quick Role Switcher for Seamless Testing & Demonstration
            Text(
                text = "⚡ Akses Cepat Simulasi Peran (Demo):",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = GameDarkNavy
            )
            Text(
                text = "Pilih salah satu peran untuk langsung menguji navigasi aplikasi:",
                fontSize = 11.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(10.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Quick Admin
                RoleDemoLoginCard(
                    title = "Admin: Pak Bambang",
                    description = "Akses Penuh: Persetujuan akun, titik sekolah, kelola banner",
                    emoji = "👑",
                    roleColor = GameSkyBlue,
                    onClick = {
                        val adminUser = users.firstOrNull { it.username == "admin" }
                        if (adminUser != null) viewModel.quickSwitchAccount(adminUser)
                    }
                )

                // Quick Staff
                RoleDemoLoginCard(
                    title = "Staff: Bu Dewi",
                    description = "Pengaju: Pinjam unit mobil (N-Max, Gran Max, Avanza), pantau GPS",
                    emoji = "👩‍🏫",
                    roleColor = GameAmber,
                    onClick = {
                        val staffUser = users.firstOrNull { it.username == "staff" }
                        if (staffUser != null) viewModel.quickSwitchAccount(staffUser)
                    }
                )

                // Quick Keamanan
                RoleDemoLoginCard(
                    title = "Keamanan: Pak Slamet",
                    description = "Pintu Keluar: Verifikasi izin gerbang, izinkan keluar & pantau",
                    emoji = "👮‍♂️",
                    roleColor = GameMintGreen,
                    onClick = {
                        val securityUser = users.firstOrNull { it.username == "keamanan" }
                        if (securityUser != null) viewModel.quickSwitchAccount(securityUser)
                    }
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun PendingApprovalCard(
    user: User,
    onCheckStatus: () -> Unit,
    onSignOut: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        border = BorderStroke(2.dp, GameAmber),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(GameAmber.copy(alpha = 0.18f))
            ) {
                Text(text = "⏳", fontSize = 32.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Menunggu Persetujuan Admin",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF92400E)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Halo ${user.fullName}, pendaftaran Anda sebagai ${user.role.displayName} sedang ditinjau oleh Admin Sekolah Rakyat.",
                fontSize = 12.sp,
                color = Color.DarkGray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFFFFBEB),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Status: ⏳ PENDING (Belum Disetujui)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB45309)
                    )
                    Text(
                        text = "Peran yang diajukan: ${user.role.displayName} (${user.role.badgeEmoji})",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GameButton(
                    text = "Cek Ulang 🔄",
                    onClick = onCheckStatus,
                    buttonColor = GameSkyBlue,
                    modifier = Modifier.weight(1f),
                    height = 42.dp,
                    fontSize = 12.sp
                )

                GameButton(
                    text = "Keluar Akun",
                    onClick = onSignOut,
                    buttonColor = Color.LightGray,
                    textColor = GameDarkNavy,
                    modifier = Modifier.weight(1f),
                    height = 42.dp,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun RoleDemoLoginCard(
    title: String,
    description: String,
    emoji: String,
    roleColor: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.2.dp, roleColor.copy(alpha = 0.4f)),
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("role_demo_${title.take(5)}")
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
                    .background(roleColor.copy(alpha = 0.15f))
            ) {
                Text(text = emoji, fontSize = 22.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = GameDarkNavy
                )
                Text(
                    text = description,
                    fontSize = 10.sp,
                    color = Color.Gray,
                    lineHeight = 14.sp
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = roleColor.copy(alpha = 0.15f)
            ) {
                Text(
                    text = "Masuk ➔",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = roleColor,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
