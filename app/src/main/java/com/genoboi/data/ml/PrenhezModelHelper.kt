package com.genoboi.data.ml

import android.content.Context
import android.util.Log
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import com.genoboi.domain.model.Animal
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

        // Cálculo de idade em anos (float)
        val idadeMatriz = calculateAge(matriz.dataNascimento)
        val idadeMacho = calculateAge(macho.dataNascimento)

        // Parentesco: 0.25 se tiverem mesmo pai, 0.02 caso contrário
        val parentesco = 0.05f

        val inputs = mutableMapOf<String, OnnxTensor>(
            "especie"                 to stringTensor(matriz.especie.label.trim()),
            "raca_matriz"             to stringTensor(matriz.raca.trim()),
            "idade_matriz"            to floatTensor(idadeMatriz),
            "peso_matriz_kg"          to floatTensor(matriz.pesoKg),
            "ecc_matriz"              to floatTensor(if (matriz.escoreCorporal > 0) matriz.escoreCorporal else 3.0f),
            "numero_partos_matriz"    to floatTensor(matriz.numeroPartos.toFloat()),
            "abortos_matriz"          to floatTensor(matriz.abortos.toFloat()),
            "dias_desde_ultimo_parto" to floatTensor(if (matriz.numeroPartos > 0 && matriz.diasDesdeUltimoParto > 0) matriz.diasDesdeUltimoParto.toFloat() else 90f),
            "filhos_nascidos_matriz"  to floatTensor(matriz.filhosNascidosMatriz.toFloat()),
            "raca_macho"              to stringTensor(macho.raca.trim()),
            "idade_macho"             to floatTensor(idadeMacho),
            "peso_macho_kg"           to floatTensor(macho.pesoKg),
            "qualidade_semen_macho"   to floatTensor(macho.qualidadeSemenMacho),
            "filhos_nascidos_macho"   to floatTensor(macho.filhosNascidosMacho.toFloat()),
            "parentesco_endogamia"    to floatTensor(parentesco)
        )

        try {
            val result = currentSession.run(inputs)
            result.use { r ->
                val label = (r[0].value as LongArray)[0]
                val probabilities = r[1].value as Array<FloatArray>
                val probability = probabilities[0][1]

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
