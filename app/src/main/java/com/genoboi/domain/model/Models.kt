package com.genoboi.domain.model

import java.time.LocalDate

// ─── Enums ───────────────────────────────────────────────────────────────────

enum class Especie(val label: String, val emoji: String) {
    BOVINO("Bovino", "🐄"),
    OVINO("Ovino", "🐑"),
    CAPRINO("Caprino", "🐐")
}

enum class Sexo(val label: String) {
    MACHO("Macho"),
    FEMEA("Fêmea")
}

enum class TipoEvento(val label: String) {
    CIO("Cio"),
    INSEMINACAO("Inseminação"),
    DIAGNOSTICO("Diagnóstico"),
    PARTO("Parto"),
    DESMAME("Desmame")
}

enum class NivelRisco(val label: String) {
    BAIXO("Baixo risco"),
    MEDIO("Risco médio"),
    ALTO("Alto risco")
}

// ─── Modelos de domínio ───────────────────────────────────────────────────────

data class Animal(
    val id: Long = 0,
    val nome: String,
    val especie: Especie,
    val raca: String,
    val linhagem: String = "",
    val sexo: Sexo,
    val dataNascimento: LocalDate,
    val rfid: String = "",
    val pesoKg: Float = 0f,
    val escoreCorporal: Float = 3f,
    val coefEndogamia: Float = 0f,
    val fazenda: String = "",
    val fotoUrl: String? = null,
    // Pedigree inline (simplificado)
    val nomePai: String = "",
    val racaPai: String = "",
    val rfidPai: String = "",
    val nomeMae: String = "",
    val racaMae: String = "",
    val rfidMae: String = ""
)

data class EventoReprodutivo(
    val id: Long = 0,
    val animalId: Long,
    val tipo: TipoEvento,
    val data: LocalDate,
    val observacao: String = "",
    val semenReprodutor: String = "",
    val tecnicoResponsavel: String = "",
    val scoreIaPrenhez: Float? = null,
    val gestacaoConfirmada: Boolean? = null
)

data class CicloCio(
    val animalId: Long,
    val dataDeteccao: LocalDate,
    val proximaPrevisao: LocalDate,
    val inseminado: Boolean = false,
    val tipoMuco: String = "",
    val comportamento: String = ""
)

data class GeneMatchResult(
    val reprodutor: Animal,
    val scoreCompatibilidade: Int,           // 0-100
    val nivelRisco: NivelRisco,
    val coefEndogamiaEstimado: Float,
    val ganhoGeneticoEstimado: String = "",
    val distanciaKm: Float = 0f,
    val disponivel: Boolean = true
)

data class AlertaItem(
    val animalNome: String,
    val descricao: String,
    val tipo: TipoAlerta,
    val data: LocalDate? = null
)

enum class TipoAlerta { CIO, INSEMINACAO, PARTO, DIAGNOSTICO, GENETICO }

// ─── Resumo do dashboard ──────────────────────────────────────────────────────

data class DashboardResumo(
    val totalAnimais: Int,
    val taxaPrenhez: Int,      // %
    val alertasHoje: Int,
    val compatibilidadeGenetica: Int  // %
)
