package com.genoboi.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.genoboi.data.local.AppDatabase
import com.genoboi.data.remote.AuthRepository
import com.genoboi.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSucesso: () -> Unit,
    onCriarConta: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val authRepo = remember { AuthRepository(context, db.produtorDao()) }
    val scope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var mostrarSenha by remember { mutableStateOf(false) }
    var carregando by remember { mutableStateOf(false) }
    var erroMsg by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(GenoGreen900, GenoGreen700, GenoGreen500)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.height(48.dp))

            // Logo
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "GENIA",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize   = 42.sp,
                    color      = GenoWhite,
                    letterSpacing = 4.sp
                )
                Spacer(Modifier.width(6.dp))
                Icon(
                    Icons.Default.Grass,
                    contentDescription = null,
                    tint     = Color(0xFF81C784),
                    modifier = Modifier.size(36.dp)
                )
            }
            Text(
                "Genética Inteligente para o Agro",
                fontSize  = 14.sp,
                color     = Color(0xFFB9F6CA),
                modifier  = Modifier.padding(top = 4.dp),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(48.dp))

            // Card do formulário
            Card(
                modifier  = Modifier.fillMaxWidth(),
                shape     = RoundedCornerShape(24.dp),
                colors    = CardDefaults.cardColors(containerColor = GenoWhite),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Entrar",
                        fontWeight = FontWeight.Bold,
                        fontSize   = 22.sp,
                        color      = GenoGray900
                    )
                    Text(
                        "Acesse sua conta para gerenciar seu rebanho",
                        fontSize = 13.sp,
                        color    = GenoGray600
                    )

                    // E-mail
                    OutlinedTextField(
                        value         = email,
                        onValueChange = { email = it; erroMsg = null },
                        label         = { Text("E-mail") },
                        singleLine    = true,
                        modifier      = Modifier.fillMaxWidth(),
                        leadingIcon   = { Icon(Icons.Default.Email, null, tint = GenoGreen700) },
                        shape         = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors        = authFieldColors()
                    )

                    // Senha
                    OutlinedTextField(
                        value         = senha,
                        onValueChange = { senha = it; erroMsg = null },
                        label         = { Text("Senha") },
                        singleLine    = true,
                        modifier      = Modifier.fillMaxWidth(),
                        leadingIcon   = { Icon(Icons.Default.Lock, null, tint = GenoGreen700) },
                        trailingIcon  = {
                            IconButton(onClick = { mostrarSenha = !mostrarSenha }) {
                                Icon(
                                    if (mostrarSenha) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = GenoGray400
                                )
                            }
                        },
                        visualTransformation = if (mostrarSenha) VisualTransformation.None
                                               else PasswordVisualTransformation(),
                        shape  = RoundedCornerShape(12.dp),
                        colors = authFieldColors()
                    )

                    // Erro
                    if (erroMsg != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(GenoRed50, RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Icon(Icons.Default.ErrorOutline, null, tint = GenoRed, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(erroMsg!!, fontSize = 13.sp, color = GenoRed)
                        }
                    }

                    // Botão entrar
                    Button(
                        onClick = {
                            if (email.isBlank() || senha.isBlank()) {
                                erroMsg = "Preencha e-mail e senha."
                                return@Button
                            }
                            carregando = true
                            erroMsg = null
                            scope.launch {
                                val result = authRepo.login(email.trim(), senha)
                                carregando = false
                                result.fold(
                                    onSuccess = { onLoginSucesso() },
                                    onFailure = { erroMsg = it.message }
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape  = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GenoGreen800,
                            contentColor   = GenoWhite
                        ),
                        enabled = !carregando
                    ) {
                        if (carregando) {
                            CircularProgressIndicator(
                                color    = GenoWhite,
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Login, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("ENTRAR", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Criar conta
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Primeiro acesso? ", color = GenoWhite, fontSize = 14.sp)
                TextButton(
                    onClick        = onCriarConta,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        "Criar conta",
                        color      = Color(0xFF80CBC4),
                        fontWeight = FontWeight.Bold,
                        fontSize   = 14.sp
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun authFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor      = GenoGreen700,
    unfocusedBorderColor    = GenoGray200,
    focusedContainerColor   = GenoWhite,
    unfocusedContainerColor = GenoWhite,
    focusedLabelColor       = GenoGreen700
)
