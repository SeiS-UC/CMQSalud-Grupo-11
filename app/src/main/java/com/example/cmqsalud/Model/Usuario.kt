package com.example.cmqsalud.Model

data class Usuario(
    val rut: String,
    val contrasena: String,
    val perfil: String // Ej: "Administrativo", "Jefatura", "Funcionario"
)