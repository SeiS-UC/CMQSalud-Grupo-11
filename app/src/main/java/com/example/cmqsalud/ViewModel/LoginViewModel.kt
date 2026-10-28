package com.example.cmqsalud.ViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cmqsalud.Model.CmqDatabase
import com.example.cmqsalud.Model.CmqRepository
import com.example.cmqsalud.Model.DocumentoEntity
import com.example.cmqsalud.Model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Estado del Formulario de Registro de Funcionarios (Validado)
data class RegistroUiState(
    val nombre: String = "",
    val errorNombre: String? = null,
    val rut: String = "",
    val errorRut: String? = null,
    val contrasena: String = "",
    val errorContrasena: String? = null,
    val centroSeleccionado: String = "CESFAM Quilpué",
    val aceptaPoliticas: Boolean = false,
    val errorPoliticas: String? = null,
    val registroExitoso: Boolean = false
)

// Estado del Formulario CRUD de Documentos (Crear y Editar con validación)
data class DocumentoFormState(
    val idEdicion: Int? = null, // null = Nuevo | != null = Editando registro existente
    val titulo: String = "",
    val errorTitulo: String? = null,
    val tipoSeleccionado: String = "Permiso Administrativo",
    val periodoHoras: String = "1 jornada",
    val errorPeriodo: String? = null,
    val tieneFotoCamara: Boolean = false,
    val guardadoExitoso: Boolean = false
)

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CmqRepository

    // Lista de documentos reactiva observada desde Room Database
    val listaDocumentos: StateFlow<List<DocumentoEntity>>

    // Bitácora de trazabilidad y auditoría (Ciberseguridad - Caso CMQ)
    private val _bitacoraTrazabilidad = MutableStateFlow<List<String>>(
        listOf("[Sistema] Repositorio SQLite local inicializado con normativas de seguridad CMQ.")
    )
    val bitacoraTrazabilidad: StateFlow<List<String>> = _bitacoraTrazabilidad

    init {
        val dao = CmqDatabase.getDatabase(application).cmqDao()
        repository = CmqRepository(dao)
        listaDocumentos = repository.todosLosDocumentos.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        // Carga la semilla inicial de cuentas por defecto mediante corrutinas
        viewModelScope.launch {
            repository.inicializarDatosPorDefecto()
        }
    }

    // --- ESTADO DEL LOGIN ---
    private val _rutLogin = MutableStateFlow("")
    val rutLogin: StateFlow<String> = _rutLogin

    private val _passLogin = MutableStateFlow("")
    val passLogin: StateFlow<String> = _passLogin

    private val _errorRutLogin = MutableStateFlow<String?>(null)
    val errorRutLogin: StateFlow<String?> = _errorRutLogin

    private val _errorPassLogin = MutableStateFlow<String?>(null)
    val errorPassLogin: StateFlow<String?> = _errorPassLogin

    private val _usuarioActivo = MutableStateFlow<Usuario?>(null)
    val usuarioActivo: StateFlow<Usuario?> = _usuarioActivo

    private val _estadoLogin = MutableStateFlow("Esperando")
    val estadoLogin: StateFlow<String> = _estadoLogin

    private val _mensajeConfirmacion = MutableStateFlow("")
    val mensajeConfirmacion: StateFlow<String> = _mensajeConfirmacion

    // --- ESTADOS DE FORMULARIOS ---
    private val _registroState = MutableStateFlow(RegistroUiState())
    val registroState: StateFlow<RegistroUiState> = _registroState

    private val _docFormState = MutableStateFlow(DocumentoFormState())
    val docFormState: StateFlow<DocumentoFormState> = _docFormState

    private fun obtenerFechaHoraActual(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun registrarTrazabilidad(accion: String) {
        val entrada = "[${obtenerFechaHoraActual()}] $accion"
        _bitacoraTrazabilidad.update { listOf(entrada) + it }
    }

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

    // --- EVENTOS DE LOGIN ---
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

        viewModelScope.launch {
            val usuarioEncontrado = repository.autenticar(rutActual, passActual)
            if (usuarioEncontrado != null) {
                _usuarioActivo.value = usuarioEncontrado
                _estadoLogin.value = "Exito"
                registrarTrazabilidad("Acceso autorizado: ${usuarioEncontrado.nombre} (${usuarioEncontrado.perfil})")
            } else {
                _estadoLogin.value = "Error: Credenciales incorrectas. Verifique su RUT y contraseña."
            }
        }
    }

    // --- EVENTOS DE REGISTRO DE FUNCIONARIOS (Guardado en Room) ---
    fun onNombreRegistroChange(nuevoNombre: String) {
        _registroState.update { it.copy(nombre = nuevoNombre, errorNombre = null) }
    }

    fun onRutRegistroChange(nuevoRut: String) {
        _registroState.update { it.copy(rut = formatearRut(nuevoRut), errorRut = null) }
    }

    fun onContrasenaRegistroChange(nuevaPass: String) {
        _registroState.update { it.copy(contrasena = nuevaPass, errorContrasena = null) }
    }

    fun onCentroChange(nuevoCentro: String) {
        _registroState.update { it.copy(centroSeleccionado = nuevoCentro) }
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

        if (state.nombre.isBlank()) errNombre = "El nombre completo es obligatorio"

        val regexRut = Regex("^[0-9]{8}-[0-9kK]$")
        if (state.rut.isBlank()) {
            errRut = "El RUT es obligatorio"
        } else if (!regexRut.matches(state.rut.trim())) {
            errRut = "Formato inválido. Ingrese los 9 dígitos (Ej: 12345678-9)"
        }

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

        if (!state.aceptaPoliticas) errPol = "Debe aceptar las políticas de confidencialidad"

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

        viewModelScope.launch {
            if (repository.existeRut(state.rut.trim())) {
                _registroState.update { it.copy(errorRut = "Este RUT ya está registrado en la base de datos local") }
                return@launch
            }

            val nuevoUsuario = Usuario(
                rut = state.rut.trim().uppercase(),
                nombre = state.nombre.trim(),
                contrasena = state.contrasena,
                perfil = "Funcionario",
                centroSalud = state.centroSeleccionado,
                cargoProfesion = "Funcionario APS",
                tipoContrato = "Contrata"
            )
            repository.registrarUsuario(nuevoUsuario)
            registrarTrazabilidad("Nuevo funcionario registrado en Room: ${nuevoUsuario.nombre} (${nuevoUsuario.centroSalud})")
            _mensajeConfirmacion.value = "Éxito: Funcionario ${nuevoUsuario.nombre} guardado localmente."
            _registroState.update { it.copy(registroExitoso = true) }
        }
    }

    // --- OPERACIONES CRUD DE DOCUMENTOS CON CORRUTINAS ---
    fun onTituloDocChange(nuevoTitulo: String) {
        _docFormState.update { it.copy(titulo = nuevoTitulo, errorTitulo = null, guardadoExitoso = false) }
    }

    fun onTipoDocChange(nuevoTipo: String) {
        _docFormState.update { it.copy(tipoSeleccionado = nuevoTipo, guardadoExitoso = false) }
    }

    fun onPeriodoDocChange(nuevoPeriodo: String) {
        _docFormState.update { it.copy(periodoHoras = nuevoPeriodo, errorPeriodo = null, guardadoExitoso = false) }
    }

    fun onFotoCamaraCapturada(capturada: Boolean) {
        _docFormState.update { it.copy(tieneFotoCamara = capturada) }
    }

    fun cargarDocumentoParaEditar(doc: DocumentoEntity) {
        _docFormState.value = DocumentoFormState(
            idEdicion = doc.id,
            titulo = doc.titulo,
            tipoSeleccionado = doc.tipoDocumento,
            periodoHoras = doc.periodoHoras,
            tieneFotoCamara = doc.tieneRespaldoCamara
        )
    }

    fun limpiarFormularioDocumento() {
        _docFormState.value = DocumentoFormState()
    }

    // CREATE / UPDATE en SQLite con Room
    fun guardarDocumentoEnRoom() {
        val form = _docFormState.value
        val usuario = _usuarioActivo.value ?: return

        if (form.titulo.isBlank() || form.titulo.trim().length < 5) {
            _docFormState.update { it.copy(errorTitulo = "Especifique un título válido (mínimo 5 caracteres)") }
            return
        }
        if (form.periodoHoras.isBlank()) {
            _docFormState.update { it.copy(errorPeriodo = "El período u horas es obligatorio") }
            return
        }

        viewModelScope.launch {
            if (form.idEdicion == null) {
                // CREATE (Insertar nuevo registro)
                val nuevoDoc = DocumentoEntity(
                    titulo = form.titulo.trim(),
                    tipoDocumento = form.tipoSeleccionado,
                    periodoHoras = form.periodoHoras.trim(),
                    centroSalud = usuario.centroSalud,
                    rutFuncionario = usuario.rut,
                    nombreFuncionario = usuario.nombre,
                    estado = "Pendiente de Revisión",
                    fechaCarga = obtenerFechaHoraActual(),
                    ultimaModificacionPor = usuario.nombre,
                    tieneRespaldoCamara = form.tieneFotoCamara
                )
                repository.insertarDocumento(nuevoDoc)
                registrarTrazabilidad("${usuario.nombre} CARGÓ expediente '${nuevoDoc.titulo}' (${nuevoDoc.tipoDocumento})")
            } else {
                // UPDATE (Modificar registro existente)
                val docOriginal = listaDocumentos.value.find { it.id == form.idEdicion }
                if (docOriginal != null) {
                    val docActualizado = docOriginal.copy(
                        titulo = form.titulo.trim(),
                        tipoDocumento = form.tipoSeleccionado,
                        periodoHoras = form.periodoHoras.trim(),
                        tieneRespaldoCamara = form.tieneFotoCamara,
                        ultimaModificacionPor = usuario.nombre,
                        fechaCarga = "${obtenerFechaHoraActual()} (Editado)"
                    )
                    repository.actualizarDocumento(docActualizado)
                    registrarTrazabilidad("${usuario.nombre} MODIFICÓ expediente ID #${docActualizado.id}: '${docActualizado.titulo}'")
                }
            }
            _docFormState.value = DocumentoFormState(guardadoExitoso = true)
        }
    }

    // UPDATE de Estado (Roles: Administrativo verifica / Jefatura aprueba o rechaza)
    fun cambiarEstadoDocumento(doc: DocumentoEntity, nuevoEstado: String) {
        val usuario = _usuarioActivo.value ?: return
        viewModelScope.launch {
            val actualizado = doc.copy(
                estado = nuevoEstado,
                ultimaModificacionPor = "${usuario.nombre} (${usuario.perfil})"
            )
            repository.actualizarDocumento(actualizado)
            registrarTrazabilidad("${usuario.nombre} (${usuario.perfil}) cambió estado de '${doc.titulo}' a [$nuevoEstado]")
        }
    }

    // DELETE (Eliminar de SQLite)
    fun eliminarDocumentoDeRoom(doc: DocumentoEntity) {
        val usuario = _usuarioActivo.value ?: return
        viewModelScope.launch {
            repository.eliminarDocumento(doc)
            registrarTrazabilidad("${usuario.nombre} ELIMINÓ expediente '${doc.titulo}' de Room")
        }
    }

    fun resetearFormularioRegistro() {
        _registroState.value = RegistroUiState()
    }

    fun limpiarMensajeConfirmacion() {
        _mensajeConfirmacion.value = ""
    }

    fun cerrarSesion() {
        val u = _usuarioActivo.value
        if (u != null) {
            registrarTrazabilidad("Cierre de sesión: ${u.nombre}")
        }
        _rutLogin.value = ""
        _passLogin.value = ""
        _errorRutLogin.value = null
        _errorPassLogin.value = null
        _usuarioActivo.value = null
        _estadoLogin.value = "Esperando"
    }
}