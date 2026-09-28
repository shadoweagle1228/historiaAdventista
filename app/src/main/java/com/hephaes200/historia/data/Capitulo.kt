package com.hephaes200.historia.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "capitulos")
data class Capitulo(
    @PrimaryKey val id: Int,
    val titulo: String,
    val contenido: String,
    val completado: Boolean = false
)