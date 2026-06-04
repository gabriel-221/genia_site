package com.genoboi.data.remote

import android.content.Context
import android.util.Log
import com.genoboi.data.remote.dto.*
import com.genoboi.domain.model.Animal
import com.genoboi.domain.model.EventoReprodutivo
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from

class SupabaseRepository(private val context: Context) {

    private val client get() = SupabaseConfig.client

    fun getProdutorId(): String? = SupabaseConfig.getProdutorId(context)

    fun temSessaoAtiva(): Boolean {
        return try {
            client.auth.currentSessionOrNull() != null
        } catch (e: Exception) {
            false
        }
    }

    fun getUsuarioLogado() = client.auth.currentUserOrNull()

    suspend fun buscarProdutorPorUserId(userId: String): ProdutorDto? {
        return try {
            client.from("genia_produtor")
                .select { filter { eq("user_id", userId) } }
                .decodeList<ProdutorDto>()
                .firstOrNull()
        } catch (e: Exception) {
            null
        }
    }

    // ── Produtor ──────────────────────────────────────────────────────────────

    suspend fun garantirProdutor(): String {
        val cached = SupabaseConfig.getProdutorId(context)
        if (cached != null) return cached

        // Tenta pegar o ID do usuário logado se o cache falhou
        val userId = client.auth.currentUserOrNull()?.id
        val idParaUsar = if (userId != null) {
            java.util.UUID.nameUUIDFromBytes(userId.toByteArray()).toString()
        } else {
            java.util.UUID.randomUUID().toString()
        }

        try {
            client.from("genia_produtor").upsert(
                ProdutorDto(id = idParaUsar, nome = "GENIA App")
            )
            SupabaseConfig.saveProdutorId(context, idParaUsar)
            Log.d("Supabase", "Produtor garantido: $idParaUsar")
        } catch (e: Exception) {
            Log.e("Supabase", "Erro ao garantir produtor: ${e.message}")
        }
        return idParaUsar
    }

    // ── Animais ───────────────────────────────────────────────────────────────

    suspend fun inserirAnimal(animal: Animal): String? {
        return try {
            val produtorId = garantirProdutor()
            val resultado = client.from("genia_animal")
                .insert(animal.toDto(produtorId)) { select() }
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

    suspend fun listarAnimais(): List<AnimalDto> {
        val produtorId = getProdutorId() ?: return emptyList()
        return try {
            // Tenta buscar animais por qualquer uma das colunas possíveis de ID
            val lista = client.from("genia_animal")
                .select { 
                    filter { 
                        or {
                            eq("produtor_id", produtorId)
                            eq("produtor_supabase_id", produtorId)
                        }
                    } 
                }
                .decodeList<AnimalDto>()
            Log.d("Supabase", "listarAnimais: ${lista.size} animais encontrados.")
            lista
        } catch (e: Exception) {
            Log.e("Supabase", "Erro ao listar animais: ${e.message}")
            emptyList()
        }
    }

    // ── Eventos ───────────────────────────────────────────────────────────────

    suspend fun inserirEvento(evento: EventoReprodutivo, supabaseAnimalId: String): String? {
        return try {
            val resultado = client.from("genia_evento_reprodutivo")
                .insert(evento.toDto(supabaseAnimalId)) { select() }
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
                .select { filter { eq("animal_id", supabaseAnimalId) } }
                .decodeList<EventoDto>()
        } catch (e: Exception) {
            Log.e("Supabase", "Erro ao listar eventos: ${e.message}")
            emptyList()
        }
    }
}
