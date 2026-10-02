package com.hephaes200.historia.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hephaes200.historia.data.HistoriaDatabase
import com.hephaes200.historia.data.HistoriaRepository
import com.hephaes200.historia.data.Pregunta
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.media.SoundPool
import com.hephaes200.historia.R

// Clases auxiliares
data class Feedback(val esCorrecta: Boolean, val mensaje: String)
enum class Pantalla { MENU, JUEGO } // Control de navegación

class HistoriaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HistoriaRepository
    private val dao = HistoriaDatabase.getDatabase(application).historiaDao()
    private val prefs = application.getSharedPreferences("JuegoPrefs", Context.MODE_PRIVATE)

    // Estado de Navegación
    private val _pantallaActual = MutableStateFlow(Pantalla.MENU)
    val pantallaActual: StateFlow<Pantalla> = _pantallaActual

    // Estados del Juego
    private val _preguntas = MutableStateFlow<List<Pregunta>>(emptyList())
    val preguntas: StateFlow<List<Pregunta>> = _preguntas

    private val _indiceActual = MutableStateFlow(0)
    val indiceActual: StateFlow<Int> = _indiceActual

    private val _vidas = MutableStateFlow(3)
    val vidas: StateFlow<Int> = _vidas

    private val _feedbackActual = MutableStateFlow<Feedback?>(null)
    val feedbackActual: StateFlow<Feedback?> = _feedbackActual

    private val _puntaje = MutableStateFlow(0)
    val puntaje: StateFlow<Int> = _puntaje

    private val _juegoTerminado = MutableStateFlow(false)
    val juegoTerminado: StateFlow<Boolean> = _juegoTerminado

    // Estados de Logros
    private val _puntajeMaximo = MutableStateFlow(prefs.getInt("puntaje_maximo", 0))
    val puntajeMaximo: StateFlow<Int> = _puntajeMaximo

    private val _rachaDias = MutableStateFlow(prefs.getInt("racha_dias", 0))
    val rachaDias: StateFlow<Int> = _rachaDias

    // Guardamos el último modo jugado para el botón "Jugar otra vez"
    private var ultimoModo: Int = 0
    private var ultimoCapitulo: Int = 1

    private val _capitulos = MutableStateFlow<List<com.hephaes200.historia.data.Capitulo>>(emptyList())
    val capitulos: StateFlow<List<com.hephaes200.historia.data.Capitulo>> = _capitulos

    private val soundPool = SoundPool.Builder().setMaxStreams(2).build()
    private val sonidoAcierto = soundPool.load(application, R.raw.acierto, 1)
    private val sonidoError = soundPool.load(application, R.raw.error, 1)

    private val _comodinUsado = MutableStateFlow(false)
    val comodinUsado: StateFlow<Boolean> = _comodinUsado

    private val _opcionesOcultas = MutableStateFlow<Set<String>>(emptySet())
    val opcionesOcultas: StateFlow<Set<String>> = _opcionesOcultas

    private val _pasoLibreUsado = MutableStateFlow(false)
    val pasoLibreUsado: StateFlow<Boolean> = _pasoLibreUsado

    private val _segundaOportUsada = MutableStateFlow(false)
    val segundaOportUsada: StateFlow<Boolean> = _segundaOportUsada

    private val _escudoActivo = MutableStateFlow(false)
    val escudoActivo: StateFlow<Boolean> = _escudoActivo

    private val _estadoNavegacion = MutableStateFlow(0)

    val estadoNavegacion: StateFlow<Int> = _estadoNavegacion

    init {
        repository = HistoriaRepository(dao, application)
        actualizarRachaDiaria()
        // Solo inicializamos el JSON en background, no arrancamos el juego aún
        viewModelScope.launch(Dispatchers.IO) {
            repository.inicializarDatos()
            _capitulos.value = dao.obtenerTodosLosCapitulos()
        }
    }

    // MODO: 0 = Aleatorio, 1 = Por Capítulo, 2 = Banco de Errores
    fun iniciarJuego(modo: Int, capituloId: Int = 1) {
        ultimoModo = modo
        ultimoCapitulo = capituloId

        viewModelScope.launch(Dispatchers.IO) {
            val ronda = when(modo) {
                1 -> dao.obtenerRondaPorCapitulo(capituloId)
                2 -> dao.obtenerRondaDeErrores()
                else -> dao.obtenerRondaAleatoria()
            }

            _preguntas.value = if (ronda.isEmpty() && modo == 2) dao.obtenerRondaAleatoria() else ronda

            _indiceActual.value = 0
            _puntaje.value = 0
            _vidas.value = 3
            _juegoTerminado.value = false
            _feedbackActual.value = null

            _comodinUsado.value = false
            _opcionesOcultas.value = emptySet()
            _pasoLibreUsado.value = false
            _segundaOportUsada.value = false
            _escudoActivo.value = false

            _estadoNavegacion.value = if (modo == 2) 2 else 1
        }
    }

    fun volverAlMenu() {
        _estadoNavegacion.value = 0
        _feedbackActual.value = null
        _pantallaActual.value = Pantalla.MENU
    }

    fun usarComodin5050() {
        if (_comodinUsado.value) return

        val pregunta = _preguntas.value[_indiceActual.value]
        val todasLasOpciones = pregunta.opciones.split(",").map { it.trim() }

        val incorrectas = todasLasOpciones.filter { it != pregunta.respuestaCorrecta }.shuffled().take(2)

        _opcionesOcultas.value = incorrectas.toSet()
        _comodinUsado.value = true
    }

    fun usarPasoLibre() {
        if (_pasoLibreUsado.value) return
        _pasoLibreUsado.value = true

        val pregunta = _preguntas.value[_indiceActual.value]

        _feedbackActual.value = Feedback(
            esCorrecta = true,
            mensaje = "¡Paso Libre activado! 🏃💨\n\nLa respuesta era: ${pregunta.respuestaCorrecta}.\n\n${pregunta.justificacion}"
        )
    }

    fun usarSegundaOportunidad() {
        if (_segundaOportUsada.value) return
        _segundaOportUsada.value = true
        _escudoActivo.value = true
    }

    fun verificarRespuesta(respuestaSeleccionada: String) {
        val pregunta = _preguntas.value[_indiceActual.value]

        if (respuestaSeleccionada == pregunta.respuestaCorrecta) {
            _puntaje.value += 10
            soundPool.play(sonidoAcierto, 1f, 1f, 0, 0, 1f)
            _feedbackActual.value = Feedback(true, "¡Correcto!\n\n${pregunta.justificacion}")
        } else {
            // La respuesta es incorrecta
            if (_escudoActivo.value) {
                // El escudo absorbe el error: oculta la opción incorrecta y desactiva el escudo
                _escudoActivo.value = false
                soundPool.play(sonidoError, 1f, 1f, 0, 0, 1f)

                val nuevasOcultas = _opcionesOcultas.value.toMutableSet()
                nuevasOcultas.add(respuestaSeleccionada)
                _opcionesOcultas.value = nuevasOcultas
            } else {
                _vidas.value -= 1
                soundPool.play(sonidoError, 1f, 1f, 0, 0, 1f)
                _feedbackActual.value = Feedback(false, "Incorrecto. La respuesta era: ${pregunta.respuestaCorrecta}.\n\n${pregunta.justificacion}")

                viewModelScope.launch(Dispatchers.IO) {
                    dao.registrarFalloPregunta(pregunta.id)
                }
            }
        }
    }

    fun avanzarPregunta() {
        if (_vidas.value <= 0) {
            finalizarRonda()
            return
        }
        if (_indiceActual.value < _preguntas.value.size - 1) {
            _indiceActual.value += 1
            _feedbackActual.value = null
            _opcionesOcultas.value = emptySet()
        } else {
            finalizarRonda()
        }
    }

    private fun finalizarRonda() {
        val puntajeHistorico = prefs.getInt("puntaje_historico_total", 0) + _puntaje.value
        prefs.edit().putInt("puntaje_historico_total", puntajeHistorico).apply()

        if (_puntaje.value > _puntajeMaximo.value) {
            _puntajeMaximo.value = _puntaje.value
            prefs.edit().putInt("puntaje_maximo", _puntaje.value).apply()
        }
        _juegoTerminado.value = true
        _estadoNavegacion.value = 3
    }

    fun reiniciarJuego() {
        iniciarJuego(ultimoModo, ultimoCapitulo)
    }

    fun obtenerRangoActual(): String {
        val total = prefs.getInt("puntaje_historico_total", 0)
        return when {
            total >= 1500 -> "Guía Mayor"
            total >= 1000 -> "Guía"
            total >= 700 -> "Viajero"
            total >= 400 -> "Orientador"
            total >= 200 -> "Explorador"
            total >= 100 -> "Compañero"
            else -> "Amigo"
        }
    }

    private fun actualizarRachaDiaria() {
        val diaActual = System.currentTimeMillis() / (1000 * 60 * 60 * 24)
        val ultimoDiaJugado = prefs.getLong("ultimo_dia", 0)
        var racha = prefs.getInt("racha_dias", 0)

        if (ultimoDiaJugado == 0L || diaActual == ultimoDiaJugado + 1) {
            racha += 1
        } else if (diaActual > ultimoDiaJugado + 1) {
            racha = 1
        }

        _rachaDias.value = racha
        prefs.edit().putLong("ultimo_dia", diaActual).putInt("racha_dias", racha).apply()
    }

    override fun onCleared() {
        super.onCleared()
        soundPool.release() // Liberamos la memoria del audio
    }
}