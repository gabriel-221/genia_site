package com.genoboi.ui.cadastro

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.genoboi.data.repository.AnimalRepository
import com.genoboi.domain.model.*
import com.genoboi.ui.theme.*
import kotlinx.coroutines.launch
import java.time.LocalDate

// ─── Estado do wizard ─────────────────────────────────────────────────────────

data class CadastroState(
    // Passo 1
    val nome: String          = "",
    val especie: Especie      = Especie.BOVINO,
    val raca: String          = "",
    val sexo: Sexo            = Sexo.FEMEA,
    val dataNascimento: String = "",
    val linhagem: String      = "",
    val rfid: String          = "",
    val peso: String          = "",
    val escoreCorporal: String = "3,0",
    val fazenda: String       = "",
    // Passo 2
    val nomePai: String       = "",
    val racaPai: String       = "",
    val rfidPai: String       = "",
    val fazendaPai: String    = "",
    val nomeMae: String       = "",
    val racaMae: String       = "",
    val rfidMae: String       = "",
    val fazendaMae: String    = "",
    // Passo 3
    val numPartosAnteriores: String = "0",
    val ultimoParto: String   = "",
    val resultadoUltimaPrenhez: String = "Negativo",
    val numInseminacoesAnteriores: String = "0",
    // Passo 4
    val dataInseminacao: String  = "",
    val tipoSemen: String        = "Congelado",
    val reprodutor: String       = "",
    val registroSemen: String    = "",
    val tecnico: String          = "",
    val metodoInseminacao: String = "Inseminação cervical",
    val observacoesIA: String    = "",
    val probabilidadePrenhez: Int = 78
)

@Composable
fun CadastroAnimalScreen(
    repository: AnimalRepository,
    onVoltar: () -> Unit,
    onSalvo: () -> Unit
) {
    var passo by remember { mutableIntStateOf(1) }
    var estado by remember { mutableStateOf(CadastroState()) }
    val scope  = rememberCoroutineScope()

    val passos = listOf("Básico", "Pedigree", "Reprodutivo", "Inseminação", "Revisão")

    Scaffold(
        topBar = {
            Surface(shadowElevation = 2.dp) {
                Column {
                    // Header com título
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .background(GenoWhite)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            if (passo > 1) passo-- else onVoltar()
                        }) {
                            Icon(Icons.Default.ArrowBack, null)
                        }
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("GENIA", fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp, color = GenoGreen800)
                                Icon(Icons.Default.Grass, null, tint = GenoGreen600,
                                    modifier = Modifier.size(16.dp))
                            }
                            Text("Cadastre um novo animal", fontSize = 12.sp, color = GenoGray600)
                        }
                        Spacer(Modifier.width(48.dp))
                    }

                    // Step indicator
                    PassoIndicador(passos = passos, atual = passo)
                }
            }
        },
        containerColor = GenoGray50
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Conteúdo do passo (scrollável)
            Box(Modifier.weight(1f)) {
                AnimatedContent(
                    targetState  = passo,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally { it } + fadeIn()) togetherWith
                            (slideOutHorizontally { -it } + fadeOut())
                        } else {
                            (slideInHorizontally { -it } + fadeIn()) togetherWith
                            (slideOutHorizontally { it } + fadeOut())
                        }
                    }
                ) { p ->
                    when (p) {
                        1 -> PassoBasico(estado)      { estado = it }
                        2 -> PassoPedigree(estado)    { estado = it }
                        3 -> PassoReprodutivo(estado) { estado = it }
                        4 -> PassoInseminacao(estado) { estado = it }
                        5 -> PassoRevisao(estado, onEditar = { passo = it })
                        else -> {}
                    }
                }
            }

            // Botões de navegação
            Surface(shadowElevation = 8.dp) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(GenoWhite)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (passo > 1) {
                        OutlinedButton(
                            onClick = { passo-- },
                            modifier = Modifier.weight(1f),
                            shape    = RoundedCornerShape(8.dp)
                        ) {
                            Text("VOLTAR")
                        }
                    }

                    Button(
                        onClick = {
                            if (passo < 5) {
                                passo++
                            } else {
                                // Salvar no banco
                                scope.launch {
                                    val animal = estado.toAnimal()
                                    repository.salvarAnimal(animal)
                                    onSalvo()
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape    = RoundedCornerShape(8.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = GenoGreen800)
                    ) {
                        if (passo == 5) {
                            Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("CONFIRMAR CADASTRO")
                        } else {
                            Text("PRÓXIMO")
                            Spacer(Modifier.width(6.dp))
                            Icon(Icons.Default.ArrowForward, null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

// ─── Step Indicator ───────────────────────────────────────────────────────────

@Composable
fun PassoIndicador(passos: List<String>, atual: Int) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(GenoWhite)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        passos.forEachIndexed { idx, label ->
            val numero  = idx + 1
            val ativo   = numero == atual
            val passado = numero < atual

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(56.dp)
            ) {
                Box(
                    modifier         = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(
                            when { ativo || passado -> GenoGreen800; else -> GenoGray200 }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (passado) {
                        Icon(Icons.Default.Check, null, tint = GenoWhite,
                            modifier = Modifier.size(14.dp))
                    } else {
                        Text(
                            "$numero",
                            color      = if (ativo) GenoWhite else GenoGray600,
                            fontSize   = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(Modifier.height(3.dp))
                Text(
                    label,
                    fontSize   = 9.sp,
                    color      = if (ativo) GenoGreen800 else GenoGray400,
                    fontWeight = if (ativo) FontWeight.SemiBold else FontWeight.Normal
                )
            }

            if (idx < passos.size - 1) {
                Box(
                    Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(if (numero < atual) GenoGreen600 else GenoGray200)
                )
            }
        }
    }
}

// ─── Passos ───────────────────────────────────────────────────────────────────

@Composable
fun PassoBasico(estado: CadastroState, onChange: (CadastroState) -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Informações Básicas", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)

        CampoTexto("Nome do animal *", estado.nome) { onChange(estado.copy(nome = it)) }

        // Espécie
        DropdownCampo(
            label = "Espécie *",
            opcoes = Especie.values().map { "${it.emoji} ${it.label}" },
            selecionado = "${estado.especie.emoji} ${estado.especie.label}",
            onSelect = { idx -> onChange(estado.copy(especie = Especie.values()[idx])) }
        )

        // Sexo
        DropdownCampo(
            label = "Sexo *",
            opcoes = Sexo.values().map { it.label },
            selecionado = estado.sexo.label,
            onSelect = { idx -> onChange(estado.copy(sexo = Sexo.values()[idx])) }
        )

        CampoTexto("Data de nascimento *", estado.dataNascimento,
            placeholder = "dd/MM/yyyy") { onChange(estado.copy(dataNascimento = it)) }
        CampoTexto("Raça *", estado.raca) { onChange(estado.copy(raca = it)) }
        CampoTexto("Linhagem", estado.linhagem) { onChange(estado.copy(linhagem = it)) }
        CampoTexto("RFID / NFC", estado.rfid) { onChange(estado.copy(rfid = it)) }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CampoTexto("Peso (kg)", estado.peso,
                teclado = KeyboardType.Number,
                modifier = Modifier.weight(1f)) { onChange(estado.copy(peso = it)) }
            CampoTexto("Escore corporal", estado.escoreCorporal,
                teclado = KeyboardType.Decimal,
                modifier = Modifier.weight(1f)) { onChange(estado.copy(escoreCorporal = it)) }
        }

        CampoTexto("Fazenda *", estado.fazenda) { onChange(estado.copy(fazenda = it)) }

        if (estado.peso.isNotEmpty()) {
            val pesoVal = estado.peso.replace(",", ".").toFloatOrNull() ?: 0f
            val especie = estado.especie
            val (min, max) = when (especie) {
                Especie.BOVINO  -> Pair(300f, 600f)
                Especie.OVINO   -> Pair(40f, 80f)
                Especie.CAPRINO -> Pair(25f, 60f)
            }
            val ideal = pesoVal in min..max
            Surface(
                shape  = RoundedCornerShape(8.dp),
                color  = if (ideal) GenoGreen100 else GenoOrange50
            ) {
                Row(
                    Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (ideal) Icons.Default.CheckCircle else Icons.Default.Info,
                        null,
                        tint = if (ideal) GenoGreen700 else GenoOrange,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (ideal) "Peso ideal para reprodução"
                        else "Peso fora do ideal para reprodução",
                        fontSize = 12.sp,
                        color = if (ideal) GenoGreen800 else GenoOrange
                    )
                }
            }
        }
    }
}

@Composable
fun PassoPedigree(estado: CadastroState, onChange: (CadastroState) -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Pedigree (Informações dos Pais)", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)

        Text("Pai", fontWeight = FontWeight.Medium, color = GenoGray600)
        CampoTexto("Nome do pai *", estado.nomePai)  { onChange(estado.copy(nomePai = it)) }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CampoTexto("Raça", estado.racaPai, modifier = Modifier.weight(1f)) { onChange(estado.copy(racaPai = it)) }
            CampoTexto("Registro / RFID", estado.rfidPai, modifier = Modifier.weight(1f)) { onChange(estado.copy(rfidPai = it)) }
        }
        CampoTexto("Fazenda de origem", estado.fazendaPai) { onChange(estado.copy(fazendaPai = it)) }

        HorizontalDivider(color = GenoGray100)

        Text("Mãe", fontWeight = FontWeight.Medium, color = GenoGray600)
        CampoTexto("Nome da mãe *", estado.nomeMae)  { onChange(estado.copy(nomeMae = it)) }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CampoTexto("Raça", estado.racaMae, modifier = Modifier.weight(1f)) { onChange(estado.copy(racaMae = it)) }
            CampoTexto("Registro / RFID", estado.rfidMae, modifier = Modifier.weight(1f)) { onChange(estado.copy(rfidMae = it)) }
        }
        CampoTexto("Fazenda de origem", estado.fazendaMae) { onChange(estado.copy(fazendaMae = it)) }

        // Coef endogamia mockado
        Card(
            shape  = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = GenoGreen50),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(Modifier.padding(14.dp)) {
                Text("Coeficiente de endogamia (estimado)", fontSize = 12.sp, color = GenoGray600)
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("6,2%", fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = GenoGreen800)
                    Spacer(Modifier.width(10.dp))
                    Surface(shape = RoundedCornerShape(20.dp), color = GenoGreen100) {
                        Text("Risco: Baixo", fontSize = 12.sp, color = GenoGreen800,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                    }
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Default.CheckCircle, null, tint = GenoGreen700, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.height(8.dp))
                Row {
                    Icon(Icons.Default.Lightbulb, null, tint = GenoAmber, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("A IA analisou o pedigree e não identificou riscos genéticos relevantes.",
                        fontSize = 12.sp, color = GenoGray600)
                }
            }
        }
    }
}

@Composable
fun PassoReprodutivo(estado: CadastroState, onChange: (CadastroState) -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Histórico Reprodutivo", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)

        CampoTexto("Número de partos anteriores", estado.numPartosAnteriores,
            teclado = KeyboardType.Number) { onChange(estado.copy(numPartosAnteriores = it)) }
        CampoTexto("Último parto", estado.ultimoParto, placeholder = "dd/MM/yyyy") {
            onChange(estado.copy(ultimoParto = it)) }

        DropdownCampo(
            label     = "Resultado da última prenhez",
            opcoes    = listOf("Positivo", "Negativo", "Não realizado"),
            selecionado = estado.resultadoUltimaPrenhez,
            onSelect  = { idx ->
                onChange(estado.copy(resultadoUltimaPrenhez = listOf("Positivo","Negativo","Não realizado")[idx]))
            }
        )

        CampoTexto("Número de inseminações anteriores", estado.numInseminacoesAnteriores,
            teclado = KeyboardType.Number) { onChange(estado.copy(numInseminacoesAnteriores = it)) }

        Text("Detecção de cio (último evento)", fontWeight = FontWeight.Medium,
            fontSize = 14.sp, color = GenoGray700)

        DropdownCampo(
            label     = "Tipo de muco",
            opcoes    = listOf("Limpido (clara de ovo)", "Turvo", "Ausente", "Hemorrágico"),
            selecionado = "Limpido (clara de ovo)",
            onSelect  = {}
        )

        DropdownCampo(
            label     = "Comportamento observado",
            opcoes    = listOf("Aceitação de monta", "Inquieta", "Mugindo", "Normal"),
            selecionado = "Aceitação de monta",
            onSelect  = {}
        )
    }
}

@Composable
fun PassoInseminacao(estado: CadastroState, onChange: (CadastroState) -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Dados da Inseminação (IA)", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)

        CampoTexto("Data da inseminação *", estado.dataInseminacao,
            placeholder = "dd/MM/yyyy") { onChange(estado.copy(dataInseminacao = it)) }

        DropdownCampo(
            label = "Tipo de sêmen *",
            opcoes = listOf("Congelado", "Resfriado", "A fresco"),
            selecionado = estado.tipoSemen,
            onSelect = { idx -> onChange(estado.copy(tipoSemen = listOf("Congelado","Resfriado","A fresco")[idx])) }
        )

        CampoTexto("Reprodutor (origem do sêmen) *", estado.reprodutor) { onChange(estado.copy(reprodutor = it)) }
        CampoTexto("Registro do sêmen", estado.registroSemen) { onChange(estado.copy(registroSemen = it)) }
        CampoTexto("Técnico responsável *", estado.tecnico) { onChange(estado.copy(tecnico = it)) }

        DropdownCampo(
            label = "Método utilizado",
            opcoes = listOf("Inseminação cervical", "Inseminação transcervical", "TE"),
            selecionado = estado.metodoInseminacao,
            onSelect = { idx ->
                onChange(estado.copy(metodoInseminacao = listOf(
                    "Inseminação cervical","Inseminação transcervical","TE")[idx]))
            }
        )

        CampoTexto("Observações", estado.observacoesIA, singleLine = false) { onChange(estado.copy(observacoesIA = it)) }

        // Probabilidade IA
        Card(
            shape  = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = GenoGreen50),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Probabilidade de prenhez (estimada pela IA)",
                    fontSize = 12.sp, color = GenoGray600)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${estado.probabilidadePrenhez}%",
                        fontWeight = FontWeight.ExtraBold, fontSize = 36.sp, color = GenoGreen800)
                    Spacer(Modifier.width(10.dp))
                    Text("Alta", fontSize = 14.sp, color = GenoGreen700, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Default.TrendingUp, null, tint = GenoGreen700,
                        modifier = Modifier.size(28.dp))
                }
            }
        }
    }
}

@Composable
fun PassoRevisao(estado: CadastroState, onEditar: (Int) -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Revisão do Cadastro", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)

        RevisaoCard(titulo = "Informações Básicas", onEditar = { onEditar(1) }) {
            RevisaoRow("Nome", estado.nome)
            RevisaoRow("Espécie / Raça", "${estado.especie.label} • ${estado.raca}")
            RevisaoRow("Sexo", estado.sexo.label)
            RevisaoRow("Nascimento", estado.dataNascimento)
            if (estado.rfid.isNotEmpty()) RevisaoRow("RFID", estado.rfid)
            if (estado.peso.isNotEmpty()) RevisaoRow("Peso", "${estado.peso} kg • ECC ${estado.escoreCorporal}")
        }

        RevisaoCard(titulo = "Pedigree", onEditar = { onEditar(2) }) {
            RevisaoRow("Pai", "${estado.nomePai} (${estado.racaPai}) - ${estado.rfidPai}")
            RevisaoRow("Mãe", "${estado.nomeMae} (${estado.racaMae}) - ${estado.rfidMae}")
            RevisaoRow("Endogamia", "6,2% (Baixo risco)")
        }

        RevisaoCard(titulo = "Histórico Reprodutivo", onEditar = { onEditar(3) }) {
            RevisaoRow("Partos anteriores", estado.numPartosAnteriores)
            if (estado.ultimoParto.isNotEmpty()) RevisaoRow("Último parto", estado.ultimoParto)
            RevisaoRow("Última prenhez", estado.resultadoUltimaPrenhez)
            RevisaoRow("IA anteriores", "${estado.numInseminacoesAnteriores}")
        }

        RevisaoCard(titulo = "Inseminação", onEditar = { onEditar(4) }) {
            if (estado.dataInseminacao.isNotEmpty()) RevisaoRow("Data IA", estado.dataInseminacao)
            RevisaoRow("Sêmen", estado.tipoSemen)
            if (estado.reprodutor.isNotEmpty()) RevisaoRow("Reprodutor", estado.reprodutor)
            if (estado.tecnico.isNotEmpty()) RevisaoRow("Técnico", "${estado.tecnico} • Método: ${estado.metodoInseminacao}")
        }
    }
}

@Composable
fun RevisaoCard(titulo: String, onEditar: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape  = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = GenoWhite),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(titulo, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                TextButton(onClick = onEditar, contentPadding = PaddingValues(0.dp)) {
                    Text("Editar", fontSize = 12.sp, color = GenoGreen700)
                }
            }
            HorizontalDivider(color = GenoGray100, modifier = Modifier.padding(vertical = 6.dp))
            content()
        }
    }
}

@Composable
fun RevisaoRow(campo: String, valor: String) {
    if (valor.isBlank()) return
    Row(Modifier.padding(vertical = 3.dp)) {
        Text("$campo: ", fontSize = 12.sp, color = GenoGray600)
        Text(valor, fontSize = 12.sp, color = GenoGray900, fontWeight = FontWeight.Medium)
    }
}

// ─── Helpers de input ─────────────────────────────────────────────────────────

@Composable
fun CampoTexto(
    label: String,
    valor: String,
    placeholder: String = "",
    teclado: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    modifier: Modifier = Modifier.fillMaxWidth(),
    onChange: (String) -> Unit
) {
    OutlinedTextField(
        value         = valor,
        onValueChange = onChange,
        label         = { Text(label, fontSize = 13.sp) },
        placeholder   = if (placeholder.isNotEmpty()) {{ Text(placeholder, fontSize = 13.sp) }} else null,
        singleLine    = singleLine,
        modifier      = modifier,
        shape         = RoundedCornerShape(10.dp),
        keyboardOptions = KeyboardOptions(keyboardType = teclado),
        colors        = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = GenoGreen700,
            unfocusedBorderColor = GenoGray200,
            focusedContainerColor   = GenoWhite,
            unfocusedContainerColor = GenoWhite
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownCampo(
    label: String,
    opcoes: List<String>,
    selecionado: String,
    onSelect: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded        = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value         = selecionado,
            onValueChange = {},
            label         = { Text(label, fontSize = 13.sp) },
            readOnly      = true,
            trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier      = Modifier.fillMaxWidth().menuAnchor(),
            shape         = RoundedCornerShape(10.dp),
            colors        = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = GenoGreen700,
                unfocusedBorderColor = GenoGray200,
                focusedContainerColor   = GenoWhite,
                unfocusedContainerColor = GenoWhite
            )
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            opcoes.forEachIndexed { idx, opcao ->
                DropdownMenuItem(
                    text    = { Text(opcao, fontSize = 14.sp) },
                    onClick = { onSelect(idx); expanded = false }
                )
            }
        }
    }
}

// Converter estado para modelo de domínio
fun CadastroState.toAnimal(): Animal {
    val nascimento = try {
        val partes = dataNascimento.split("/")
        if (partes.size == 3) LocalDate.of(partes[2].toInt(), partes[1].toInt(), partes[0].toInt())
        else LocalDate.now()
    } catch (e: Exception) { LocalDate.now() }

    return Animal(
        nome           = nome,
        especie        = especie,
        raca           = raca,
        linhagem       = linhagem,
        sexo           = sexo,
        dataNascimento = nascimento,
        rfid           = rfid,
        pesoKg         = peso.replace(",", ".").toFloatOrNull() ?: 0f,
        escoreCorporal = escoreCorporal.replace(",", ".").toFloatOrNull() ?: 3f,
        fazenda        = fazenda,
        nomePai        = nomePai,
        racaPai        = racaPai,
        rfidPai        = rfidPai,
        nomeMae        = nomeMae,
        racaMae        = racaMae,
        rfidMae        = rfidMae
    )
}

val GenoGray700 = com.genoboi.ui.theme.GenoGray600
