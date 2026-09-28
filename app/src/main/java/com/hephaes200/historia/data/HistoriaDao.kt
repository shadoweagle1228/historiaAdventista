package com.hephaes200.historia.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface HistoriaDao {

    @Query("SELECT * FROM capitulos")
    fun obtenerTodosLosCapitulos(): List<Capitulo>

    @Query("SELECT * FROM preguntas WHERE capituloId = :capituloId")
    fun obtenerPreguntasPorCapitulo(capituloId: Int): List<Pregunta>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertarCapitulos(capitulos: List<Capitulo>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertarPreguntas(preguntas: List<Pregunta>)

    @Query("UPDATE capitulos SET completado = 1 WHERE id = :id")
    fun marcarCapituloCompletado(id: Int)

    @Query("SELECT * FROM preguntas ORDER BY RANDOM() LIMIT 20")
    fun obtenerRondaAleatoria(): List<Pregunta>

    // Filtro 1: Ronda de 20 preguntas de un capítulo específico
    @Query("SELECT * FROM preguntas WHERE capituloId = :capituloId ORDER BY RANDOM() LIMIT 20")
    fun obtenerRondaPorCapitulo(capituloId: Int): List<Pregunta>

    // Filtro 2: El "Banco de Errores" (solo preguntas que ha fallado antes)
    @Query("SELECT * FROM preguntas WHERE vecesFallada > 0 ORDER BY RANDOM() LIMIT 20")
    fun obtenerRondaDeErrores(): List<Pregunta>

    // Acción: Sumar 1 al contador de errores de una pregunta específica
    @Query("UPDATE preguntas SET vecesFallada = vecesFallada + 1 WHERE id = :id")
    fun registrarFalloPregunta(id: Int)
}