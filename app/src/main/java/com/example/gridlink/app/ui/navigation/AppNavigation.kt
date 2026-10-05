package com.example.gridlink.app.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.gridlink.app.data.local.SessionManager
import com.example.gridlink.app.data.remote.ReservationResponseDto
import com.example.gridlink.app.ui.screens.auth.LoginScreen
import com.example.gridlink.app.ui.screens.auth.RegisterScreen
import com.example.gridlink.app.ui.screens.operator.OperatorHomeScreen
import com.example.gridlink.app.ui.screens.operator.OperatorQrScannerScreen
import com.example.gridlink.app.ui.screens.operator.OperatorReservationsScreen
import com.example.gridlink.app.ui.screens.operator.OperatorStationsScreen
import com.example.gridlink.app.ui.screens.prosumer.ActionSummaryScreen
import com.example.gridlink.app.ui.screens.prosumer.BookingsListScreen
import com.example.gridlink.app.ui.screens.prosumer.CreateReservationScreen
import com.example.gridlink.app.ui.screens.prosumer.ProsumerHomeScreen
import com.example.gridlink.app.ui.screens.prosumer.ProsumerProfileScreen
import com.example.gridlink.app.ui.screens.prosumer.ReservationDetailScreen
import com.example.gridlink.app.ui.screens.prosumer.StationMapScreen
import com.example.gridlink.app.ui.theme.GreenPrimary

sealed class Screen {
    data object Login : Screen()
    data object Register : Screen()
    data object ProsumerHome : Screen()
    data class CreateReservation(val stationId: String? = null) : Screen()
    data object BookingsList : Screen()
    data class ReservationDetail(val id: String) : Screen()
    data class ActionSummary(val actionType: String, val reservation: ReservationResponseDto) : Screen()
    data object ProsumerProfile : Screen()
    data object StationMap : Screen()

    data object OperatorHome : Screen()
    data object OperatorQrScanner : Screen()
    data object OperatorStations : Screen()
    data object OperatorReservations : Screen()
}

@Composable
fun AppNavigation(initialScreenKey: String? = null) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    val backStack = remember { mutableStateListOf<Screen>() }
    var isCheckingSession by remember { mutableStateOf(true) }

    // Check existing SQLite session or route to explicit screen
    LaunchedEffect(initialScreenKey) {
        val session = sessionManager.getSession()
        backStack.clear()
        when (initialScreenKey) {
            "map", "create", "bookings", "profile", "detail", "summary", "prosumer_home" -> {
                if (session == null || !session.role.equals("Prosumer", ignoreCase = true)) {
                    sessionManager.saveSession(
                        com.example.gridlink.app.data.local.UserSession(
                            userId = "usr-demo-prosumer",
                            nic = "200012345678",
                            fullName = "Sunil Perera",
                            email = "sunil.perera@gridlink.lk",
                            phoneNumber = "+94771234567",
                            address = "No. 45, Temple Road, Colombo 03",
                            role = "Prosumer",
                            status = "Active",
                            token = "demo-jwt-token-prosumer",
                            expiration = "2026-12-31T23:59:59Z"
                        )
                    )
                }
            }
            "operator_home", "operator_qr", "operator_slots", "operator_res" -> {
                if (session == null || !session.role.equals("GridOperator", ignoreCase = true)) {
                    sessionManager.saveSession(
                        com.example.gridlink.app.data.local.UserSession(
                            userId = "usr-demo-operator",
                            nic = "OPER00001V",
                            fullName = "Nimal Silva",
                            email = "operator.silva@ceb.lk",
                            phoneNumber = "+94777654321",
                            address = "CEB Regional Dispatch Hub, Colombo",
                            role = "GridOperator",
                            status = "Active",
                            token = "demo-jwt-token-operator",
                            expiration = "2026-12-31T23:59:59Z"
                        )
                    )
                }
            }
        }
        when (initialScreenKey) {
            "map" -> backStack.add(Screen.StationMap)
            "create" -> backStack.add(Screen.CreateReservation())
            "bookings" -> backStack.add(Screen.BookingsList)
            "profile" -> backStack.add(Screen.ProsumerProfile)
            "detail" -> backStack.add(Screen.ReservationDetail("res-demo-001"))
            "summary" -> backStack.add(
                Screen.ActionSummary(
                    actionType = "Created",
                    reservation = ReservationResponseDto(
                        id = "res-demo-001",
                        reservationNumber = "RES-202610-001",
                        userId = "usr-demo-prosumer",
                        nic = "200012345678",
                        prosumerName = "Sunil Perera",
                        stationId = "node-001",
                        stationName = "Colombo Central Solar Hub",
                        reservationDateTime = "2026-10-07T10:00:00Z",
                        durationMinutes = 60,
                        tradingType = "DropOff",
                        requestedEnergyKWh = 25.0,
                        status = "Approved",
                        qrCodeData = "GRIDLINK:RES-202610-001:200012345678:node-001:25.0",
                        actualEnergyTransferredKWh = null
                    )
                )
            )
            "operator_home" -> backStack.add(Screen.OperatorHome)
            "operator_qr" -> backStack.add(Screen.OperatorQrScanner)
            "operator_slots" -> backStack.add(Screen.OperatorStations)
            "operator_res" -> backStack.add(Screen.OperatorReservations)
            "prosumer_home" -> backStack.add(Screen.ProsumerHome)
            "register" -> backStack.add(Screen.Register)
            "login" -> backStack.add(Screen.Login)
            else -> {
                if (session != null && session.token.isNotBlank()) {
                    if (session.role.equals("GridOperator", ignoreCase = true)) {
                        backStack.add(Screen.OperatorHome)
                    } else {
                        backStack.add(Screen.ProsumerHome)
                    }
                } else {
                    backStack.add(Screen.Login)
                }
            }
        }
        isCheckingSession = false
    }

    if (isCheckingSession || backStack.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = GreenPrimary)
        }
        return
    }

    val currentScreen = backStack.last()

    fun navigateTo(screen: Screen) {
        backStack.add(screen)
    }

    fun navigateBack() {
        if (backStack.size > 1) {
            backStack.removeAt(backStack.size - 1)
        }
    }

    fun logout() {
        sessionManager.clearSession()
        backStack.clear()
        backStack.add(Screen.Login)
    }

    BackHandler(enabled = backStack.size > 1) {
        navigateBack()
    }

    when (currentScreen) {
        is Screen.Login -> {
            LoginScreen(
                onLoginSuccess = { role ->
                    backStack.clear()
                    if (role.equals("GridOperator", ignoreCase = true)) {
                        backStack.add(Screen.OperatorHome)
                    } else {
                        backStack.add(Screen.ProsumerHome)
                    }
                },
                onNavigateToRegister = {
                    navigateTo(Screen.Register)
                }
            )
        }

        is Screen.Register -> {
            RegisterScreen(
                onNavigateBackToLogin = {
                    navigateBack()
                }
            )
        }

        is Screen.ProsumerHome -> {
            ProsumerHomeScreen(
                onNavigateToCreateReservation = {
                    navigateTo(Screen.CreateReservation())
                },
                onNavigateToBookingsList = {
                    navigateTo(Screen.BookingsList)
                },
                onNavigateToMap = {
                    navigateTo(Screen.StationMap)
                },
                onNavigateToProfile = {
                    navigateTo(Screen.ProsumerProfile)
                },
                onNavigateToReservationDetail = { id ->
                    navigateTo(Screen.ReservationDetail(id))
                },
                onLogout = { logout() }
            )
        }

        is Screen.CreateReservation -> {
            CreateReservationScreen(
                initialStationId = currentScreen.stationId,
                onNavigateBack = { navigateBack() },
                onNavigateToSummary = { actionType, reservation ->
                    backStack.removeAt(backStack.size - 1) // pop create screen
                    navigateTo(Screen.ActionSummary(actionType, reservation))
                }
            )
        }

        is Screen.BookingsList -> {
            BookingsListScreen(
                onNavigateBack = { navigateBack() },
                onNavigateToDetail = { id ->
                    navigateTo(Screen.ReservationDetail(id))
                }
            )
        }

        is Screen.ReservationDetail -> {
            ReservationDetailScreen(
                reservationId = currentScreen.id,
                onNavigateBack = { navigateBack() },
                onNavigateToSummary = { actionType, reservation ->
                    backStack.removeAt(backStack.size - 1) // pop detail screen
                    navigateTo(Screen.ActionSummary(actionType, reservation))
                }
            )
        }

        is Screen.ActionSummary -> {
            ActionSummaryScreen(
                actionType = currentScreen.actionType,
                reservation = currentScreen.reservation,
                onNavigateToHome = {
                    // Navigate back to ProsumerHome or OperatorHome
                    val isOperator = sessionManager.getUserRole()?.equals("GridOperator", ignoreCase = true) == true
                    backStack.clear()
                    if (isOperator) {
                        backStack.add(Screen.OperatorHome)
                    } else {
                        backStack.add(Screen.ProsumerHome)
                    }
                },
                onNavigateToDetail = { id ->
                    backStack.removeAt(backStack.size - 1)
                    navigateTo(Screen.ReservationDetail(id))
                }
            )
        }

        is Screen.ProsumerProfile -> {
            ProsumerProfileScreen(
                onNavigateBack = { navigateBack() },
                onLogout = { logout() }
            )
        }

        is Screen.StationMap -> {
            StationMapScreen(
                onNavigateBack = { navigateBack() },
                onSelectStationForBooking = { stationId ->
                    navigateTo(Screen.CreateReservation(stationId))
                }
            )
        }

        is Screen.OperatorHome -> {
            OperatorHomeScreen(
                onNavigateToQrScanner = {
                    navigateTo(Screen.OperatorQrScanner)
                },
                onNavigateToStations = {
                    navigateTo(Screen.OperatorStations)
                },
                onNavigateToReservations = {
                    navigateTo(Screen.OperatorReservations)
                },
                onNavigateToReservationDetail = { id ->
                    navigateTo(Screen.ReservationDetail(id))
                },
                onLogout = { logout() }
            )
        }

        is Screen.OperatorQrScanner -> {
            OperatorQrScannerScreen(
                onNavigateBack = { navigateBack() },
                onJobFinalized = { res ->
                    backStack.removeAt(backStack.size - 1)
                    navigateTo(Screen.ActionSummary("Finalized", res))
                }
            )
        }

        is Screen.OperatorStations -> {
            OperatorStationsScreen(
                onNavigateBack = { navigateBack() }
            )
        }

        is Screen.OperatorReservations -> {
            OperatorReservationsScreen(
                onNavigateBack = { navigateBack() },
                onNavigateToDetail = { id ->
                    navigateTo(Screen.ReservationDetail(id))
                }
            )
        }
    }
}
