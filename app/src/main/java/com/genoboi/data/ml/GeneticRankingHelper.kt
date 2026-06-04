package com.genoboi.data.ml

import com.genoboi.domain.model.*
import java.time.LocalDate
import java.time.Period
import kotlin.math.abs

/**
 * Resultado do ranqueamento individual de um animal.
 */
data class ScoredAnimal(
    val animal: Animal,
    val scoreFinal: Float,
    val classificacao: String,
    val fatores: List<String>
)

/**
 * Sugestão de acasalamento (Match) entre um macho e uma fêmea.
 */
data class MatingSuggestion(
    val macho: Animal,
    val femea: Animal,
    val scoreMatch: Float,
    val justificativa: String,
    val ganhosEstimados: List<String>
)

object GeneticRankingHelper {

    /**
     * Gera uma lista de sugestões de acasalamento baseada no objetivo genético.
     * Cruza os melhores machos com as melhores fêmeas, evitando endogamia.
     */
    fun sugerirAcasalamentos(objetivo: String, animais: List<Animal>): List<MatingSuggestion> {
        val machos = animais.filter { it.sexo == Sexo.MACHO }
            .map { calcularScoreParaAnimal(objetivo.lowercase(), it) }
            .sortedByDescending { it.scoreFinal }
            .take(5) // Top 5 machos

        val femeas = animais.filter { it.sexo == Sexo.FEMEA }
            .map { calcularScoreParaAnimal(objetivo.lowercase(), it) }
            .sortedByDescending { it.scoreFinal }
            .take(10) // Top 10 fêmeas

        val sugestoes = mutableListOf<MatingSuggestion>()

        for (m in machos) {
            for (f in femeas) {
                // Evita cruzar mesma espécie se houver erro nos dados, 
                // mas aqui assumimos que o usuário quer compatibilidade de espécie.
                if (m.animal.especie != f.animal.especie) continue

                val scoreBase = (m.scoreFinal + f.scoreFinal) / 2f
                
                // Penalidade por Endogamia (Parentesco)
                // Se forem muito parentes (estimado por raca/nome ou campo específico), reduz o score.
                val endogamiaEstimada = if (m.animal.raca == f.animal.raca) 0.05f else 0f
                val penalty = (1f - endogamiaEstimada * 2f).coerceIn(0.7f, 1f)
                
                val finalScore = scoreBase * penalty

                sugestoes.add(
                    MatingSuggestion(
                        macho = m.animal,
                        femea = f.animal,
                        scoreMatch = finalScore,
                        justificativa = gerarJustificativa(objetivo, m, f),
                        ganhosEstimados = gerarGanhos(objetivo, m, f)
                    )
                )
            }
        }

        return sugestoes.sortedByDescending { it.scoreMatch }.take(10)
    }

    /**
     * Explicação do Cálculo:
     * 1. Base Reprodutiva: Avalia ECC, Prolificidade e Idade Ideal.
     * 2. Objetivo Leite:
     *    - Fêmea: Produção atual (40%) + Prolificidade (20%) + Saúde (20%) + ECC (20%).
     *    - Macho: Aptidão Racial (50%) + Qualidade de Sêmen (30%) + Histórico de Filhos (20%).
     * 3. Objetivo Corte: Peso (40%) + ECC (30%) + Aptidão Racial (30%).
     * 4. Objetivo Fertilidade: Histórico de Partos/Sêmen (50%) + ECC (30%) + Idade (20%).
     */
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

        val breedBonus = calcularBonusRaca(objetivo, animal.raca)
        
        val specificScore = when (objetivo.lowercase()) {
            "leite" -> if (animal.sexo == Sexo.FEMEA) {
                // Fêmea: Valoriza produção real
                val milkScore = (animal.producaoLeiteDiaria / 40f).coerceAtMost(1f)
                milkScore * 0.5f + reproductiveBase * 0.5f
            } else {
                // Macho: Valoriza potencial genético (Raca + Sêmen)
                val geneticPotential = (animal.qualidadeSemenMacho / 5f) * 0.4f + (animal.filhosNascidosMacho / 50f).coerceAtMost(1f) * 0.3f
                geneticPotential * 0.6f + breedBonus * 0.4f
            }
            "corte" -> {
                val weightTarget = if (animal.especie == Especie.BOVINO) 800f else 100f
                val weightScore = (animal.pesoKg / weightTarget).coerceAtMost(1f)
                weightScore * 0.4f + (animal.escoreCorporal / 5f) * 0.3f + reproductiveBase * 0.3f
            }
            "fertilidade" -> {
                val reproScore = if (animal.sexo == Sexo.FEMEA) {
                    (animal.filhosNascidosMatriz / 10f).coerceAtMost(1f)
                } else {
                    (animal.qualidadeSemenMacho / 5f)
                }
                reproScore * 0.6f + reproductiveBase * 0.4f
            }
            else -> reproductiveBase
        }

        val finalValue = ((specificScore + (breedBonus * 0.2f)) * 100).coerceAtMost(100f)
        
        return ScoredAnimal(
            animal = animal,
            scoreFinal = finalValue,
            classificacao = when {
                finalValue >= 80 -> "Elite"
                finalValue >= 65 -> "Superior"
                finalValue >= 50 -> "Regular"
                else -> "Monitorar"
            },
            fatores = identificarFatores(animal, objetivo, breedBonus > 0)
        )
    }

    private fun calcularBaseFemea(a: Animal, age: Float): Float {
        val eccScore = (a.escoreCorporal / 5f) * 0.3f
        val healthScore = (1f - (a.abortos / 3f).coerceAtMost(1f)) * 0.4f
        val ageScore = scoreRange(age, 3f, 8f) * 0.3f
        return eccScore + healthScore + ageScore
    }

    private fun calcularBaseMacho(a: Animal, age: Float): Float {
        val semenScore = (a.qualidadeSemenMacho / 5f) * 0.5f
        val ageScore = scoreRange(age, 2f, 9f) * 0.3f
        val vigorScore = (a.escoreCorporal / 5f) * 0.2f
        return semenScore + ageScore + vigorScore
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
        val aptas = when (objetivo.lowercase()) {
            "leite" -> listOf("girolando", "holandes", "gir", "jersey", "pardo suico")
            "corte" -> listOf("angus", "nelore", "hereford", "brahman", "senepol")
            "fertilidade" -> listOf("nelore", "gir", "girolando", "sindi")
            else -> emptyList()
        }
        return if (aptas.any { racaNorm.contains(it) }) 0.25f else 0f
    }

    private fun identificarFatores(a: Animal, objetivo: String, temBonusRaca: Boolean): List<String> {
        val list = mutableListOf<String>()
        if (temBonusRaca) list.add("Aptidão racial: $objetivo")
        if (a.escoreCorporal >= 3.5f) list.add("Ótimo estado nutricional")
        if (a.sexo == Sexo.MACHO && a.qualidadeSemenMacho >= 4f) list.add("Sêmen de alta performance")
        if (a.sexo == Sexo.FEMEA && a.producaoLeiteDiaria > 20) list.add("Alta produtividade leiteira")
        return list.take(2)
    }

    private fun gerarJustificativa(objetivo: String, m: ScoredAnimal, f: ScoredAnimal): String {
        return "Cruzamento de ${m.animal.nome} (M) com ${f.animal.nome} (F) focado em $objetivo. " +
               "Combina ${if (m.scoreFinal > 70) "excelência genética" else "bom vigor"} do reprodutor com " +
               "${if (f.scoreFinal > 70) "alta performance" else "estabilidade"} da matriz."
    }

    private fun gerarGanhos(objetivo: String, m: ScoredAnimal, f: ScoredAnimal): List<String> {
        val ganhos = mutableListOf<String>()
        when (objetivo.lowercase()) {
            "leite" -> {
                ganhos.add("Aumento estimado na lactação das filhas")
                ganhos.add("Melhoria na conformação de úbere")
            }
            "corte" -> {
                ganhos.add("Ganho de peso na desmama")
                ganhos.add("Melhor acabamento de carcaça")
            }
            "fertilidade" -> {
                ganhos.add("Redução no intervalo entre partos")
                ganhos.add("Aumento na taxa de prenhez")
            }
        }
        return ganhos
    }

    private fun calculateAge(nascimento: LocalDate): Float {
        val p = Period.between(nascimento, LocalDate.now())
        return p.years + (p.months / 12f)
    }
}
