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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.genoboi.data.repository.AnimalRepository
import com.genoboi.domain.model.Animal
import com.genoboi.domain.model.Especie
import com.genoboi.domain.model.Sexo
import com.genoboi.ui.components.EspecieChip
import com.genoboi.ui.theme.*
import java.time.LocalDate

@Composable
fun AnimaisScreen(
    repository: AnimalRepository,
    onCadastrarAnimal: () -> Unit,
    onAnimalClick: (Long) -> Unit,
    onSimularCruzamento: () -> Unit = {}
) {
    val animais by repository.observarAnimais().collectAsState(initial = emptyList())
    var filtroEspecie by remember { mutableStateOf<Especie?>(null) }
    var query by remember { mutableStateOf("") }

    val animaisFiltrados = animais.filter { a ->
        (filtroEspecie == null || a.especie == filtroEspecie) &&
        (query.isBlank() || a.nome.contains(query, ignoreCase = true) ||
         a.raca.contains(query, ignoreCase = true))
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick           = onCadastrarAnimal,
                containerColor    = GenoGreen800,
                contentColor      = Color.White,
                icon              = { Icon(Icons.Default.Add, null, tint = Color.White) },
                text              = { Text("CADASTRAR ANIMAL", color = Color.White, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp) }
            )
        },
        containerColor = GenoGray50
    ) { padding ->
        LazyColumn(
            modifier       = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Search bar e Título
            item {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(GenoWhite)
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Seu Rebanho",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize   = 28.sp,
                            color      = GenoGray900
                        )
                        Button(
                            onClick = onSimularCruzamento,
                            colors = ButtonDefaults.buttonColors(containerColor = GenoBlue, contentColor = Color.White),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Favorite, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("SIMULAR", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value          = query,
                        onValueChange  = { query = it },
                        placeholder    = { Text("Buscar por nome ou raça...", fontSize = 15.sp) },
                        leadingIcon    = { Icon(Icons.Default.Search, null, tint = GenoGreen700) },
                        trailingIcon   = if (query.isNotEmpty()) {{
                            IconButton(onClick = { query = "" }) {
                                Icon(Icons.Default.Clear, null, tint = GenoGray400)
                            }
                        }} else null,
                        modifier       = Modifier.fillMaxWidth(),
                        shape          = RoundedCornerShape(14.dp),
                        singleLine     = true,
                        colors         = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = GenoGreen700,
                            unfocusedBorderColor = GenoGray200,
                            unfocusedContainerColor = GenoGray50,
                            focusedContainerColor   = GenoWhite
                        )
                    )
                }
            }

            // Filtros por espécie
            item {
                Surface(
                    color = GenoWhite,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LazyRow(
                        contentPadding        = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
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
                }
                Spacer(Modifier.height(8.dp))
            }

            // Contador
            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Resultados",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = GenoGray900
                    )
                    Text(
                        "${animaisFiltrados.size} animais",
                        fontSize = 13.sp,
                        color    = GenoGreen800,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (animaisFiltrados.isEmpty()) {
                item { EmptyAnimaisPlaceholder(onCadastrar = onCadastrarAnimal) }
            } else {
                items(animaisFiltrados, key = { it.id }) { animal ->
                    AnimalCard(
                        animal  = animal,
                        onClick = { onAnimalClick(animal.id) }
                    )
                    Spacer(Modifier.height(4.dp))
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
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = GenoWhite),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Row(
            Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar maior
            Box(
                modifier            = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(GenoGreen100),
                contentAlignment    = Alignment.Center
            ) {
                Text(animal.especie.emoji, fontSize = 32.sp)
            }

            Spacer(Modifier.width(16.dp))

            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        animal.nome,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 18.sp,
                        color      = GenoGray900
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        imageVector        = if (animal.sexo == Sexo.FEMEA)
                            Icons.Default.Female else Icons.Default.Male,
                        contentDescription = animal.sexo.label,
                        tint               = if (animal.sexo == Sexo.FEMEA) GenoRed else GenoBlue,
                        modifier           = Modifier.size(18.dp)
                    )
                }
                Text(
                    "${animal.especie.label} • ${animal.raca}",
                    fontSize = 13.sp, color = GenoGray600
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    val idade = idadeAnimal(animal.dataNascimento)
                    AnimalChip(idade, GenoGray100, GenoGray600)
                    if (animal.pesoKg > 0) AnimalChip("${animal.pesoKg.toInt()} kg", GenoBlue50, GenoBlue)
                }
            }

            Icon(Icons.Default.ChevronRight, null, tint = GenoGray400, modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
fun AnimalChip(label: String, bg: androidx.compose.ui.graphics.Color, fg: androidx.compose.ui.graphics.Color) {
    Surface(shape = RoundedCornerShape(8.dp), color = bg) {
        Text(label, fontSize = 12.sp, color = fg, fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
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
            colors  = ButtonDefaults.buttonColors(
                containerColor = GenoGreen800,
                contentColor = Color.White
            )
        ) {
            Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp), tint = Color.White)
            Spacer(Modifier.width(8.dp))
            Text("Cadastrar animal", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

