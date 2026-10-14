package com.example.cmqsalud.ViewModel

import androidx.lifecycle.ViewModel
import com.example.cmqsalud.Model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LoginViewModel : ViewModel() {

    // Aquí se guarda el usuario simulado
    private val usuarioFicticio = Usuario("12345678-9", "1234", "Administrativo")

    // Esta variable guardada resultado validacion
    private val _estadoLogin = MutableStateFlow<String>("Esperando")
    val estadoLogin: StateFlow<String> = _estadoLogin

    // Función de validacion del formulario
    fun validarCredenciales(rutIngresado: String, passIngresada: String) {
        if (rutIngresado == usuarioFicticio.rut && passIngresada == usuarioFicticio.contrasena) {
            _estadoLogin.value = "Exito_${usuarioFicticio.perfil}" // Resultado exitoso con rol
        } else {
            _estadoLogin.value = "Error" // Resultado fallido
        }
    }
}