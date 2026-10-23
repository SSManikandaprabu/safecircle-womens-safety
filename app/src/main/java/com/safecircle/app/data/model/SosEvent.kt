package com.safecircle.app.data.model

enum class SosState { PENDING, DELIVERED, ACKNOWLEDGED, FAILED }

data class SosEvent(
    val id: String = "",
    val triggerUid: String = "",
    val circleId: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val accuracyMeters: Float? = null,
    val triggeredAt: Long = 0L,
    val state: SosState = SosState.PENDING,
    val deliveredAt: Long? = null,
    val acknowledgedAt: Long? = null
)
