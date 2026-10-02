package com.hephaes200.historia.data

import android.app.Application
import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader

// Wrapper para parsear el JSON de assets
data class HistoriaDataWrapper(
    val capitulos: List<Capitulo>,
    val preguntas: List<Pregunta>
)

class HistoriaRepository(private val historiaDao: HistoriaDao, private val context: Application) {

    suspend fun inicializarDatos() {
        // Verificamos si la base de datos ya tiene información
        val capitulosGuardados = historiaDao.obtenerTodosLosCapitulos()

        if (capitulosGuardados.isEmpty()) {
            poblarBaseDeDatos()
        }
    }

    private suspend fun poblarBaseDeDatos() {
        context.assets.open("historia_base.json").use { inputStream ->
            val reader = InputStreamReader(inputStream)
            val type = object : TypeToken<HistoriaDataWrapper>() {}.type

            // Parseamos el JSON
            val dataWrapper: HistoriaDataWrapper = Gson().fromJson(reader, type)

            // Como el JSON ya está separado, los insertamos directamente en SQLite
            // sin necesidad de hacer transformaciones ni bucles (forEeach)
            historiaDao.insertarCapitulos(dataWrapper.capitulos)
            historiaDao.insertarPreguntas(dataWrapper.preguntas)
        }
    }
}