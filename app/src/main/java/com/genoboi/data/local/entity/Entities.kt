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
    val especie: String,          // enum name
    val raca: String,
    val linhagem: String = "",
    val sexo: String,             // enum name
    val dataNascimento: String,   // ISO-8601 yyyy-MM-dd
    val rfid: String = "",
    val pesoKg: Float = 0f,
    val escoreCorporal: Float = 3f,
    val coefEndogamia: Float = 0f,
    val fazenda: String = "",
    val fotoUrl: String? = null,
    val nomePai: String = "",
    val racaPai: String = "",
    val rfidPai: String = "",
    val nomeMae: String = "",
    val racaMae: String = "",
    val rfidMae: String = "",
    val criadoEm: String = ""     // ISO-8601 timestamp
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
    val tipo: String,             // TipoEvento.name
    val data: String,             // yyyy-MM-dd
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
