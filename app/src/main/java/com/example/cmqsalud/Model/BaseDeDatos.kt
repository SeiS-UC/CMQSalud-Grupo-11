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

// 1. DAO: Operaciones CRUD con Corrutinas (suspend) y Flow en tiempo real
@Dao
interface CmqDao {

    // --- CRUD DOCUMENTOS (Expedientes de Funcionarios APS) ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarDocumento(documento: DocumentoEntity)

    @Query("SELECT * FROM documentos_funcionarios ORDER BY id DESC")
    fun obtenerTodosLosDocumentos(): Flow<List<DocumentoEntity>>

    @Update
    suspend fun actualizarDocumento(documento: DocumentoEntity)

    @Delete
    suspend fun eliminarDocumento(documento: DocumentoEntity)

    // --- OPERACIONES DE USUARIOS EN ROOM ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarUsuario(usuario: Usuario)

    @Query("SELECT * FROM usuarios WHERE UPPER(rut) = UPPER(:rut) AND contrasena = :pass LIMIT 1")
    suspend fun loginUsuario(rut: String, pass: String): Usuario?

    @Query("SELECT * FROM usuarios WHERE UPPER(rut) = UPPER(:rut) LIMIT 1")
    suspend fun buscarUsuarioPorRut(rut: String): Usuario?

    @Query("SELECT COUNT(*) FROM usuarios")
    suspend fun contarUsuarios(): Int
}

// 2. ROOM DATABASE: Conexión SQLite Local
@Database(
    entities = [Usuario::class, DocumentoEntity::class],
    version = 1,
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
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// 3. REPOSITORY: Intermediario limpio entre ViewModel y DAO (Flujo exigido)
class CmqRepository(private val dao: CmqDao) {

    val todosLosDocumentos: Flow<List<DocumentoEntity>> = dao.obtenerTodosLosDocumentos()

    suspend fun insertarDocumento(doc: DocumentoEntity) {
        dao.insertarDocumento(doc)
    }

    suspend fun actualizarDocumento(doc: DocumentoEntity) {
        dao.actualizarDocumento(doc)
    }

    suspend fun eliminarDocumento(doc: DocumentoEntity) {
        dao.eliminarDocumento(doc)
    }

    suspend fun registrarUsuario(usuario: Usuario) {
        dao.insertarUsuario(usuario)
    }

    suspend fun autenticar(rut: String, pass: String): Usuario? {
        return dao.loginUsuario(rut, pass)
    }

    suspend fun existeRut(rut: String): Boolean {
        return dao.buscarUsuarioPorRut(rut) != null
    }

    // Semilla inicial para que las cuentas de Administrativo, Jefatura y un documento de prueba existan al arrancar
    suspend fun inicializarDatosPorDefecto() {
        if (dao.contarUsuarios() == 0) {
            dao.insertarUsuario(
                Usuario(
                    rut = "12345678-9",
                    nombre = "Sebastián Admin",
                    contrasena = "1234",
                    perfil = "Administrativo",
                    centroSalud = "Administración Central CMQ",
                    cargoProfesion = "Encargado TI y Sistemas",
                    tipoContrato = "Planta"
                )
            )
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
                    rutFuncionario = "12345678-9",
                    nombreFuncionario = "Sebastián Admin",
                    estado = "Verificado por Admin",
                    fechaCarga = "27/10/2026 21:00",
                    ultimaModificacionPor = "Sebastián Admin",
                    tieneRespaldoCamara = true
                )
            )
        }
    }
}