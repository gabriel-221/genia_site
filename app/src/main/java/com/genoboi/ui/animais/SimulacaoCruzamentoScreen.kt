package com.genoboi.ui.animais

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.genoboi.data.repository.AnimalRepository
import com.genoboi.domain.model.Animal
import com.genoboi.domain.model.Sexo
import com.genoboi.ui.components.GenoTopBar
import com.genoboi.ui.theme.*
import kotlin.random.Random

@Composable
fun SelecaoMatrizScreen(
    repository: AnimalRepository,
    onMatrizSelecionada: (Long) -> Unit,
    onVoltar: () -> Unit
) {
    val animais by repository.observarAnimais().collectAsState(initial = emptyList())
    val matrizes = animais.filter { it.sexo == Sexo.FEMEA }

    Scaffold(
        topBar = {
            GenoTopBar(titulo = "Selecione a Matriz", showBack = true, onBack = onVoltar)
        },
        containerColor = GenoGray50
    ) { padding ->
        if (matrizes.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Nenhuma matriz cadastrada.", color = GenoGray600)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(matrizes) { matriz ->
                    AnimalSelectionCard(matriz) { onMatrizSelecionada(matriz.id) }
                }
            }
        }
    }
}

@Composable
fun SelecaoMachoScreen(
    matrizId: Long,
    repository: AnimalRepository,
    onMachoSelecionado: (Long, Long) -> Unit,
    onVoltar: () -> Unit
) {
    val animais by repository.observarAnimais().collectAsState(initial = emptyList())
    val machos = animais.filter { it.sexo == Sexo.MACHO }

    Scaffold(
        topBar = {
            GenoTopBar(titulo = "Selecione o Reprodutor", showBack = true, onBack = onVoltar)
        },
        containerColor = GenoGray50
    ) { padding ->
        if (machos.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Nenhum reprodutor cadastrado.", color = GenoGray600)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(machos) { macho ->
                    AnimalSelectionCard(macho) { onMachoSelecionado(matrizId, macho.id) }
                }
            }
        }
    }
}

@Composable
fun ResultadoSimulacaoScreen(
    matrizId: Long,
    machoId: Long,
    repository: AnimalRepository,
    onFinalizar: () -> Unit
) {
    var matriz by remember { mutableStateOf<Animal?>(null) }
    var macho by remember { mutableStateOf<Animal?>(null) }
    var resultado by remember { mutableStateOf<com.genoboi.domain.model.ResultadoPrenhez?>(null) }
    var erroMsg by remember { mutableStateOf<String?>(null) }
    var calculando by remember { mutableStateOf(true) }

    LaunchedEffect(matrizId, machoId) {
        calculando = true
        try {
            val mat = repository.buscarAnimalPorId(matrizId)
            val mac = repository.buscarAnimalPorId(machoId)
            matriz = mat
            macho = mac
            
            if (mat != null && mac != null) {
                val res = repository.simularPrenhez(mat, mac)
                resultado = res
                if (res != null) {
                    // Salva o resultado da simulação no perfil da fêmea para o relatório
                    repository.atualizarAnimal(mat.copy(chancePrenhezGerada = res.probabilidade))
                } else {
                    erroMsg = "Falha na leitura do modelo."
                }
            } else {
                erroMsg = "Animal não encontrado."
            }
        } catch (e: Exception) {
            erroMsg = "Erro: ${e.message}"
        } finally {
            calculando = false
        }
    }

    Scaffold(
        topBar = {
            GenoTopBar(titulo = "Previsão de Prenhez", showBack = true, onBack = onFinalizar)
        },
        containerColor = GenoGray50
    ) { padding ->
        if (calculando) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GenoGreen800)
            }
        } else if (erroMsg != null) {
            Column(
                Modifier.fillMaxSize().padding(padding).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.Error, null, tint = GenoRed, modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(16.dp))
                Text(erroMsg!!, textAlign = TextAlign.Center, color = GenoGray900)
                Spacer(Modifier.height(24.dp))
                Button(onClick = onFinalizar, colors = ButtonDefaults.buttonColors(containerColor = GenoGreen800)) {
                    Text("Tentar novamente", color = Color.White)
                }
            }
        } else {
            val res = resultado
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Taxa de Prenhez Estimada", fontSize = 16.sp, color = GenoGray600)
                Spacer(Modifier.height(16.dp))
                
                if (res != null) {
                    val percentual = res.percentual
                    val cor = when {
                        percentual >= 70 -> GenoGreen800
                        percentual >= 50 -> GenoAmber
                        else -> GenoRed
                    }
                    val label = when {
                        percentual >= 70 -> "Alta chance"
                        percentual >= 50 -> "Chance moderada"
                        else -> "Baixa chance"
                    }

                    Text(
                        "$percentual%",
                        fontSize = 72.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = cor
                    )
                    Text(
                        label,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = cor
                    )
                }
                
                Spacer(Modifier.height(32.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = GenoWhite),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("♀ Matriz: ", fontWeight = FontWeight.Bold, color = GenoGray600)
                            Text(matriz?.nome ?: "", fontWeight = FontWeight.Bold, color = GenoGray900)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("♂ Reprodutor: ", fontWeight = FontWeight.Bold, color = GenoGray600)
                            Text(macho?.nome ?: "", fontWeight = FontWeight.Bold, color = GenoGray900)
                        }
                        
                        HorizontalDivider(color = GenoGray100)
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, null, tint = GenoBlue, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Esta estimativa baseia-se no histórico reprodutivo e qualidade seminal dos animais selecionados via IA.",
                                fontSize = 12.sp,
                                color = GenoGray600
                            )
                        }
                    }
                }
                
                Spacer(Modifier.height(48.dp))
                
                Button(
                    onClick = onFinalizar,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GenoGreen800, contentColor = Color.White)
                ) {
                    Text("CONCLUIR", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AnimalSelectionCard(animal: Animal, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = GenoWhite),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(48.dp).background(GenoGreen100, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(animal.especie.emoji, fontSize = 24.sp)
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(animal.nome, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("${animal.raca} • ${animal.fazenda}", fontSize = 13.sp, color = GenoGray600)
            }
            Spacer(Modifier.weight(1f))
            Icon(Icons.Default.AddCircleOutline, null, tint = GenoGreen700)
        }
    }
}
