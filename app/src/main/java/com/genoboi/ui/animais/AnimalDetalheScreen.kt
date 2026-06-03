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
import com.genoboi.ui.calendario.DialogNovoEvento
import com.genoboi.ui.components.GenoTopBar
import com.genoboi.ui.theme.*
import kotlinx.coroutines.launch
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

    var showDialogEvento by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            GenoTopBar(
                titulo = animal?.nome ?: "Detalhes",
                showBack = true,
                onBack = onVoltar,
                actions = {
                    IconButton(onClick = { showDialogEvento = true }) {
                        Icon(Icons.Default.AddCircleOutline, "Evento", tint = GenoGreen800)
                    }
                    var showMenu by remember { mutableStateOf(false) }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, "Menu", tint = GenoGreen800)
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Editar") },
                            onClick = {
                                showMenu = false
                                animal?.let { onEditar(it.id) }
                            },
                            leadingIcon = { Icon(Icons.Default.Edit, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Excluir", color = Color.Red) },
                            onClick = {
                                showMenu = false
                                animal?.let {
                                    scope.launch {
                                        repository.deletarAnimal(it.id)
                                        onVoltar()
                                    }
                                }
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, null, tint = Color.Red) }
                        )
                    }
                }
            )
        },
        containerColor = GenoGray50
    ) { padding ->
        val currentAnimal: Animal? = animal
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
                        }
                    }
                }

                // Info Sections based on the Image requirements
                InfoSection(titulo = "Informações da Unidade") {
                    InfoItem("Espécie", currentAnimal.especie.label)
                    InfoItem("Raça", currentAnimal.raca)
                    InfoItem("Nascimento", currentAnimal.dataNascimento.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                    InfoItem("Idade", idadeAnimal(currentAnimal.dataNascimento))
                    InfoItem("Peso", "${currentAnimal.pesoKg} kg")
                    InfoItem("Fazenda", currentAnimal.fazenda)
                }

                if (currentAnimal.sexo == Sexo.FEMEA) {
                    InfoSection(titulo = "Histórico da Matriz") {
                        InfoItem("Escore Corporal (ECC)", currentAnimal.escoreCorporal.toString())
                        InfoItem("Número de Partos", currentAnimal.numeroPartos.toString())
                        InfoItem("Abortos", currentAnimal.abortos.toString())
                        InfoItem("Dias pós-parto", currentAnimal.diasDesdeUltimoParto.toString())
                        InfoItem("Filhos nascidos", currentAnimal.filhosNascidosMatriz.toString())
                    }
                } else {
                    InfoSection(titulo = "Histórico do Reprodutor") {
                        InfoItem("Qualidade Seminal", currentAnimal.qualidadeSemenMacho.toString())
                        InfoItem("Filhos nascidos", currentAnimal.filhosNascidosMacho.toString())
                    }
                }

                if (currentAnimal.nomePai.isNotEmpty() || currentAnimal.nomeMae.isNotEmpty()) {
                    InfoSection(titulo = "Pedigree") {
                        if (currentAnimal.nomePai.isNotEmpty()) {
                            InfoItem("Pai", currentAnimal.nomePai)
                            if (currentAnimal.racaPai.isNotEmpty()) InfoItem("Raça do Pai", currentAnimal.racaPai)
                            if (currentAnimal.rfidPai.isNotEmpty()) InfoItem("Registro/RFID Pai", currentAnimal.rfidPai)
                        }
                        if (currentAnimal.nomeMae.isNotEmpty()) {
                            InfoItem("Mãe", currentAnimal.nomeMae)
                            if (currentAnimal.racaMae.isNotEmpty()) InfoItem("Raça da Mãe", currentAnimal.racaMae)
                            if (currentAnimal.rfidMae.isNotEmpty()) InfoItem("Registro/RFID Mãe", currentAnimal.rfidMae)
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }

    if (showDialogEvento) {
        DialogNovoEvento(onDismiss = { showDialogEvento = false })
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
