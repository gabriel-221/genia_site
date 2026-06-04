package com.genoboi.data.local.entity

import com.genoboi.domain.model.*
import java.time.LocalDate

fun AnimalEntity.toDomain(): Animal = Animal(
    id                   = id,
    nome                 = nome,
    especie              = Especie.valueOf(especie),
    raca                 = raca,
    sexo                 = Sexo.valueOf(sexo),
    dataNascimento       = LocalDate.parse(dataNascimento),
    pesoKg               = pesoKg,
    escoreCorporal       = escoreCorporal,
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
    diasDesdeUltimoParto = diasDesdeUltimoParto,
    filhosNascidosMatriz = filhosNascidosMatriz,
    qualidadeSemenMacho  = qualidadeSemenMacho,
    filhosNascidosMacho  = filhosNascidosMacho,
    parentescoEndogamia  = parentescoEndogamia,
    chancePrenhezGerada  = chancePrenhezGerada,
    prenhou              = prenhou,
    disponivelMatch      = disponivelMatch
)

fun Animal.toEntity(produtorId: String? = null): AnimalEntity = AnimalEntity(
    id                   = id,
    nome                 = nome,
    especie              = especie.name,
    raca                 = raca,
    sexo                 = sexo.name,
    dataNascimento       = dataNascimento.toString(),
    pesoKg               = pesoKg,
    escoreCorporal       = escoreCorporal,
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
    diasDesdeUltimoParto = diasDesdeUltimoParto,
    filhosNascidosMatriz = filhosNascidosMatriz,
    qualidadeSemenMacho  = qualidadeSemenMacho,
    filhosNascidosMacho  = filhosNascidosMacho,
    parentescoEndogamia  = parentescoEndogamia,
    chancePrenhezGerada  = chancePrenhezGerada,
    prenhou              = prenhou,
    produtorSupabaseId   = produtorId,
    disponivelMatch      = disponivelMatch
)

fun EventoReprodutivoEntity.toDomain(): EventoReprodutivo = EventoReprodutivo(
    id                 = id,
    animalId           = animalId,
    tipo               = TipoEvento.valueOf(tipo),
    data               = LocalDate.parse(data),
    observacao         = observacao,
    semenReprodutor    = semenReprodutor,
    tecnicoResponsavel = tecnicoResponsavel,
    scoreIaPrenhez     = scoreIaPrenhez,
    gestacaoConfirmada = gestacaoConfirmada
)

fun EventoReprodutivo.toEntity(): EventoReprodutivoEntity = EventoReprodutivoEntity(
    id                 = id,
    animalId           = animalId,
    tipo               = tipo.name,
    data               = data.toString(),
    observacao         = observacao,
    semenReprodutor    = semenReprodutor,
    tecnicoResponsavel = tecnicoResponsavel,
    scoreIaPrenhez     = scoreIaPrenhez,
    gestacaoConfirmada = gestacaoConfirmada
)
