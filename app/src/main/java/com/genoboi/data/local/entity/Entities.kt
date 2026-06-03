package com.genoboi.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "animais")
data class AnimalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nome: String,
    val especie: String,
    val raca: String,
    val sexo: String,
    val dataNascimento: String,
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
    
    // Atributos específicos Matriz
    val numeroPartos: Int = 0,
    val abortos: Int = 0,
    val diasDesdeUltimoParto: Int = 0,
    val filhosNascidosMatriz: Int = 0,
    
    // Atributos específicos Macho
    val qualidadeSemenMacho: Float,
    val filhosNascidosMacho: Int = 0,
    
    // Atributos de saída / IA
    val parentescoEndogamia: Float = 0f,
    val chancePrenhezGerada: Float = 0f,
    val prenhou: Boolean = false,

    // ID do registro no Supabase (null enquanto não sincronizado)
    val supabaseId: String? = null
)

@Entity(
    tableName = "eventos_reprodutivos",
    foreignKeys = [ForeignKey(
        entity = AnimalEntity::class,
        parentColumns = ["id"],
        childColumns = ["animalId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("animalId")]
)
data class EventoReprodutivoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val animalId: Long,
    val tipo: String,
    val data: String,
    val observacao: String = "",
    val semenReprodutor: String = "",
    val tecnicoResponsavel: String = "",
    val scoreIaPrenhez: Float? = null,
    val gestacaoConfirmada: Boolean? = null
)

@Entity(
    tableName = "ciclos_cio",
    foreignKeys = [ForeignKey(
        entity = AnimalEntity::class,
        parentColumns = ["id"],
        childColumns = ["animalId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("animalId")]
)
data class CicloCioEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val animalId: Long,
    val dataDeteccao: String,
    val proximaPrevisao: String,
    val inseminado: Boolean = false,
    val tipoMuco: String = "",
    val comportamento: String = ""
)
