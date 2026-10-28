package com.example.cmqsalud.View

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cmqsalud.Model.DocumentoEntity
import com.example.cmqsalud.Model.Usuario
import com.example.cmqsalud.ViewModel.LoginViewModel
import com.example.cmqsalud.R // Asegúrate de que este import coincida con el paquete de tu proyecto
import kotlinx.coroutines.delay

// --- PALETA DE COLORES VICHY ---
val VichyPrimary = Color(0xFF05AD98)
val VichySurfaceGray = Color(0xFFBBBFBF)
val VichyDarkGray = Color(0xFF4A4A4A)
val VichyBlackText = Color(0xFF1A1A1A)
val VichyWhite = Color(0xFFFFFFFF)
val VichyBackgroundSoft = Color(0xFFF6F8F8)

// Almacén estático para respaldo de imágenes por título
object GaleriaRespaldoLocal {
    val fotosPorTitulo = mutableStateMapOf<String, Bitmap>()
}

@Composable
fun basicTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    errorMessage: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = VichyDarkGray) },
        singleLine = true,
        isError = errorMessage != null,
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = VichyBlackText,
            unfocusedTextColor = VichyBlackText,
            errorTextColor = VichyBlackText,
            focusedBorderColor = VichyPrimary,
            unfocusedBorderColor = VichySurfaceGray
        ),
        supportingText = {
            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    )
}

@Composable
fun basicButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = VichyPrimary),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .padding(vertical = 4.dp)
    ) {
        Text(text = text, fontSize = 15.sp, color = VichyWhite, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = viewModel()
) {
    val rut by viewModel.rutLogin.collectAsState()
    val password by viewModel.passLogin.collectAsState()
    val errorRut by viewModel.errorRutLogin.collectAsState()
    val errorPass by viewModel.errorPassLogin.collectAsState()
    val estadoLogin by viewModel.estadoLogin.collectAsState()
    val usuarioActivo by viewModel.usuarioActivo.collectAsState()
    val mensajeConfirmacion by viewModel.mensajeConfirmacion.collectAsState()

    var mostrandoRegistro by remember { mutableStateOf(false) }

    if (estadoLogin == "Exito" && usuarioActivo != null) {
        HomeScreen(
            modifier = modifier,
            usuario = usuarioActivo!!,
            viewModel = viewModel,
            onCerrarSesion = { viewModel.cerrarSesion() }
        )
    } else if (mostrandoRegistro) {
        RegistroScreen(
            modifier = modifier,
            viewModel = viewModel,
            onVolverAlLogin = {
                viewModel.resetearFormularioRegistro()
                mostrandoRegistro = false
            }
        )
    } else {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(VichyBackgroundSoft)
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = VichyWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // --- NUEVO LOGO EN LOGIN ---
                    Image(
                        painter = painterResource(id = R.drawable.logo_cmq),
                        contentDescription = "Logo CMQ Salud",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .padding(bottom = 16.dp)
                    )

                    if (mensajeConfirmacion.isNotEmpty()) {
                        Surface(
                            color = VichyPrimary.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Text(
                                text = mensajeConfirmacion,
                                color = VichyPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    basicTextField(
                        value = rut,
                        onValueChange = { viewModel.onRutLoginChange(it) },
                        label = "RUT del Funcionario (Ej: 12345678-9)",
                        errorMessage = errorRut
                    )

                    basicTextField(
                        value = password,
                        onValueChange = { viewModel.onPassLoginChange(it) },
                        label = "Contraseña",
                        errorMessage = errorPass
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    basicButton(
                        text = "Iniciar Sesión",
                        onClick = {
                            viewModel.limpiarMensajeConfirmacion()
                            viewModel.validarCredenciales()
                        }
                    )

                    OutlinedButton(
                        onClick = {
                            viewModel.limpiarMensajeConfirmacion()
                            viewModel.resetearFormularioRegistro()
                            mostrandoRegistro = true
                        },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.5.dp, VichyPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .padding(vertical = 4.dp)
                    ) {
                        Text(text = "Registrarme", fontSize = 15.sp, color = VichyPrimary, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    if (estadoLogin.startsWith("Error")) {
                        Text(
                            text = estadoLogin,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    usuario: Usuario,
    viewModel: LoginViewModel,
    onCerrarSesion: () -> Unit
) {
    var moduloActivo by remember { mutableStateOf("LISTADO") }
    var textoBusqueda by remember { mutableStateOf("") }

    val listaDocumentos by viewModel.listaDocumentos.collectAsState()
    val docForm by viewModel.docFormState.collectAsState()
    val bitacora by viewModel.bitacoraTrazabilidad.collectAsState()

    val puedeSubir = usuario.perfil == "Funcionario" || usuario.perfil == "Desarrollador"
    val puedeVerLogs = usuario.perfil == "Administrativo" || usuario.perfil == "Jefatura" || usuario.perfil == "Desarrollador"

    var fotoTemporalFormulario by remember { mutableStateOf<Bitmap?>(null) }
    var mostrarAnimacionGuardado by remember { mutableStateOf(false) }
    var fotoAmpliada by remember { mutableStateOf<Bitmap?>(null) }
    var documentoAEliminar by remember { mutableStateOf<DocumentoEntity?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            fotoTemporalFormulario = bitmap
            viewModel.onFotoCamaraCapturada(true)
        }
    }

    LaunchedEffect(mostrarAnimacionGuardado) {
        if (mostrarAnimacionGuardado) {
            delay(1100)
            mostrarAnimacionGuardado = false
            moduloActivo = "LISTADO"
        }
    }

    if (documentoAEliminar != null) {
        AlertDialog(
            onDismissRequest = { documentoAEliminar = null },
            title = { Text("Confirmar Eliminación", fontWeight = FontWeight.Bold, color = VichyBlackText) },
            text = { Text("¿Está seguro que desea eliminar el expediente '${documentoAEliminar?.titulo}'? Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.eliminarDocumentoDeRoom(documentoAEliminar!!)
                        documentoAEliminar = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar", color = VichyWhite, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { documentoAEliminar = null },
                    border = BorderStroke(1.dp, VichyDarkGray)
                ) {
                    Text("Cancelar", color = VichyDarkGray, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (fotoAmpliada != null) {
        Dialog(
            onDismissRequest = { fotoAmpliada = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.95f))
                    .clickable { fotoAmpliada = null },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = fotoAmpliada!!.asImageBitmap(),
                    contentDescription = "Documento Ampliado",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
                Text(
                    text = "✕ Cerrar",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(24.dp)
                )
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(VichyBackgroundSoft)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        // --- CABECERA ERGONÓMICA CON LOGO PEQUEÑO ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = VichyWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo_cmq),
                            contentDescription = "Logo Pequeño",
                            modifier = Modifier
                                .size(46.dp)
                                .padding(end = 10.dp)
                        )
                        Column {
                            Text(
                                text = "Repositorio Central",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = VichyPrimary
                            )
                            Text(
                                text = usuario.nombre,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = VichyBlackText
                            )
                        }
                    }
                    Surface(
                        color = VichyPrimary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = usuario.perfil,
                            color = VichyPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "RUT: ${usuario.rut}  •  ${usuario.centroSalud}",
                    fontSize = 13.sp,
                    color = VichyDarkGray
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- BARRA DE NAVEGACIÓN ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val esListado = moduloActivo == "LISTADO"
            Button(
                onClick = { moduloActivo = "LISTADO" },
                colors = ButtonDefaults.buttonColors(containerColor = if (esListado) VichyPrimary else VichyWhite),
                border = BorderStroke(1.dp, VichyPrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("📁 Expedientes", fontSize = 11.sp, color = if (esListado) VichyWhite else VichyPrimary, fontWeight = FontWeight.Bold)
            }

            if (puedeSubir) {
                val esForm = moduloActivo == "FORMULARIO"
                Button(
                    onClick = {
                        viewModel.limpiarFormularioDocumento()
                        fotoTemporalFormulario = null
                        moduloActivo = "FORMULARIO"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = if (esForm) VichyPrimary else VichyWhite),
                    border = BorderStroke(1.dp, VichyPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("➕ Subir", fontSize = 11.sp, color = if (esForm) VichyWhite else VichyPrimary, fontWeight = FontWeight.Bold)
                }
            }

            if (puedeVerLogs) {
                val esTrazabilidad = moduloActivo == "TRAZABILIDAD"
                Button(
                    onClick = { moduloActivo = "TRAZABILIDAD" },
                    colors = ButtonDefaults.buttonColors(containerColor = if (esTrazabilidad) VichyPrimary else VichyWhite),
                    border = BorderStroke(1.dp, VichyPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("🛡️ Logs", fontSize = 11.sp, color = if (esTrazabilidad) VichyWhite else VichyPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- CONTENIDO ANIMADO ---
        AnimatedContent(targetState = moduloActivo, label = "cambio_modulo") { modulo ->
            Column(modifier = Modifier.fillMaxWidth()) {

                // --- MÓDULO 1: FORMULARIO CRUD ---
                if (modulo == "FORMULARIO" && puedeSubir) {
                    val tiposDoc = listOf("Permiso Administrativo", "Certificado Capacitación", "Contrato / Anexo Laboral", "Licencia / Respaldo Médico")
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = VichyWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            AnimatedVisibility(
                                visible = mostrarAnimacionGuardado,
                                enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
                                exit = fadeOut()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(72.dp)
                                            .background(VichyPrimary, CircleShape)
                                    ) { Text("✓", color = VichyWhite, fontSize = 38.sp, fontWeight = FontWeight.Bold) }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text("¡Documento guardado con éxito!", color = VichyPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                            }

                            if (!mostrarAnimacionGuardado) {
                                Text(
                                    text = if (docForm.idEdicion == null) "Cargar Nuevo Documento" else "Editar Documento #${docForm.idEdicion}",
                                    fontSize = 18.sp, fontWeight = FontWeight.Bold, color = VichyPrimary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                basicTextField(value = docForm.titulo, onValueChange = { viewModel.onTituloDocChange(it) }, label = "Título (Obligatorio, mín 5 caracteres)", errorMessage = docForm.errorTitulo)
                                basicTextField(value = docForm.periodoHoras, onValueChange = { viewModel.onPeriodoDocChange(it) }, label = "Período u Horas (Ej: 30 hrs)", errorMessage = docForm.errorPeriodo)

                                Spacer(modifier = Modifier.height(10.dp))
                                Text("Categoría Documental:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = VichyBlackText)
                                tiposDoc.forEach { tipo ->
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                        RadioButton(selected = (docForm.tipoSeleccionado == tipo), onClick = { viewModel.onTipoDocChange(tipo) })
                                        Text(text = tipo, fontSize = 14.sp, color = VichyBlackText, fontWeight = FontWeight.Medium)
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedButton(
                                    onClick = { cameraLauncher.launch() },
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.5.dp, VichyPrimary),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(text = if (docForm.tieneFotoCamara || fotoTemporalFormulario != null) "📷 Volver a tomar" else "📷 Capturar Documento", color = VichyPrimary, fontWeight = FontWeight.Bold)
                                }

                                val fotoActual = fotoTemporalFormulario ?: GaleriaRespaldoLocal.fotosPorTitulo[docForm.titulo.trim()]
                                AnimatedVisibility(
                                    visible = fotoActual != null,
                                    enter = scaleIn(spring(dampingRatio = Spring.DampingRatioLowBouncy)) + fadeIn(),
                                    exit = fadeOut()
                                ) {
                                    if (fotoActual != null) {
                                        Column(modifier = Modifier.fillMaxWidth().padding(top = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                            Image(bitmap = fotoActual.asImageBitmap(), contentDescription = "Vista previa", contentScale = ContentScale.Crop, modifier = Modifier.size(150.dp).clip(RoundedCornerShape(12.dp)).border(2.dp, VichyPrimary, RoundedCornerShape(12.dp)))
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(text = "✓ Imagen vinculada al expediente", fontSize = 12.sp, color = VichyPrimary, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))
                                basicButton(
                                    text = if (docForm.idEdicion == null) "Guardar" else "Actualizar",
                                    onClick = {
                                        val tituloLimpio = docForm.titulo.trim()
                                        if (tituloLimpio.length >= 5 && docForm.periodoHoras.trim().isNotEmpty()) {
                                            if (fotoTemporalFormulario != null) { GaleriaRespaldoLocal.fotosPorTitulo[tituloLimpio] = fotoTemporalFormulario!! }
                                            viewModel.guardarDocumentoEnRoom()
                                            mostrarAnimacionGuardado = true
                                        } else {
                                            viewModel.guardarDocumentoEnRoom()
                                        }
                                    }
                                )
                                OutlinedButton(onClick = { viewModel.limpiarFormularioDocumento(); fotoTemporalFormulario = null; moduloActivo = "LISTADO" }, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                                    Text("Cancelar", color = VichyDarkGray, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // --- MÓDULO 2: LISTADO DE EXPEDIENTES ---
                if (modulo == "LISTADO") {

                    OutlinedTextField(
                        value = textoBusqueda,
                        onValueChange = { textoBusqueda = it },
                        label = { Text("🔍 Buscar por RUT o Nombre", color = VichyDarkGray) },
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    )

                    val documentosFiltrados = listaDocumentos.filter { doc ->
                        val pasaFiltroPrivacidad = (usuario.perfil != "Funcionario") || (doc.rutFuncionario == usuario.rut)
                        val pasaBusqueda = doc.nombreFuncionario.contains(textoBusqueda, ignoreCase = true) || doc.rutFuncionario.contains(textoBusqueda, ignoreCase = true)
                        pasaFiltroPrivacidad && pasaBusqueda
                    }.sortedByDescending { it.id }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Documentos Encontrados: ${documentosFiltrados.size}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = VichyPrimary)
                    }

                    if (documentosFiltrados.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = VichyWhite)
                        ) {
                            Text("No se encontraron documentos registrados.", color = VichyDarkGray, modifier = Modifier.padding(20.dp))
                        }
                    }

                    documentosFiltrados.forEach { doc ->
                        val fotoGuardada = GaleriaRespaldoLocal.fotosPorTitulo[doc.titulo.trim()]
                        val esPropietario = doc.rutFuncionario == usuario.rut

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .animateContentSize(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = VichyWhite),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = doc.titulo, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = VichyBlackText)
                                        Text(text = "${doc.tipoDocumento} • ${doc.periodoHoras}", fontSize = 13.sp, color = VichyDarkGray, fontWeight = FontWeight.Medium)
                                    }

                                    if (fotoGuardada != null) {
                                        Image(
                                            bitmap = fotoGuardada.asImageBitmap(),
                                            contentDescription = "Foto del documento",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(64.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .border(1.5.dp, VichyPrimary, RoundedCornerShape(10.dp))
                                                .clickable { fotoAmpliada = fotoGuardada }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(color = VichySurfaceGray.copy(alpha = 0.4f))
                                Spacer(modifier = Modifier.height(8.dp))

                                Text(text = "Funcionario: ${doc.nombreFuncionario} (${doc.centroSalud})", fontSize = 13.sp, color = VichyBlackText)
                                Text(text = "RUT: ${doc.rutFuncionario} • Fecha carga: ${doc.fechaCarga}", fontSize = 12.sp, color = VichyDarkGray)

                                Spacer(modifier = Modifier.height(8.dp))

                                Surface(
                                    color = when {
                                        doc.estado.contains("Aprobado") -> VichyPrimary.copy(alpha = 0.14f)
                                        doc.estado.contains("Rechazado") -> MaterialTheme.colorScheme.error.copy(alpha = 0.14f)
                                        doc.estado.contains("Verificado") -> Color(0xFF1565C0).copy(alpha = 0.14f)
                                        else -> Color(0xFFE65100).copy(alpha = 0.14f)
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "Estado: ${doc.estado}",
                                        fontSize = 13.sp, fontWeight = FontWeight.Bold,
                                        color = when {
                                            doc.estado.contains("Aprobado") -> VichyPrimary
                                            doc.estado.contains("Rechazado") -> MaterialTheme.colorScheme.error
                                            doc.estado.contains("Verificado") -> Color(0xFF1565C0)
                                            else -> Color(0xFFE65100)
                                        },
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }

                                if (esPropietario && puedeSubir) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                                        OutlinedButton(
                                            onClick = {
                                                fotoTemporalFormulario = GaleriaRespaldoLocal.fotosPorTitulo[doc.titulo.trim()]
                                                viewModel.cargarDocumentoParaEditar(doc)
                                                moduloActivo = "FORMULARIO"
                                            },
                                            shape = RoundedCornerShape(8.dp)
                                        ) { Text("✏️ Editar", fontSize = 12.sp, color = VichyPrimary, fontWeight = FontWeight.Bold) }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        OutlinedButton(
                                            onClick = { documentoAEliminar = doc },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                        ) { Text("🗑️ Eliminar", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                                    }
                                }

                                if (usuario.perfil == "Administrativo") {
                                    if (doc.estado.contains("Pendiente")) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Button(
                                            onClick = { viewModel.cambiarEstadoDocumento(doc, "Verificado por Admin") },
                                            colors = ButtonDefaults.buttonColors(containerColor = VichyPrimary),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) { Text("✓ Verificar Integridad (Rol Admin)", fontSize = 13.sp, color = VichyWhite, fontWeight = FontWeight.Bold) }
                                    }
                                }

                                if (usuario.perfil == "Jefatura") {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    if (doc.estado == "Verificado por Admin") {
                                        Row(modifier = Modifier.fillMaxWidth()) {
                                            Button(
                                                onClick = { viewModel.cambiarEstadoDocumento(doc, "Aprobado por Jefatura") },
                                                modifier = Modifier.weight(1f).padding(end = 4.dp),
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = VichyPrimary)
                                            ) { Text("Aprobar", fontSize = 13.sp, color = VichyWhite, fontWeight = FontWeight.Bold) }

                                            Button(
                                                onClick = { viewModel.cambiarEstadoDocumento(doc, "Rechazado por Jefatura") },
                                                modifier = Modifier.weight(1f).padding(start = 4.dp),
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                            ) { Text("Rechazar", fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                                        }
                                    } else if (doc.estado.contains("Pendiente")) {
                                        Text("⏳ Esperando revisión del Administrativo para poder evaluar.", fontSize = 12.sp, color = VichyDarkGray, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }
                    }
                }

                // --- MÓDULO 3: BITÁCORA / LOGS ---
                if (modulo == "TRAZABILIDAD" && puedeVerLogs) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = VichyWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Logs del Sistema", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = VichyPrimary)
                            Text("Registro histórico de accesos y aprobaciones:", fontSize = 13.sp, color = VichyDarkGray, modifier = Modifier.padding(bottom = 12.dp))
                            bitacora.forEach { registro ->
                                Text("• $registro", fontSize = 13.sp, color = VichyBlackText, modifier = Modifier.padding(vertical = 4.dp))
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        basicButton(text = "Cerrar Sesión", onClick = onCerrarSesion)
    }
}

@Composable
fun RegistroScreen(
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel,
    onVolverAlLogin: () -> Unit
) {
    val uiState by viewModel.registroState.collectAsState()
    val centrosSaludAps = listOf("CESFAM Quilpué", "CESFAM Belloto Sur", "CECOSF El Retiro", "Administración Central CMQ")

    LaunchedEffect(uiState.registroExitoso) {
        if (uiState.registroExitoso) {
            delay(1500)
            onVolverAlLogin()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(VichyBackgroundSoft)
            .padding(18.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = VichyWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedVisibility(
                    visible = uiState.registroExitoso,
                    enter = scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(90.dp).background(VichyPrimary, shape = CircleShape)) {
                            Text(text = "✓", color = VichyWhite, fontSize = 52.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "¡Usuario registrado en Room!", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = VichyPrimary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Perfil: Funcionario (${uiState.centroSeleccionado})", fontSize = 14.sp, color = VichyDarkGray)
                    }
                }

                if (!uiState.registroExitoso) {
                    // --- NUEVO LOGO EN REGISTRO ---
                    Image(
                        painter = painterResource(id = R.drawable.logo_cmq),
                        contentDescription = "Logo CMQ Salud",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .padding(bottom = 8.dp)
                    )

                    Text(text = "Registro de Funcionario", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = VichyPrimary, modifier = Modifier.padding(bottom = 4.dp))
                    Text(text = "Rol asignado por seguridad: Funcionario", fontSize = 13.sp, color = VichyDarkGray, modifier = Modifier.padding(bottom = 14.dp))

                    basicTextField(value = uiState.nombre, onValueChange = { viewModel.onNombreRegistroChange(it) }, label = "Nombre Completo", errorMessage = uiState.errorNombre)
                    basicTextField(value = uiState.rut, onValueChange = { viewModel.onRutRegistroChange(it) }, label = "RUT (9 dígitos, guion automático)", errorMessage = uiState.errorRut)
                    basicTextField(value = uiState.contrasena, onValueChange = { viewModel.onContrasenaRegistroChange(it) }, label = "Contraseña (8+ caract, 1 Mayús, 1 Especial)", errorMessage = uiState.errorContrasena)

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Seleccione Establecimiento de Salud (APS):", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = VichyBlackText, modifier = Modifier.align(Alignment.Start))

                    centrosSaludAps.forEach { centro ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            RadioButton(selected = (centro == uiState.centroSeleccionado), onClick = { viewModel.onCentroChange(centro) })
                            Text(text = centro, fontSize = 14.sp, color = VichyBlackText, fontWeight = FontWeight.Medium)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Checkbox(checked = uiState.aceptaPoliticas, onCheckedChange = { viewModel.onPoliticasChange(it) })
                        Text(text = "Acepto políticas de protección de datos de CMQ Salud", fontSize = 13.sp, color = VichyBlackText)
                    }

                    if (uiState.errorPoliticas != null) {
                        Text(text = uiState.errorPoliticas!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.align(Alignment.Start).padding(start = 12.dp, bottom = 8.dp))
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    basicButton(text = "Guardar y Registrar", onClick = { viewModel.procesarRegistro() })
                    OutlinedButton(
                        onClick = onVolverAlLogin,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) { Text(text = "Volver al Inicio de Sesión", color = VichyPrimary, fontWeight = FontWeight.Bold) }
                }
            }
        }

    }
}