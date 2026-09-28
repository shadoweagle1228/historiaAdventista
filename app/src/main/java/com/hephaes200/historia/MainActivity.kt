package com.hephaes200.historia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hephaes200.historia.ui.HistoriaViewModel
import com.hephaes200.historia.ui.theme.HistoriaTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.material3.TextButton
import kotlin.math.ceil
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material.icons.filled.Search

class MainActivity : ComponentActivity() {

    private val viewModel: HistoriaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HistoriaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavegacion(viewModel)
                }
            }
        }
    }
}

// Orquestador de pantallas
@Composable
fun AppNavegacion(viewModel: HistoriaViewModel) {
    val estadoNavegacion = viewModel.estadoNavegacion.collectAsState().value

    when (estadoNavegacion) {
        0 -> PantallaMenu(viewModel)
        1, 2 -> PantallaTrivia(viewModel)
        3 -> PantallaResumen(viewModel)
    }
}

@Composable
fun PantallaMenu(viewModel: HistoriaViewModel) {
    val rango = viewModel.obtenerRangoActual()
    val racha = viewModel.rachaDias.collectAsState().value
    // Observamos la lista de capítulos desde la base de datos
    val listaCapitulos by viewModel.capitulos.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Text(text = "La Rama Quebrada", fontSize = 36.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center)
        Text(text = "Trivia y Estudio", fontSize = 20.sp, color = MaterialTheme.colorScheme.secondary)

        Spacer(modifier = Modifier.height(48.dp))
        Text(text = "Selecciona un Modo de Juego", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.iniciarJuego(0) },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            contentPadding = PaddingValues(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(text = "🎲 Desafío Aleatorio (Global)", fontSize = 18.sp)
        }

        Button(
            onClick = { viewModel.iniciarJuego(2) },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            contentPadding = PaddingValues(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text(text = "🛠️ Banco de Errores (Repaso)", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "Estudio por Capítulos", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        // DIBUJADO DINÁMICO DE BOTONES
        if (listaCapitulos.isEmpty()) {
            CircularProgressIndicator() // Muestra un indicador mientras lee la BD
        } else {
            // 1. Estado de memoria (Solo necesitamos el de la búsqueda)
            var textoBusqueda by remember { mutableStateOf("") }

            // 2. LA BARRA DE BÚSQUEDA
            OutlinedTextField(
                value = textoBusqueda,
                onValueChange = { textoBusqueda = it },
                label = { Text("Buscar capítulo...") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Buscar")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3. LÓGICA DE FILTRADO (Con la búsqueda a prueba de balas)
            val capitulosFiltrados = listaCapitulos.filter { capitulo ->
                val textoCompleto = "cap ${capitulo.id} capitulo ${capitulo.id} ${capitulo.titulo}"
                textoCompleto.contains(textoBusqueda.trim(), ignoreCase = true)
            }

            // 4. LISTA CONTINUA DE RESULTADOS
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {

                // Mensaje si no hay resultados
                if (capitulosFiltrados.isEmpty()) {
                    Text(
                        text = "No encontramos ningún capítulo con ese nombre.",
                        modifier = Modifier.padding(vertical = 16.dp).fillMaxWidth(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                } else {
                    // Dibujamos todos los capítulos que pasen el filtro
                    capitulosFiltrados.forEach { capitulo ->
                        Button(
                            onClick = { viewModel.iniciarJuego(1, capitulo.id) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            contentPadding = PaddingValues(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Text(
                                text = "📖 Cap ${capitulo.id}: ${capitulo.titulo}",
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun PantallaTrivia(viewModel: HistoriaViewModel) {
    // Observamos todos los estados del juego
    val preguntas by viewModel.preguntas.collectAsState()
    val indice by viewModel.indiceActual.collectAsState()
    val puntaje by viewModel.puntaje.collectAsState()
    val terminado by viewModel.juegoTerminado.collectAsState()
    val vidas by viewModel.vidas.collectAsState()
    val feedback by viewModel.feedbackActual.collectAsState()
    val opcionesOcultas = viewModel.opcionesOcultas.collectAsState().value
    val comodinUsado = viewModel.comodinUsado.collectAsState().value
    val lottieComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.confetti))
    val pasoLibreUsado = viewModel.pasoLibreUsado.collectAsState().value
    val segundaOportUsada = viewModel.segundaOportUsada.collectAsState().value
    val escudoActivo = viewModel.escudoActivo.collectAsState().value

    val lottieProgress by animateLottieCompositionAsState(
        composition = lottieComposition,
        isPlaying = feedback?.esCorrecta == true,
        iterations = 1
    )

    // Pantalla de carga inicial
    if (preguntas.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // Pantalla final (Resultados)
    if (terminado) {
        val puntajeMax = viewModel.puntajeMaximo.collectAsState().value
        val racha = viewModel.rachaDias.collectAsState().value
        val rango = viewModel.obtenerRangoActual()

        PantallaResultados(
            puntaje = puntaje,
            total = preguntas.size * 10,
            puntajeMaximo = puntajeMax,
            rachaDias = racha,
            rangoJA = rango,
            vidasRestantes = vidas,
            onReiniciar = { viewModel.reiniciarJuego() },
            onVolverMenu = { viewModel.volverAlMenu() }
        )
    } else {
        // Pantalla de la pregunta actual
        val preguntaActual = preguntas[indice]
        val opciones = preguntaActual.opciones.split(",")

        // 1. POP-UP DE RETROALIMENTACIÓN
        feedback?.let { fb ->
            AlertDialog(
                onDismissRequest = { /* Vacio para obligar a tocar "Continuar" */ },
                title = {
                    Text(
                        text = if (fb.esCorrecta) "¡Correcto! 🎉" else "Incorrecto ❌",
                        fontWeight = FontWeight.Bold,
                        color = if (fb.esCorrecta) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                },
                text = {
                    Text(text = fb.mensaje, fontSize = 16.sp)
                },
                confirmButton = {
                    Button(onClick = { viewModel.avanzarPregunta() }) {
                        Text("Continuar")
                    }
                }
            )
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 2. CABECERA: VIDAS Y PUNTAJE
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { viewModel.volverAlMenu() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Volver al Menú",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        val corazones = "❤️".repeat(vidas) + "🤍".repeat(3 - vidas)
                        Text(text = corazones, fontSize = 24.sp)
                    }
                    Text(text = "Puntos: $puntaje", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Pregunta ${indice + 1} de ${preguntas.size}",
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = preguntaActual.enunciado,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    lineHeight = 32.sp
                )
                Spacer(modifier = Modifier.height(48.dp))

                val preguntas = viewModel.preguntas.collectAsState().value
                val indiceActual = viewModel.indiceActual.collectAsState().value

                val pregunta = preguntas[indiceActual]

                val opcionesList = pregunta.opciones.split(",").map { it.trim() }

                // Botones de opciones
                opcionesList.forEach { opcion ->
                    if (!opcionesOcultas.contains(opcion)) {
                        Button(
                            onClick = { viewModel.verificarRespuesta(opcion) },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            contentPadding = PaddingValues(16.dp),
                            enabled = feedback == null
                        ) {
                            Text(text = opcion, fontSize = 18.sp, textAlign = TextAlign.Center)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                if (feedback == null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Comodín 1: 50/50
                        OutlinedButton(
                            onClick = { viewModel.usarComodin5050() },
                            enabled = !comodinUsado,
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(8.dp)
                        ) {
                            Text(text = "🪄\n50/50", fontSize = 12.sp, textAlign = TextAlign.Center)
                        }

                        // Comodín 2: Paso Libre
                        OutlinedButton(
                            onClick = { viewModel.usarPasoLibre() },
                            enabled = !pasoLibreUsado,
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(8.dp)
                        ) {
                            Text(text = "🏃\nSaltar", fontSize = 12.sp, textAlign = TextAlign.Center)
                        }

                        // Comodín 3: Escudo / Segunda Oportunidad
                        OutlinedButton(
                            onClick = { viewModel.usarSegundaOportunidad() },
                            enabled = !segundaOportUsada,
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(8.dp),
                            // Le damos un color especial si está activo esperando a ser usado
                            colors = if (escudoActivo) ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer) else ButtonDefaults.outlinedButtonColors()
                        ) {
                            Text(text = "🛡️\nEscudo", fontSize = 12.sp, textAlign = TextAlign.Center)
                        }
                    }
                }

                if (feedback?.esCorrecta == true && lottieProgress < 1f) {
                    LottieAnimation(
                        composition = lottieComposition,
                        progress = { lottieProgress },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
fun PantallaResultados(
    puntaje: Int,
    total: Int,
    puntajeMaximo: Int,
    rachaDias: Int,
    rangoJA: String,
    vidasRestantes: Int,
    onReiniciar: () -> Unit,
    onVolverMenu: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val titulo = if (vidasRestantes > 0) "¡Ronda Completada!" else "¡Sin Vidas!"
        Text(text = titulo, fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "⭐ Rango Actual: $rangoJA", fontSize = 22.sp, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "🔥 Racha de Estudio: $rachaDias días", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(text = "🏆 Puntaje Máximo: $puntajeMaximo", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(48.dp))
        Text(text = "Tu puntaje en esta ronda:", fontSize = 20.sp)
        Text(
            text = "$puntaje / $total",
            fontSize = 48.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onReiniciar,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(16.dp)
        ) {
            Text(text = "Jugar otra ronda", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onVolverMenu,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(16.dp)
        ) {
            Text(text = "Volver al Menú", fontSize = 18.sp)
        }
    }
}

@Composable
fun PantallaResumen(viewModel: HistoriaViewModel) {
    val puntaje = viewModel.puntaje.collectAsState().value
    val vidas = viewModel.vidas.collectAsState().value

    // Evaluamos el resultado para dar un feedback apropiado
    val titulo = if (vidas > 0) "¡Capítulo Completado!" else "¡Sigue Intentándolo!"
    val mensaje = if (vidas > 0) {
        "Excelente trabajo. Has demostrado un gran dominio de este tema."
    } else {
        "Se agotaron las vidas, pero cada error es un paso más hacia el dominio del tema."
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = titulo,
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Puntaje Final", fontSize = 18.sp)
                Text(
                    text = "$puntaje",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Vidas restantes: $vidas ❤️", fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = mensaje,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = { viewModel.volverAlMenu() },
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(16.dp)
        ) {
            Text(text = "Volver al Menú Principal", fontSize = 18.sp)
        }
    }
}