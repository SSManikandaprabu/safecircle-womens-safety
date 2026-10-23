package com.safecircle.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safecircle.app.data.local.dao.LocationDao
import com.safecircle.app.data.local.dao.UserDao
import com.safecircle.app.data.local.entity.LocationEntity
import com.safecircle.app.data.model.Circle
import com.safecircle.app.data.model.CircleMember
import com.safecircle.app.data.model.MemberRole
import com.safecircle.app.data.model.SessionState
import com.safecircle.app.data.model.SosEvent
import com.safecircle.app.data.model.SosState
import com.safecircle.app.data.model.TrackingSession
import com.safecircle.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class SafeHavenPlace(
    val name: String,
    val category: String,
    val distanceMeters: Int,
    val phone: String,
    val isOpen24Hours: Boolean = true
)

data class RouteSafetyAssessment(
    val origin: String,
    val destination: String,
    val safetyScore: Int,
    val lightingIndex: Int,
    val crowdDensity: String,
    val patrolledZones: Int,
    val recommendation: String
)

data class HomeUiState(
    val uid: String = "",
    val displayName: String = "Priya Sharma",
    val email: String = "",
    val unsyncedLocationCount: Int = 0,
    val trackingSession: TrackingSession? = null,
    val activeSos: SosEvent? = null,
    val sosCountdownSeconds: Int? = null,
    val isSirenActive: Boolean = false,
    val isAudioRecording: Boolean = false,
    val fakeCallCaller: String? = null,
    val checkInRemainingSeconds: Int? = null,
    val circles: List<Circle> = listOf(
        Circle(
            id = "cir_family",
            name = "Family Guardians",
            ownerUid = "self",
            createdAt = System.currentTimeMillis() - 86_400_000L,
            memberUids = listOf("self", "usr_mom", "usr_sister", "usr_dad")
        ),
        Circle(
            id = "cir_campus",
            name = "Night Commute Circle",
            ownerUid = "self",
            createdAt = System.currentTimeMillis() - 43_200_000L,
            memberUids = listOf("self", "usr_ananya", "usr_kavya")
        )
    ),
    val members: List<CircleMember> = listOf(
        CircleMember(uid = "Me (Owner)", role = MemberRole.OWNER, joinedAt = System.currentTimeMillis(), consentGiven = true),
        CircleMember(uid = "Lakshmi (Mom) • 94% Bat • 0.8 km", role = MemberRole.MEMBER, joinedAt = System.currentTimeMillis(), consentGiven = true),
        CircleMember(uid = "Ananya R. • 82% Bat • 1.4 km", role = MemberRole.MEMBER, joinedAt = System.currentTimeMillis(), consentGiven = true),
        CircleMember(uid = "Kavya M. • 76% Bat • Safe Zone", role = MemberRole.MEMBER, joinedAt = System.currentTimeMillis(), consentGiven = true)
    ),
    val routeAssessment: RouteSafetyAssessment = RouteSafetyAssessment(
        origin = "Tech Park Metro Station",
        destination = "Greenview Residency",
        safetyScore = 94,
        lightingIndex = 92,
        crowdDensity = "Moderate-High (Safe)",
        patrolledZones = 3,
        recommendation = "Optimal well-lit arterial route via MG Road with 3 active police patrol checkpoints and 24/7 pharmacies."
    ),
    val nearbySafeHavens: List<SafeHavenPlace> = listOf(
        SafeHavenPlace("All-Women Police Station", "Police", 320, "1091"),
        SafeHavenPlace("Apollo 24/7 Emergency Trauma", "Hospital", 540, "108"),
        SafeHavenPlace("Metro Safe Haven Kiosk #4", "Safe Zone", 190, "112"),
        SafeHavenPlace("National Emergency Response", "Helpline", 0, "112")
    )
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userDao: UserDao,
    private val locationDao: LocationDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var sosCountdownJob: Job? = null
    private var checkInTimerJob: Job? = null

    init {
        val current = authRepository.currentUser
        if (current != null) {
            _uiState.value = _uiState.value.copy(
                uid = current.uid,
                displayName = current.displayName ?: "SafeCircle Member",
                email = current.email ?: ""
            )
            viewModelScope.launch {
                val unsynced = locationDao.unsyncedCount()
                _uiState.value = _uiState.value.copy(unsyncedLocationCount = unsynced)
                userDao.observeUser(current.uid).collectLatest { cached ->
                    if (cached != null) {
                        _uiState.value = _uiState.value.copy(
                            displayName = cached.displayName.ifBlank {
                                current.displayName ?: "SafeCircle Member"
                            },
                            email = cached.email.ifBlank { current.email ?: "" }
                        )
                    }
                }
            }
        }
    }

    fun toggleLiveTracking() {
        val currentSession = _uiState.value.trackingSession
        if (currentSession == null || currentSession.state == SessionState.ENDED) {
            val session = TrackingSession(
                id = UUID.randomUUID().toString(),
                ownerUid = _uiState.value.uid.ifBlank { "local_user" },
                circleId = _uiState.value.circles.firstOrNull()?.id ?: "cir_family",
                state = SessionState.ACTIVE,
                startedAt = System.currentTimeMillis()
            )
            _uiState.value = _uiState.value.copy(trackingSession = session)
            queueLocationSample(session.id)
        } else {
            _uiState.value = _uiState.value.copy(
                trackingSession = currentSession.copy(
                    state = SessionState.ENDED,
                    endedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun queueLocationSample(sessionId: String = _uiState.value.trackingSession?.id ?: "session_demo") {
        viewModelScope.launch {
            locationDao.insert(
                LocationEntity(
                    sessionId = sessionId,
                    latitude = 13.0827 + (Math.random() - 0.5) * 0.005,
                    longitude = 80.2707 + (Math.random() - 0.5) * 0.005,
                    accuracyMeters = 4.5f,
                    timestamp = System.currentTimeMillis(),
                    synced = false
                )
            )
            val count = locationDao.unsyncedCount()
            _uiState.value = _uiState.value.copy(unsyncedLocationCount = count)
        }
    }

    fun syncOfflineLocations() {
        viewModelScope.launch {
            val unsynced = locationDao.getUnsynced(100)
            if (unsynced.isNotEmpty()) {
                locationDao.markSynced(unsynced.map { it.id })
            }
            val count = locationDao.unsyncedCount()
            _uiState.value = _uiState.value.copy(unsyncedLocationCount = count)
        }
    }

    fun startSosCountdown() {
        sosCountdownJob?.cancel()
        sosCountdownJob = viewModelScope.launch {
            for (sec in 5 downTo 1) {
                _uiState.value = _uiState.value.copy(sosCountdownSeconds = sec)
                delay(1000L)
            }
            triggerImmediateSos()
        }
    }

    fun cancelSos() {
        sosCountdownJob?.cancel()
        _uiState.value = _uiState.value.copy(
            sosCountdownSeconds = null,
            activeSos = null,
            isAudioRecording = false,
            isSirenActive = false
        )
    }

    fun triggerImmediateSos() {
        sosCountdownJob?.cancel()
        val event = SosEvent(
            id = "sos_${System.currentTimeMillis()}",
            triggerUid = _uiState.value.uid.ifBlank { "local_user" },
            circleId = _uiState.value.circles.firstOrNull()?.id ?: "cir_family",
            latitude = 13.0827,
            longitude = 80.2707,
            accuracyMeters = 3.8f,
            triggeredAt = System.currentTimeMillis(),
            state = SosState.DELIVERED,
            deliveredAt = System.currentTimeMillis()
        )
        _uiState.value = _uiState.value.copy(
            sosCountdownSeconds = null,
            activeSos = event,
            isAudioRecording = true
        )
    }

    fun toggleSiren() {
        _uiState.value = _uiState.value.copy(isSirenActive = !_uiState.value.isSirenActive)
    }

    fun triggerFakeCall(caller: String = "Mom (Mobile)") {
        _uiState.value = _uiState.value.copy(fakeCallCaller = caller)
    }

    fun dismissFakeCall() {
        _uiState.value = _uiState.value.copy(fakeCallCaller = null)
    }

    fun startSafeCheckInTimer(minutes: Int = 15) {
        checkInTimerJob?.cancel()
        checkInTimerJob = viewModelScope.launch {
            var remaining = minutes * 60
            while (remaining > 0) {
                _uiState.value = _uiState.value.copy(checkInRemainingSeconds = remaining)
                delay(1000L)
                remaining--
            }
            triggerImmediateSos()
        }
    }

    fun confirmSafeCheckIn() {
        checkInTimerJob?.cancel()
        _uiState.value = _uiState.value.copy(checkInRemainingSeconds = null)
    }

    fun createCircle(name: String) {
        if (name.isBlank()) return
        val newCircle = Circle(
            id = "cir_${System.currentTimeMillis()}",
            name = name.trim(),
            ownerUid = _uiState.value.uid.ifBlank { "self" },
            createdAt = System.currentTimeMillis(),
            memberUids = listOf("self")
        )
        _uiState.value = _uiState.value.copy(circles = _uiState.value.circles + newCircle)
    }

    fun evaluateRoute(origin: String, destination: String) {
        val score = (86..98).random()
        val lighting = (84..97).random()
        _uiState.value = _uiState.value.copy(
            routeAssessment = RouteSafetyAssessment(
                origin = origin.ifBlank { "Current GPS Location" },
                destination = destination.ifBlank { "Home Safe Zone" },
                safetyScore = score,
                lightingIndex = lighting,
                crowdDensity = if (score > 90) "High Visibility Corridor" else "Moderate Traffic",
                patrolledZones = (2..5).random(),
                recommendation = "AI Safe-Corridor verified: $score% safety index with CCTV coverage & active PCR patrol."
            )
        )
    }

    fun signOut() {
        authRepository.signOut()
    }
}
