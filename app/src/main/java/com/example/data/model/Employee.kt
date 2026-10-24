package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "employees")
data class Employee(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nip: String,
    val name: String,
    val department: String,
    val position: String,
    val pin: String = "1234",
    val phone: String = "",
    val avatarColorHex: Long = 0xFF2563EB,
    val faceRegistered: Boolean = true
)
