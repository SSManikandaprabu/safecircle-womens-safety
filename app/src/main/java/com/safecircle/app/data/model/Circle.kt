package com.safecircle.app.data.model

data class Circle(
    val id: String = "",
    val name: String = "",
    val ownerUid: String = "",
    val createdAt: Long = 0L,
    val memberUids: List<String> = emptyList()
)
