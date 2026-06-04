package com.genoboi.ui.dashboard

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import com.genoboi.data.repository.AnimalRepository
import com.genoboi.domain.model.Especie
import com.genoboi.domain.model.Sexo
import com.genoboi.ui.components.GenoTopBar
import com.genoboi.ui.theme.*

@Composable
fun RelatoriosScreen(
    repository: AnimalRepository,
    onVoltar: () -> Unit
) {
    var especieSelecionada by remember { mutableStateOf<Especie?>(null) }
    val todosAnimais by repository.observarAnimais().collectAsState(initial = emptyList())

    val animais = if (especieSelecionada == null) todosAnimais
                  else todosAnimais.filter { it.especie == especieSelecionada }

    val total     = animais.size
    val femeas    = animais.count { it.sexo == Sexo.FEMEA }
    val machos    = animais.count { it.sexo == Sexo.MACHO }
    val prenhas   = animais.count { it.prenhou }
    val taxaPrenhez = if (femeas > 0) (prenhas * 100f / femeas) else 0f

    val mediaECC  = if (femeas > 0) animais.filter { it.sexo == Sexo.FEMEA }
                        .map { it.escoreCorporal }.average().toFloat() else 0f
    val mediaPeso = if (total > 0) animais.map { it.pesoKg }.average().toFloat() else 0f
    val mediaLeite = animais.filter { it.producaoLeiteDiaria > 0 }
                        .map { it.producaoLeiteDiaria }.average()
                        .let { if (it.isNaN()) 0.0 else it }

    val mediaPartos = if (femeas > 0) animais.filter { it.sexo == Sexo.FEMEA }
                        .map { it.numeroPartos }.average() else 0.0
    val mediaQldSemen = if (machos > 0) animais.filter { it.sexo == Sexo.MACHO }
                        .map { it.qualidadeSemenMacho }.average() else 0.0

    Scaffold(
        topBar = {
            GenoTopBar(
                titulo   = "Relatórios de Desempenho",
                showBack = true,
                onBack   = onVoltar
            )
        },
        containerColor = GenoGray50
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding)
                .verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Filtro espécie
            Text("Filtrar por espécie", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterButton("Todos", especieSelecionada == null) { especieSelecionada = null }
                Especie.entries.forEach { esp ->
                    FilterButton(esp.label, especieSelecionada == esp) { especieSelecionada = esp }
                }
            }

            // Estado vazio
            if (total == 0) {
                Card(
                    colors    = CardDefaults.cardColors(containerColor = GenoWhite),
                    shape     = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Column(
                        Modifier.fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.BarChart, null, tint = GenoGray200, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(12.dp))
                        Text(
                            if (especieSelecionada == null) "Nenhum animal cadastrado ainda."
                            else "Nenhum ${especieSelecionada?.label} cadastrado.",
                            color = GenoGray400, fontSize = 14.sp
                        )
                    }
                }
            } else {
                // KPIs principais
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    KpiCard(
                        titulo = "Taxa de Prenhez",
                        valor  = if (femeas == 0) "—" else "${taxaPrenhez.toInt()}%",
                        status = when {
                            femeas == 0        -> "Sem fêmeas"
                            taxaPrenhez >= 70  -> "Excelente"
                            taxaPrenhez >= 50  -> "Bom"
                            else               -> "Melhorar"
                        },
                        cor    = if (taxaPrenhez >= 70) GenoGreen800 else if (taxaPrenhez >= 50) GenoAmber else GenoRed,
                        modifier = Modifier.weight(1f)
                    )
                    KpiCard(
                        titulo = "Total de Animais",
                        valor  = "$total",
                        status = "$femeas fêmeas · $machos machos",
                        cor    = GenoBlue,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Reprodução
                ReportSection(titulo = "Reprodução") {
                    MetricRow("Animais totais",    "$total",  Icons.Default.Pets)
                    MetricRow("Fêmeas",            "$femeas", null)
                    MetricRow("Em gestação",        "$prenhas", Icons.Default.Favorite)
                    MetricRow("Média de partos",   String.format(Locale.getDefault(), "%.1f", mediaPartos), Icons.AutoMirrored.Filled.ShowChart)
                    MetricRow("Média ECC (fêmeas)", String.format(Locale.getDefault(), "%.1f", mediaECC), null)
                }

                // Genética
                ReportSection(titulo = "Genética") {
                    MetricRow("Peso médio", "${mediaPeso.toInt()} kg", Icons.AutoMirrored.Filled.TrendingUp)
                    if (machos > 0)
                        MetricRow("Qld. seminal média", String.format(Locale.getDefault(), "%.1f / 5", mediaQldSemen), Icons.Default.Science)
                    if (mediaLeite > 0)
                        MetricRow("Produção leite média", String.format(Locale.getDefault(), "%.1f L/dia", mediaLeite), Icons.Default.WaterDrop)
                    val disponivelMatch = animais.count { it.disponivelMatch }
                    MetricRow("Disponíveis no GeneMatch", "$disponivelMatch", Icons.Default.Favorite)
                }

                // Dica dinâmica
                Card(
                    colors = CardDefaults.cardColors(containerColor = GenoGreen50),
                    shape  = RoundedCornerShape(12.dp)
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("💡", fontSize = 24.sp)
                        Spacer(Modifier.width(12.dp))
                        val dica = when {
                            femeas == 0   -> "Cadastre fêmeas para obter análises de prenhez e recomendações de cruzamento."
                            taxaPrenhez < 50 -> "Taxa de prenhez abaixo de 50%. Use o GeneMatch para selecionar melhores reprodutores."
                            taxaPrenhez < 70 -> "Taxa de prenhez pode melhorar. Verifique o escore corporal das fêmeas e o intervalo entre partos."
                            else             -> "Rebanho com bom desempenho reprodutivo! Use o Copilot para recomendações genéticas avançadas."
                        }
                        Text(dica, fontSize = 13.sp, color = GenoGreen900, fontWeight = FontWeight.Medium)
                    }
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
        colors  = ButtonDefaults.buttonColors(
            containerColor = if (selected) GenoGreen800 else GenoWhite,
            contentColor   = if (selected) Color.White else GenoGray600
        ),
        elevation      = ButtonDefaults.buttonElevation(if (selected) 2.dp else 0.dp),
        shape          = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
        modifier       = Modifier.height(36.dp)
    ) {
        Text(label, fontSize = 12.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
fun KpiCard(titulo: String, valor: String, status: String, cor: Color, modifier: Modifier = Modifier) {
    Card(
        modifier  = modifier,
        colors    = CardDefaults.cardColors(containerColor = GenoWhite),
        elevation = CardDefaults.cardElevation(2.dp),
        shape     = RoundedCornerShape(16.dp)
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
        modifier  = Modifier.fillMaxWidth(),
        colors    = CardDefaults.cardColors(containerColor = GenoWhite),
        elevation = CardDefaults.cardElevation(1.dp),
        shape     = RoundedCornerShape(14.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(titulo, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = GenoGray900)
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun MetricRow(label: String, valor: String, icon: ImageVector?) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
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
