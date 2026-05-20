package com.genoboi.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.genoboi.domain.model.MockData
import com.genoboi.domain.model.TipoAlerta
import com.genoboi.ui.components.*
import com.genoboi.ui.theme.*

@Composable
fun DashboardScreen(
    onNavigateToAnimais: () -> Unit = {},
    onNavigateToAlertas: () -> Unit = {}
) {
    val resumo   = MockData.dashboardResumo
    val alertas  = MockData.alertas
    val prenhez  = MockData.prenhez6Meses
    val especies = MockData.animaisPorEspecie

    LazyColumn(
        modifier            = Modifier
            .fillMaxSize()
            .background(GenoGray50),
        contentPadding      = PaddingValues(bottom = 24.dp)
    ) {
        // Saudação
        item {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(GenoWhite)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Text("Olá, João! 👋", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Fazenda Boa Esperança", fontSize = 14.sp, color = GenoGray600)
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null,
                        tint = GenoGray400, modifier = Modifier.size(16.dp))
                }
            }
        }

        // Cards de KPI
        item {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    icon     = Icons.Default.Pets,
                    valor    = resumo.totalAnimais.toString(),
                    label    = "Total de animais",
                    iconTint = GenoGreen700,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    icon     = Icons.Default.Favorite,
                    valor    = "${resumo.taxaPrenhez}%",
                    label    = "Taxa de prenhez",
                    iconTint = GenoRed,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    icon     = Icons.Default.Warning,
                    valor    = resumo.alertasHoje.toString(),
                    label    = "Alertas hoje",
                    iconTint = GenoOrange,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    icon     = Icons.Default.Science,
                    valor    = "${resumo.compatibilidadeGenetica}%",
                    label    = "Compat. genética",
                    iconTint = GenoBlue,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Gráfico prenhez 6 meses
        item {
            Card(
                modifier  = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                shape     = RoundedCornerShape(12.dp),
                colors    = CardDefaults.cardColors(containerColor = GenoWhite),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    SectionHeader(titulo = "Prenhez últimos 6 meses")
                    Spacer(Modifier.height(12.dp))
                    SimpleLinhaPrenhez(dados = prenhez)
                }
            }
        }

        // Animais por espécie
        item {
            Card(
                modifier  = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape     = RoundedCornerShape(12.dp),
                colors    = CardDefaults.cardColors(containerColor = GenoWhite),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    SectionHeader(titulo = "Animais por espécie")
                    Spacer(Modifier.height(12.dp))
                    especies.forEach { (esp, qtd, _) ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(esp.emoji, fontSize = 18.sp)
                            Spacer(Modifier.width(8.dp))
                            Text(esp.label, Modifier.weight(1f), fontSize = 13.sp)
                            Text("$qtd", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }

        // Alertas importantes
        item {
            Card(
                modifier  = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                shape     = RoundedCornerShape(12.dp),
                colors    = CardDefaults.cardColors(containerColor = GenoWhite),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    SectionHeader(
                        titulo    = "Alertas importantes",
                        acaoLabel = "Ver todos",
                        onAcao    = onNavigateToAlertas
                    )
                    Spacer(Modifier.height(4.dp))
                    alertas.take(3).forEach { alerta ->
                        AlertaRow(
                            animalNome = alerta.animalNome,
                            descricao  = alerta.descricao,
                            tipo       = alerta.tipo
                        )
                        if (alerta != alertas.take(3).last()) {
                            HorizontalDivider(color = GenoGray100, thickness = 0.5.dp)
                        }
                    }
                }
            }
        }

        // Recomendação da IA
        item {
            Card(
                modifier  = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape     = RoundedCornerShape(12.dp),
                colors    = CardDefaults.cardColors(containerColor = GenoGreen800),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Row(
                    Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(Icons.Default.SmartToy, contentDescription = null,
                        tint = GenoWhite, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("IA recomenda", fontWeight = FontWeight.Bold,
                            fontSize = 14.sp, color = GenoWhite)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Evitar cruzamento entre Frida e Trovão. Risco elevado de endogamia.",
                            fontSize = 13.sp, color = GenoWhite.copy(alpha = 0.88f)
                        )
                        Spacer(Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = {},
                            border  = ButtonDefaults.outlinedButtonBorder.copy(
                                width = 1.dp
                            ),
                            colors  = ButtonDefaults.outlinedButtonColors(
                                contentColor = GenoWhite
                            )
                        ) {
                            Text("Ver detalhes", fontSize = 12.sp)
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

// Gráfico de linhas simples com Canvas/Compose
@Composable
fun SimpleLinhaPrenhez(dados: List<Pair<String, Int>>) {
    val max = dados.maxOf { it.second }.toFloat()
    val min = (dados.minOf { it.second } - 10f).coerceAtLeast(0f)

    Column {
        // Barras visuais simples
        Row(
            Modifier.fillMaxWidth().height(80.dp),
            verticalAlignment   = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            dados.forEach { (mes, valor) ->
                val fracaoAltura = ((valor - min) / (max - min)).coerceIn(0.1f, 1f)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(36.dp)
                ) {
                    Text("$valor%", fontSize = 9.sp, color = GenoGray400)
                    Spacer(Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .width(20.dp)
                            .height((64 * fracaoAltura).dp)
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(GenoGreen600.copy(alpha = 0.75f + 0.25f * fracaoAltura))
                    )
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            dados.forEach { (mes, _) ->
                Text(mes, fontSize = 10.sp, color = GenoGray400,
                    modifier = Modifier.width(36.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        }
    }
}
