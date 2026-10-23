package com.safecircle.app.data.model

enum class SessionState { ACTIVE, PAUSED, ENDED }

data class TrackingSession(
    val id: String = "",
    val ownerUid: String = "",
    val circleId: String = "",
    val state: SessionState = SessionState.ACTIVE,
    val startedAt: Long = 0L,
    val endedAt: Long? = null,
    /** Visible persistent notification text shown to the tracked user. */
    val notificationText: String = "SafeCircle is sharing your live location with your circle"
)
