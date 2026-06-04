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

        val produtorDto = buscarOuCriarProdutor(user.id, email)
        val entity = ProdutorEntity(
            supabaseId  = produtorDto.id ?: UUID.randomUUID().toString(),
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
        SupabaseConfig.setLogado(context, true)
        Log.d("Auth", "Login online OK: ${entity.email}")
        return Result.success(entity)
    }

    suspend fun loginOffline(email: String, senha: String): Result<ProdutorEntity> {
        val entity = produtorDao.buscarPorEmail(email)
            ?: return Result.failure(
                Exception("Usuário não encontrado. Conecte à internet para o primeiro acesso.")
            )
        return if (entity.senhaHash == hashSenha(senha)) {
            SupabaseConfig.saveProdutorId(context, entity.supabaseId)
            SupabaseConfig.setLogado(context, true)
            Log.d("Auth", "Login offline OK: ${entity.email}")
            Result.success(entity)
        } else {
            Result.failure(Exception("Senha incorreta."))
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
            // Aguarda sessão após sign-up (auto-confirm deve estar habilitado no Supabase)
            val user = client.auth.currentUserOrNull()
                ?: return Result.failure(
                    Exception("Conta criada! Verifique seu e-mail para ativar o acesso.")
                )

            val novoId = UUID.randomUUID().toString()
            val produtorDto = ProdutorDto(
                id          = novoId,
                userId      = user.id,
                nome        = nome,
                municipio   = municipio,
                estado      = estado,
                nomeFazenda = nomeFazenda
            )
            client.from("genia_produtor").insert(produtorDto)

            val entity = ProdutorEntity(
                supabaseId  = novoId,
                userId      = user.id,
                email       = email,
                senhaHash   = hashSenha(senha),
                nome        = nome,
                nomeFazenda = nomeFazenda,
                municipio   = municipio,
                estado      = estado
            )
            produtorDao.salvar(entity)
            SupabaseConfig.saveProdutorId(context, novoId)
            SupabaseConfig.setLogado(context, true)
            Log.d("Auth", "Cadastro OK: $email")
            Result.success(entity)
        } catch (e: Exception) {
            Log.e("Auth", "Erro no cadastro: ${e.message}")
            Result.failure(traduzirErro(e))
        }
    }

    suspend fun logout() {
        try { client.auth.signOut() } catch (_: Exception) {}
        // Mantém ProdutorEntity para permitir login offline futuro
        SupabaseConfig.setLogado(context, false)
        SupabaseConfig.clearProdutorId(context)
        Log.d("Auth", "Logout realizado")
    }

    fun isLogado(): Boolean = SupabaseConfig.isLogado(context)

    fun getProdutorId(): String? = SupabaseConfig.getProdutorId(context)

    // ── Privados ──────────────────────────────────────────────────────────────

    private suspend fun buscarOuCriarProdutor(userId: String, email: String): ProdutorDto {
        return try {
            val lista = client.from("genia_produtor")
                .select { filter { eq("user_id", userId) } }
                .decodeList<ProdutorDto>()
            lista.firstOrNull() ?: criarProdutorRemoto(userId, email)
        } catch (e: Exception) {
            Log.e("Auth", "Erro ao buscar produtor remoto: ${e.message}")
            // Retorna um DTO mínimo para não bloquear o login
            ProdutorDto(
                id        = UUID.randomUUID().toString(),
                userId    = userId,
                nome      = email.substringBefore("@"),
                municipio = "",
                estado    = "CE"
            )
        }
    }

    private suspend fun criarProdutorRemoto(userId: String, email: String): ProdutorDto {
        val novoId = UUID.randomUUID().toString()
        val dto = ProdutorDto(
            id        = novoId,
            userId    = userId,
            nome      = email.substringBefore("@"),
            municipio = "",
            estado    = "CE"
        )
        client.from("genia_produtor").upsert(dto)
        return dto
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
            else -> Exception("Erro: $msg")
        }
    }
}
