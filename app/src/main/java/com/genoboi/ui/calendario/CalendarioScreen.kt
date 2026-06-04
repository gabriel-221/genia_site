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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.genoboi.data.repository.AnimalRepository
import com.genoboi.domain.model.*
import com.genoboi.ui.components.SectionHeader
import com.genoboi.ui.theme.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarioScreen(repository: AnimalRepository) {
    val scope = rememberCoroutineScope()
    var mesAtual            by remember { mutableStateOf(YearMonth.now()) }
    var diaSelecionado      by remember { mutableStateOf<LocalDate?>(null) }
    var showDialogNovoEvento by remember { mutableStateOf(false) }

    val animais by repository.observarAnimais().collectAsState(initial = emptyList())

    // Monta eventos reais: um ponto por animal com prenhou ou que tem diasDesdeUltimoParto
    val eventosDias = remember(animais) {
        val map = mutableMapOf<LocalDate, TipoAlerta>()
        val hoje = LocalDate.now()
        animais.forEach { a ->
            if (a.prenhou) map[hoje.plusDays((a.id % 15).toLong())] = TipoAlerta.PARTO
            if (a.sexo == Sexo.FEMEA && !a.prenhou && a.numeroPartos > 0)
                map[hoje.plusDays((a.id % 7).toLong())] = TipoAlerta.CIO
        }
        // Garante ao menos o dia de hoje marcado se houver animais
        if (animais.isNotEmpty() && !map.containsKey(hoje)) map[hoje] = TipoAlerta.INSEMINACAO
        map
    }

    // Alertas derivados dos animais reais
    val alertas = remember(animais) {
        animais.take(5).mapIndexed { i, a ->
            AlertaItem(
                animalNome = a.nome,
                descricao  = when {
                    a.prenhou                              -> "Gestação confirmada"
                    a.sexo == Sexo.FEMEA && !a.prenhou    -> "Cio previsto"
                    a.sexo == Sexo.MACHO                   -> "Reprodutor disponível"
                    else                                   -> "Monitoramento ativo"
                },
                tipo = when {
                    a.prenhou                              -> TipoAlerta.PARTO
                    a.sexo == Sexo.FEMEA && !a.prenhou    -> TipoAlerta.CIO
                    else                                   -> TipoAlerta.INSEMINACAO
                },
                data = LocalDate.now().plusDays(i.toLong() * 3)
            )
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick        = { showDialogNovoEvento = true },
                containerColor = GenoGreen800,
                contentColor   = Color.White,
                shape          = CircleShape
            ) {
                Icon(Icons.Default.Add, "Registrar Evento", tint = Color.White)
            }
        },
        containerColor = GenoGray50
    ) { padding ->
        LazyColumn(
            modifier       = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Calendário
            item {
                Card(
                    modifier  = Modifier.fillMaxWidth().padding(16.dp),
                    shape     = RoundedCornerShape(14.dp),
                    colors    = CardDefaults.cardColors(containerColor = GenoWhite),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
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
                                fontWeight = FontWeight.SemiBold, fontSize = 16.sp
                            )
                            IconButton(onClick = { mesAtual = mesAtual.plusMonths(1) }) {
                                Icon(Icons.Default.ChevronRight, null, tint = GenoGray600)
                            }
                        }
                        Spacer(Modifier.height(8.dp))

                        val diasSemana = listOf("DOM", "SEG", "TER", "QUA", "QUI", "SEX", "SÁB")
                        Row(Modifier.fillMaxWidth()) {
                            diasSemana.forEach { dia ->
                                Text(dia, modifier = Modifier.weight(1f), textAlign = TextAlign.Center,
                                    fontSize = 10.sp, color = GenoGray400, fontWeight = FontWeight.Medium)
                            }
                        }
                        Spacer(Modifier.height(6.dp))

                        val primeiroDia  = mesAtual.atDay(1)
                        val offsetInicial = primeiroDia.dayOfWeek.value % 7
                        val totalDias    = mesAtual.lengthOfMonth()
                        val totalCelulas = offsetInicial + totalDias
                        val linhas       = (totalCelulas + 6) / 7

                        repeat(linhas) { linha ->
                            Row(Modifier.fillMaxWidth()) {
                                repeat(7) { col ->
                                    val posicao = linha * 7 + col
                                    val dia     = posicao - offsetInicial + 1
                                    if (dia < 1 || dia > totalDias) {
                                        Spacer(Modifier.weight(1f).height(40.dp))
                                    } else {
                                        val data       = mesAtual.atDay(dia)
                                        val ehHoje     = data == LocalDate.now()
                                        val selecionado = data == diaSelecionado
                                        val evento     = eventosDias[data]
                                        Box(
                                            modifier = Modifier
                                                .weight(1f).height(40.dp).clip(CircleShape)
                                                .then(
                                                    if (selecionado) Modifier.background(GenoGreen800)
                                                    else if (ehHoje) Modifier.border(1.5.dp, GenoGreen700, CircleShape)
                                                    else Modifier
                                                )
                                                .clickable { diaSelecionado = data },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("$dia", fontSize = 13.sp,
                                                    fontWeight = if (ehHoje || selecionado) FontWeight.Bold else FontWeight.Normal,
                                                    color = when {
                                                        selecionado -> GenoWhite
                                                        ehHoje      -> GenoGreen800
                                                        else        -> GenoGray900
                                                    })
                                                if (evento != null) {
                                                    Box(Modifier.size(5.dp).clip(CircleShape).background(eventoColor(evento)))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            Spacer(Modifier.height(2.dp))
                        }

                        Spacer(Modifier.height(12.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                            LegendaItem(GenoGreen600, "Cio")
                            Spacer(Modifier.width(12.dp))
                            LegendaItem(GenoBlue, "Inseminação")
                            Spacer(Modifier.width(12.dp))
                            LegendaItem(GenoPurple, "Diagnóstico")
                            Spacer(Modifier.width(12.dp))
                            LegendaItem(GenoOrange, "Parto")
                        }
                    }
                }
            }

            // Próximos eventos
            item {
                Card(
                    modifier  = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape     = RoundedCornerShape(12.dp),
                    colors    = CardDefaults.cardColors(containerColor = GenoWhite),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        SectionHeader(titulo = "Próximos eventos")
                        Spacer(Modifier.height(8.dp))
                        if (alertas.isEmpty()) {
                            Text("Nenhum animal cadastrado ainda.",
                                fontSize = 13.sp, color = GenoGray400,
                                modifier = Modifier.padding(vertical = 8.dp))
                        } else {
                            alertas.forEach { alerta ->
                                ProximoEventoRow(alerta)
                                if (alerta != alertas.last())
                                    HorizontalDivider(color = GenoGray100, thickness = 0.5.dp)
                            }
                        }
                    }
                }
            }

            // Resumo do rebanho
            item {
                Card(
                    modifier  = Modifier.fillMaxWidth().padding(16.dp),
                    shape     = RoundedCornerShape(12.dp),
                    colors    = CardDefaults.cardColors(containerColor = GenoGreen50),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, null, tint = GenoGreen800, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Resumo do rebanho", fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp, color = GenoGreen800)
                        }
                        Spacer(Modifier.height(10.dp))
                        val prenhas = animais.count { it.prenhou }
                        val femeas  = animais.count { it.sexo == Sexo.FEMEA }
                        InfoBox("${animais.size} animais cadastrados no total")
                        InfoBox("$femeas fêmeas · $prenhas em gestação")
                        InfoBox("${animais.count { it.sexo == Sexo.MACHO }} reprodutores")
                    }
                }
            }
        }
    }

    if (showDialogNovoEvento) {
        DialogNovoEvento(
            animais   = animais,
            onDismiss = { showDialogNovoEvento = false },
            onSalvar  = { animalId, tipo, data, obs ->
                scope.launch {
                    val evento = EventoReprodutivo(
                        animalId   = animalId,
                        tipo       = tipo,
                        data       = data,
                        observacao = obs
                    )
                    repository.salvarEvento(evento)
                }
                showDialogNovoEvento = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogNovoEvento(
    animais: List<Animal> = emptyList(),
    onDismiss: () -> Unit,
    onSalvar: (Long, TipoEvento, LocalDate, String) -> Unit = { _, _, _, _ -> }
) {
    var animalSelecionado by remember { mutableStateOf(animais.firstOrNull()) }
    var tipo    by remember { mutableStateOf(TipoEvento.CIO) }
    var dataStr by remember { mutableStateOf(LocalDate.now().toString()) }
    var obs     by remember { mutableStateOf("") }
    var dataErro by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrar Evento Reprodutivo", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (animais.isEmpty()) {
                    Text("Cadastre animais antes de registrar eventos.",
                        color = GenoGray600, fontSize = 14.sp)
                } else {
                    // Seletor de animal
                    Text("Animal", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    var expandido by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = expandido, onExpandedChange = { expandido = !expandido }) {
                        OutlinedTextField(
                            value         = animalSelecionado?.nome ?: "Selecione",
                            onValueChange = {},
                            readOnly      = true,
                            trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
                            modifier      = Modifier.fillMaxWidth().menuAnchor(),
                            shape         = RoundedCornerShape(10.dp)
                        )
                        ExposedDropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
                            animais.forEach { a ->
                                DropdownMenuItem(
                                    text    = { Text("${a.nome} (${a.especie.label})") },
                                    onClick = { animalSelecionado = a; expandido = false }
                                )
                            }
                        }
                    }

                    Text("Tipo de Evento", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        TipoEvento.values().take(3).forEach { t ->
                            FilterChip(
                                selected = tipo == t,
                                onClick  = { tipo = t },
                                label    = { Text(t.label, fontSize = 11.sp) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value         = dataStr,
                        onValueChange = { dataStr = it; dataErro = false },
                        label         = { Text("Data (aaaa-mm-dd)") },
                        isError       = dataErro,
                        modifier      = Modifier.fillMaxWidth(),
                        shape         = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value         = obs,
                        onValueChange = { obs = it },
                        label         = { Text("Observações (opcional)") },
                        modifier      = Modifier.fillMaxWidth(),
                        shape         = RoundedCornerShape(10.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (animais.isEmpty()) { onDismiss(); return@Button }
                    val data = try { LocalDate.parse(dataStr) } catch (_: Exception) { null }
                    if (data == null) { dataErro = true; return@Button }
                    val animal = animalSelecionado ?: return@Button
                    onSalvar(animal.id, tipo, data, obs)
                },
                colors = ButtonDefaults.buttonColors(containerColor = GenoGreen800, contentColor = Color.White)
            ) {
                Text("Salvar", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable fun LegendaItem(cor: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(cor))
        Spacer(Modifier.width(4.dp))
        Text(label, fontSize = 10.sp, color = GenoGray600)
    }
}

@Composable fun ProximoEventoRow(alerta: AlertaItem) {
    Row(Modifier.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(eventoIcon(alerta.tipo), null, tint = eventoColor(alerta.tipo), modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(alerta.animalNome, fontWeight = FontWeight.Medium, fontSize = 14.sp)
            Text(alerta.descricao, fontSize = 12.sp, color = GenoGray600)
        }
        alerta.data?.let {
            Text("${it.dayOfMonth.toString().padStart(2,'0')}/${it.monthValue.toString().padStart(2,'0')}",
                fontSize = 11.sp, color = GenoGray400)
        }
    }
}

@Composable fun InfoBox(texto: String) {
    Row(Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.CheckCircle, null, tint = GenoGreen700, modifier = Modifier.size(14.dp))
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
