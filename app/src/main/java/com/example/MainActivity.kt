package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.AdminPanelScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LoanRequestScreen
import com.example.ui.screens.SecurityVerificationScreen
import com.example.ui.screens.TrackingMapScreen
import com.example.ui.screens.TripHistoryScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.ScreenState
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp(
    viewModel: MainViewModel = viewModel()
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val users by viewModel.users.collectAsStateWithLifecycle()
    val vehicles by viewModel.vehicles.collectAsStateWithLifecycle()
    val loans by viewModel.loans.collectAsStateWithLifecycle()
    val schoolLocation by viewModel.schoolLocation.collectAsStateWithLifecycle()
    val promoBanner by viewModel.promoBanner.collectAsStateWithLifecycle()
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val selectedVehicleForLoan by viewModel.selectedVehicleForLoan.collectAsStateWithLifecycle()
    val selectedLoanForTracking by viewModel.selectedLoanForTracking.collectAsStateWithLifecycle()
    val mapRouteProgress by viewModel.mapRouteProgress.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.userMessage.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    // Predictive back handler for sub-screens
    if (currentScreen != ScreenState.DASHBOARD && currentScreen != ScreenState.AUTH) {
        BackHandler {
            viewModel.navigateTo(ScreenState.DASHBOARD)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (currentUser == null || currentScreen == ScreenState.AUTH) {
                AuthScreen(
                    viewModel = viewModel,
                    users = users
                )
            } else {
                when (currentScreen) {
                    ScreenState.DASHBOARD -> {
                        HomeScreen(
                            viewModel = viewModel,
                            currentUser = currentUser!!,
                            vehicles = vehicles,
                            loans = loans,
                            promoBanner = promoBanner,
                            schoolLocation = schoolLocation
                        )
                    }

                    ScreenState.PINJAM_FORM -> {
                        LoanRequestScreen(
                            viewModel = viewModel,
                            currentUser = currentUser!!,
                            vehicles = vehicles,
                            initialVehicle = selectedVehicleForLoan
                        )
                    }

                    ScreenState.TRACKING_MAP -> {
                        TrackingMapScreen(
                            viewModel = viewModel,
                            currentUser = currentUser!!,
                            schoolLocation = schoolLocation,
                            selectedLoan = selectedLoanForTracking,
                            vehicles = vehicles,
                            routeProgress = mapRouteProgress
                        )
                    }

                    ScreenState.TRIP_HISTORY -> {
                        TripHistoryScreen(
                            viewModel = viewModel,
                            currentUser = currentUser!!,
                            loans = loans
                        )
                    }

                    ScreenState.ADMIN_PANEL -> {
                        AdminPanelScreen(
                            viewModel = viewModel,
                            currentUser = currentUser!!,
                            users = users,
                            schoolLocation = schoolLocation,
                            promoBanner = promoBanner,
                            loans = loans
                        )
                    }

                    ScreenState.SECURITY_VERIFY -> {
                        SecurityVerificationScreen(
                            viewModel = viewModel,
                            currentUser = currentUser!!,
                            loans = loans
                        )
                    }

                    ScreenState.AUTH -> {
                        AuthScreen(
                            viewModel = viewModel,
                            users = users
                        )
                    }
                }
            }
        }
    }
}
