package com.genoboi.ui.match

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.genoboi.domain.model.*
import com.genoboi.ui.theme.*

@Composable
fun GeneMatchScreen() {
    var step by remember { mutableIntStateOf(1) } // 1: Pergunta, 2: Comparação
    var objetivo by remember { mutableStateOf("") }
    var localizacao by remember { mutableStateOf("Local") }

    val matriz = MockData.animalFrida
    val reprodutor = MockData.reprodutores.first().reprodutor
    val score = MockData.reprodutores.first().scoreCompatibilidade

    Column(
        Modifier
            .fillMaxSize()
            .background(GenoGray50)
    ) {
        // Header GeneMatch
        Surface(shadowElevation = 2.dp) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(GenoWhite)
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Favorite, null, tint = GenoGreen700,
                            modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("GeneMatch", fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp, color = GenoGreen800)
                    }
                    Text("Inteligência Genética GENIA",
                        fontSize = 12.sp, color = GenoGray600)
                }
            }
        }

        if (step == 1) {
            // Passo 1: Perguntas de Preferência
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    "O que você quer aumentar?",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = GenoGray900
                )

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ObjetivoButton("Leite", "🥛", objetivo == "Leite", Modifier.weight(1f)) { objetivo = "Leite" }
                    ObjetivoButton("Corte", "🥩", objetivo == "Corte", Modifier.weight(1f)) { objetivo = "Corte" }
                    ObjetivoButton("Fertilidade", "🌱", objetivo == "Fertilidade", Modifier.weight(1f)) { objetivo = "Fertilidade" }
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    "Preferência de Localização",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = GenoGray900
                )

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FilterChip(
                        selected = localizacao == "Local",
                        onClick = { localizacao = "Local" },
                        label = { Text("Match Local") },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = GenoGreen100, selectedLabelColor = GenoGreen800)
                    )
                    FilterChip(
                        selected = localizacao == "Externo",
                        onClick = { localizacao = "Externo" },
                        label = { Text("Externo (Geral)") },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = GenoGreen100, selectedLabelColor = GenoGreen800)
                    )
                }

                Spacer(Modifier.weight(1f))

                Button(
                    onClick = { if (objetivo.isNotEmpty()) step = 2 },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = objetivo.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = GenoGreen800, contentColor = Color.White)
                ) {
                    Text("BUSCAR MELHOR MATCH", fontWeight = FontWeight.Bold)
                }
            }
        } else {
            // Passo 2: Comparação Lado a Lado
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Score de Match
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = GenoGreen800)
                ) {
                    Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("COMPATIBILIDADE IDEAL", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("$score%", color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.ExtraBold)
                        Text("Foco: $objetivo ($localizacao)", color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp)
                    }
                }

                // Casal Lado a Lado
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MatchAnimalCard(matriz, "MATRIZ", Modifier.weight(1f))
                    MatchAnimalCard(reprodutor, "REPRODUTOR", Modifier.weight(1f))
                }

                Spacer(Modifier.weight(1f))

                // Botões de Ação Existentes
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = { step = 1; objetivo = "" },
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GenoRed),
                        border = borderStroke(1.dp, GenoRed)
                    ) {
                        Icon(Icons.Default.Close, null)
                        Spacer(Modifier.width(8.dp))
                        Text("DESCARTAR")
                    }

                    Button(
                        onClick = { /* Simular match final */ },
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GenoGreen800, contentColor = Color.White)
                    ) {
                        Text("DAR MATCH")
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Default.Check, null)
                    }
                }
            }
        }
    }
}

@Composable
fun ObjetivoButton(label: String, emoji: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        border = if (selected) borderStroke(2.dp, GenoGreen800) else null,
        colors = CardDefaults.cardColors(containerColor = if (selected) GenoGreen50 else GenoWhite),
        elevation = CardDefaults.cardElevation(if (selected) 4.dp else 1.dp)
    ) {
        Column(
            Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(emoji, fontSize = 24.sp)
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (selected) GenoGreen800 else GenoGray600)
        }
    }
}

@Composable
fun MatchAnimalCard(animal: Animal, label: String, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = GenoWhite),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(shape = RoundedCornerShape(4.dp), color = GenoGray100) {
                Text(label, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = GenoGray600)
            }
            Spacer(Modifier.height(12.dp))
            Box(Modifier.size(64.dp).background(GenoGreen100, CircleShape), contentAlignment = Alignment.Center) {
                Text(animal.especie.emoji, fontSize = 32.sp)
            }
            Spacer(Modifier.height(8.dp))
            Text(animal.nome, fontWeight = FontWeight.Bold, fontSize = 16.sp, textAlign = TextAlign.Center)
            Text(animal.raca, fontSize = 12.sp, color = GenoGray600)
            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = GenoGray100)
            Spacer(Modifier.height(8.dp))
            Text("Peso: ${animal.pesoKg.toInt()}kg", fontSize = 11.sp, color = GenoGray600)
        }
    }
}

private fun borderStroke(width: androidx.compose.ui.unit.Dp, color: Color) = androidx.compose.foundation.BorderStroke(width, color)
