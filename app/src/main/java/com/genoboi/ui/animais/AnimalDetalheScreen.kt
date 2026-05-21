package com.genoboi.ui.animais

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.genoboi.data.repository.AnimalRepository
import com.genoboi.domain.model.Animal
import com.genoboi.domain.model.Sexo
import com.genoboi.ui.components.GenoTopBar
import com.genoboi.ui.components.RfidTag
import com.genoboi.ui.theme.*
import java.time.format.DateTimeFormatter

@Composable
fun AnimalDetalheScreen(
    animalId: Long,
    repository: AnimalRepository,
    onVoltar: () -> Unit,
    onEditar: (Long) -> Unit
) {
    var animal by remember { mutableStateOf<Animal?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(animalId) {
        animal = repository.buscarAnimalPorId(animalId)
    }

    Scaffold(
        topBar = {
            GenoTopBar(
                titulo = animal?.nome ?: "Detalhes",
                showBack = true,
                onBack = onVoltar,
                actions = {
                    IconButton(onClick = { animal?.let { onEditar(it.id) } }) {
                        Icon(Icons.Default.Edit, "Editar", tint = GenoGreen800)
                    }
                }
            )
        },
        containerColor = GenoGray50
    ) { padding ->
        val currentAnimal = animal
        if (currentAnimal == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GenoGreen800)
            }
        } else {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = GenoWhite),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(GenoGreen100),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(currentAnimal.especie.emoji, fontSize = 40.sp)
                        }
                        Spacer(Modifier.width(20.dp))
                        Column {
                            Text(
                                currentAnimal.nome,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = GenoGray900
                            )
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (currentAnimal.sexo == Sexo.FEMEA)
                                        Icons.Default.Female else Icons.Default.Male,
                                    contentDescription = null,
                                    tint = if (currentAnimal.sexo == Sexo.FEMEA) GenoRed else GenoBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    currentAnimal.sexo.label,
                                    fontSize = 14.sp,
                                    color = GenoGray600
                                )
                            }
                            if (currentAnimal.rfid.isNotEmpty()) {
                                Spacer(Modifier.height(8.dp))
                                RfidTag(currentAnimal.rfid)
                            }
                        }
                    }
                }

                // Info Sections
                InfoSection(titulo = "Informações Gerais") {
                    InfoItem("Espécie", currentAnimal.especie.label)
                    InfoItem("Raça", currentAnimal.raca)
                    if (currentAnimal.linhagem.isNotEmpty()) InfoItem("Linhagem", currentAnimal.linhagem)
                    InfoItem("Nascimento", currentAnimal.dataNascimento.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                    InfoItem("Idade", idadeAnimal(currentAnimal.dataNascimento))
                    if (currentAnimal.pesoKg > 0) InfoItem("Peso", "${currentAnimal.pesoKg} kg")
                    InfoItem("Fazenda", currentAnimal.fazenda)
                }

                if (currentAnimal.nomePai.isNotEmpty() || currentAnimal.nomeMae.isNotEmpty()) {
                    InfoSection(titulo = "Pedigree") {
                        if (currentAnimal.nomePai.isNotEmpty()) {
                            InfoItem("Pai", currentAnimal.nomePai)
                            if (currentAnimal.racaPai.isNotEmpty()) InfoItem("Raça do Pai", currentAnimal.racaPai)
                        }
                        if (currentAnimal.nomeMae.isNotEmpty()) {
                            InfoItem("Mãe", currentAnimal.nomeMae)
                            if (currentAnimal.racaMae.isNotEmpty()) InfoItem("Raça da Mãe", currentAnimal.racaMae)
                        }
                        InfoItem("Endogamia", "${currentAnimal.coefEndogamia}%")
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun InfoSection(titulo: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            titulo,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = GenoGreen800,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = GenoWhite),
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}

@Composable
fun InfoItem(label: String, valor: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 14.sp, color = GenoGray500)
        Text(valor, fontSize = 14.sp, color = GenoGray900, fontWeight = FontWeight.Medium)
    }
}

val GenoGray500 = Color(0xFF9AA0A6)
