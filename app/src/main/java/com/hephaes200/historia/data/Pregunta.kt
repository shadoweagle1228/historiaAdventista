package com.hephaes200.historia.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "preguntas")
data class Pregunta(
    @PrimaryKey val id: Int,
    val capituloId: Int,
    val enunciado: String,
    val opciones: String, // Guardamos las opciones como un string separado por comas
    val respuestaCorrecta: String,
    val justificacion: String = "", // Lo que mostraremos si se equivoca
    val vecesFallada: Int = 0 // El contador para el "Banco de Errores"
)