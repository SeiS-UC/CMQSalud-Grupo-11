package com.example.cmqsalud.Model

import kotlinx.coroutines.flow.Flow

class CmqRepository(private val dao: CmqDao) {

    val todosLosDocumentos: Flow<List<DocumentoEntity>> = dao.obtenerTodosLosDocumentos()

    suspend fun insertarDocumento(doc: DocumentoEntity) = dao.insertarDocumento(doc)
    suspend fun actualizarDocumento(doc: DocumentoEntity) = dao.actualizarDocumento(doc)
    suspend fun eliminarDocumento(doc: DocumentoEntity) = dao.eliminarDocumento(doc)
    suspend fun registrarUsuario(usuario: Usuario) = dao.insertarUsuario(usuario)
    suspend fun autenticar(rut: String, pass: String): Usuario? = dao.loginUsuario(rut, pass)
    suspend fun existeRut(rut: String): Boolean = dao.buscarUsuarioPorRut(rut) != null

    suspend fun inicializarDatosPorDefecto() {
        if (dao.contarUsuarios() == 0) {
            // 1. Cuenta Desarrollador / Funcionario
            dao.insertarUsuario(
                Usuario(
                    rut = "21257883-5",
                    nombre = "Developer SS",
                    contrasena = "Admin123*",
                    perfil = "Desarrollador",
                    centroSalud = "Administración Central CMQ",
                    cargoProfesion = "Encargado TI y Sistemas",
                    tipoContrato = "Planta"
                )
            )
            // 2. Cuenta Administrativo (Solo valida integridad)
            dao.insertarUsuario(
                Usuario(
                    rut = "12345678-9",
                    nombre = "Sebastián Admin",
                    contrasena = "Admin123*",
                    perfil = "Administrativo",
                    centroSalud = "CESFAM Quilpué",
                    cargoProfesion = "Administrativo RRHH",
                    tipoContrato = "Planta"
                )
            )
            // 3. Cuenta Jefatura (Solo aprueba o rechaza)
            dao.insertarUsuario(
                Usuario(
                    rut = "98765432-1",
                    nombre = "Dra. Soto",
                    contrasena = "Admin123*",
                    perfil = "Jefatura",
                    centroSalud = "CESFAM Quilpué",
                    cargoProfesion = "Directora de Consultorio",
                    tipoContrato = "Contrata"
                )
            )

            dao.insertarDocumento(
                DocumentoEntity(
                    titulo = "Certificado Curso RCP Avanzado (30 hrs)",
                    tipoDocumento = "Certificado Capacitación",
                    periodoHoras = "30 hrs pedagógicas",
                    centroSalud = "CESFAM Quilpué",
                    rutFuncionario = "21257883-5",
                    nombreFuncionario = "Developer SS",
                    estado = "Pendiente de Revisión",
                    fechaCarga = "28/10/2026 02:00",
                    ultimaModificacionPor = "Developer SS",
                    tieneRespaldoCamara = false,
                    fotoBase64 = null
                )
            )
        }
    }
}