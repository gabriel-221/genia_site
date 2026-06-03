package com.genoboi.data.remote

import android.content.Context
import android.util.Log
import com.genoboi.data.remote.dto.*
import com.genoboi.domain.model.Animal
import com.genoboi.domain.model.EventoReprodutivo
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import java.util.UUID

class SupabaseRepository(private val context: Context) {

    private val client get() = SupabaseConfig.client

    // ── Produtor ──────────────────────────────────────────────────────────────

    // Garante que existe um registro genia_produtor para este dispositivo.
    // Retorna o produtorId (UUID) a ser usado em todas as operações.
    suspend fun garantirProdutor(): String {
        val cached = SupabaseConfig.getProdutorId(context)
        if (cached != null) return cached

        val novoId = UUID.randomUUID().toString()
        try {
            client.from("genia_produtor").upsert(
                ProdutorDto(id = novoId, nome = "GENIA App")
            )
            SupabaseConfig.saveProdutorId(context, novoId)
            Log.d("Supabase", "Produtor criado: $novoId")
        } catch (e: Exception) {
            Log.e("Supabase", "Erro ao criar produtor: ${e.message}")
        }
        return novoId
    }

    // ── Animais ───────────────────────────────────────────────────────────────

    suspend fun inserirAnimal(animal: Animal): String? {
        return try {
            val produtorId = garantirProdutor()
            val resultado = client.from("genia_animal")
                .insert(animal.toDto(produtorId)) {
                    select()
                }
                .decodeSingle<AnimalDto>()
            Log.d("Supabase", "Animal inserido: ${resultado.id}")
            resultado.id
        } catch (e: Exception) {
            Log.e("Supabase", "Erro ao inserir animal: ${e.message}")
            null
        }
    }

    suspend fun atualizarAnimal(animal: Animal, supabaseId: String): Boolean {
        return try {
            val produtorId = garantirProdutor()
            client.from("genia_animal")
                .update(animal.toDto(produtorId)) {
                    filter { eq("id", supabaseId) }
                }
            true
        } catch (e: Exception) {
            Log.e("Supabase", "Erro ao atualizar animal: ${e.message}")
            false
        }
    }

    suspend fun deletarAnimal(supabaseId: String): Boolean {
        return try {
            client.from("genia_animal").delete {
                filter { eq("id", supabaseId) }
            }
            true
        } catch (e: Exception) {
            Log.e("Supabase", "Erro ao deletar animal: ${e.message}")
            false
        }
    }

    // Carrega todos os animais do produtor do Supabase
    suspend fun listarAnimais(): List<AnimalDto> {
        return try {
            val produtorId = garantirProdutor()
            client.from("genia_animal")
                .select {
                    filter { eq("produtor_id", produtorId) }
                }
                .decodeList<AnimalDto>()
        } catch (e: Exception) {
            Log.e("Supabase", "Erro ao listar animais: ${e.message}")
            emptyList()
        }
    }

    // ── Eventos ───────────────────────────────────────────────────────────────

    suspend fun inserirEvento(evento: EventoReprodutivo, supabaseAnimalId: String): String? {
        return try {
            val resultado = client.from("genia_evento_reprodutivo")
                .insert(evento.toDto(supabaseAnimalId)) {
                    select()
                }
                .decodeSingle<EventoDto>()
            resultado.id
        } catch (e: Exception) {
            Log.e("Supabase", "Erro ao inserir evento: ${e.message}")
            null
        }
    }

    suspend fun listarEventos(supabaseAnimalId: String): List<EventoDto> {
        return try {
            client.from("genia_evento_reprodutivo")
                .select {
                    filter { eq("animal_id", supabaseAnimalId) }
                }
                .decodeList<EventoDto>()
        } catch (e: Exception) {
            Log.e("Supabase", "Erro ao listar eventos: ${e.message}")
            emptyList()
        }
    }
}
