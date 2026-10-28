package com.example.cmqsalud.Model

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CmqDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarDocumento(documento: DocumentoEntity)

    @Query("SELECT * FROM documentos_funcionarios ORDER BY id DESC")
    fun obtenerTodosLosDocumentos(): Flow<List<DocumentoEntity>>

    @Update
    suspend fun actualizarDocumento(documento: DocumentoEntity)

    @Delete
    suspend fun eliminarDocumento(documento: DocumentoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarUsuario(usuario: Usuario)

    @Query("SELECT * FROM usuarios WHERE UPPER(rut) = UPPER(:rut) AND contrasena = :pass LIMIT 1")
    suspend fun loginUsuario(rut: String, pass: String): Usuario?

    @Query("SELECT * FROM usuarios WHERE UPPER(rut) = UPPER(:rut) LIMIT 1")
    suspend fun buscarUsuarioPorRut(rut: String): Usuario?

    @Query("SELECT COUNT(*) FROM usuarios")
    suspend fun contarUsuarios(): Int
}

@Database(
    entities = [Usuario::class, DocumentoEntity::class],
    version = 3, // Subimos a versión 3 por la nueva columna fotoBase64
    exportSchema = false
)
abstract class CmqDatabase : RoomDatabase() {
    abstract fun cmqDao(): CmqDao

    companion object {
        @Volatile
        private var INSTANCE: CmqDatabase? = null

        fun getDatabase(context: Context): CmqDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CmqDatabase::class.java,
                    "cmq_salud_local_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

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