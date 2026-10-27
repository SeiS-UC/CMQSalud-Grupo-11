package com.example.cmqsalud.ViewModel

import androidx.lifecycle.ViewModel
import com.example.cmqsalud.Model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

// Data class que concentra todo el estado del formulario y sus errores individuales (Meta 4 y Meta 5)
data class RegistroUiState(
    val nombre: String = "",
    val errorNombre: String? = null,
    val rut: String = "",
    val errorRut: String? = null,
    val contrasena: String = "",
    val errorContrasena: String? = null,
    val areaSeleccionada: String = "Urgencias",
    val aceptaPoliticas: Boolean = false,
    val errorPoliticas: String? = null,
    val registroExitoso: Boolean = false
)

class LoginViewModel : ViewModel() {

    // Cuentas privilegiadas pre-creadas en el sistema
    private val listaUsuarios = mutableListOf(
        Usuario("Sebastián Admin", "12345678-9", "1234", "Administrativo", "Dirección"),
        Usuario("Dra. Soto", "98765432-1", "Admin123*", "Jefatura", "Jefatura Médica")
    )

    // --- ESTADO DEL LOGIN ---
    private val _rutLogin = MutableStateFlow("")
    val rutLogin: StateFlow<String> = _rutLogin

    private val _passLogin = MutableStateFlow("")
    val passLogin: StateFlow<String> = _passLogin

    private val _errorRutLogin = MutableStateFlow<String?>(null)
    val errorRutLogin: StateFlow<String?> = _errorRutLogin

    private val _errorPassLogin = MutableStateFlow<String?>(null)
    val errorPassLogin: StateFlow<String?> = _errorPassLogin

    private val _estadoLogin = MutableStateFlow("Esperando")
    val estadoLogin: StateFlow<String> = _estadoLogin

    private val _mensajeConfirmacion = MutableStateFlow("")
    val mensajeConfirmacion: StateFlow<String> = _mensajeConfirmacion

    // --- ESTADO DEL FORMULARIO DE REGISTRO (Meta 5) ---
    private val _registroState = MutableStateFlow(RegistroUiState())
    val registroState: StateFlow<RegistroUiState> = _registroState

    // Lógica de formateo de RUT en el ViewModel (9 dígitos exactos con guion automático)
    private fun formatearRut(input: String): String {
        val limpio = input.filter { it.isDigit() || it.equals('k', ignoreCase = true) }
            .uppercase()
            .take(9)

        return if (limpio.length == 9) {
            limpio.substring(0, 8) + "-" + limpio.last()
        } else {
            limpio
        }
    }

    // --- EVENTOS DE LOGIN (Meta 5: dato incorrecto -> mensaje -> corrección) ---
    fun onRutLoginChange(nuevoRut: String) {
        _rutLogin.value = formatearRut(nuevoRut)
        _errorRutLogin.value = null
        if (_estadoLogin.value.startsWith("Error")) _estadoLogin.value = "Esperando"
    }

    fun onPassLoginChange(nuevaPass: String) {
        _passLogin.value = nuevaPass
        _errorPassLogin.value = null
        if (_estadoLogin.value.startsWith("Error")) _estadoLogin.value = "Esperando"
    }

    fun validarCredenciales() {
        val rutActual = _rutLogin.value.trim()
        val passActual = _passLogin.value

        var hayError = false
        if (rutActual.isBlank()) {
            _errorRutLogin.value = "El RUT es obligatorio"
            hayError = true
        }
        if (passActual.isBlank()) {
            _errorPassLogin.value = "La contraseña es obligatoria"
            hayError = true
        }
        if (hayError) return

        val usuarioEncontrado = listaUsuarios.find {
            it.rut.equals(rutActual, ignoreCase = true) && it.contrasena == passActual
        }

        if (usuarioEncontrado != null) {
            _estadoLogin.value = "Exito_${usuarioEncontrado.nombre}_${usuarioEncontrado.perfil}_${usuarioEncontrado.area}"
        } else {
            _estadoLogin.value = "Error: Credenciales incorrectas. Verifique su RUT y contraseña."
        }
    }

    // --- EVENTOS DEL FORMULARIO DE REGISTRO (Meta 4 y Meta 5) ---
    fun onNombreRegistroChange(nuevoNombre: String) {
        _registroState.update { it.copy(nombre = nuevoNombre, errorNombre = null) }
    }

    fun onRutRegistroChange(nuevoRut: String) {
        _registroState.update { it.copy(rut = formatearRut(nuevoRut), errorRut = null) }
    }

    fun onContrasenaRegistroChange(nuevaPass: String) {
        _registroState.update { it.copy(contrasena = nuevaPass, errorContrasena = null) }
    }

    fun onAreaChange(nuevaArea: String) {
        _registroState.update { it.copy(areaSeleccionada = nuevaArea) }
    }

    fun onPoliticasChange(acepta: Boolean) {
        _registroState.update { it.copy(aceptaPoliticas = acepta, errorPoliticas = null) }
    }

    fun procesarRegistro() {
        val state = _registroState.value

        var errNombre: String? = null
        var errRut: String? = null
        var errPass: String? = null
        var errPol: String? = null

        // 1. Validación de Nombre
        if (state.nombre.isBlank()) {
            errNombre = "El nombre completo es obligatorio"
        }

        // 2. Validación de RUT (Obligatorio + Formato 9 dígitos + Duplicados)
        val regexRut = Regex("^[0-9]{8}-[0-9kK]$")
        if (state.rut.isBlank()) {
            errRut = "El RUT es obligatorio"
        } else if (!regexRut.matches(state.rut.trim())) {
            errRut = "Formato inválido. Ingrese los 9 dígitos (Ej: 12345678-9)"
        } else if (listaUsuarios.any { it.rut.equals(state.rut.trim(), ignoreCase = true) }) {
            errRut = "Este RUT ya se encuentra registrado en el sistema"
        }

        // 3. Validaciones de Contraseña (Obligatoria + 8 caracteres + Mayúscula + Especial)
        val tieneEspecial = state.contrasena.any { !it.isLetterOrDigit() && !it.isWhitespace() }
        if (state.contrasena.isBlank()) {
            errPass = "La contraseña es obligatoria"
        } else if (state.contrasena.length < 8) {
            errPass = "Debe tener al menos 8 caracteres"
        } else if (!state.contrasena.any { it.isUpperCase() }) {
            errPass = "Debe incluir al menos una letra mayúscula"
        } else if (!tieneEspecial) {
            errPass = "Debe incluir al menos un carácter especial (*, @, #, ., -)"
        }

        // 4. Validación de Checkbox de Políticas
        if (!state.aceptaPoliticas) {
            errPol = "Debe aceptar las políticas de confidencialidad para continuar"
        }

        // Si algún campo tiene error, actualizamos el estado para mostrar isError y supportingText (Meta 4)
        if (errNombre != null || errRut != null || errPass != null || errPol != null) {
            _registroState.update {
                it.copy(
                    errorNombre = errNombre,
                    errorRut = errRut,
                    errorContrasena = errPass,
                    errorPoliticas = errPol
                )
            }
            return
        }

        // Si todo es válido, creamos el usuario con perfil "Funcionario" por seguridad
        val nuevoUsuario = Usuario(
            nombre = state.nombre.trim(),
            rut = state.rut.trim().uppercase(),
            contrasena = state.contrasena,
            perfil = "Funcionario",
            area = state.areaSeleccionada
        )
        listaUsuarios.add(nuevoUsuario)
        _mensajeConfirmacion.value = "Éxito: Funcionario ${nuevoUsuario.nombre} (${nuevoUsuario.area}) registrado."
        _registroState.update { it.copy(registroExitoso = true) }
    }

    fun resetearFormularioRegistro() {
        _registroState.value = RegistroUiState()
    }

    fun limpiarMensajeConfirmacion() {
        _mensajeConfirmacion.value = ""
    }

    fun cerrarSesion() {
        _rutLogin.value = ""
        _passLogin.value = ""
        _errorRutLogin.value = null
        _errorPassLogin.value = null
        _estadoLogin.value = "Esperando"
    }
}