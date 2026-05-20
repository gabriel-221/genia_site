package com.genoboi.data.repository

import com.genoboi.data.local.dao.AnimalDao
import com.genoboi.data.local.dao.CicloCioDao
import com.genoboi.data.local.dao.EventoReprodutivoDao
import com.genoboi.data.local.entity.*
import com.genoboi.domain.model.Animal
import com.genoboi.domain.model.EventoReprodutivo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AnimalRepository(
    private val animalDao: AnimalDao,
    private val eventoDao: EventoReprodutivoDao,
    private val cicloDao: CicloCioDao
) {

    fun observarAnimais(): Flow<List<Animal>> =
        animalDao.observarTodos().map { list -> list.map { it.toDomain() } }

    suspend fun salvarAnimal(animal: Animal): Long =
        animalDao.inserir(animal.toEntity())

    suspend fun buscarAnimalPorId(id: Long): Animal? =
        animalDao.buscarPorId(id)?.toDomain()

    suspend fun buscarPorRfid(rfid: String): Animal? =
        animalDao.buscarPorRfid(rfid)?.toDomain()

    suspend fun atualizarAnimal(animal: Animal) =
        animalDao.atualizar(animal.toEntity())

    suspend fun deletarAnimal(id: Long) =
        animalDao.deletarPorId(id)

    suspend fun contarAnimais(): Int =
        animalDao.contarTotal()

    fun observarEventos(animalId: Long): Flow<List<EventoReprodutivo>> =
        eventoDao.observarPorAnimal(animalId).map { list -> list.map { it.toDomain() } }

    suspend fun salvarEvento(evento: EventoReprodutivo): Long =
        eventoDao.inserir(evento.toEntity())
}
