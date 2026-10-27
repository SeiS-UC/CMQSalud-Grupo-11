package com.example.cmqsalud.View

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cmqsalud.ViewModel.LoginViewModel
import kotlinx.coroutines.delay

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
        label = { Text(label) },
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Text(text = text, fontSize = 16.sp)
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
    val mensajeConfirmacion by viewModel.mensajeConfirmacion.collectAsState()

    var mostrandoRegistro by remember { mutableStateOf(false) }

    // 1. PANTALLA PRINCIPAL / HOME (Meta 6: Navegación y uso de datos ingresados)
    if (estadoLogin.startsWith("Exito_")) {
        val partes = estadoLogin.split("_")
        val nombreActivo = partes.getOrNull(1) ?: "Usuario"
        val perfilActivo = partes.getOrNull(2) ?: "Funcionario"
        val areaActiva = partes.getOrNull(3) ?: "General"

        HomeScreen(
            modifier = modifier,
            nombre = nombreActivo,
            perfil = perfilActivo,
            area = areaActiva,
            onCerrarSesion = { viewModel.cerrarSesion() }
        )
    }
    // 2. PANTALLA DE REGISTRO
    else if (mostrandoRegistro) {
        RegistroScreen(
            modifier = modifier,
            viewModel = viewModel,
            onVolverAlLogin = {
                viewModel.resetearFormularioRegistro()
                mostrandoRegistro = false
            }
        )
    }
    // 3. PANTALLA DE LOGIN
    else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "CMQ Área Salud",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            if (mensajeConfirmacion.isNotEmpty()) {
                Text(
                    text = mensajeConfirmacion,
                    color = Color(0xFF2E7D32),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            basicTextField(
                value = rut,
                onValueChange = { viewModel.onRutLoginChange(it) },
                label = "RUT (Ej: 12345678-9)",
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
                Text(text = "Registrar nuevo funcionario", fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (estadoLogin.startsWith("Error")) {
                Text(
                    text = estadoLogin,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Medium
                )
            } else {
                Text(text = "Estado del sistema: $estadoLogin")
            }
        }
    }
}

// Pantalla Principal (Home) conectada que recibe y utiliza los datos del usuario (Meta 6)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    nombre: String,
    perfil: String,
    area: String,
    onCerrarSesion: () -> Unit
) {
    var seccionSeleccionada by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        // Cabecera de bienvenida con los datos dinámicos del usuario
        Text(
            text = "Panel de Control — CMQ Salud",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Bienvenido/a, $nombre", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Text(text = "Rol: $perfil", fontSize = 15.sp)
                Text(text = "Unidad Asignada: $area", fontSize = 15.sp, color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Opciones del Sistema:",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Opciones ideales para el caso de salud
        val opcionesMenu = listOf(
            "📋 Gestión y Registro de Pacientes",
            "💊 Solicitud de Insumos Médicos",
            "📅 Revisión de Turnos y Horarios",
            "📊 Reporte de Estado de Unidad"
        )

        opcionesMenu.forEach { opcion ->
            OutlinedButton(
                onClick = { seccionSeleccionada = opcion },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(text = opcion, fontSize = 15.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Si el usuario hace clic en una opción, se muestra la simulación del módulo (Meta 6)
        if (seccionSeleccionada != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Módulo Activo:",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = seccionSeleccionada!!)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Acceso autorizado para personal de $area.",
                        fontSize = 13.sp,
                        color = Color.DarkGray
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Spacer(modifier = Modifier.weight(1f, fill = false))

        basicButton(
            text = "Cerrar Sesión",
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
    val areasMedicas = listOf("Urgencias", "Farmacia", "Laboratorio Clínico")

    LaunchedEffect(uiState.registroExitoso) {
        if (uiState.registroExitoso) {
            delay(1500)
            onVolverAlLogin()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
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
                        .background(Color(0xFF2E7D32), shape = CircleShape)
                ) {
                    Text(
                        text = "✓",
                        color = Color.White,
                        fontSize = 60.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "¡Usuario registrado!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Perfil asignado: Funcionario (${uiState.areaSeleccionada})",
                    fontSize = 16.sp
                )
            }
        }

        if (!uiState.registroExitoso) {
            Text(
                text = "Registro de Funcionario",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "Rol asignado por defecto: Funcionario",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.secondary,
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
                text = "Seleccione su Unidad / Área:",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.align(Alignment.Start)
            )

            areasMedicas.forEach { area ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    RadioButton(
                        selected = (area == uiState.areaSeleccionada),
                        onClick = { viewModel.onAreaChange(area) }
                    )
                    Text(text = area, fontSize = 15.sp)
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
                    text = "Acepto políticas de confidencialidad de CMQ Salud",
                    fontSize = 14.sp
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
                Text(text = "Volver al Inicio de Sesión")
            }
        }
    }
}