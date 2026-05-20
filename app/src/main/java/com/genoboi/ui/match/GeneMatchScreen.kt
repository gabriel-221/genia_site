package com.genoboi.ui.match

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
    val matriz      = MockData.animalFrida
    val reprodutores = MockData.reprodutores
    var indice by remember { mutableIntStateOf(0) }
    var decisao by remember { mutableStateOf<Boolean?>(null) }  // null=pendente, true=match, false=pass

    val reprodutor = reprodutores.getOrNull(indice)

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
                    Text("${reprodutores.size} reprodutores compatíveis na região",
                        fontSize = 12.sp, color = GenoGray600)
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = GenoGreen100
                ) {
                    Row(
                        Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Pets, null, tint = GenoGreen700,
                            modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Sua matriz: ${matriz.nome}", fontSize = 12.sp, color = GenoGreen800)
                    }
                }
            }
        }

        if (reprodutor == null) {
            // Acabaram os reprodutores
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🎉", fontSize = 48.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("Você viu todos os reprodutores!", fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { indice = 0; decisao = null },
                        colors = ButtonDefaults.buttonColors(containerColor = GenoGreen800)
                    ) { Text("Recomeçar") }
                }
            }
            return
        }

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Card principal do match
            AnimatedContent(
                targetState = indice,
                transitionSpec = {
                    (slideInHorizontally { it } + fadeIn()) togetherWith
                    (slideOutHorizontally { -it } + fadeOut())
                }
            ) { _ ->
                Card(
                    shape     = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors    = CardDefaults.cardColors(containerColor = GenoWhite)
                ) {
                    Column(Modifier.padding(20.dp)) {
                        // Foto placeholder + nome
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.Top
                        ) {
                            // Avatar
                            Box(
                                Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(GenoGreen100),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(reprodutor.reprodutor.especie.emoji, fontSize = 32.sp)
                            }

                            // Score
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Compatibilidade genética",
                                    fontSize = 11.sp, color = GenoGray600)
                                Text(
                                    "${reprodutor.scoreCompatibilidade}%",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize   = 36.sp,
                                    color      = scoreColor(reprodutor.scoreCompatibilidade)
                                )
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = scoreColor(reprodutor.scoreCompatibilidade).copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        "COMPATÍVEL",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color    = scoreColor(reprodutor.scoreCompatibilidade),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        Text(reprodutor.reprodutor.nome,
                            fontWeight = FontWeight.Bold, fontSize = 22.sp)
                        Text("${reprodutor.reprodutor.especie.label} • ${reprodutor.reprodutor.raca}",
                            fontSize = 13.sp, color = GenoGray600)
                        Text("${reprodutor.reprodutor.fazenda}",
                            fontSize = 12.sp, color = GenoGray400)

                        Spacer(Modifier.height(14.dp))
                        HorizontalDivider(color = GenoGray100)
                        Spacer(Modifier.height(14.dp))

                        // Comparação genética
                        Text("Comparação genética", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Spacer(Modifier.height(8.dp))

                        GeneticaRow("Fertilidade",   "Alta",  GenoGreen700)
                        GeneticaRow("Endogamia",     nivelRiscoLabel(reprodutor.nivelRisco),
                            nivelRiscoColor(reprodutor.nivelRisco))
                        GeneticaRow("Ganho genético","Alto",  GenoGreen700)
                        GeneticaRow("Docilidade",    "Alta",  GenoGreen700)

                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider(color = GenoGray100)
                        Spacer(Modifier.height(12.dp))

                        // Informações adicionais
                        Text("Informações adicionais", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Spacer(Modifier.height(8.dp))

                        InfoRow(Icons.Default.Science,   "Disponível para IA")
                        InfoRow(Icons.Default.AcUnit,    "Sêmen: ${
                            if (reprodutor.reprodutor.pesoKg > 0) "Congelado" else "Consultar"
                        }")
                        InfoRow(Icons.Default.Person,    "Proprietário: ${reprodutor.reprodutor.fazenda}")
                        InfoRow(Icons.Default.LocationOn,"Distância: ${reprodutor.distanciaKm.toInt()} km")
                        if (reprodutor.ganhoGeneticoEstimado.isNotEmpty()) {
                            InfoRow(Icons.Default.TrendingUp, reprodutor.ganhoGeneticoEstimado)
                        }
                    }
                }
            }
        }

        // Botões Match / Pass
        Surface(shadowElevation = 8.dp) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(GenoWhite)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Não compatível / Pass
                OutlinedButton(
                    onClick = {
                        decisao = false
                        if (indice < reprodutores.size - 1) indice++ else indice = reprodutores.size
                    },
                    modifier = Modifier.weight(1f),
                    shape    = RoundedCornerShape(8.dp),
                    colors   = ButtonDefaults.outlinedButtonColors(contentColor = GenoRed),
                    border   = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp)
                ) {
                    Icon(Icons.Default.Close, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("NÃO COMPATÍVEL")
                }

                // Match!
                Button(
                    onClick = {
                        decisao = true
                        if (indice < reprodutores.size - 1) indice++ else indice = reprodutores.size
                    },
                    modifier = Modifier.weight(1f),
                    shape    = RoundedCornerShape(8.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = GenoGreen800)
                ) {
                    Text("UTILIZAR REPRODUTOR")
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun GeneticaRow(atributo: String, valor: String, cor: Color) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(atributo, fontSize = 13.sp, color = GenoGray600)
        Text(valor, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = cor)
    }
}

@Composable
fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, texto: String) {
    Row(
        Modifier.padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = GenoGreen700, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(texto, fontSize = 13.sp, color = GenoGray600)
    }
}

fun scoreColor(score: Int) = when {
    score >= 80 -> GenoGreen700
    score >= 60 -> GenoAmber
    else        -> GenoRed
}

fun nivelRiscoLabel(nivel: NivelRisco) = when (nivel) {
    NivelRisco.BAIXO -> "Baixa"
    NivelRisco.MEDIO -> "Média"
    NivelRisco.ALTO  -> "Alta"
}

fun nivelRiscoColor(nivel: NivelRisco) = when (nivel) {
    NivelRisco.BAIXO -> GenoGreen700
    NivelRisco.MEDIO -> GenoAmber
    NivelRisco.ALTO  -> GenoRed
}
