package com.genoboi.data.ml

import com.genoboi.domain.model.*
import java.time.LocalDate
import java.time.Period
import kotlin.math.abs

data class ScoredAnimal(
    val animal: Animal,
    val scoreFinal: Float,
    val classificacao: String,
    val fatores: List<String>
)

object GeneticRankingHelper {

    fun ranquearAnimais(objetivo: String, animais: List<Animal>): List<ScoredAnimal> {
        return animais.map { animal ->
            calcularScoreParaAnimal(objetivo.lowercase(), animal)
        }.sortedByDescending { it.scoreFinal }
    }

    private fun calcularScoreParaAnimal(objetivo: String, animal: Animal): ScoredAnimal {
        val age = calculateAge(animal.dataNascimento)
        val reproductiveBase = if (animal.sexo == Sexo.FEMEA) {
            calcularBaseFemea(animal, age)
        } else {
            calcularBaseMacho(animal, age)
        }

        val prolificidade = if (animal.sexo == Sexo.FEMEA) {
            (animal.filhosNascidosMatriz / 12f).coerceAtMost(1f)
        } else {
            (animal.filhosNascidosMacho / 40f).coerceAtMost(1f)
        }

        val breedBonus = calcularBonusRaca(objetivo, animal.raca)
        
        val baseScore = when (objetivo) {
            "leite" -> {
                val fertilityPenalty = (1f - (animal.abortos / 4f)).coerceIn(0.4f, 1f)
                reproductiveBase * 0.30f + (animal.escoreCorporal / 5f) * 0.24f + prolificidade * 0.20f + fertilityPenalty * 0.14f + (animal.pesoKg / 700f).coerceAtMost(1f) * 0.12f
            }
            "corte" -> {
                val weightDivisor = if (animal.sexo == Sexo.MACHO) 1100f else 700f
                val semenScore = if (animal.sexo == Sexo.MACHO) animal.qualidadeSemenMacho / 5f else 0.6f // default para fêmeas no corte
                (animal.pesoKg / weightDivisor).coerceAtMost(1f) * 0.34f + reproductiveBase * 0.24f + semenScore * 0.16f + prolificidade * 0.14f + (animal.escoreCorporal / 5f) * 0.12f
            }
            "fertilidade" -> {
                val fertilityPenalty = (1f - (animal.abortos / 4f)).coerceIn(0.4f, 1f)
                val semenScore = if (animal.sexo == Sexo.MACHO) animal.qualidadeSemenMacho / 5f else 0.6f
                reproductiveBase * 0.42f + prolificidade * 0.20f + semenScore * 0.18f + fertilityPenalty * 0.10f + (animal.escoreCorporal / 5f) * 0.10f
            }
            else -> reproductiveBase
        }

        val finalValue = ((baseScore + breedBonus) * 100).coerceAtMost(100f)
        
        return ScoredAnimal(
            animal = animal,
            scoreFinal = finalValue,
            classificacao = when {
                finalValue >= 82 -> "Elite genética"
                finalValue >= 68 -> "Alto potencial"
                finalValue >= 54 -> "Bom desempenho"
                else -> "Monitorar"
            },
            fatores = identificarFatores(animal, objetivo, breedBonus > 0)
        )
    }

    private fun calcularBaseFemea(a: Animal, age: Float): Float {
        val eccScore = (a.escoreCorporal / 5f) * 0.24f
        val prolificidade = (a.filhosNascidosMatriz / 12f).coerceAtMost(1f) * 0.18f
        val endogamia = (1f - (a.parentescoEndogamia * 1.5f).coerceAtMost(0.5f)) * 0.16f
        val abortos = (1f - (a.abortos / 4f).coerceAtMost(0.6f)) * 0.14f
        
        val posPartoVal = if (a.diasDesdeUltimoParto > 0) a.diasDesdeUltimoParto.toFloat() else 120f
        val posParto = scoreRange(posPartoVal, 70f, 160f) * 0.14f
        
        val idealAge = when (a.especie) {
            Especie.BOVINO -> scoreRange(age, 3f, 7f)
            else -> scoreRange(age, 2f, 6f)
        } * 0.08f
        
        val peso = (a.pesoKg / 700f).coerceAtMost(1f) * 0.06f
        
        return eccScore + prolificidade + endogamia + abortos + posParto + idealAge + peso
    }

    private fun calcularBaseMacho(a: Animal, age: Float): Float {
        val semen = (a.qualidadeSemenMacho / 5f) * 0.34f
        val prolificidade = (a.filhosNascidosMacho / 40f).coerceAtMost(1f) * 0.24f
        val endogamia = (1f - (a.parentescoEndogamia * 1.5f).coerceAtMost(0.5f)) * 0.18f
        
        val idealAge = when (a.especie) {
            Especie.BOVINO -> scoreRange(age, 3f, 8f)
            else -> scoreRange(age, 2f, 6.5f)
        } * 0.14f
        
        val peso = (a.pesoKg / 1100f).coerceAtMost(1f) * 0.10f
        
        return semen + prolificidade + endogamia + idealAge + peso
    }

    private fun scoreRange(valor: Float, min: Float, max: Float): Float {
        val centro = (min + max) / 2f
        val range = (max - min) / 2f
        if (range == 0f) return 1f
        val res = 1f - abs(valor - centro) / range
        return res.coerceIn(0f, 1f)
    }

    private fun calcularBonusRaca(objetivo: String, raca: String): Float {
        val racaNorm = raca.lowercase().trim()
        val bonus = when (objetivo.lowercase()) {
            "leite" -> listOf("girolando", "holandes", "gir", "saanen", "toggenburg")
            "corte" -> listOf("angus", "nelore", "boer", "dorper", "texel", "morada nova")
            "fertilidade" -> listOf("gir", "girolando", "santa ines", "moxoto", "anglo nubiana")
            else -> emptyList()
        }
        return if (bonus.any { racaNorm.contains(it) }) 0.18f else 0f
    }

    private fun identificarFatores(a: Animal, objetivo: String, temBonusRaca: Boolean): List<String> {
        val list = mutableListOf<String>()
        if (temBonusRaca) list.add("Aptidão racial para $objetivo")
        if (a.escoreCorporal >= 3.5f) list.add("Excelente escore corporal")
        if (a.parentescoEndogamia < 0.05f) list.add("Baixa endogamia (vigor híbrido)")
        if (a.sexo == Sexo.MACHO && a.qualidadeSemenMacho >= 4f) list.add("Alta qualidade seminal")
        if (a.sexo == Sexo.FEMEA && a.abortos == 0) list.add("Histórico reprodutivo estável")
        
        return list.take(3)
    }

    private fun calculateAge(nascimento: LocalDate): Float {
        val p = Period.between(nascimento, LocalDate.now())
        return p.years + (p.months / 12f)
    }
}
