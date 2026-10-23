package com.safecircle.app.data.model

enum class MemberRole { OWNER, MEMBER }

data class CircleMember(
    val uid: String = "",
    val role: MemberRole = MemberRole.MEMBER,
    val joinedAt: Long = 0L,
    /** Both parties must consent: this is the member's own consent flag. */
    val consentGiven: Boolean = false
)
