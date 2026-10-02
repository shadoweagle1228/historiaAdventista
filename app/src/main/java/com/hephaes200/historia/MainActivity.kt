package com.hephaes200.historia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.booleanResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.hephaes200.historia.ui.HistoriaViewModel
import com.hephaes200.historia.ui.NavEstado
import com.hephaes200.historia.ui.theme.HistoriaTheme

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

// Orquestador de pantallas usando NavEstado tipado (sin numeros magicos)
@Composable
fun AppNavegacion(viewModel: HistoriaViewModel) {
    val navEstado by viewModel.navEstado.collectAsState()

    when (navEstado) {
        is NavEstado.Menu      -> PantallaMenu(viewModel)
        is NavEstado.Trivia    -> PantallaTrivia(viewModel)
        is NavEstado.Resultados -> PantallaResultados(viewModel)
    }
}

@Composable
fun PantallaMenu(viewModel: HistoriaViewModel) {
    val racha by viewModel.rachaDias.collectAsState()
    val rango by viewModel.rangoActual.collectAsState()
    val listaCapitulos by viewModel.capitulos.collectAsState()

    // rememberSaveable: sobrevive a rotaciones de pantalla
    var textoBusqueda by rememberSaveable { mutableStateOf("") }

    // Filtrado memorizado para no recalcular en cada recomposicion
    val capitulosFiltrados = remember(listaCapitulos, textoBusqueda) {
        listaCapitulos.filter { capitulo ->
            val textoCompleto = "cap ${capitulo.id} capitulo ${capitulo.id} ${capitulo.titulo}"
            textoCompleto.contains(textoBusqueda.trim(), ignoreCase = true)
        }
    }

    // LazyColumn para todo el menu: header como item{}, lista de capitulos con items()
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(vertical = 24.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.app_name),
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(R.string.app_subtitle),
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // La tarjeta de rango y racha solo aparece en el flavor "historia"
            // (controlado por R.bool.show_rango_racha en cada flavor/res/values/bools.xml)
            if (booleanResource(R.bool.show_rango_racha)) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Text(text = "⭐ Rango: $rango", fontWeight = FontWeight.Bold)
                        Text(text = "🔥 Racha: $racha días", fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Text(text = "Selecciona un Modo de Juego", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.iniciarJuego(0) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                contentPadding = PaddingValues(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(text = "Desafio Aleatorio (Global)", fontSize = 18.sp)
            }

            Button(
                onClick = { viewModel.iniciarJuego(2) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                contentPadding = PaddingValues(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(text = "Banco de Errores (Repaso)", fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(24.dp))
            Text(text = "Estudio por Capitulos", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            if (listaCapitulos.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                Spacer(modifier = Modifier.height(16.dp))
            } else {
                OutlinedTextField(
                    value = textoBusqueda,
                    onValueChange = { textoBusqueda = it },
                    label = { Text("Buscar capitulo...") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Buscar")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (capitulosFiltrados.isEmpty()) {
                    Text(
                        text = "No encontramos ningun capitulo con ese nombre.",
                        modifier = Modifier.padding(vertical = 16.dp).fillMaxWidth(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        // Botones de capitulos renderizados de forma eficiente con LazyColumn
        if (listaCapitulos.isNotEmpty()) {
            items(capitulosFiltrados) { capitulo ->
                Button(
                    onClick = { viewModel.iniciarJuego(1, capitulo.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    contentPadding = PaddingValues(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text(
                        text = "Cap ${capitulo.id}: ${capitulo.titulo}",
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun PantallaTrivia(viewModel: HistoriaViewModel) {
    val preguntas by viewModel.preguntas.collectAsState()
    val indice by viewModel.indiceActual.collectAsState()
    val puntaje by viewModel.puntaje.collectAsState()
    val vidas by viewModel.vidas.collectAsState()
    val feedback by viewModel.feedbackActual.collectAsState()
    val opcionesOcultas by viewModel.opcionesOcultas.collectAsState()
    val comodinUsado by viewModel.comodinUsado.collectAsState()
    val pasoLibreUsado by viewModel.pasoLibreUsado.collectAsState()
    val segundaOportUsada by viewModel.segundaOportUsada.collectAsState()
    val escudoActivo by viewModel.escudoActivo.collectAsState()
    val lottieComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.confetti))

    val lottieProgress by animateLottieCompositionAsState(
        composition = lottieComposition,
        isPlaying = feedback?.esCorrecta == true,
        iterations = 1
    )

    // Pantalla de carga: las preguntas aun no estan listas
    if (preguntas.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val preguntaActual = preguntas[indice]
    val opcionesList = preguntaActual.opciones.split(",").map { it.trim() }

    // Pop-up de retroalimentacion (bloquea interaccion hasta tocar "Continuar")
    feedback?.let { fb ->
        AlertDialog(
            onDismissRequest = { /* Vacio: obliga a tocar Continuar */ },
            title = {
                Text(
                    text = if (fb.esCorrecta) "¡Correcto! 🎉" else "Incorrecto ❌",
                    fontWeight = FontWeight.Bold,
                    color = if (fb.esCorrecta) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            },
            text = { Text(text = fb.mensaje, fontSize = 16.sp) },
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
            // Cabecera: boton atras, corazones, puntaje
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.volverAlMenu() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver al Menu",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    // coerceIn(0,3): evita crash si vidas baja de 0 por edge case
                    val vidasSeguras = vidas.coerceIn(0, 3)
                    val corazones = "❤️".repeat(vidasSeguras) + "🤍".repeat(3 - vidasSeguras)
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

            // Botones de opciones (oculta las eliminadas por el comodin 50/50 o el escudo)
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

            // Comodines: solo visibles mientras no hay feedback activo
            if (feedback == null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.usarComodin5050() },
                        enabled = !comodinUsado,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(8.dp)
                    ) {
                        Text(text = "🪄\n50/50", fontSize = 12.sp, textAlign = TextAlign.Center)
                    }

                    OutlinedButton(
                        onClick = { viewModel.usarPasoLibre() },
                        enabled = !pasoLibreUsado,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(8.dp)
                    ) {
                        Text(text = "🏃\nSaltar", fontSize = 12.sp, textAlign = TextAlign.Center)
                    }

                    OutlinedButton(
                        onClick = { viewModel.usarSegundaOportunidad() },
                        enabled = !segundaOportUsada,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(8.dp),
                        colors = if (escudoActivo)
                            ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        else
                            ButtonDefaults.outlinedButtonColors()
                    ) {
                        Text(text = "🛡️\nEscudo", fontSize = 12.sp, textAlign = TextAlign.Center)
                    }
                }
            }

            // Animacion de confetti al acertar
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

// Pantalla unificada de resultados: muestra rango, racha, puntaje maximo y mensaje motivacional
@Composable
fun PantallaResultados(viewModel: HistoriaViewModel) {
    val puntaje by viewModel.puntaje.collectAsState()
    val preguntas by viewModel.preguntas.collectAsState()
    val puntajeMaximo by viewModel.puntajeMaximo.collectAsState()
    val rachaDias by viewModel.rachaDias.collectAsState()
    val rango by viewModel.rangoActual.collectAsState()
    val vidas by viewModel.vidas.collectAsState()

    val total = preguntas.size * 10
    val titulo = if (vidas > 0) "Ronda Completada!" else "Sin Vidas!"
    val mensaje = if (vidas > 0) {
        "Excelente trabajo. Has demostrado un gran dominio de este tema."
    } else {
        "Se agotaron las vidas, pero cada error es un paso mas hacia el dominio del tema."
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = titulo,
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            color = if (vidas > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Rango Actual: $rango", fontSize = 22.sp, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Racha de Estudio: $rachaDias dias", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(text = "Puntaje Maximo: $puntajeMaximo", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = mensaje,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "Tu puntaje en esta ronda:", fontSize = 20.sp)
        Text(
            text = "$puntaje / $total",
            fontSize = 48.sp,
            fontWeight = FontWeight.Black,
            color = if (vidas > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { viewModel.reiniciarJuego() },
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(16.dp)
        ) {
            Text(text = "Jugar otra ronda", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = { viewModel.volverAlMenu() },
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(16.dp)
        ) {
            Text(text = "Volver al Menu", fontSize = 18.sp)
        }
    }
}
