package com.genoboi.ui.animais

import android.app.Activity
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.genoboi.data.nfc.NfcHelper
import com.genoboi.data.repository.AnimalRepository
import com.genoboi.domain.model.Animal
import com.genoboi.domain.model.Sexo
import com.genoboi.ui.calendario.DialogNovoEvento
import com.genoboi.ui.components.GenoTopBar
import com.genoboi.ui.theme.*
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

@Composable
fun AnimalDetalheScreen(
    animalId: Long,
    repository: AnimalRepository,
    onVoltar: () -> Unit,
    onEditar: (Long) -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val nfcAdapter = remember { NfcAdapter.getDefaultAdapter(context) }

    var animal by remember { mutableStateOf<Animal?>(null) }
    var supabaseId by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(animalId) {
        animal = repository.buscarAnimalPorId(animalId)
        supabaseId = repository.buscarSupabaseId(animalId)
    }

    var showDialogEvento by remember { mutableStateOf(false) }

    // ── Estado NFC ────────────────────────────────────────────────────────────
    var showNfcDialog by remember { mutableStateOf(false) }
    var nfcStatus by remember { mutableStateOf<NfcStatus>(NfcStatus.Aguardando) }

    // Ativa/desativa NFC reader mode junto com o dialog
    DisposableEffect(showNfcDialog) {
        if (showNfcDialog && activity != null && nfcAdapter != null) {
            nfcAdapter.enableReaderMode(
                activity,
                { tag: Tag ->
                    val sid = supabaseId
                    if (sid == null) {
                        Handler(Looper.getMainLooper()).post {
                            nfcStatus = NfcStatus.SemSync
                        }
                        return@enableReaderMode
                    }
                    val url = NfcHelper.buildAnimalUrl(sid)
                    val ok = NfcHelper.escreverUrl(tag, url)
                    Handler(Looper.getMainLooper()).post {
                        nfcStatus = if (ok) NfcStatus.Sucesso(url) else NfcStatus.Erro
                    }
                },
                NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_NFC_B or
                NfcAdapter.FLAG_READER_NFC_F or NfcAdapter.FLAG_READER_NFC_V,
                null
            )
        }
        onDispose {
            if (activity != null && nfcAdapter != null) {
                try { nfcAdapter.disableReaderMode(activity) } catch (_: Exception) {}
            }
        }
    }

    Scaffold(
        topBar = {
            GenoTopBar(
                titulo = animal?.nome ?: "Detalhes",
                showBack = true,
                onBack = onVoltar,
                actions = {
                    IconButton(onClick = { showDialogEvento = true }) {
                        Icon(Icons.Default.AddCircleOutline, "Evento", tint = GenoGreen800)
                    }
                    var showMenu by remember { mutableStateOf(false) }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, "Menu", tint = GenoGreen800)
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Editar") },
                            onClick = {
                                showMenu = false
                                animal?.let { onEditar(it.id) }
                            },
                            leadingIcon = { Icon(Icons.Default.Edit, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Excluir", color = Color.Red) },
                            onClick = {
                                showMenu = false
                                animal?.let {
                                    scope.launch {
                                        repository.deletarAnimal(it.id)
                                        onVoltar()
                                    }
                                }
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, null, tint = Color.Red) }
                        )
                    }
                }
            )
        },
        containerColor = GenoGray50
    ) { padding ->
        val currentAnimal: Animal? = animal
        if (currentAnimal == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GenoGreen800)
            }
        } else {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = GenoWhite),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(GenoGreen100),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(currentAnimal.especie.emoji, fontSize = 40.sp)
                        }
                        Spacer(Modifier.width(20.dp))
                        Column {
                            Text(
                                currentAnimal.nome,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = GenoGray900
                            )
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (currentAnimal.sexo == Sexo.FEMEA)
                                        Icons.Default.Female else Icons.Default.Male,
                                    contentDescription = null,
                                    tint = if (currentAnimal.sexo == Sexo.FEMEA) GenoRed else GenoBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    currentAnimal.sexo.label,
                                    fontSize = 14.sp,
                                    color = GenoGray600
                                )
                            }
                        }
                    }
                }

                // Info Sections based on the Image requirements
                InfoSection(titulo = "Informações da Unidade") {
                    InfoItem("Espécie", currentAnimal.especie.label)
                    InfoItem("Raça", currentAnimal.raca)
                    InfoItem("Nascimento", currentAnimal.dataNascimento.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                    InfoItem("Idade", idadeAnimal(currentAnimal.dataNascimento))
                    InfoItem("Peso", "${currentAnimal.pesoKg} kg")
                    InfoItem("Fazenda", currentAnimal.fazenda)
                }

                if (currentAnimal.sexo == Sexo.FEMEA) {
                    InfoSection(titulo = "Histórico da Matriz") {
                        InfoItem("Escore Corporal (ECC)", currentAnimal.escoreCorporal.toString())
                        InfoItem("Número de Partos", currentAnimal.numeroPartos.toString())
                        InfoItem("Abortos", currentAnimal.abortos.toString())
                        InfoItem("Dias pós-parto", currentAnimal.diasDesdeUltimoParto.toString())
                        InfoItem("Filhos nascidos", currentAnimal.filhosNascidosMatriz.toString())
                    }
                } else {
                    InfoSection(titulo = "Histórico do Reprodutor") {
                        InfoItem("Qualidade Seminal", currentAnimal.qualidadeSemenMacho.toString())
                        InfoItem("Filhos nascidos", currentAnimal.filhosNascidosMacho.toString())
                    }
                }

                if (currentAnimal.nomePai.isNotEmpty() || currentAnimal.nomeMae.isNotEmpty()) {
                    InfoSection(titulo = "Pedigree") {
                        if (currentAnimal.nomePai.isNotEmpty()) {
                            InfoItem("Pai", currentAnimal.nomePai)
                            if (currentAnimal.racaPai.isNotEmpty()) InfoItem("Raça do Pai", currentAnimal.racaPai)
                            if (currentAnimal.rfidPai.isNotEmpty()) InfoItem("Registro/RFID Pai", currentAnimal.rfidPai)
                        }
                        if (currentAnimal.nomeMae.isNotEmpty()) {
                            InfoItem("Mãe", currentAnimal.nomeMae)
                            if (currentAnimal.racaMae.isNotEmpty()) InfoItem("Raça da Mãe", currentAnimal.racaMae)
                            if (currentAnimal.rfidMae.isNotEmpty()) InfoItem("Registro/RFID Mãe", currentAnimal.rfidMae)
                        }
                    }
                }

                // ── Botão NFC ─────────────────────────────────────────────────
                Button(
                    onClick = {
                        nfcStatus = NfcStatus.Aguardando
                        showNfcDialog = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (nfcAdapter != null) GenoGreen800 else GenoGray200,
                        contentColor = Color.White
                    ),
                    enabled = nfcAdapter != null
                ) {
                    Icon(Icons.Default.Nfc, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (nfcAdapter != null) "Cadastrar Tag NFC"
                        else "NFC não disponível neste dispositivo",
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }

    if (showDialogEvento) {
        DialogNovoEvento(onDismiss = { showDialogEvento = false })
    }

    // ── Dialog NFC ────────────────────────────────────────────────────────────
    if (showNfcDialog) {
        AlertDialog(
            onDismissRequest = {
                showNfcDialog = false
                nfcStatus = NfcStatus.Aguardando
            },
            icon = { Icon(Icons.Default.Nfc, null, tint = GenoGreen800, modifier = Modifier.size(36.dp)) },
            title = {
                Text(
                    when (nfcStatus) {
                        is NfcStatus.Aguardando -> "Cadastrar Tag NFC"
                        is NfcStatus.Sucesso    -> "Tag gravada!"
                        is NfcStatus.Erro       -> "Erro na gravação"
                        is NfcStatus.SemSync    -> "Animal não sincronizado"
                    },
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    when (val s = nfcStatus) {
                        is NfcStatus.Aguardando -> {
                            CircularProgressIndicator(color = GenoGreen800, modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "Aproxime o celular da tag NFC do animal",
                                textAlign = TextAlign.Center,
                                color = GenoGray600,
                                fontSize = 14.sp
                            )
                        }
                        is NfcStatus.Sucesso -> {
                            Icon(Icons.Default.CheckCircle, null, tint = GenoGreen800, modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(12.dp))
                            Text("URL gravada com sucesso!", textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            Text(s.url, textAlign = TextAlign.Center, fontSize = 12.sp, color = GenoGray600)
                        }
                        is NfcStatus.Erro -> {
                            Icon(Icons.Default.Error, null, tint = GenoRed, modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "Não foi possível gravar a tag.\nVerifique se a tag é gravável e tente novamente.",
                                textAlign = TextAlign.Center,
                                color = GenoGray600,
                                fontSize = 14.sp
                            )
                        }
                        is NfcStatus.SemSync -> {
                            Icon(Icons.Default.CloudOff, null, tint = GenoAmber, modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "Animal ainda não sincronizado com o servidor.\nAguarde a sincronização e tente novamente.",
                                textAlign = TextAlign.Center,
                                color = GenoGray600,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showNfcDialog = false
                    nfcStatus = NfcStatus.Aguardando
                }) {
                    Text(if (nfcStatus is NfcStatus.Aguardando) "Cancelar" else "Fechar", color = GenoGreen800)
                }
            }
        )
    }
}

@Composable
fun InfoSection(titulo: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            titulo,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = GenoGreen800,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = GenoWhite),
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}

@Composable
fun InfoItem(label: String, valor: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 14.sp, color = GenoGray500)
        Text(valor, fontSize = 14.sp, color = GenoGray900, fontWeight = FontWeight.Medium)
    }
}

val GenoGray500 = Color(0xFF9AA0A6)

// ── NFC status ────────────────────────────────────────────────────────────────

sealed class NfcStatus {
    object Aguardando : NfcStatus()
    data class Sucesso(val url: String) : NfcStatus()
    object Erro : NfcStatus()
    object SemSync : NfcStatus()
}

// ── Helpers ───────────────────────────────────────────────────────────────────

fun idadeAnimal(dataNascimento: java.time.LocalDate): String {
    val hoje = java.time.LocalDate.now()
    val anos = java.time.temporal.ChronoUnit.YEARS.between(dataNascimento, hoje)
    val meses = java.time.temporal.ChronoUnit.MONTHS.between(dataNascimento.plusYears(anos), hoje)
    return if (anos > 0) "$anos ano${if (anos > 1) "s" else ""} e $meses mês${if (meses != 1L) "es" else ""}"
    else "$meses mês${if (meses != 1L) "es" else ""}"
}
