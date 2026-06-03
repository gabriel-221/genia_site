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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.genoboi.data.repository.AnimalRepository
import com.genoboi.domain.model.*
import com.genoboi.ui.theme.*
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.AnnotatedString
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

fun isValidDate(dateStr: String): Boolean {
    if (dateStr.length != 8) return false
    return try {
        val d = dateStr.substring(0, 2).toInt()
        val m = dateStr.substring(2, 4).toInt()
        val a = dateStr.substring(4, 8).toInt()
        LocalDate.of(a, m, d)
        true
    } catch (e: Exception) {
        false
    }
}

// ─── Visual Transformation para Data ──────────────────────────────────────────

class DateVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = if (text.text.length >= 8) text.text.substring(0, 8) else text.text
        var out = ""
        for (i in trimmed.indices) {
            out += trimmed[i]
            if (i == 1 || i == 3) out += "/"
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 1) return offset
                if (offset <= 3) return offset + 1
                if (offset <= 8) return offset + 2
                return 10
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 2) return offset
                if (offset <= 5) return offset - 1
                if (offset <= 10) return offset - 2
                return 8
            }
        }

        return TransformedText(AnnotatedString(out), offsetMapping)
    }
}

// ─── Estado do wizard ─────────────────────────────────────────────────────────

data class CadastroState(
    // Identificação básica
    val nome: String          = "",
    val especie: Especie      = Especie.BOVINO,
    val sexo: Sexo            = Sexo.FEMEA,
    val dataNascimento: String = "",
    val raca: String          = "",
    val peso: String          = "",
    val escoreCorporal: String = "3,0",
    val fazenda: String       = "",
    
    // Atributos específicos Fêmea (Matriz)
    val numeroPartos: String = "0",
    val abortos: String = "0",
    val diasDesdeUltimoParto: String = "0",
    val filhosNascidosMatriz: String = "0",
    
    // Atributos específicos Macho (Reprodutor)
    val qualidadeSemenMacho: String = "",
    val filhosNascidosMacho: String = "0",

    // Pedigree
    val nomePai: String       = "",
    val racaPai: String       = "",
    val rfidPai: String       = "",
    val nomeMae: String       = "",
    val racaMae: String       = "",
    val rfidMae: String       = ""
)

@Composable
fun CadastroAnimalScreen(
    animalId: Long? = null,
    repository: AnimalRepository,
    onVoltar: () -> Unit,
    onSalvo: () -> Unit
) {
    var passo by remember { mutableIntStateOf(1) }
    var estado by remember { mutableStateOf(CadastroState()) }
    val scope  = rememberCoroutineScope()

    LaunchedEffect(animalId) {
        if (animalId != null) {
            repository.buscarAnimalPorId(animalId)?.let {
                estado = it.toState()
            }
        }
    }

    val passos = listOf("Informações", "Pedigree", "Revisão")

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
                            Text(if (animalId == null) "Cadastre um novo animal" else "Editar animal",
                                fontSize = 12.sp, color = GenoGray600)
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
                        3 -> PassoRevisao(estado, onEditar = { passo = it })
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
                            if (passo < 3) {
                                passo++
                            } else {
                                // Salvar no banco
                                scope.launch {
                                    val animal = estado.toAnimal().copy(id = animalId ?: 0L)
                                    if (animalId == null) {
                                        repository.salvarAnimal(animal)
                                    } else {
                                        repository.atualizarAnimal(animal)
                                    }
                                    onSalvo()
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape    = RoundedCornerShape(8.dp),
                        colors   = ButtonDefaults.buttonColors(
                            containerColor = GenoGreen800,
                            contentColor = Color.White
                        )
                    ) {
                        if (passo == 3) {
                            Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp), tint = Color.White)
                            Spacer(Modifier.width(6.dp))
                            Text(if (animalId == null) "CONFIRMAR CADASTRO" else "SALVAR ALTERAÇÕES", color = Color.White, fontWeight = FontWeight.Bold)
                        } else {
                            Text("PRÓXIMO", color = Color.White, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(6.dp))
                            Icon(Icons.Default.ArrowForward, null, modifier = Modifier.size(16.dp), tint = Color.White)
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
                modifier = Modifier.width(64.dp)
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
                    fontSize   = 10.sp,
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
        Text("Informações Gerais", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)

        CampoTexto("Nome do animal *", estado.nome) { onChange(estado.copy(nome = it)) }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            // Espécie
            Box(Modifier.weight(1f)) {
                DropdownCampo(
                    label = "Espécie *",
                    opcoes = Especie.values().map { "${it.emoji} ${it.label}" },
                    selecionado = "${estado.especie.emoji} ${estado.especie.label}",
                    onSelect = { idx -> onChange(estado.copy(especie = Especie.values()[idx])) }
                )
            }

            // Sexo
            Box(Modifier.weight(1f)) {
                DropdownCampo(
                    label = "Sexo *",
                    opcoes = Sexo.values().map { it.label },
                    selecionado = estado.sexo.label,
                    onSelect = { idx -> onChange(estado.copy(sexo = Sexo.values()[idx])) }
                )
            }
        }

        CampoTexto(
            label = "Data de nascimento *",
            valor = estado.dataNascimento,
            placeholder = "dd/MM/yyyy",
            teclado = KeyboardType.Number,
            isError = estado.dataNascimento.isNotEmpty() && !isValidDate(estado.dataNascimento),
            visualTransformation = DateVisualTransformation()
        ) { 
            if (it.length <= 8) onChange(estado.copy(dataNascimento = it)) 
        }
        
        val racasDisponiveis = com.genoboi.data.ml.PrenhezModelHelper.RACAS_POR_ESPECIE[estado.especie] ?: emptyList()
        DropdownCampo(
            label = "Raça *",
            opcoes = racasDisponiveis,
            selecionado = if (estado.raca in racasDisponiveis) estado.raca else "",
            onSelect = { idx -> onChange(estado.copy(raca = racasDisponiveis[idx])) }
        )
        CampoTexto("Fazenda *", estado.fazenda) { onChange(estado.copy(fazenda = it)) }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CampoTexto("Peso (kg) *", estado.peso,
                teclado = KeyboardType.Number,
                modifier = Modifier.weight(1f)) { onChange(estado.copy(peso = it)) }
            
            if (estado.sexo == Sexo.FEMEA) {
                val eccVal = estado.escoreCorporal.replace(",", ".").toFloatOrNull() ?: 0f
                CampoTexto(
                    label = "ECC 1-5 (ideal: 3.0-3.5)",
                    valor = estado.escoreCorporal,
                    teclado = KeyboardType.Decimal,
                    isError = eccVal > 0f && (eccVal < 1f || eccVal > 5f),
                    modifier = Modifier.weight(1f)
                ) { onChange(estado.copy(escoreCorporal = it)) }
            }
        }

        // Atributos específicos baseados no sexo (da imagem)
        if (estado.sexo == Sexo.FEMEA) {
            Text("Histórico da Matriz", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = GenoGreen800)
            
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CampoTexto("Qtd. Partos", estado.numeroPartos,
                    teclado = KeyboardType.Number, modifier = Modifier.weight(1f)) { onChange(estado.copy(numeroPartos = it)) }
                CampoTexto("Abortos", estado.abortos,
                    teclado = KeyboardType.Number, modifier = Modifier.weight(1f)) { onChange(estado.copy(abortos = it)) }
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CampoTexto("Dias pós-parto", estado.diasDesdeUltimoParto,
                    teclado = KeyboardType.Number, modifier = Modifier.weight(1f)) { onChange(estado.copy(diasDesdeUltimoParto = it)) }
                CampoTexto("Filhos nascidos", estado.filhosNascidosMatriz,
                    teclado = KeyboardType.Number, modifier = Modifier.weight(1f)) { onChange(estado.copy(filhosNascidosMatriz = it)) }
            }
        } else {
            Text("Histórico do Reprodutor", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = GenoGreen800)
            
            val semenVal = estado.qualidadeSemenMacho.replace(",", ".").toFloatOrNull() ?: 0f
            CampoTexto(
                label = "Qualidade Seminal (1-5, ideal: 3-5)",
                valor = estado.qualidadeSemenMacho,
                teclado = KeyboardType.Decimal,
                isError = semenVal > 0f && (semenVal < 1f || semenVal > 5f)
            ) { onChange(estado.copy(qualidadeSemenMacho = it)) }
            CampoTexto("Filhos nascidos", estado.filhosNascidosMacho,
                teclado = KeyboardType.Number) { onChange(estado.copy(filhosNascidosMacho = it)) }
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
        Text("Pedigree (Opcional)", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)

        Text("Pai", fontWeight = FontWeight.Medium, color = GenoGray600)
        CampoTexto("Nome do pai", estado.nomePai)  { onChange(estado.copy(nomePai = it)) }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CampoTexto("Raça do Pai", estado.racaPai, modifier = Modifier.weight(1f)) { onChange(estado.copy(racaPai = it)) }
            CampoTexto("Registro / RFID", estado.rfidPai, modifier = Modifier.weight(1f)) { onChange(estado.copy(rfidPai = it)) }
        }

        HorizontalDivider(color = GenoGray100, modifier = Modifier.padding(vertical = 4.dp))

        Text("Mãe", fontWeight = FontWeight.Medium, color = GenoGray600)
        CampoTexto("Nome da mãe", estado.nomeMae)  { onChange(estado.copy(nomeMae = it)) }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CampoTexto("Raça da Mãe", estado.racaMae, modifier = Modifier.weight(1f)) { onChange(estado.copy(racaMae = it)) }
            CampoTexto("Registro / RFID", estado.rfidMae, modifier = Modifier.weight(1f)) { onChange(estado.copy(rfidMae = it)) }
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

        RevisaoCard(titulo = "Informações Gerais", onEditar = { onEditar(1) }) {
            RevisaoRow("Nome", estado.nome)
            RevisaoRow("Espécie / Raça", "${estado.especie.label} • ${estado.raca}")
            RevisaoRow("Sexo", estado.sexo.label)
            RevisaoRow("Nascimento", estado.dataNascimento)
            RevisaoRow("Peso", "${estado.peso} kg")
            if (estado.sexo == Sexo.FEMEA) {
                RevisaoRow("ECC", estado.escoreCorporal)
                RevisaoRow("Partos", estado.numeroPartos)
                RevisaoRow("Abortos", estado.abortos)
                RevisaoRow("Dias pós-parto", estado.diasDesdeUltimoParto)
                RevisaoRow("Filhos nascidos", estado.filhosNascidosMatriz)
            } else {
                RevisaoRow("Qld. Sêmen", estado.qualidadeSemenMacho)
                RevisaoRow("Filhos nascidos", estado.filhosNascidosMacho)
            }
            RevisaoRow("Fazenda", estado.fazenda)
        }

        RevisaoCard(titulo = "Pedigree", onEditar = { onEditar(2) }) {
            if (estado.nomePai.isNotEmpty()) RevisaoRow("Pai", "${estado.nomePai} (${estado.racaPai})")
            if (estado.nomeMae.isNotEmpty()) RevisaoRow("Mãe", "${estado.nomeMae} (${estado.racaMae})")
            if (estado.nomePai.isEmpty() && estado.nomeMae.isEmpty()) Text("Não informado", fontSize = 12.sp, color = GenoGray400)
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
    visualTransformation: VisualTransformation = VisualTransformation.None,
    isError: Boolean = false,
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
        isError       = isError,
        shape         = RoundedCornerShape(10.dp),
        keyboardOptions = KeyboardOptions(keyboardType = teclado),
        visualTransformation = visualTransformation,
        colors        = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = GenoGreen700,
            unfocusedBorderColor = GenoGray200,
            focusedContainerColor   = GenoWhite,
            unfocusedContainerColor = GenoWhite,
            errorBorderColor = GenoRed
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
        if (dataNascimento.length == 8) {
            val d = dataNascimento.substring(0, 2).toInt()
            val m = dataNascimento.substring(2, 4).toInt()
            val a = dataNascimento.substring(4, 8).toInt()
            LocalDate.of(a, m, d)
        } else LocalDate.now()
    } catch (e: Exception) { LocalDate.now() }

    return Animal(
        nome           = nome,
        especie        = especie,
        raca           = raca,
        sexo           = sexo,
        dataNascimento = nascimento,
        pesoKg         = peso.replace(",", ".").toFloatOrNull() ?: 0f,
        escoreCorporal = escoreCorporal.replace(",", ".").toFloatOrNull() ?: 3f,
        fazenda        = fazenda,
        nomePai        = nomePai,
        racaPai        = racaPai,
        rfidPai        = rfidPai,
        nomeMae        = nomeMae,
        racaMae        = racaMae,
        rfidMae        = rfidMae,
        
        // Novos campos
        numeroPartos         = numeroPartos.toIntOrNull() ?: 0,
        abortos              = abortos.toIntOrNull() ?: 0,
        diasDesdeUltimoParto = diasDesdeUltimoParto.toIntOrNull() ?: 0,
        filhosNascidosMatriz = filhosNascidosMatriz.toIntOrNull() ?: 0,
        qualidadeSemenMacho   = qualidadeSemenMacho.replace(",", ".").toFloatOrNull() ?: 3f,
        filhosNascidosMacho   = filhosNascidosMacho.toIntOrNull() ?: 0
    )
}

fun Animal.toState(): CadastroState {
    val dataStr = "${dataNascimento.dayOfMonth.toString().padStart(2,'0')}${dataNascimento.monthValue.toString().padStart(2,'0')}${dataNascimento.year}"
    return CadastroState(
        nome = nome,
        especie = especie,
        raca = raca,
        sexo = sexo,
        dataNascimento = dataStr,
        peso = if (pesoKg > 0) pesoKg.toString() else "",
        escoreCorporal = escoreCorporal.toString().replace(".", ","),
        fazenda = fazenda,
        nomePai = nomePai,
        racaPai = racaPai,
        rfidPai = rfidPai,
        nomeMae = nomeMae,
        racaMae = racaMae,
        rfidMae = rfidMae,
        
        // Novos campos
        numeroPartos = numeroPartos.toString(),
        abortos = abortos.toString(),
        diasDesdeUltimoParto = diasDesdeUltimoParto.toString(),
        filhosNascidosMatriz = filhosNascidosMatriz.toString(),
        qualidadeSemenMacho = qualidadeSemenMacho.toString().replace(".", ","),
        filhosNascidosMacho = filhosNascidosMacho.toString()
    )
}

val GenoGray700 = com.genoboi.ui.theme.GenoGray600
