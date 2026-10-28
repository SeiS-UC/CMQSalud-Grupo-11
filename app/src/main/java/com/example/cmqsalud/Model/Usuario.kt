package com.example.cmqsalud.Model

import androidx.room.Entity
import androidx.room.PrimaryKey

// Entidad 1: Datos de identificación e información laboral del funcionario APS (Sección 3.2)
@Entity(tableName = "usuarios")
data class Usuario(
    @PrimaryKey
    val rut: String,
    val nombre: String,
    val contrasena: String,
    val perfil: String, // "Funcionario", "Administrativo", "Jefatura"
    val centroSalud: String, // "CESFAM Quilpué", "CESFAM Belloto Sur", "CECOSF El Retiro", "Administración Central CMQ"
    val cargoProfesion: String = "TENS / Profesional APS", // Cargo o profesión
    val tipoContrato: String = "Planta / Contrata APS" // Situación contractual
)

// Entidad 2: Expediente documental, permisos, capacitaciones y metadatos de trazabilidad (Sección 3.2 y 3.3)
@Entity(tableName = "documentos_funcionarios")
data class DocumentoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val titulo: String, // Nombre del certificado, curso, contrato o solicitud
    val tipoDocumento: String, // "Permiso Administrativo", "Certificado Capacitación", "Contrato / Anexo", "Resolución / Decreto"
    val periodoHoras: String, // Ej: "30 hrs pedagógicas", "3 días hábiles", "Vigencia 2026"
    val centroSalud: String, // Establecimiento APS asociado
    val rutFuncionario: String, // Usuario que incorpora el archivo
    val nombreFuncionario: String,
    val estado: String, // "Pendiente de Revisión", "Verificado por Admin", "Aprobado por Jefatura", "Rechazado"
    val fechaCarga: String, // Metadato de fecha de carga
    val ultimaModificacionPor: String, // Metadato de trazabilidad (quién modificó o revisó por última vez)
    val tieneRespaldoCamara: Boolean = false // Evidencia de captura física con cámara móvil (Sección 3.3)
)