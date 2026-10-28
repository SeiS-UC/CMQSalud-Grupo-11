package com.example.cmqsalud.View

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cmqsalud.Model.DocumentoEntity
import com.example.cmqsalud.Model.Usuario
import com.example.cmqsalud.ViewModel.LoginViewModel
import kotlinx.coroutines.delay

// --- PALETA DE COLORES VICHY ---
val VichyPrimary = Color(0xFF05AD98)
val VichySurfaceGray = Color(0xFFBBBFBF)
val VichyDarkGray = Color(0xFF4A4A4A)     // Gris oscuro optimizado para alta legibilidad
val VichyBlackText = Color(0xFF1A1A1A)    // Texto principal casi negro
val VichyWhite = Color(0xFFFFFFFF)

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
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Text(text = text, fontSize = 16.sp, color = VichyWhite, fontWeight = FontWeight.Bold)
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
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(VichyWhite)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "CMQ Área Salud — APS",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = VichyPrimary
            )
            Text(
                text = "Gestión Documental de Funcionarios",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = VichyDarkGray,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            if (mensajeConfirmacion.isNotEmpty()) {
                Text(
                    text = mensajeConfirmacion,
                    color = VichyPrimary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
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

            Spacer(modifier = Modifier.height(12.dp))

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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                Text(text = "Registrarme", fontSize = 16.sp, color = VichyPrimary, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (estadoLogin.startsWith("Error")) {
                Text(
                    text = estadoLogin,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Medium
                )
            } else {
                Text(
                    text = "Version MVP",
                    fontSize = 13.sp,
                    color = VichyDarkGray
                )
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

    val listaDocumentos by viewModel.listaDocumentos.collectAsState()
    val docForm by viewModel.docFormState.collectAsState()
    val bitacora by viewModel.bitacoraTrazabilidad.collectAsState()

    var fotoCapturadaBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            fotoCapturadaBitmap = bitmap
            viewModel.onFotoCamaraCapturada(true)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(VichyWhite)
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Repositorio Central — CMQ Salud",
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold,
            color = VichyPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = VichySurfaceGray.copy(alpha = 0.25f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(text = "Funcionario/a: ${usuario.nombre}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = VichyBlackText)
                Text(text = "RUT: ${usuario.rut} | Rol: ${usuario.perfil}", fontSize = 14.sp, color = VichyDarkGray)
                Text(text = "Establecimiento APS: ${usuario.centroSalud}", fontSize = 14.sp, color = VichyDarkGray)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(
                onClick = { moduloActivo = "LISTADO" },
                modifier = Modifier.weight(1f).padding(end = 4.dp)
            ) {
                Text("📁 Mis Docs (${listaDocumentos.size})", fontSize = 11.sp, color = VichyPrimary, fontWeight = FontWeight.Bold)
            }
            OutlinedButton(
                onClick = {
                    viewModel.limpiarFormularioDocumento()
                    fotoCapturadaBitmap = null
                    moduloActivo = "FORMULARIO"
                },
                modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
            ) {
                Text("➕ Subir", fontSize = 11.sp, color = VichyPrimary, fontWeight = FontWeight.Bold)
            }
            OutlinedButton(
                onClick = { moduloActivo = "TRAZABILIDAD" },
                modifier = Modifier.weight(1f).padding(start = 4.dp)
            ) {
                Text("🛡️ Revisión", fontSize = 11.sp, color = VichyPrimary, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- MÓDULO 1: FORMULARIO CRUD ---
        if (moduloActivo == "FORMULARIO") {
            val tiposDoc = listOf(
                "Permiso Administrativo",
                "Certificado Capacitación",
                "Contrato / Anexo Laboral",
                "Licencia / Respaldo Médico"
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = VichySurfaceGray.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (docForm.idEdicion == null) "Cargar Nuevo Documento" else "Editar Documento #${docForm.idEdicion}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = VichyPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    basicTextField(
                        value = docForm.titulo,
                        onValueChange = { viewModel.onTituloDocChange(it) },
                        label = "Título o detalle (Obligatorio, mín 5 caracteres)",
                        errorMessage = docForm.errorTitulo
                    )

                    basicTextField(
                        value = docForm.periodoHoras,
                        onValueChange = { viewModel.onPeriodoDocChange(it) },
                        label = "Período u Horas (Ej: 30 hrs / 3 días)",
                        errorMessage = docForm.errorPeriodo
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Categoría Documental:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = VichyBlackText)

                    tiposDoc.forEach { tipo ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            RadioButton(
                                selected = (docForm.tipoSeleccionado == tipo),
                                onClick = { viewModel.onTipoDocChange(tipo) }
                            )
                            Text(text = tipo, fontSize = 14.sp, color = VichyBlackText, fontWeight = FontWeight.Medium)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { cameraLauncher.launch() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (docForm.tieneFotoCamara) "📷 Volver a tomar" else "📷 Capturar Documento",
                            color = VichyPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (fotoCapturadaBitmap != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Image(
                            bitmap = fotoCapturadaBitmap!!.asImageBitmap(),
                            contentDescription = "Vista previa documento digitalizado",
                            modifier = Modifier
                                .size(120.dp)
                                .align(Alignment.CenterHorizontally)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    basicButton(
                        text = if (docForm.idEdicion == null) "Guardar" else "Actualizar",
                        onClick = {
                            viewModel.guardarDocumentoEnRoom()
                            if (docForm.titulo.trim().length >= 5) {
                                moduloActivo = "LISTADO"
                            }
                        }
                    )

                    OutlinedButton(
                        onClick = {
                            viewModel.limpiarFormularioDocumento()
                            moduloActivo = "LISTADO"
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancelar", color = VichyDarkGray, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- MÓDULO 2: LISTADO CRUD ---
        if (moduloActivo == "LISTADO") {
            Text(
                text = "Documentos Almacenados",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = VichyPrimary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (listaDocumentos.isEmpty()) {
                Text(
                    text = "No hay documentos registrados aún. Presione '➕' para agregar uno.",
                    color = VichyDarkGray,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }

            listaDocumentos.forEach { doc ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = VichyWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(text = doc.titulo, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = VichyBlackText)
                        Text(text = "Tipo: ${doc.tipoDocumento} (${doc.periodoHoras})", fontSize = 14.sp, color = VichyBlackText)
                        Text(text = "Funcionario: ${doc.nombreFuncionario} (${doc.centroSalud})", fontSize = 13.sp, color = VichyDarkGray)
                        Text(text = "Fecha carga: ${doc.fechaCarga}", fontSize = 12.sp, color = VichyDarkGray)
                        Text(text = "Última modificación: ${doc.ultimaModificacionPor}", fontSize = 12.sp, color = VichyDarkGray)

                        if (doc.tieneRespaldoCamara) {
                            Text(
                                text = "📷 Incluye respaldo digitalizado por cámara",
                                fontSize = 12.sp,
                                color = VichyPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Estado: ${doc.estado}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                doc.estado.contains("Aprobado") -> VichyPrimary
                                doc.estado.contains("Rechazado") -> MaterialTheme.colorScheme.error
                                doc.estado.contains("Verificado") -> Color(0xFF1565C0)
                                else -> Color(0xFFE65100)
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.cargarDocumentoParaEditar(doc)
                                    moduloActivo = "FORMULARIO"
                                }
                            ) {
                                Text("✏️ Editar", fontSize = 12.sp, color = VichyPrimary, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedButton(
                                onClick = { viewModel.eliminarDocumentoDeRoom(doc) },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("🗑️ Eliminar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (usuario.perfil == "Administrativo") {
                            Spacer(modifier = Modifier.height(6.dp))
                            Button(
                                onClick = { viewModel.cambiarEstadoDocumento(doc, "Verificado por Admin") },
                                colors = ButtonDefaults.buttonColors(containerColor = VichyPrimary),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("✓ Verificar Integridad (Rol Administrativo)", fontSize = 13.sp, color = VichyWhite, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (usuario.perfil == "Jefatura") {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Button(
                                    onClick = { viewModel.cambiarEstadoDocumento(doc, "Aprobado por Jefatura") },
                                    modifier = Modifier.weight(1f).padding(end = 4.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = VichyPrimary)
                                ) {
                                    Text("Aprobar", fontSize = 13.sp, color = VichyWhite, fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = { viewModel.cambiarEstadoDocumento(doc, "Rechazado por Jefatura") },
                                    modifier = Modifier.weight(1f).padding(start = 4.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Text("Rechazar", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- MÓDULO 3: BITÁCORA ---
        if (moduloActivo == "TRAZABILIDAD") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = VichySurfaceGray.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Registro de Trazabilidad y Auditoría",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = VichyPrimary
                    )
                    Text(
                        text = "Control de accesos, cargas, modificaciones y aprobaciones en CMQ Salud:",
                        fontSize = 13.sp,
                        color = VichyDarkGray,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    bitacora.forEach { registro ->
                        Text(
                            text = "• $registro",
                            fontSize = 13.sp,
                            color = VichyBlackText,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        basicButton(
            text = "Cerrar Sesión Segura",
            onClick = onCerrarSesion
        )
    }
}

@Composable
fun RegistroScreen(
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel,
    onVolverAlLogin: () -> Unit
) {
    val uiState by viewModel.registroState.collectAsState()
    val centrosSaludAps = listOf(
        "CESFAM Quilpué",
        "CESFAM Belloto Sur",
        "CECOSF El Retiro",
        "Administración Central CMQ"
    )

    LaunchedEffect(uiState.registroExitoso) {
        if (uiState.registroExitoso) {
            delay(1500)
            onVolverAlLogin()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(VichyWhite)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
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
                modifier = Modifier.padding(32.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(100.dp)
                        .background(VichyPrimary, shape = CircleShape)
                ) {
                    Text(
                        text = "✓",
                        color = VichyWhite,
                        fontSize = 60.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "¡Usuario registrado en Room!",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = VichyPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Perfil: Funcionario (${uiState.centroSeleccionado})",
                    fontSize = 15.sp,
                    color = VichyDarkGray
                )
            }
        }

        if (!uiState.registroExitoso) {
            Text(
                text = "Registro de Funcionario APS",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = VichyPrimary,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "Rol asignado por seguridad: Funcionario",
                fontSize = 14.sp,
                color = VichyDarkGray,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            basicTextField(
                value = uiState.nombre,
                onValueChange = { viewModel.onNombreRegistroChange(it) },
                label = "Nombre Completo",
                errorMessage = uiState.errorNombre
            )

            basicTextField(
                value = uiState.rut,
                onValueChange = { viewModel.onRutRegistroChange(it) },
                label = "RUT (9 dígitos, guion automático)",
                errorMessage = uiState.errorRut
            )

            basicTextField(
                value = uiState.contrasena,
                onValueChange = { viewModel.onContrasenaRegistroChange(it) },
                label = "Contraseña (8+ caract, 1 Mayús, 1 Especial)",
                errorMessage = uiState.errorContrasena
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Seleccione Establecimiento de Salud (APS):",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = VichyBlackText,
                modifier = Modifier.align(Alignment.Start)
            )

            centrosSaludAps.forEach { centro ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    RadioButton(
                        selected = (centro == uiState.centroSeleccionado),
                        onClick = { viewModel.onCentroChange(centro) }
                    )
                    Text(text = centro, fontSize = 14.sp, color = VichyBlackText, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = uiState.aceptaPoliticas,
                    onCheckedChange = { viewModel.onPoliticasChange(it) }
                )
                Text(
                    text = "Acepto políticas de protección de datos de CMQ Salud",
                    fontSize = 13.sp,
                    color = VichyBlackText
                )
            }

            if (uiState.errorPoliticas != null) {
                Text(
                    text = uiState.errorPoliticas!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(start = 12.dp, bottom = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            basicButton(
                text = "Guardar y Registrar",
                onClick = { viewModel.procesarRegistro() }
            )

            OutlinedButton(
                onClick = onVolverAlLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(text = "Volver al Inicio de Sesión", color = VichyPrimary, fontWeight = FontWeight.Bold)
            }
        }
    }
}