package com.example.cmqsalud.Model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
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