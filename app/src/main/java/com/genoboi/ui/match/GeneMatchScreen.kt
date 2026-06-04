package com.genoboi.ui.match

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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

import com.genoboi.data.ml.GeneticRankingHelper
import com.genoboi.data.ml.ScoredAnimal
import com.genoboi.data.repository.AnimalRepository
import java.util.Locale
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun GeneMatchScreen(repository: AnimalRepository) {
    var step by remember { mutableIntStateOf(1) } // 1: Pergunta, 2: Ranking
    var objetivo by remember { mutableStateOf("") }
    var ranking by remember { mutableStateOf<List<ScoredAnimal>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

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
                if (step == 2) {
                    IconButton(onClick = { step = 1 }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Favorite, null, tint = GenoGreen700,
                            modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("GeneMatch", fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp, color = GenoGreen800)
                    }
                    Text("Ranqueamento por Objetivo Genético",
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
                    "Qual o seu objetivo genético?",
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

                Spacer(Modifier.height(16.dp))
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = GenoGreen50.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, null, tint = GenoGreen800, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "O GENIA analisará seu plantel e ranqueará os melhores animais com base em critérios técnicos de cada objetivo.",
                            fontSize = 12.sp, color = GenoGreen900
                        )
                    }
                }

                Spacer(Modifier.weight(1f))

                Button(
                    onClick = { 
                        if (objetivo.isNotEmpty()) {
                            isLoading = true
                            scope.launch {
                                try {
                                    val animais = repository.observarAnimais().first()
                                    ranking = GeneticRankingHelper.ranquearAnimais(objetivo, animais)
                                    step = 2
                                } finally {
                                    isLoading = false
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = objetivo.isNotEmpty() && !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = GenoGreen800, contentColor = Color.White)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("RANQUEAR ANIMAIS", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            // Passo 2: Ranking de Animais
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Resultados: $objetivo",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = GenoGray900
                )

                if (ranking.isEmpty()) {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("Nenhum animal cadastrado para ranqueamento.", color = GenoGray600)
                    }
                }

                ranking.forEachIndexed { index, item ->
                    RankingCard(index + 1, item)
                }
                
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun RankingCard(posicao: Int, item: ScoredAnimal) {
    val a = item.animal
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = GenoWhite),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            // Posição
            Box(
                Modifier.size(32.dp).background(
                    if (posicao == 1) Color(0xFFFFD700) else GenoGray100,
                    CircleShape
                ),
                contentAlignment = Alignment.Center
            ) {
                Text("$posicao", fontWeight = FontWeight.Bold, color = if (posicao == 1) Color(0xFF8B7500) else GenoGray600)
            }

            Spacer(Modifier.width(16.dp))

            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(a.nome, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.width(8.dp))
                    Badge(
                        containerColor = when(item.classificacao) {
                            "Elite genética" -> Color(0xFFE3F2FD)
                            "Alto potencial" -> Color(0xFFE8F5E9)
                            "Bom desempenho" -> Color(0xFFFFF3E0)
                            else -> GenoGray100
                        },
                        contentColor = when(item.classificacao) {
                            "Elite genética" -> Color(0xFF1976D2)
                            "Alto potencial" -> Color(0xFF2E7D32)
                            "Bom desempenho" -> Color(0xFFE65100)
                            else -> GenoGray600
                        }
                    ) {
                        Text(item.classificacao, modifier = Modifier.padding(horizontal = 4.dp), fontSize = 10.sp)
                    }
                }
                Text("${a.especie.label} • ${a.raca}", fontSize = 12.sp, color = GenoGray600)
                
                Spacer(Modifier.height(8.dp))
                
                item.fatores.forEach { fator ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, null, tint = GenoGreen600, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(fator, fontSize = 11.sp, color = GenoGray700)
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text("Score", fontSize = 10.sp, color = GenoGray400)
                Text(
                    String.format(Locale.getDefault(), "%.1f", item.scoreFinal),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = GenoGreen800
                )
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
