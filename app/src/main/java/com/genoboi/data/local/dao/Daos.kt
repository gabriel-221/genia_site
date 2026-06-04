package com.genoboi.data.local.dao

import androidx.room.*
import com.genoboi.data.local.entity.AnimalEntity
import com.genoboi.data.local.entity.CicloCioEntity
import com.genoboi.data.local.entity.EventoReprodutivoEntity
import com.genoboi.data.local.entity.ProdutorEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProdutorDao {

    @Query("SELECT * FROM produtores LIMIT 1")
    fun observarAtual(): Flow<ProdutorEntity?>

    @Query("SELECT * FROM produtores WHERE email = :email LIMIT 1")
    suspend fun buscarPorEmail(email: String): ProdutorEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun salvar(produtor: ProdutorEntity)

    @Query("DELETE FROM produtores")
    suspend fun limparTodos()
}

@Dao
interface AnimalDao {

    @Query("SELECT * FROM animais ORDER BY nome ASC")
    fun observarTodos(): Flow<List<AnimalEntity>>

    @Query("SELECT * FROM animais WHERE produtorSupabaseId = :produtorId ORDER BY nome ASC")
    fun observarPorProdutor(produtorId: String): Flow<List<AnimalEntity>>

    @Query("SELECT * FROM animais WHERE especie = :especie AND produtorSupabaseId = :produtorId ORDER BY nome ASC")
    fun observarPorEspecieEProdutor(especie: String, produtorId: String): Flow<List<AnimalEntity>>

    @Query("SELECT * FROM animais WHERE especie = :especie ORDER BY nome ASC")
    fun observarPorEspecie(especie: String): Flow<List<AnimalEntity>>

    @Query("SELECT * FROM animais WHERE id = :id")
    suspend fun buscarPorId(id: Long): AnimalEntity?

    @Query("SELECT COUNT(*) FROM animais WHERE produtorSupabaseId = :produtorId")
    suspend fun contarPorProdutor(produtorId: String): Int

    @Query("SELECT COUNT(*) FROM animais")
    suspend fun contarTotal(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(animal: AnimalEntity): Long

    @Update
    suspend fun atualizar(animal: AnimalEntity)

    @Delete
    suspend fun deletar(animal: AnimalEntity)

    @Query("DELETE FROM animais WHERE id = :id")
    suspend fun deletarPorId(id: Long)

    @Query("SELECT * FROM animais WHERE supabaseId = :supabaseId LIMIT 1")
    suspend fun buscarPorSupabaseId(supabaseId: String): AnimalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun salvarProdutor(produtor: ProdutorEntity)
}

@Dao
interface EventoReprodutivoDao {

    @Query("SELECT * FROM eventos_reprodutivos WHERE animalId = :animalId ORDER BY data DESC")
    fun observarPorAnimal(animalId: Long): Flow<List<EventoReprodutivoEntity>>

    @Query("SELECT * FROM eventos_reprodutivos WHERE animalId = :animalId ORDER BY data DESC")
    suspend fun listarPorAnimal(animalId: Long): List<EventoReprodutivoEntity>

    @Query("SELECT * FROM eventos_reprodutivos WHERE data = :data ORDER BY animalId")
    suspend fun listarPorData(data: String): List<EventoReprodutivoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(evento: EventoReprodutivoEntity): Long

    @Delete
    suspend fun deletar(evento: EventoReprodutivoEntity)
}

@Dao
interface CicloCioDao {

    @Query("SELECT * FROM ciclos_cio WHERE animalId = :animalId ORDER BY dataDeteccao DESC LIMIT 1")
    suspend fun ultimoCiclo(animalId: Long): CicloCioEntity?

    @Query("SELECT * FROM ciclos_cio WHERE proximaPrevisao <= :data ORDER BY proximaPrevisao ASC")
    suspend fun ciosPrevistosAte(data: String): List<CicloCioEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(ciclo: CicloCioEntity): Long
}
