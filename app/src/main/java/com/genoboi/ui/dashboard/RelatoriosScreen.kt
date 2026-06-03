package com.genoboi.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.genoboi.domain.model.Especie
import com.genoboi.domain.model.MockData
import com.genoboi.domain.model.RelatorioDesempenho
import com.genoboi.ui.components.GenoTopBar
import com.genoboi.ui.theme.*

@Composable
fun RelatoriosScreen(onVoltar: () -> Unit) {
    var especieSelecionada by remember { mutableStateOf<Especie?>(null) }
    val relatorio = if (especieSelecionada == null) MockData.relatorioConsolidado else MockData.relatorioPorEspecie[especieSelecionada]!!

    Scaffold(
        topBar = {
            GenoTopBar(
                titulo = "Relatórios de Desempenho",
                showBack = true,
                onBack = onVoltar,
                actions = {
                    IconButton(onClick = { /* Download PDF mock */ }) {
                        Icon(Icons.Default.Download, null, tint = GenoGreen800)
                    }
                }
            )
        },
        containerColor = GenoGray50
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Seletor de Espécie
            Text("Filtrar por espécie", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterButton("Todos", especieSelecionada == null) { especieSelecionada = null }
                Especie.values().forEach { esp ->
                    FilterButton(esp.label, especieSelecionada == esp) { especieSelecionada = esp }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Cards de KPIs principais
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                KpiCard(
                    "Taxa de Prenhez", 
                    "${relatorio.taxaPrenhez.toInt()}%", 
                    if (relatorio.taxaPrenhez > 70) "Excelente" else "Bom",
                    GenoGreen800,
                    Modifier.weight(1f)
                )
                KpiCard(
                    "Sucesso IA", 
                    "${relatorio.taxaSucessoIA.toInt()}%", 
                    "Média local",
                    GenoBlue,
                    Modifier.weight(1f)
                )
            }

            // Seção de Detalhes
            ReportSection(titulo = "Reprodução") {
                MetricRow("Intervalo entre partos", "${relatorio.intervaloPartosDias} dias", Icons.Default.ShowChart)
                MetricRow("Cios detectados (mês)", "${relatorio.totalCiosDetectados}", Icons.Default.TrendingUp)
                MetricRow("Animais em gestação", "${relatorio.animaisEmGestacao}", null)
            }

            ReportSection(titulo = "Genética") {
                MetricRow("Ganho genético médio", "+${relatorio.mediaGanhoGenetico}%", Icons.Default.TrendingUp)
                LinearProgressIndicator(
                    progress = relatorio.mediaGanhoGenetico / 20f,
                    modifier = Modifier.fillMaxWidth().height(8.dp).padding(vertical = 8.dp),
                    color = GenoGreen700,
                    trackColor = GenoGreen100
                )
                Text(
                    "Seu rebanho está evoluindo acima da média da região (+4.2%)",
                    fontSize = 12.sp, color = GenoGray600
                )
            }

            // Dica da IA
            Card(
                colors = CardDefaults.cardColors(containerColor = GenoGreen50),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("💡", fontSize = 24.sp)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Dica: Aumentar a detecção de cio em fêmeas ${especieSelecionada?.label ?: "de todas as espécies"} pode elevar sua taxa de prenhez em até 12%.",
                        fontSize = 13.sp, color = GenoGreen900, fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun FilterButton(label: String, selected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) GenoGreen800 else GenoWhite,
            contentColor = if (selected) Color.White else GenoGray600
        ),
        elevation = ButtonDefaults.buttonElevation(if (selected) 2.dp else 0.dp),
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
        modifier = Modifier.height(36.dp)
    ) {
        Text(label, fontSize = 12.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
fun KpiCard(titulo: String, valor: String, status: String, cor: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = GenoWhite),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(titulo, fontSize = 12.sp, color = GenoGray600)
            Spacer(Modifier.height(4.dp))
            Text(valor, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = cor)
            Text(status, fontSize = 11.sp, color = cor.copy(alpha = 0.8f), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ReportSection(titulo: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = GenoWhite),
        elevation = CardDefaults.cardElevation(1.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(titulo, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = GenoGray900)
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun MetricRow(label: String, valor: String, icon: androidx.compose.ui.graphics.vector.ImageVector?) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(icon, null, tint = GenoGray400, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
            }
            Text(label, fontSize = 14.sp, color = GenoGray600)
        }
        Text(valor, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GenoGray900)
    }
}
