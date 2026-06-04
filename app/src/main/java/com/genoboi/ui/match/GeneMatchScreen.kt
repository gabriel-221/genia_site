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
import com.genoboi.data.ml.MatingSuggestion
import com.genoboi.data.ml.ScoredAnimal
import com.genoboi.data.repository.AnimalRepository
import java.util.Locale
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun GeneMatchScreen(repository: AnimalRepository) {
    var step by remember { mutableIntStateOf(1) } // 1: Pergunta, 2: Ranking
    var objetivo by remember { mutableStateOf("") }
    var suggestions by remember { mutableStateOf<List<MatingSuggestion>>(emptyList()) }
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
                    Text("Sugestões de Acasalamento Genético",
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
                    "Qual o seu objetivo de produção?",
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
                        Icon(Icons.Default.Psychology, null, tint = GenoGreen800, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Cálculo Genético GENIA", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = GenoGreen900)
                            Text(
                                "Analisamos o vigor dos machos e a produtividade das fêmeas para sugerir casais que maximizam o ganho genético do seu plantel.",
                                fontSize = 12.sp, color = GenoGreen800
                            )
                        }
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
                                    suggestions = GeneticRankingHelper.sugerirAcasalamentos(objetivo, animais)
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
                        Text("ENCONTRAR MELHORES CASAIS", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            // Passo 2: Ranking de Casais
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Top Sugestões: $objetivo",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = GenoGray900
                )

                if (suggestions.isEmpty()) {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("Não encontramos casais compatíveis. Verifique se há machos e fêmeas cadastrados.", textAlign = TextAlign.Center, color = GenoGray600)
                    }
                }

                suggestions.forEachIndexed { index, suggestion ->
                    MatchSuggestionCard(index + 1, suggestion)
                }
                
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun MatchSuggestionCard(posicao: Int, suggestion: MatingSuggestion) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = GenoWhite),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            // Header: Posição e Score
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = if (posicao == 1) Color(0xFFFFD700) else GenoGray100,
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("$posicao", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (posicao == 1) Color(0xFF8B7500) else GenoGray600)
                    }
                }
                Spacer(Modifier.width(12.dp))
                Text("Compatibilidade", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = GenoGray700)
                Spacer(Modifier.weight(1f))
                Text(
                    String.format(Locale.getDefault(), "%.0f%%", suggestion.scoreMatch),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    color = GenoGreen800
                )
            }

            Spacer(Modifier.height(16.dp))

            // Visual do Casal
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SmallAnimalMatchCard(suggestion.macho, "Reprodutor", GenoBlue50)
                Icon(Icons.Default.Favorite, null, tint = Color.Red.copy(alpha = 0.6f), modifier = Modifier.size(24.dp))
                SmallAnimalMatchCard(suggestion.femea, "Matriz", Color(0xFFFCE4EC))
            }

            Spacer(Modifier.height(16.dp))

            // Justificativa
            Surface(
                color = GenoGray50,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text("Por que este par?", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = GenoGray600)
                    Text(suggestion.justificativa, fontSize = 12.sp, color = GenoGray800)
                }
            }

            Spacer(Modifier.height(12.dp))

            // Ganhos Estimados
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                suggestion.ganhosEstimados.forEach { ganho ->
                    Surface(
                        color = GenoGreen50,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            ganho,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = GenoGreen900
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SmallAnimalMatchCard(animal: Animal, label: String, bgColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .size(60.dp)
                .background(bgColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(animal.especie.emoji, fontSize = 32.sp)
        }
        Spacer(Modifier.height(4.dp))
        Text(animal.nome, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Text(label, fontSize = 10.sp, color = GenoGray600)
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

private fun borderStroke(width: androidx.compose.ui.unit.Dp, color: Color) = androidx.compose.foundation.BorderStroke(width, color)
