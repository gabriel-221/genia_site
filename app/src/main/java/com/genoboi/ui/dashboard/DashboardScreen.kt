package com.genoboi.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.genoboi.data.local.AppDatabase
import com.genoboi.data.remote.SupabaseConfig
import com.genoboi.data.repository.AnimalRepository
import com.genoboi.domain.model.Especie
import com.genoboi.domain.model.Sexo
import com.genoboi.ui.components.*
import com.genoboi.ui.theme.*

@Composable
fun DashboardScreen(
    repository: AnimalRepository,
    onNavigateToAnimais: () -> Unit = {},
    onNavigateToAlertas: () -> Unit = {},
    onNavigateToRelatorios: () -> Unit = {},
    onNavigateToCopilot: () -> Unit = {}
) {
    val context = LocalContext.current
    val animais by repository.observarAnimais().collectAsState(initial = emptyList())

    // Métricas calculadas dos dados reais
    val totalAnimais   = animais.size
    val totalFemeas    = animais.count { it.sexo == Sexo.FEMEA }
    val prenhas        = animais.count { it.prenhou }
    val taxaPrenhez    = if (totalFemeas > 0) (prenhas * 100 / totalFemeas) else 0
    val disponivelMatch = animais.count { it.disponivelMatch }

    val bovinos  = animais.count { it.especie == Especie.BOVINO }
    val ovinos   = animais.count { it.especie == Especie.OVINO }
    val caprinos = animais.count { it.especie == Especie.CAPRINO }
    val especiesReais = listOf(
        Triple(Especie.BOVINO,  bovinos,  if (totalAnimais > 0) bovinos.toFloat()  / totalAnimais else 0f),
        Triple(Especie.OVINO,   ovinos,   if (totalAnimais > 0) ovinos.toFloat()   / totalAnimais else 0f),
        Triple(Especie.CAPRINO, caprinos, if (totalAnimais > 0) caprinos.toFloat() / totalAnimais else 0f)
    ).filter { it.second > 0 }

    // Nome do produtor (observado para atualizar após sync)
    val db = remember { AppDatabase.getInstance(context) }
    val produtorLocal by db.produtorDao().observarAtual().collectAsState(initial = null)
    
    val nomeProdutor = produtorLocal?.nome?.substringBefore(" ") ?: run {
        SupabaseConfig.getEmail(context)?.substringBefore("@") ?: "Produtor"
    }
    val nomeFazenda = produtorLocal?.nomeFazenda?.ifBlank { null }
        ?: produtorLocal?.municipio?.ifBlank { null }
        ?: "GENIA"

    LazyColumn(
        modifier       = Modifier.fillMaxSize().background(GenoGray50),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Saudação
        item {
            Column(
                Modifier.fillMaxWidth().background(GenoWhite)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Olá, $nomeProdutor!",
                            fontWeight = FontWeight.Bold,
                            fontSize   = 20.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(nomeFazenda, fontSize = 14.sp, color = GenoGray600)
                            Icon(Icons.Default.KeyboardArrowDown, null, tint = GenoGray400,
                                modifier = Modifier.size(16.dp))
                        }
                    }
                    Button(
                        onClick = onNavigateToRelatorios,
                        colors  = ButtonDefaults.buttonColors(containerColor = GenoGreen800, contentColor = Color.White),
                        shape   = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        elevation = ButtonDefaults.buttonElevation(4.dp)
                    ) {
                        Icon(Icons.Default.BarChart, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("RELATÓRIOS", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }

        // KPIs
        item {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    icon     = Icons.Default.Pets,
                    valor    = if (totalAnimais == 0) "-" else totalAnimais.toString(),
                    label    = "Total de animais",
                    iconTint = GenoGreen700,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    icon     = Icons.Default.Favorite,
                    valor    = if (totalFemeas == 0) "-" else "$taxaPrenhez%",
                    label    = "Taxa de prenhez",
                    iconTint = GenoRed,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    icon     = Icons.Default.Favorite,
                    valor    = if (totalAnimais == 0) "-" else disponivelMatch.toString(),
                    label    = "No GeneMatch",
                    iconTint = GenoGreen600,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    icon     = Icons.Default.Science,
                    valor    = if (totalFemeas == 0) "-" else "${especiesReais.size}",
                    label    = "Espécies",
                    iconTint = GenoBlue,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Distribuição por espécie (dados reais ou estado vazio)
        item {
            Card(
                modifier  = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                shape     = RoundedCornerShape(12.dp),
                colors    = CardDefaults.cardColors(containerColor = GenoWhite),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    SectionHeader(
                        titulo    = "Animais por espécie",
                        acaoLabel = if (totalAnimais > 0) "Ver todos" else "",
                        onAcao    = onNavigateToAnimais
                    )
                    Spacer(Modifier.height(12.dp))
                    if (totalAnimais == 0) {
                        Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Nenhum animal cadastrado", color = GenoGray400, fontSize = 14.sp)
                                Spacer(Modifier.height(8.dp))
                                TextButton(onClick = onNavigateToAnimais) {
                                    Text("Cadastrar primeiro animal", color = GenoGreen700)
                                }
                            }
                        }
                    } else {
                        especiesReais.forEach { (esp, qtd, pct) ->
                            Row(
                                Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(esp.emoji, fontSize = 18.sp)
                                Spacer(Modifier.width(8.dp))
                                Text(esp.label, Modifier.weight(1f), fontSize = 13.sp)
                                Text(
                                    "$qtd (${(pct * 100).toInt()}%)",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize   = 13.sp
                                )
                            }
                            if (qtd > 0) {
                                LinearProgressIndicator(
                                    progress  = { pct },
                                    modifier  = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                                    color     = GenoGreen600,
                                    trackColor = GenoGreen100
                                )
                                Spacer(Modifier.height(4.dp))
                            }
                        }
                    }
                }
            }
        }

        // Recomendação da IA (card fixo, encaminha para Copilot)
        item {
            Card(
                modifier  = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                shape     = RoundedCornerShape(12.dp),
                colors    = CardDefaults.cardColors(containerColor = GenoGreen800),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.AutoAwesome, null, tint = GenoWhite, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("GENIA Copilot", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = GenoWhite)
                        Spacer(Modifier.height(4.dp))
                        val msg = when {
                            totalAnimais == 0 -> "Cadastre seus animais e deixe a IA analisar seu rebanho com recomendações genéticas personalizadas."
                            totalFemeas == 0  -> "Cadastre fêmeas para obter análises de prenhez e recomendações de cruzamento."
                            taxaPrenhez < 50  -> "Taxa de prenhez abaixo de 50%. Converse com o Copilot para recomendações de melhoramento."
                            else              -> "Seu rebanho tem $totalAnimais animais. Pergunte ao Copilot sobre o potencial genético do seu plantel!"
                        }
                        Text(msg, fontSize = 13.sp, color = GenoWhite.copy(alpha = 0.88f))
                        Spacer(Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = onNavigateToCopilot,
                            border  = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp),
                            colors  = ButtonDefaults.outlinedButtonColors(contentColor = GenoWhite)
                        ) {
                            Text("Abrir Copilot", fontSize = 12.sp)
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
fun SimpleLinhaPrenhez(dados: List<Pair<String, Int>>) {
    val max = dados.maxOf { it.second }.toFloat()
    val min = (dados.minOf { it.second } - 10f).coerceAtLeast(0f)
    Column {
        Row(
            Modifier.fillMaxWidth().height(80.dp),
            verticalAlignment     = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            dados.forEach { (_, valor) ->
                val fracao = ((valor - min) / (max - min)).coerceIn(0.1f, 1f)
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(36.dp)) {
                    Text("$valor%", fontSize = 9.sp, color = GenoGray400)
                    Spacer(Modifier.height(2.dp))
                    Box(
                        Modifier.width(20.dp).height((64 * fracao).dp)
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(GenoGreen600.copy(alpha = 0.75f + 0.25f * fracao))
                    )
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            dados.forEach { (mes, _) ->
                Text(mes, fontSize = 10.sp, color = GenoGray400,
                    modifier = Modifier.width(36.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        }
    }
}
