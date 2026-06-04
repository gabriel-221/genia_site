package com.genoboi.data.remote

import android.content.Context
import android.util.Log
import com.genoboi.data.local.dao.ProdutorDao
import com.genoboi.data.local.entity.ProdutorEntity
import com.genoboi.data.remote.dto.ProdutorDto
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import java.security.MessageDigest
import java.util.UUID

class AuthRepository(
    private val context: Context,
    private val produtorDao: ProdutorDao
) {
    private val client get() = SupabaseConfig.client

    fun hashSenha(senha: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(senha.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    // Tenta online; se erro de rede, cai no offline
    suspend fun login(email: String, senha: String): Result<ProdutorEntity> {
        return try {
            loginOnline(email, senha)
        } catch (e: Exception) {
            if (isErroDeRede(e)) {
                loginOffline(email, senha)
            } else {
                Result.failure(traduzirErro(e))
            }
        }
    }

    suspend fun loginOnline(email: String, senha: String): Result<ProdutorEntity> {
        client.auth.signInWith(Email) {
            this.email = email
            this.password = senha
        }
        val user = client.auth.currentUserOrNull()
            ?: return Result.failure(Exception("Autenticação falhou. Tente novamente."))

        // Tenta buscar/criar produtor no Supabase; se falhar, cria localmente com UUID derivado
        // do userId para garantir consistência entre dispositivos mesmo sem acesso ao Supabase.
        val produtorDto = buscarOuCriarProdutor(user.id, email)
            ?: ProdutorDto(
                id        = java.util.UUID.nameUUIDFromBytes(user.id.toByteArray()).toString(),
                userId    = user.id,
                nome      = email.substringBefore("@").replaceFirstChar { it.uppercase() },
                municipio = "",
                estado    = "CE"
            ).also { Log.w("Auth", "Usando produtor local fallback para userId=${user.id}") }

        val entity = ProdutorEntity(
            supabaseId  = produtorDto.id ?: java.util.UUID.nameUUIDFromBytes(user.id.toByteArray()).toString(),
            userId      = user.id,
            email       = email,
            senhaHash   = hashSenha(senha),
            nome        = produtorDto.nome,
            nomeFazenda = produtorDto.nomeFazenda ?: "",
            municipio   = produtorDto.municipio,
            estado      = produtorDto.estado,
            cpf         = produtorDto.cpf ?: "",
            telefone    = produtorDto.telefone ?: ""
        )
        produtorDao.salvar(entity)
        SupabaseConfig.saveProdutorId(context, entity.supabaseId)
        SupabaseConfig.saveEmail(context, email)
        SupabaseConfig.setLogado(context, true)
        Log.d("Auth", "Login online OK: ${entity.email} — produtorId=${entity.supabaseId}")
        return Result.success(entity)
    }

    suspend fun loginOffline(email: String, senha: String): Result<ProdutorEntity> {
        val entity = produtorDao.buscarPorEmail(email)
            ?: return Result.failure(
                Exception("Usuário não encontrado. Conecte à internet para o primeiro acesso.")
            )
        return if (entity.senhaHash == hashSenha(senha)) {
            SupabaseConfig.saveProdutorId(context, entity.supabaseId)
            SupabaseConfig.saveEmail(context, email)
            SupabaseConfig.setLogado(context, true)
            Log.d("Auth", "Login offline OK: ${entity.email}")
            Result.success(entity)
        } else {
            Result.failure(Exception("Senha incorreta."))
        }
    }

    // Reconecta a sessão do Supabase ao reiniciar o app.
    // Sem isso, o client está sem JWT e o RLS bloqueia as queries.
    suspend fun restaurarSessao(email: String, senha: String): Boolean {
        return try {
            client.auth.signInWith(Email) {
                this.email = email
                this.password = senha
            }
            client.auth.currentUserOrNull() != null
        } catch (e: Exception) {
            Log.w("Auth", "Não foi possível restaurar sessão online: ${e.message}")
            false
        }
    }

    suspend fun cadastrar(
        email: String,
        senha: String,
        nome: String,
        nomeFazenda: String,
        municipio: String,
        estado: String
    ): Result<ProdutorEntity> {
        return try {
            client.auth.signUpWith(Email) {
                this.email = email
                this.password = senha
            }
            val user = client.auth.currentUserOrNull()
                ?: return Result.failure(
                    Exception("Conta criada! Verifique seu e-mail para ativar o acesso.")
                )

            // Verifica se já existe produtor para este user_id antes de criar
            val existente = try {
                client.from("genia_produtor")
                    .select { filter { eq("user_id", user.id) } }
                    .decodeList<ProdutorDto>()
                    .firstOrNull()
            } catch (_: Exception) { null }

            val produtorId: String
            if (existente != null && existente.id != null) {
                produtorId = existente.id
                Log.d("Auth", "Produtor já existe: $produtorId")
            } else {
                produtorId = UUID.randomUUID().toString()
                val produtorDto = ProdutorDto(
                    id          = produtorId,
                    userId      = user.id,
                    nome        = nome,
                    municipio   = municipio,
                    estado      = estado,
                    nomeFazenda = nomeFazenda
                )
                client.from("genia_produtor").upsert(produtorDto)
                Log.d("Auth", "Produtor criado: $produtorId")
            }

            val entity = ProdutorEntity(
                supabaseId  = produtorId,
                userId      = user.id,
                email       = email,
                senhaHash   = hashSenha(senha),
                nome        = nome,
                nomeFazenda = nomeFazenda,
                municipio   = municipio,
                estado      = estado
            )
            produtorDao.salvar(entity)
            SupabaseConfig.saveProdutorId(context, produtorId)
            SupabaseConfig.saveEmail(context, email)
            SupabaseConfig.setLogado(context, true)
            Log.d("Auth", "Cadastro OK: $email — produtorId=$produtorId")
            Result.success(entity)
        } catch (e: Exception) {
            Log.e("Auth", "Erro no cadastro: ${e.message}")
            Result.failure(traduzirErro(e))
        }
    }

    suspend fun logout() {
        try { client.auth.signOut() } catch (_: Exception) {}
        SupabaseConfig.setLogado(context, false)
        SupabaseConfig.clearProdutorId(context)
        SupabaseConfig.clearEmail(context)
        Log.d("Auth", "Logout realizado")
    }

    fun isLogado(): Boolean = SupabaseConfig.isLogado(context)
    fun getProdutorId(): String? = SupabaseConfig.getProdutorId(context)
    fun getSavedEmail(): String? = SupabaseConfig.getEmail(context)

    // Retorna null em vez de UUID aleatório — força o caller a tratar o erro
    private suspend fun buscarOuCriarProdutor(userId: String, email: String): ProdutorDto? {
        return try {
            val lista = client.from("genia_produtor")
                .select { filter { eq("user_id", userId) } }
                .decodeList<ProdutorDto>()

            lista.firstOrNull() ?: run {
                val novoId = UUID.randomUUID().toString()
                val dto = ProdutorDto(
                    id        = novoId,
                    userId    = userId,
                    nome      = email.substringBefore("@"),
                    municipio = "",
                    estado    = "CE"
                )
                client.from("genia_produtor").upsert(dto)
                Log.d("Auth", "Produtor criado automaticamente: $novoId para userId=$userId")
                dto
            }
        } catch (e: Exception) {
            Log.e("Auth", "Erro ao buscar/criar produtor: ${e.message}")
            // Retorna null — NÃO cria UUID aleatório que quebraria o sync
            null
        }
    }

    private fun isErroDeRede(e: Exception): Boolean =
        e is java.io.IOException ||
        e.cause is java.io.IOException ||
        e.message?.contains("Unable to resolve host", ignoreCase = true) == true ||
        e.message?.contains("timeout", ignoreCase = true) == true ||
        e.message?.contains("network", ignoreCase = true) == true ||
        e.message?.contains("connect", ignoreCase = true) == true

    private fun traduzirErro(e: Exception): Exception {
        val msg = e.message ?: return Exception("Erro desconhecido. Tente novamente.")
        return when {
            msg.contains("Invalid login credentials", ignoreCase = true) ->
                Exception("E-mail ou senha incorretos.")
            msg.contains("Email not confirmed", ignoreCase = true) ->
                Exception("Confirme seu e-mail antes de fazer login.")
            msg.contains("User already registered", ignoreCase = true) ->
                Exception("Este e-mail já está cadastrado.")
            msg.contains("Password should be", ignoreCase = true) ->
                Exception("A senha deve ter pelo menos 6 caracteres.")
            msg.contains("email rate limit", ignoreCase = true) ||
            msg.contains("rate limit", ignoreCase = true) ->
                Exception("Muitos cadastros em pouco tempo. Aguarde alguns minutos e tente novamente.")
            else -> Exception("Erro: $msg")
        }
    }
}
