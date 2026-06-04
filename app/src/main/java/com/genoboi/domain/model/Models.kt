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

data class Produtor(
    val supabaseId: String,
    val userId: String,
    val email: String,
    val nome: String,
    val nomeFazenda: String = "",
    val municipio: String = "",
    val estado: String = "CE"
)

data class Animal(
    val id: Long = 0,
    val nome: String,
    val especie: Especie,
    val raca: String,
    val sexo: Sexo,
    val dataNascimento: LocalDate,
    val pesoKg: Float = 0f,
    val escoreCorporal: Float = 3f,
    val fazenda: String = "",
    val fotoUrl: String? = null,

    // Pedigree
    val nomePai: String = "",
    val racaPai: String = "",
    val rfidPai: String = "",
    val nomeMae: String = "",
    val racaMae: String = "",
    val rfidMae: String = "",

    // Atributos Matriz (Fêmea)
    val numeroPartos: Int = 0,
    val abortos: Int = 0,
    val diasDesdeUltimoParto: Int = 0,
    val filhosNascidosMatriz: Int = 0,

    // Atributos Reprodutor (Macho)
    val qualidadeSemenMacho: Float,
    val filhosNascidosMacho: Int = 0,

    // IA / saída
    val parentescoEndogamia: Float = 0f,
    val chancePrenhezGerada: Float = 0f,
    val prenhou: Boolean = false,

    // GeneMatch
    val disponivelMatch: Boolean = true
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

// ─── Relatórios ──────────────────────────────────────────────────────────────

data class RelatorioDesempenho(
    val especie: Especie?, // null para consolidado
    val taxaPrenhez: Float,
    val intervaloPartosDias: Int,
    val totalCiosDetectados: Int,
    val totalInseminacoes: Int,
    val taxaSucessoIA: Float,
    val mediaGanhoGenetico: Float,
    val animaisEmGestacao: Int
)

data class ResultadoPrenhez(
    val probabilidade: Float,  // 0.0 a 1.0
    val percentual: Int,       // (probabilidade * 100).toInt()
    val prenhez: Boolean       // classePredita == 1
)
