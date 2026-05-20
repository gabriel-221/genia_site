package com.genoboi.ui.calendario

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.genoboi.domain.model.AlertaItem
import com.genoboi.domain.model.MockData
import com.genoboi.domain.model.TipoAlerta
import com.genoboi.ui.components.AlertaRow
import com.genoboi.ui.components.SectionHeader
import com.genoboi.ui.theme.*
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarioScreen() {
    var mesAtual by remember { mutableStateOf(YearMonth.now()) }
    var diaSelecionado by remember { mutableStateOf<LocalDate?>(null) }
    val alertas = MockData.alertas

    LazyColumn(
        modifier            = Modifier
            .fillMaxSize()
            .background(GenoGray50),
        contentPadding      = PaddingValues(bottom = 24.dp)
    ) {
        // Calendário
        item {
            Card(
                modifier  = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape     = RoundedCornerShape(14.dp),
                colors    = CardDefaults.cardColors(containerColor = GenoWhite),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    // Header do mês
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { mesAtual = mesAtual.minusMonths(1) }) {
                            Icon(Icons.Default.ChevronLeft, null, tint = GenoGray600)
                        }
                        Text(
                            "${mesAtual.month.getDisplayName(TextStyle.FULL, Locale("pt", "BR")).replaceFirstChar { it.uppercase() }} ${mesAtual.year}",
                            fontWeight = FontWeight.SemiBold,
                            fontSize   = 16.sp
                        )
                        IconButton(onClick = { mesAtual = mesAtual.plusMonths(1) }) {
                            Icon(Icons.Default.ChevronRight, null, tint = GenoGray600)
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Cabeçalho dos dias da semana
                    val diasSemana = listOf("DOM", "SEG", "TER", "QUA", "QUI", "SEX", "SÁB")
                    Row(Modifier.fillMaxWidth()) {
                        diasSemana.forEach { dia ->
                            Text(
                                dia,
                                modifier  = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                fontSize  = 10.sp,
                                color     = GenoGray400,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(Modifier.height(6.dp))

                    // Grid do calendário
                    val primeiroDia = mesAtual.atDay(1)
                    val offsetInicial = primeiroDia.dayOfWeek.value % 7  // domingo = 0
                    val totalDias    = mesAtual.lengthOfMonth()
                    val totalCelulas = offsetInicial + totalDias
                    val linhas       = (totalCelulas + 6) / 7

                    // Eventos mockados para pontilhar no calendário
                    val eventosDias = mapOf(
                        LocalDate.now() to TipoAlerta.CIO,
                        LocalDate.now().plusDays(2) to TipoAlerta.INSEMINACAO,
                        LocalDate.now().plusDays(5) to TipoAlerta.PARTO,
                        LocalDate.now().plusDays(7) to TipoAlerta.DIAGNOSTICO,
                        LocalDate.now().minusDays(3) to TipoAlerta.INSEMINACAO,
                    )

                    repeat(linhas) { linha ->
                        Row(Modifier.fillMaxWidth()) {
                            repeat(7) { col ->
                                val posicao = linha * 7 + col
                                val dia     = posicao - offsetInicial + 1

                                if (dia < 1 || dia > totalDias) {
                                    Spacer(Modifier.weight(1f).height(40.dp))
                                } else {
                                    val data    = mesAtual.atDay(dia)
                                    val ehHoje  = data == LocalDate.now()
                                    val selecionado = data == diaSelecionado
                                    val evento  = eventosDias[data]

                                    Box(
                                        modifier         = Modifier
                                            .weight(1f)
                                            .height(40.dp)
                                            .clip(CircleShape)
                                            .then(
                                                if (selecionado) Modifier.background(GenoGreen800)
                                                else if (ehHoje) Modifier.border(1.5.dp, GenoGreen700, CircleShape)
                                                else Modifier
                                            )
                                            .clickable { diaSelecionado = data },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                "$dia",
                                                fontSize   = 13.sp,
                                                fontWeight = if (ehHoje || selecionado) FontWeight.Bold else FontWeight.Normal,
                                                color      = when {
                                                    selecionado -> GenoWhite
                                                    ehHoje      -> GenoGreen800
                                                    else        -> GenoGray900
                                                }
                                            )
                                            if (evento != null) {
                                                Box(
                                                    Modifier
                                                        .size(5.dp)
                                                        .clip(CircleShape)
                                                        .background(eventoColor(evento))
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(2.dp))
                    }

                    Spacer(Modifier.height(12.dp))

                    // Legenda
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        LegendaItem(GenoGreen600,  "Cio")
                        Spacer(Modifier.width(12.dp))
                        LegendaItem(GenoBlue,      "Inseminação")
                        Spacer(Modifier.width(12.dp))
                        LegendaItem(GenoPurple,    "Diagnóstico")
                        Spacer(Modifier.width(12.dp))
                        LegendaItem(GenoOrange,    "Parto")
                    }
                }
            }
        }

        // Próximos eventos
        item {
            Card(
                modifier  = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape     = RoundedCornerShape(12.dp),
                colors    = CardDefaults.cardColors(containerColor = GenoWhite),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    SectionHeader(titulo = "Próximos eventos", acaoLabel = "Ver todos")
                    Spacer(Modifier.height(8.dp))
                    alertas.take(4).forEach { alerta ->
                        ProximoEventoRow(alerta)
                        if (alerta != alertas.take(4).last())
                            HorizontalDivider(color = GenoGray100, thickness = 0.5.dp)
                    }
                }
            }
        }

        // Alertas importantes
        item {
            Card(
                modifier  = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape     = RoundedCornerShape(12.dp),
                colors    = CardDefaults.cardColors(containerColor = GenoRed50),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, null, tint = GenoRed, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Alertas importantes", fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp, color = GenoRed)
                    }
                    Spacer(Modifier.height(10.dp))
                    AlertaBox("3 animais precisam de inseminação")
                    AlertaBox("1 animal com cio irregular")
                    AlertaBox("2 diagnósticos pendentes")
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {},
                        colors  = ButtonDefaults.buttonColors(containerColor = GenoRed),
                        modifier = Modifier.fillMaxWidth(),
                        shape   = RoundedCornerShape(8.dp)
                    ) { Text("Ver todos") }
                }
            }
        }
    }
}

@Composable
fun LegendaItem(cor: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(cor))
        Spacer(Modifier.width(4.dp))
        Text(label, fontSize = 10.sp, color = GenoGray600)
    }
}

@Composable
fun ProximoEventoRow(alerta: AlertaItem) {
    Row(
        Modifier.padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = eventoIcon(alerta.tipo),
            contentDescription = null,
            tint = eventoColor(alerta.tipo),
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(alerta.animalNome, fontWeight = FontWeight.Medium, fontSize = 14.sp)
            Text(alerta.descricao, fontSize = 12.sp, color = GenoGray600)
        }
        alerta.data?.let {
            Text(
                "${it.dayOfMonth.toString().padStart(2,'0')}/${it.monthValue.toString().padStart(2,'0')}",
                fontSize = 11.sp, color = GenoGray400
            )
        }
    }
}

@Composable
fun AlertaBox(texto: String) {
    Row(
        Modifier.padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Circle, null, tint = GenoRed, modifier = Modifier.size(8.dp))
        Spacer(Modifier.width(8.dp))
        Text(texto, fontSize = 13.sp, color = GenoGray900)
    }
}

fun eventoColor(tipo: TipoAlerta) = when (tipo) {
    TipoAlerta.CIO        -> GenoGreen600
    TipoAlerta.INSEMINACAO -> GenoBlue
    TipoAlerta.DIAGNOSTICO -> GenoPurple
    TipoAlerta.PARTO      -> GenoOrange
    TipoAlerta.GENETICO   -> GenoRed
}

fun eventoIcon(tipo: TipoAlerta) = when (tipo) {
    TipoAlerta.CIO        -> Icons.Default.Circle
    TipoAlerta.INSEMINACAO -> Icons.Default.Vaccines
    TipoAlerta.DIAGNOSTICO -> Icons.Default.MedicalServices
    TipoAlerta.PARTO      -> Icons.Default.ChildCare
    TipoAlerta.GENETICO   -> Icons.Default.Warning
}
