package com.safecircle.app.data.model

data class User(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String? = null,
    val createdAt: Long = 0L,
    val fcmToken: String? = null
)
