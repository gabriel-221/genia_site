package com.genoboi.data.local.entity

import com.genoboi.domain.model.*
import java.time.LocalDate

fun AnimalEntity.toDomain(): Animal = Animal(
    id               = id,
    nome             = nome,
    especie          = Especie.valueOf(especie),
    raca             = raca,
    linhagem         = linhagem,
    sexo             = Sexo.valueOf(sexo),
    dataNascimento   = LocalDate.parse(dataNascimento),
    rfid             = rfid,
    pesoKg           = pesoKg,
    escoreCorporal   = escoreCorporal,
    coefEndogamia    = coefEndogamia,
    fazenda          = fazenda,
    fotoUrl          = fotoUrl,
    nomePai          = nomePai,
    racaPai          = racaPai,
    rfidPai          = rfidPai,
    nomeMae          = nomeMae,
    racaMae          = racaMae,
    rfidMae          = rfidMae
)

fun Animal.toEntity(): AnimalEntity = AnimalEntity(
    id               = id,
    nome             = nome,
    especie          = especie.name,
    raca             = raca,
    linhagem         = linhagem,
    sexo             = sexo.name,
    dataNascimento   = dataNascimento.toString(),
    rfid             = rfid,
    pesoKg           = pesoKg,
    escoreCorporal   = escoreCorporal,
    coefEndogamia    = coefEndogamia,
    fazenda          = fazenda,
    fotoUrl          = fotoUrl,
    nomePai          = nomePai,
    racaPai          = racaPai,
    rfidPai          = rfidPai,
    nomeMae          = nomeMae,
    racaMae          = racaMae,
    rfidMae          = rfidMae,
    criadoEm         = java.time.LocalDateTime.now().toString()
)

fun EventoReprodutivoEntity.toDomain(): EventoReprodutivo = EventoReprodutivo(
    id                  = id,
    animalId            = animalId,
    tipo                = TipoEvento.valueOf(tipo),
    data                = LocalDate.parse(data),
    observacao          = observacao,
    semenReprodutor     = semenReprodutor,
    tecnicoResponsavel  = tecnicoResponsavel,
    scoreIaPrenhez      = scoreIaPrenhez,
    gestacaoConfirmada  = gestacaoConfirmada
)

fun EventoReprodutivo.toEntity(): EventoReprodutivoEntity = EventoReprodutivoEntity(
    id                  = id,
    animalId            = animalId,
    tipo                = tipo.name,
    data                = data.toString(),
    observacao          = observacao,
    semenReprodutor     = semenReprodutor,
    tecnicoResponsavel  = tecnicoResponsavel,
    scoreIaPrenhez      = scoreIaPrenhez,
    gestacaoConfirmada  = gestacaoConfirmada
)
