package com.genoboi.ui.animais

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.genoboi.data.repository.AnimalRepository
import com.genoboi.domain.model.Animal
import com.genoboi.domain.model.Especie
import com.genoboi.domain.model.Sexo
import com.genoboi.ui.components.EspecieChip
import com.genoboi.ui.components.RfidTag
import com.genoboi.ui.theme.*
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.Period

@Composable
fun AnimaisScreen(
    repository: AnimalRepository,
    onCadastrarAnimal: () -> Unit,
    onAnimalClick: (Long) -> Unit
) {
    val animais by repository.observarAnimais().collectAsState(initial = emptyList())
    var filtroEspecie by remember { mutableStateOf<Especie?>(null) }
    var query by remember { mutableStateOf("") }

    val animaisFiltrados = animais.filter { a ->
        (filtroEspecie == null || a.especie == filtroEspecie) &&
        (query.isBlank() || a.nome.contains(query, ignoreCase = true) ||
         a.raca.contains(query, ignoreCase = true) ||
         a.rfid.contains(query, ignoreCase = true))
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick           = onCadastrarAnimal,
                containerColor    = GenoGreen800,
                contentColor      = GenoWhite,
                icon              = { Icon(Icons.Default.Add, null) },
                text              = { Text("Cadastrar animal") }
            )
        },
        containerColor = GenoGray50
    ) { padding ->
        LazyColumn(
            modifier       = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Search bar
            item {
                OutlinedTextField(
                    value          = query,
                    onValueChange  = { query = it },
                    placeholder    = { Text("Buscar por nome, raça, RFID...", fontSize = 14.sp) },
                    leadingIcon    = { Icon(Icons.Default.Search, null, tint = GenoGray400) },
                    trailingIcon   = if (query.isNotEmpty()) {{
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Default.Clear, null, tint = GenoGray400)
                        }
                    }} else null,
                    modifier       = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    shape          = RoundedCornerShape(12.dp),
                    singleLine     = true,
                    colors         = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = GenoGreen700,
                        unfocusedBorderColor = GenoGray200,
                        unfocusedContainerColor = GenoWhite,
                        focusedContainerColor   = GenoWhite
                    )
                )
            }

            // Filtros por espécie
            item {
                LazyRow(
                    contentPadding        = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        EspecieChip(
                            label    = "Todos",
                            selected = filtroEspecie == null,
                            onClick  = { filtroEspecie = null }
                        )
                    }
                    items(Especie.values()) { esp ->
                        EspecieChip(
                            label    = "${esp.emoji} ${esp.label}",
                            selected = filtroEspecie == esp,
                            onClick  = { filtroEspecie = if (filtroEspecie == esp) null else esp }
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            // Contador
            item {
                Text(
                    "${animaisFiltrados.size} animal(is)",
                    fontSize = 12.sp,
                    color    = GenoGray400,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            if (animaisFiltrados.isEmpty()) {
                item { EmptyAnimaisPlaceholder(onCadastrar = onCadastrarAnimal) }
            } else {
                items(animaisFiltrados, key = { it.id }) { animal ->
                    AnimalCard(
                        animal  = animal,
                        onClick = { onAnimalClick(animal.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun AnimalCard(animal: Animal, onClick: () -> Unit) {
    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clickable(onClick = onClick),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = GenoWhite),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier            = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(GenoGreen100),
                contentAlignment    = Alignment.Center
            ) {
                Text(animal.especie.emoji, fontSize = 24.sp)
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(animal.nome, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        imageVector        = if (animal.sexo == Sexo.FEMEA)
                            Icons.Default.Female else Icons.Default.Male,
                        contentDescription = animal.sexo.label,
                        tint               = if (animal.sexo == Sexo.FEMEA) GenoRed else GenoBlue,
                        modifier           = Modifier.size(16.dp)
                    )
                }
                Text(
                    "${animal.especie.label} • ${animal.raca}" +
                    if (animal.linhagem.isNotEmpty()) " • ${animal.linhagem}" else "",
                    fontSize = 12.sp, color = GenoGray600
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    val idade = idadeAnimal(animal.dataNascimento)
                    AnimalChip(idade, GenoGray100, GenoGray600)
                    if (animal.pesoKg > 0) AnimalChip("${animal.pesoKg.toInt()} kg", GenoBlue50, GenoBlue)
                    if (animal.rfid.isNotEmpty()) RfidTag(animal.rfid)
                }
            }

            Icon(Icons.Default.ChevronRight, null, tint = GenoGray300)
        }
    }
}

@Composable
fun AnimalChip(label: String, bg: androidx.compose.ui.graphics.Color, fg: androidx.compose.ui.graphics.Color) {
    Surface(shape = RoundedCornerShape(6.dp), color = bg) {
        Text(label, fontSize = 11.sp, color = fg,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
    }
}

@Composable
fun EmptyAnimaisPlaceholder(onCadastrar: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🐄", fontSize = 48.sp)
        Spacer(Modifier.height(12.dp))
        Text("Nenhum animal cadastrado", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        Spacer(Modifier.height(6.dp))
        Text("Toque no botão abaixo para cadastrar seu primeiro animal.",
            fontSize = 13.sp, color = GenoGray600,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = onCadastrar,
            colors  = ButtonDefaults.buttonColors(containerColor = GenoGreen800)
        ) {
            Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text("Cadastrar animal")
        }
    }
}

fun idadeAnimal(nascimento: LocalDate): String {
    val p = Period.between(nascimento, LocalDate.now())
    return when {
        p.years > 0  -> "${p.years} ano${if (p.years > 1) "s" else ""}"
        p.months > 0 -> "${p.months} mes${if (p.months > 1) "es" else ""}"
        else         -> "${p.days} dias"
    }
}

// Cor auxiliar
val GenoGray300 = androidx.compose.ui.graphics.Color(0xFFDADCE0)
