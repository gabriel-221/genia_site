package com.genoboi.data.ml

import android.content.Context
import android.util.Log
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import com.genoboi.domain.model.Animal
import com.genoboi.domain.model.Especie
import com.genoboi.domain.model.ResultadoPrenhez
import java.nio.FloatBuffer
import java.time.LocalDate
import java.time.Period

class PrenhezModelHelper(private val context: Context) {

    private val env: OrtEnvironment = OrtEnvironment.getEnvironment()
    private var session: OrtSession? = null

    init {
        try {
            session = carregarModelo(context)
            Log.d("ONNX_DEBUG", "Modelo carregado com sucesso.")
        } catch (e: Exception) {
            Log.e("ONNX_ERROR", "Falha ao carregar modelo: ${e.message}")
        }
    }

    companion object {
        // Raças reconhecidas pelo modelo (exatamente como foram treinadas).
        // Qualquer valor fora dessa lista cai no galho padrão da Random Forest.
        val RACAS_POR_ESPECIE: Map<Especie, List<String>> = mapOf(
            Especie.BOVINO  to listOf(
                "Girolando", "Nelore", "Gir", "Angus", "Brahman", "Holandês",
                "Jersey", "Simmental", "Limousin", "Tabapua", "Senepol",
                "Brangus", "Hereford", "Caracu", "Canchim", "Guzerá"
            ),
            Especie.OVINO   to listOf(
                "Dorper", "Santa Inês", "Texel", "Merino", "Suffolk",
                "Morada Nova", "Somali", "Bergamácia", "Hampshire"
            ),
            Especie.CAPRINO to listOf(
                "Boer", "Anglo-Nubiana", "Saanen", "Alpina", "Toggenburg",
                "Canindé", "Moxotó", "Repartida"
            )
        )

        // Normaliza a string de raça para Title Case, que é o formato esperado pelo modelo.
        fun normalizarRaca(raca: String): String =
            raca.trim().split(Regex("\\s+"))
                .joinToString(" ") { word ->
                    word.lowercase().replaceFirstChar { it.uppercase() }
                }
    }

    private fun carregarModelo(context: Context): OrtSession {
        val bytes = context.assets.open("random_forest_prenhez.onnx").readBytes()
        return env.createSession(bytes)
            ?: throw IllegalStateException("Falha ao carregar o modelo ONNX.")
    }

    private fun floatTensor(v: Float): OnnxTensor {
        return OnnxTensor.createTensor(
            env,
            FloatBuffer.wrap(floatArrayOf(v)),
            longArrayOf(1L, 1L)
        )
    }

    private fun stringTensor(value: String): OnnxTensor {
        return OnnxTensor.createTensor(env, arrayOf(arrayOf(value)))
    }

    fun predict(matriz: Animal, macho: Animal): ResultadoPrenhez {
        val currentSession = session ?: throw IllegalStateException("Sessão ONNX não inicializada.")

        val idadeMatriz = calculateAge(matriz.dataNascimento)
        val idadeMacho  = calculateAge(macho.dataNascimento)
        val parentesco  = 0.05f

        val eccFinal   = if (matriz.escoreCorporal > 0) matriz.escoreCorporal else 3.0f
        val diasFinal  = if (matriz.numeroPartos > 0 && matriz.diasDesdeUltimoParto > 0) matriz.diasDesdeUltimoParto.toFloat() else 90f

        val especieStr  = matriz.especie.label          // já em Title Case pelo enum
        val racaMatriz  = normalizarRaca(matriz.raca)
        val racaMacho   = normalizarRaca(macho.raca)

        Log.d("ONNX_INPUT", """
            |--- INPUTS DO MODELO ---
            |especie               = $especieStr
            |raca_matriz           = $racaMatriz  (original='${matriz.raca}')
            |idade_matriz          = $idadeMatriz  (nascimento=${matriz.dataNascimento})
            |peso_matriz_kg        = ${matriz.pesoKg}
            |ecc_matriz            = $eccFinal  (campo=${matriz.escoreCorporal})
            |numero_partos_matriz  = ${matriz.numeroPartos}
            |abortos_matriz        = ${matriz.abortos}
            |dias_desde_ultimo_parto = $diasFinal  (campo=${matriz.diasDesdeUltimoParto})
            |filhos_nascidos_matriz= ${matriz.filhosNascidosMatriz}
            |raca_macho            = $racaMacho  (original='${macho.raca}')
            |idade_macho           = $idadeMacho  (nascimento=${macho.dataNascimento})
            |peso_macho_kg         = ${macho.pesoKg}
            |qualidade_semen_macho = ${macho.qualidadeSemenMacho}
            |filhos_nascidos_macho = ${macho.filhosNascidosMacho}
            |parentesco_endogamia  = $parentesco
        """.trimMargin())

        val inputs = mutableMapOf<String, OnnxTensor>(
            "especie"                 to stringTensor(especieStr),
            "raca_matriz"             to stringTensor(racaMatriz),
            "idade_matriz"            to floatTensor(idadeMatriz),
            "peso_matriz_kg"          to floatTensor(matriz.pesoKg),
            "ecc_matriz"              to floatTensor(eccFinal),
            "numero_partos_matriz"    to floatTensor(matriz.numeroPartos.toFloat()),
            "abortos_matriz"          to floatTensor(matriz.abortos.toFloat()),
            "dias_desde_ultimo_parto" to floatTensor(diasFinal),
            "filhos_nascidos_matriz"  to floatTensor(matriz.filhosNascidosMatriz.toFloat()),
            "raca_macho"              to stringTensor(racaMacho),
            "idade_macho"             to floatTensor(idadeMacho),
            "peso_macho_kg"           to floatTensor(macho.pesoKg),
            "qualidade_semen_macho"   to floatTensor(macho.qualidadeSemenMacho),
            "filhos_nascidos_macho"   to floatTensor(macho.filhosNascidosMacho.toFloat()),
            "parentesco_endogamia"    to floatTensor(parentesco)
        )

        try {
            val result = currentSession.run(inputs)
            result.use { r ->
                val label         = (r[0].value as LongArray)[0]
                val probabilities = r[1].value as Array<FloatArray>
                val probability   = probabilities[0][1]

                Log.d("ONNX_OUTPUT", "label=$label  p[0]=${probabilities[0][0]}  p[1]=${probabilities[0][1]}  => ${(probability*100).toInt()}%")

                return ResultadoPrenhez(
                    probabilidade = probability,
                    percentual    = (probability * 100).toInt(),
                    prenhez       = label == 1L
                )
            }
        } finally {
            inputs.values.forEach { it.close() }
        }
    }

    private fun calculateAge(nascimento: LocalDate): Float {
        val p = Period.between(nascimento, LocalDate.now())
        return p.years + (p.months / 12f)
    }

    fun close() {
        session?.close()
        env.close()
    }
}
