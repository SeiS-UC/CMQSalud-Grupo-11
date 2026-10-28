package com.example.cmqsalud.Model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usuarios")
data class Usuario(
    @PrimaryKey val rut: String,
    val nombre: String,
    val contrasena: String,
    val perfil: String, // "Funcionario", "Administrativo", "Jefatura", "Desarrollador"
    val centroSalud: String,
    val cargoProfesion: String = "Funcionario APS",
    val tipoContrato: String = "Planta"
)

@Entity(tableName = "documentos_funcionarios")
data class DocumentoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val titulo: String,
    val tipoDocumento: String,
    val periodoHoras: String,
    val centroSalud: String,
    val rutFuncionario: String,
    val nombreFuncionario: String,
    val estado: String,
    val fechaCarga: String,
    val ultimaModificacionPor: String,
    val tieneRespaldoCamara: Boolean = false,
    val fotoBase64: String? = null // <--- NUEVO: Guarda la imagen real en SQLite
)