package com.genoboi.data.repository

import android.util.Log
import com.genoboi.data.local.dao.AnimalDao
import com.genoboi.data.local.dao.CicloCioDao
import com.genoboi.data.local.dao.EventoReprodutivoDao
import com.genoboi.data.local.entity.*
import com.genoboi.data.ml.PrenhezModelHelper
import com.genoboi.data.remote.SupabaseRepository
import com.genoboi.data.remote.dto.toDomain
import com.genoboi.domain.model.Animal
import com.genoboi.domain.model.EventoReprodutivo
import com.genoboi.domain.model.ResultadoPrenhez
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class AnimalRepository(
    private val animalDao: AnimalDao,
    private val eventoDao: EventoReprodutivoDao,
    private val cicloDao: CicloCioDao,
    private val modelHelper: PrenhezModelHelper? = null,
    private val remoteRepo: SupabaseRepository? = null
) {
    private val ioScope = CoroutineScope(Dispatchers.IO)

    // ── ML ────────────────────────────────────────────────────────────────────

    fun simularPrenhez(matriz: Animal, macho: Animal): ResultadoPrenhez? =
        modelHelper?.predict(matriz, macho)

    // ── Leitura ───────────────────────────────────────────────────────────────

    fun observarAnimais(): Flow<List<Animal>> {
        val produtorId = remoteRepo?.getProdutorId()
        return if (produtorId != null) {
            animalDao.observarPorProdutor(produtorId).map { list -> list.map { it.toDomain() } }
        } else {
            animalDao.observarTodos().map { list -> list.map { it.toDomain() } }
        }
    }

    suspend fun buscarAnimalPorId(id: Long): Animal? =
        animalDao.buscarPorId(id)?.toDomain()

    suspend fun buscarSupabaseId(id: Long): String? =
        animalDao.buscarPorId(id)?.supabaseId

    suspend fun contarAnimais(): Int {
        val produtorId = remoteRepo?.getProdutorId()
        return if (produtorId != null) {
            animalDao.contarPorProdutor(produtorId)
        } else {
            animalDao.contarTotal()
        }
    }

    fun observarEventos(animalId: Long): Flow<List<EventoReprodutivo>> =
        eventoDao.observarPorAnimal(animalId).map { list -> list.map { it.toDomain() } }

    // ── Escrita (local + sync remoto assíncrono) ───────────────────────────────

    suspend fun salvarAnimal(animal: Animal): Long {
        val produtorId = remoteRepo?.getProdutorId()
        val entity = animal.toEntity(produtorId)
        val localId = animalDao.inserir(entity)

        remoteRepo?.let { remote ->
            ioScope.launch {
                val supabaseId = remote.inserirAnimal(animal.copy(id = localId))
                if (supabaseId != null) {
                    animalDao.buscarPorId(localId)?.let { existing ->
                        animalDao.atualizar(existing.copy(supabaseId = supabaseId))
                    }
                }
            }
        }
        return localId
    }

    suspend fun atualizarAnimal(animal: Animal) {
        val produtorId = remoteRepo?.getProdutorId()
        animalDao.atualizar(animal.toEntity(produtorId).copy(
            supabaseId = animalDao.buscarPorId(animal.id)?.supabaseId
        ))

        remoteRepo?.let { remote ->
            val supabaseId = animalDao.buscarPorId(animal.id)?.supabaseId
            ioScope.launch {
                if (supabaseId != null) {
                    remote.atualizarAnimal(animal, supabaseId)
                } else {
                    val newId = remote.inserirAnimal(animal)
                    if (newId != null) {
                        animalDao.buscarPorId(animal.id)?.let {
                            animalDao.atualizar(it.copy(supabaseId = newId))
                        }
                    }
                }
            }
        }
    }

    suspend fun deletarAnimal(id: Long) {
        val supabaseId = animalDao.buscarPorId(id)?.supabaseId
        animalDao.deletarPorId(id)
        if (supabaseId != null) {
            remoteRepo?.let { remote ->
                ioScope.launch { remote.deletarAnimal(supabaseId) }
            }
        }
    }

    suspend fun salvarEvento(evento: EventoReprodutivo): Long {
        val localId = eventoDao.inserir(evento.toEntity())
        remoteRepo?.let { remote ->
            ioScope.launch {
                val supabaseAnimalId = animalDao.buscarPorId(evento.animalId)?.supabaseId
                if (supabaseAnimalId != null) {
                    remote.inserirEvento(evento, supabaseAnimalId)
                }
            }
        }
        return localId
    }

    // ── Sync inicial ──────────────────────────────────────────────────────────

    suspend fun sincronizarDeRemoto() {
        val remote = remoteRepo ?: return
        val produtorId = remote.getProdutorId() ?: return
        try {
            val remotos = remote.listarAnimais()
            for (dto in remotos) {
                val supabaseId = dto.id ?: continue
                val animal = dto.toDomain()
                val existente = animalDao.buscarPorSupabaseId(supabaseId)
                if (existente == null) {
                    val localId = animalDao.inserir(
                        animal.toEntity(produtorId).copy(supabaseId = supabaseId)
                    )
                    Log.d("Supabase", "Importado do remoto: ${animal.nome} (localId=$localId)")
                } else {
                    animalDao.atualizar(
                        animal.toEntity(produtorId).copy(id = existente.id, supabaseId = supabaseId)
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("Supabase", "Erro na sincronização: ${e.message}")
        }
    }
}
