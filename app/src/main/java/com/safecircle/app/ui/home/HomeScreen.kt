package com.safecircle.app.ui.home

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PhoneCallback
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.safecircle.app.data.model.SessionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onSignOut: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    var newCircleName by remember { mutableStateOf("") }
    var routeOrigin by remember { mutableStateOf(state.routeAssessment.origin) }
    var routeDest by remember { mutableStateOf(state.routeAssessment.destination) }

    // Fake Call Modal Overlay
    state.fakeCallCaller?.let { caller ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissFakeCall() },
            icon = {
                Icon(
                    imageVector = Icons.Filled.PhoneCallback,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = { Text("Incoming Call • $caller") },
            text = {
                Text("Simulated emergency escape call active. Answer to play pre-recorded guardian audio prompt.")
            },
            confirmButton = {
                Button(onClick = { viewModel.dismissFakeCall() }) {
                    Text("End Call")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Shield,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text("SafeCircle AI", style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = state.displayName,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.signOut()
                            onSignOut()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Sign Out"
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Filled.Warning, contentDescription = null) },
                    label = { Text("SOS Hub") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Filled.Navigation, contentDescription = null) },
                    label = { Text("Safe Route AI") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Filled.Group, contentDescription = null) },
                    label = { Text("Circles") }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Filled.LocalHospital, contentDescription = null) },
                    label = { Text("Safe Havens") }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            when (selectedTab) {
                0 -> SosDashboardTab(state = state, viewModel = viewModel)
                1 -> SafeRouteAiTab(
                    state = state,
                    origin = routeOrigin,
                    destination = routeDest,
                    onOriginChange = { routeOrigin = it },
                    onDestinationChange = { routeDest = it },
                    onEvaluate = { viewModel.evaluateRoute(routeOrigin, routeDest) },
                    onToggleTracking = { viewModel.toggleLiveTracking() },
                    onQueuePoint = { viewModel.queueLocationSample() },
                    onSyncPoints = { viewModel.syncOfflineLocations() }
                )
                2 -> CirclesTab(
                    state = state,
                    newCircleName = newCircleName,
                    onNewCircleNameChange = { newCircleName = it },
                    onCreateCircle = {
                        viewModel.createCircle(newCircleName)
                        newCircleName = ""
                    }
                )
                3 -> SafeHavensTab(state = state)
            }
        }
    }
}

@Composable
private fun SosDashboardTab(
    state: HomeUiState,
    viewModel: HomeViewModel
) {
    // Active SOS or Countdown Banner
    if (state.activeSos != null || state.sosCountdownSeconds != null) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFB3261E))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (state.sosCountdownSeconds != null)
                        "TRIGGERING EMERGENCY SOS IN ${state.sosCountdownSeconds}s..."
                    else
                        "SOS DISPATCHED • LIVE LOCATION & AUDIO STREAMING",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (state.isAudioRecording) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Filled.Mic, contentDescription = null, tint = Color.White)
                        Text(
                            text = "Recording 30s Encrypted Cloud Evidence...",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                Button(
                    onClick = { viewModel.cancelSos() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFFB3261E)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("I AM SAFE — CANCEL SOS", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Giant 1-Tap SOS Card
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(112.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFB3261E)),
                contentAlignment = Alignment.Center
            ) {
                TextButton(
                    onClick = { viewModel.startSosCountdown() },
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "SOS",
                            color = Color.White,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "5s Countdown",
                            color = Color.White.copy(alpha = 0.9f),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
            Text(
                text = "Tap for 5-second countdown or trigger immediate multi-channel alert to your circles.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            OutlinedButton(onClick = { viewModel.triggerImmediateSos() }) {
                Text("Instant SOS (No Delay)")
            }
        }
    }

    // Smart Safety Toolkit Grid
    Text(
        text = "Smart Safety Toolkit",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        FilledTonalButton(
            onClick = { viewModel.triggerFakeCall("Mom (Mobile)") },
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Filled.Call, contentDescription = null)
            Spacer(Modifier.width(6.dp))
            Text("Fake Call")
        }
        FilledTonalButton(
            onClick = { viewModel.toggleSiren() },
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Filled.NotificationsActive, contentDescription = null)
            Spacer(Modifier.width(6.dp))
            Text(if (state.isSirenActive) "Stop Siren" else "Loud Siren")
        }
    }

    // Check-In Timer Card
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Timer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Automated Safe Check-In Timer",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            if (state.checkInRemainingSeconds != null) {
                val mins = state.checkInRemainingSeconds / 60
                val secs = state.checkInRemainingSeconds % 60
                Text(
                    text = "Check-in required in %02d:%02d or SOS will auto-trigger.".format(mins, secs),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
                Button(
                    onClick = { viewModel.confirmSafeCheckIn() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("I'm Safe — Complete Check-In")
                }
            } else {
                Text(
                    text = "Going on a cab ride or walking alone? Set a timer that alerts your circle if you don't check in.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { viewModel.startSafeCheckInTimer(15) }) {
                        Text("15 Min")
                    }
                    OutlinedButton(onClick = { viewModel.startSafeCheckInTimer(30) }) {
                        Text("30 Min")
                    }
                    OutlinedButton(onClick = { viewModel.startSafeCheckInTimer(60) }) {
                        Text("1 Hour")
                    }
                }
            }
        }
    }
}

@Composable
private fun SafeRouteAiTab(
    state: HomeUiState,
    origin: String,
    destination: String,
    onOriginChange: (String) -> Unit,
    onDestinationChange: (String) -> Unit,
    onEvaluate: () -> Unit,
    onToggleTracking: () -> Unit,
    onQueuePoint: () -> Unit,
    onSyncPoints: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AI Corridor Safety Score",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Badge {
                    Text("${state.routeAssessment.safetyScore}/100 SAFE")
                }
            }
            LinearProgressIndicator(
                progress = { state.routeAssessment.safetyScore / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
            Text(
                text = "Lighting: ${state.routeAssessment.lightingIndex}% • Crowd: ${state.routeAssessment.crowdDensity} • Patrols: ${state.routeAssessment.patrolledZones}",
                style = MaterialTheme.typography.labelMedium
            )
            Text(
                text = state.routeAssessment.recommendation,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }

    OutlinedTextField(
        value = origin,
        onValueChange = onOriginChange,
        label = { Text("Origin") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    OutlinedTextField(
        value = destination,
        onValueChange = onDestinationChange,
        label = { Text("Destination") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    Button(
        onClick = onEvaluate,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Run AI Safety Route Analysis")
    }

    // Live Location & Offline Room Queue Card
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val isActive = state.trackingSession?.state == SessionState.ACTIVE
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (isActive) "Live Location Sharing: ACTIVE" else "Live Location Sharing: PAUSED",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Text(
                text = state.trackingSession?.notificationText
                    ?: "Offline-resilient Room queue (${state.unsyncedLocationCount} unsynced GPS points)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onToggleTracking) {
                    Text(if (isActive) "Stop Sharing" else "Start Live Share")
                }
                OutlinedButton(onClick = onQueuePoint) {
                    Text("+ Queue GPS (${state.unsyncedLocationCount})")
                }
                if (state.unsyncedLocationCount > 0) {
                    IconButton(onClick = onSyncPoints) {
                        Icon(Icons.Filled.CloudSync, contentDescription = "Sync")
                    }
                }
            }
        }
    }
}

@Composable
private fun CirclesTab(
    state: HomeUiState,
    newCircleName: String,
    onNewCircleNameChange: (String) -> Unit,
    onCreateCircle: () -> Unit
) {
    Text(
        text = "My Trusted Safety Circles",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = newCircleName,
            onValueChange = onNewCircleNameChange,
            label = { Text("New Circle Name") },
            singleLine = true,
            modifier = Modifier.weight(1f)
        )
        Button(onClick = onCreateCircle) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(Modifier.width(4.dp))
            Text("Create")
        }
    }

    state.circles.forEach { circle ->
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(circle.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    text = "${circle.memberUids.size} members • Mutual consent verified",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }

    Text(
        text = "Active Circle Guardians",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )

    state.members.forEach { member ->
        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(member.uid, fontWeight = FontWeight.Medium)
                    Text(
                        text = "Role: ${member.role} • Mutual Consent: ${if (member.consentGiven) "Granted" else "Pending"}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun SafeHavensTab(state: HomeUiState) {
    Text(
        text = "Verified 24/7 Safe Havens & Helplines",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
    state.nearbySafeHavens.forEach { place ->
        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(place.name, fontWeight = FontWeight.SemiBold)
                    Text(
                        text = "${place.category} • ${if (place.distanceMeters > 0) "${place.distanceMeters}m away" else "Toll-Free National"} • 24/7 Open",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                FilledTonalButton(onClick = {}) {
                    Icon(Icons.Filled.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(place.phone)
                }
            }
        }
    }
}
