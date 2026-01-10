package com.simplemobiletools.notes.pro.models

import kotlinx.serialization.Serializable

@Serializable
data class CounterItem(
    val id: Int,
    val dateCreated: Long = 0L,
    var title: String,
    var count: Int
)
