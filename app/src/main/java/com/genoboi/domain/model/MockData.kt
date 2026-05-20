package com.genoboi.domain.model

import java.time.LocalDate

object MockData {

    val dashboardResumo = DashboardResumo(
        totalAnimais          = 248,
        taxaPrenhez           = 74,
        alertasHoje           = 5,
        compatibilidadeGenetica = 82
    )

    val alertas = listOf(
        AlertaItem("Frida",   "Cio previsto hoje",      TipoAlerta.CIO,         LocalDate.now()),
        AlertaItem("Baleia",  "Parto em 5 dias",         TipoAlerta.PARTO,       LocalDate.now().plusDays(5)),
        AlertaItem("Trovão",  "Alto risco genético",     TipoAlerta.GENETICO,    null),
        AlertaItem("Mimosa",  "Diagnóstico pendente",    TipoAlerta.DIAGNOSTICO, LocalDate.now().plusDays(1)),
        AlertaItem("Neguinha","Cio irregular detectado", TipoAlerta.CIO,         LocalDate.now())
    )

    val prenhez6Meses = listOf(
        Pair("JAN", 65), Pair("FEV", 70), Pair("MAR", 68),
        Pair("ABR", 72), Pair("MAI", 74), Pair("JUN", 74)
    )

    val animaisPorEspecie = listOf(
        Triple(Especie.BOVINO,  148, 0.60f),
        Triple(Especie.OVINO,    62, 0.25f),
        Triple(Especie.CAPRINO,  38, 0.15f)
    )

    val animalFrida = Animal(
        id             = 1,
        nome           = "Frida",
        especie        = Especie.BOVINO,
        raca           = "Nelore",
        linhagem       = "A12",
        sexo           = Sexo.FEMEA,
        dataNascimento = LocalDate.of(2022, 3, 14),
        rfid           = "A3F2019C",
        pesoKg         = 420f,
        escoreCorporal = 3.0f,
        coefEndogamia  = 6.2f,
        fazenda        = "Fazenda Boa Esperança",
        nomePai        = "Trovão",
        racaPai        = "Nelore",
        rfidPai        = "TRV-001",
        nomeMae        = "Mimosa",
        racaMae        = "Nelore",
        rfidMae        = "MIM-045"
    )

    val reprodutores = listOf(
        GeneMatchResult(
            reprodutor = Animal(
                id             = 10,
                nome           = "Trovão",
                especie        = Especie.BOVINO,
                raca           = "Nelore",
                sexo           = Sexo.MACHO,
                dataNascimento = LocalDate.of(2021, 6, 10),
                rfid           = "TRV-001",
                fazenda        = "Fazenda Esperança",
                pesoKg         = 580f
            ),
            scoreCompatibilidade     = 94,
            nivelRisco               = NivelRisco.BAIXO,
            coefEndogamiaEstimado    = 2.1f,
            ganhoGeneticoEstimado    = "+8% peso médio",
            distanciaKm              = 18f
        ),
        GeneMatchResult(
            reprodutor = Animal(
                id             = 11,
                nome           = "Sultão",
                especie        = Especie.BOVINO,
                raca           = "Gir",
                sexo           = Sexo.MACHO,
                dataNascimento = LocalDate.of(2020, 2, 5),
                rfid           = "SUL-002",
                fazenda        = "Fazenda São José",
                pesoKg         = 650f
            ),
            scoreCompatibilidade     = 81,
            nivelRisco               = NivelRisco.BAIXO,
            coefEndogamiaEstimado    = 3.5f,
            ganhoGeneticoEstimado    = "+5% taxa leiteira",
            distanciaKm              = 32f
        ),
        GeneMatchResult(
            reprodutor = Animal(
                id             = 12,
                nome           = "Imperador",
                especie        = Especie.BOVINO,
                raca           = "Brahman",
                sexo           = Sexo.MACHO,
                dataNascimento = LocalDate.of(2019, 9, 20),
                rfid           = "IMP-003",
                fazenda        = "Fazenda Bela Vista",
                pesoKg         = 720f
            ),
            scoreCompatibilidade     = 67,
            nivelRisco               = NivelRisco.MEDIO,
            coefEndogamiaEstimado    = 8.2f,
            ganhoGeneticoEstimado    = "+3% musculatura",
            distanciaKm              = 55f
        )
    )

    val eventosTimelineFrida = listOf(
        EventoReprodutivo(1, 1, TipoEvento.CIO,         LocalDate.of(2025,5,2),  "Muco limpido"),
        EventoReprodutivo(2, 1, TipoEvento.INSEMINACAO,  LocalDate.of(2025,5,4),  semenReprodutor = "Trovão", tecnicoResponsavel = "João Silva", scoreIaPrenhez = 0.78f),
        EventoReprodutivo(3, 1, TipoEvento.DIAGNOSTICO,  LocalDate.of(2025,5,24), observacao = "Previsto"),
        EventoReprodutivo(4, 1, TipoEvento.PARTO,        LocalDate.of(2026,2,15), observacao = "Previsto"),
        EventoReprodutivo(5, 1, TipoEvento.DESMAME,      LocalDate.of(2026,8,15), observacao = "Previsto"),
    )
}
