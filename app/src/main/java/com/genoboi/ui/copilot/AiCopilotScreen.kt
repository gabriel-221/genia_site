package com.genoboi.ui.copilot

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.genoboi.data.remote.AiCopilotRepository
import com.genoboi.data.repository.AnimalRepository
import com.genoboi.domain.model.Animal
import com.genoboi.ui.theme.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class ChatMessage(
    val role: String,
    val content: String,
    val isLoading: Boolean = false
)

@Composable
fun AiCopilotScreen(repository: AnimalRepository) {
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val copilotRepo = remember { AiCopilotRepository() }

    var mensagem by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var animais by remember { mutableStateOf<List<Animal>>(emptyList()) }
    val historico = remember { mutableStateListOf<ChatMessage>() }

    LaunchedEffect(Unit) {
        animais = repository.observarAnimais().first()
        historico.add(
            ChatMessage(
                role = "assistant",
                content = "Olá! Sou o GENIA Copilot, seu assistente de manejo genético e reprodutivo.\n\nIdentifiquei ${animais.size} animais no seu rebanho. Como posso ajudar você hoje?"
            )
        )
    }

    LaunchedEffect(historico.size) {
        if (historico.isNotEmpty()) {
            listState.animateScrollToItem(historico.size - 1)
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(GenoGray50)
    ) {
        // Header
        Surface(shadowElevation = 2.dp) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(GenoGreen800)
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(40.dp)
                        .background(GenoGreen700, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AutoAwesome, null, tint = Color.White, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("GENIA Copilot", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color.White)
                    Text("IA com dados do seu rebanho", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                }
                Spacer(Modifier.weight(1f))
                Surface(shape = CircleShape, color = GenoGreen600) {
                    Text(
                        "${animais.size} animais",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Sugestões rápidas (só quando chat vazio)
        if (historico.size <= 1) {
            val sugestoes = listOf(
                "Qual é a situação atual do meu rebanho?",
                "Quais animais têm maior potencial genético para leite?",
                "Como melhorar a taxa de prenhez?",
                "Dicas para manejo reprodutivo de bovinos"
            )
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    if (historico.isNotEmpty()) {
                        AssistantBubble(historico.first())
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Sugestões para começar:",
                            fontSize = 13.sp,
                            color = GenoGray600,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                }
                items(sugestoes) { sugestao ->
                    SugestaoChip(sugestao) {
                        mensagem = sugestao
                        scope.launch { enviar(mensagem, historico, animais, copilotRepo) { mensagem = ""; isLoading = it } }
                    }
                }
            }
        } else {
            // Chat messages
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(historico) { msg ->
                    when {
                        msg.isLoading -> LoadingBubble()
                        msg.role == "user" -> UserBubble(msg)
                        else -> AssistantBubble(msg)
                    }
                }
            }
        }

        // Input bar
        Surface(shadowElevation = 8.dp) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(GenoWhite)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = mensagem,
                    onValueChange = { mensagem = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Pergunte sobre seu rebanho...", fontSize = 14.sp, color = GenoGray400) },
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 3,
                    enabled = !isLoading,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = {
                        if (mensagem.isNotBlank() && !isLoading) {
                            scope.launch {
                                enviar(mensagem, historico, animais, copilotRepo) { mensagem = ""; isLoading = it }
                            }
                        }
                    }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GenoGreen700,
                        unfocusedBorderColor = GenoGray200,
                        focusedContainerColor = GenoGray50,
                        unfocusedContainerColor = GenoGray50
                    )
                )
                Spacer(Modifier.width(8.dp))
                AnimatedVisibility(visible = mensagem.isNotBlank() && !isLoading) {
                    FilledIconButton(
                        onClick = {
                            scope.launch {
                                enviar(mensagem, historico, animais, copilotRepo) { mensagem = ""; isLoading = it }
                            }
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = GenoGreen800)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, null, tint = Color.White)
                    }
                }
                AnimatedVisibility(visible = isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(40.dp).padding(8.dp),
                        color = GenoGreen800,
                        strokeWidth = 2.dp
                    )
                }
            }
        }
    }
}

private suspend fun enviar(
    texto: String,
    historico: MutableList<ChatMessage>,
    animais: List<Animal>,
    repo: AiCopilotRepository,
    onStateChange: (Boolean) -> Unit
) {
    if (texto.isBlank()) return
    onStateChange(true)

    historico.add(ChatMessage(role = "user", content = texto))
    historico.add(ChatMessage(role = "assistant", content = "", isLoading = true))

    val msgParaApi = historico
        .filter { !it.isLoading && it.content.isNotBlank() }
        .dropLast(1)
        .map { it.role to it.content }

    val result = repo.enviarMensagem(texto, msgParaApi, animais)

    historico.removeLastOrNull()

    result.fold(
        onSuccess = { resposta ->
            historico.add(ChatMessage(role = "assistant", content = resposta))
        },
        onFailure = { err ->
            historico.add(ChatMessage(role = "assistant", content = "Desculpe, ocorreu um erro: ${err.message}. Verifique sua conexão e tente novamente."))
        }
    )
    onStateChange(false)
}

@Composable
fun UserBubble(msg: ChatMessage) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        Box(
            Modifier
                .widthIn(max = 280.dp)
                .clip(RoundedCornerShape(18.dp, 4.dp, 18.dp, 18.dp))
                .background(GenoGreen800)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text(msg.content, color = Color.White, fontSize = 14.sp, lineHeight = 20.sp)
        }
    }
}

@Composable
fun AssistantBubble(msg: ChatMessage) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.Top) {
        Box(
            Modifier
                .size(32.dp)
                .background(GenoGreen100, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.AutoAwesome, null, tint = GenoGreen800, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(8.dp))
        Box(
            Modifier
                .widthIn(max = 280.dp)
                .clip(RoundedCornerShape(4.dp, 18.dp, 18.dp, 18.dp))
                .background(GenoWhite)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text(
                msg.content,
                color = GenoGray900,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun LoadingBubble() {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(32.dp).background(GenoGreen100, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.AutoAwesome, null, tint = GenoGreen800, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(8.dp))
        Box(
            Modifier
                .clip(RoundedCornerShape(4.dp, 18.dp, 18.dp, 18.dp))
                .background(GenoWhite)
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = GenoGreen700, strokeWidth = 2.dp)
        }
    }
}

@Composable
fun SugestaoChip(texto: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, GenoGray200),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = GenoWhite)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Lightbulb, null, tint = GenoGreen700, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text(texto, fontSize = 13.sp, color = GenoGray800, modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, null, tint = GenoGray400, modifier = Modifier.size(16.dp))
        }
    }
}
