package com.example.cmqsalud.Model

data class Usuario(
    val nombre: String,
    val rut: String,
    val contrasena: String,
    val perfil: String, // "Funcionario", "Administrativo", "Jefatura"
    val area: String = "General" // "Urgencias", "Farmacia", "Laboratorio Clínico", etc.
)