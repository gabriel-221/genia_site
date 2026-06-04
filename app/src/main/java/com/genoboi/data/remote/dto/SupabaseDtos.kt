package com.genoboi.data.remote.dto

import com.genoboi.domain.model.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ── Produtor ──────────────────────────────────────────────────────────────────

@Serializable
data class ProdutorDto(
    val id: String? = null,
    @SerialName("user_id")      val userId: String? = null,
    val nome: String,
    val municipio: String = "",
    val estado: String = "CE",
    @SerialName("nome_fazenda") val nomeFazenda: String? = null,
    val cpf: String? = null,
    val telefone: String? = null
)

// ── Animal ────────────────────────────────────────────────────────────────────

@Serializable
data class AnimalDto(
    val id: String? = null,
    @SerialName("produtor_id")          val produtorId: String,
    val nome: String,
    val especie: String,
    val raca: String,
    val sexo: String,
    @SerialName("data_nascimento")      val dataNascimento: String,
    @SerialName("peso_kg")              val pesoKg: Double = 0.0,
    @SerialName("escore_corporal")      val escoreCorporal: Double = 3.0,
    val fazenda: String = "",
    @SerialName("foto_url")             val fotoUrl: String? = null,
    @SerialName("nome_pai")             val nomePai: String = "",
    @SerialName("raca_pai")             val racaPai: String = "",
    @SerialName("rfid_pai")             val rfidPai: String = "",
    @SerialName("nome_mae")             val nomeMae: String = "",
    @SerialName("raca_mae")             val racaMae: String = "",
    @SerialName("rfid_mae")             val rfidMae: String = "",
    @SerialName("numero_partos")        val numeroPartos: Int = 0,
    val abortos: Int = 0,
    @SerialName("dias_ultimo_parto")    val diasUltimoParto: Int = 0,
    @SerialName("filhos_matriz")        val filhosMatriz: Int = 0,
    @SerialName("qualidade_semen")      val qualidadeSemen: Double = 3.0,
    @SerialName("filhos_macho")         val filhosMacho: Int = 0,
    @SerialName("chance_prenhez")       val chancePrenhez: Double? = null,
    val prenhou: Boolean = false,
    @SerialName("disponivel_match")     val disponivelMatch: Boolean = true
)

fun Animal.toDto(produtorId: String): AnimalDto = AnimalDto(
    produtorId       = produtorId,
    nome             = nome,
    especie          = especie.label.lowercase(),
    raca             = raca,
    sexo             = sexo.name.lowercase(),
    dataNascimento   = dataNascimento.toString(),
    pesoKg           = pesoKg.toDouble(),
    escoreCorporal   = escoreCorporal.toDouble(),
    fazenda          = fazenda,
    fotoUrl          = fotoUrl,
    nomePai          = nomePai,
    racaPai          = racaPai,
    rfidPai          = rfidPai,
    nomeMae          = nomeMae,
    racaMae          = racaMae,
    rfidMae          = rfidMae,
    numeroPartos     = numeroPartos,
    abortos          = abortos,
    diasUltimoParto  = diasDesdeUltimoParto,
    filhosMatriz     = filhosNascidosMatriz,
    qualidadeSemen   = qualidadeSemenMacho.toDouble(),
    filhosMacho      = filhosNascidosMacho,
    chancePrenhez    = if (chancePrenhezGerada > 0) chancePrenhezGerada.toDouble() else null,
    prenhou          = prenhou,
    disponivelMatch  = disponivelMatch
)

fun AnimalDto.toDomain(localId: Long = 0): Animal = Animal(
    id                   = localId,
    nome                 = nome,
    especie              = when (especie.lowercase()) {
        "ovino"   -> Especie.OVINO
        "caprino" -> Especie.CAPRINO
        else      -> Especie.BOVINO
    },
    raca                 = raca,
    sexo                 = if (sexo.lowercase() == "macho") Sexo.MACHO else Sexo.FEMEA,
    dataNascimento       = java.time.LocalDate.parse(dataNascimento),
    pesoKg               = pesoKg.toFloat(),
    escoreCorporal       = escoreCorporal.toFloat(),
    fazenda              = fazenda,
    fotoUrl              = fotoUrl,
    nomePai              = nomePai,
    racaPai              = racaPai,
    rfidPai              = rfidPai,
    nomeMae              = nomeMae,
    racaMae              = racaMae,
    rfidMae              = rfidMae,
    numeroPartos         = numeroPartos,
    abortos              = abortos,
    diasDesdeUltimoParto = diasUltimoParto,
    filhosNascidosMatriz = filhosMatriz,
    qualidadeSemenMacho  = qualidadeSemen.toFloat(),
    filhosNascidosMacho  = filhosMacho,
    chancePrenhezGerada  = chancePrenhez?.toFloat() ?: 0f,
    prenhou              = prenhou,
    disponivelMatch      = disponivelMatch
)

// ── Evento Reprodutivo ────────────────────────────────────────────────────────

@Serializable
data class EventoDto(
    val id: String? = null,
    @SerialName("animal_id")            val animalId: String,
    val tipo: String,
    @SerialName("data_evento")          val dataEvento: String,
    val observacoes: String = "",
    @SerialName("semen_reprodutor")     val semenReprodutor: String = "",
    @SerialName("tecnico_responsavel")  val tecnicoResponsavel: String = "",
    @SerialName("score_ia_prenhez")     val scoreIaPrenhez: Double? = null,
    @SerialName("gestacao_confirmada")  val gestacaoConfirmada: Boolean? = null
)

fun EventoReprodutivo.toDto(supabaseAnimalId: String): EventoDto = EventoDto(
    animalId           = supabaseAnimalId,
    tipo               = when (tipo) {
        TipoEvento.CIO         -> "cio"
        TipoEvento.INSEMINACAO -> "inseminacao"
        TipoEvento.DIAGNOSTICO -> "diagnostico"
        TipoEvento.PARTO       -> "parto"
        TipoEvento.DESMAME     -> "desmame"
    },
    dataEvento         = data.toString(),
    observacoes        = observacao,
    semenReprodutor    = semenReprodutor,
    tecnicoResponsavel = tecnicoResponsavel,
    scoreIaPrenhez     = scoreIaPrenhez?.toDouble(),
    gestacaoConfirmada = gestacaoConfirmada
)
