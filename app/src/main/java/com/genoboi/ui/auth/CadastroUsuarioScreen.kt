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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.genoboi.data.local.AppDatabase
import com.genoboi.data.remote.AuthRepository
import com.genoboi.ui.theme.*
import kotlinx.coroutines.launch

private val ESTADOS = listOf(
    "AC","AL","AP","AM","BA","CE","DF","ES","GO","MA",
    "MT","MS","MG","PA","PB","PR","PE","PI","RJ","RN",
    "RS","RO","RR","SC","SP","SE","TO"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CadastroUsuarioScreen(
    onVoltar: () -> Unit,
    onCadastrado: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val authRepo = remember { AuthRepository(context, db.produtorDao()) }
    val scope = rememberCoroutineScope()

    var nome by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var confirmarSenha by remember { mutableStateOf("") }
    var nomeFazenda by remember { mutableStateOf("") }
    var municipio by remember { mutableStateOf("") }
    var estado by remember { mutableStateOf("CE") }

    var mostrarSenha by remember { mutableStateOf(false) }
    var mostrarConfirmar by remember { mutableStateOf(false) }
    var expandidoEstado by remember { mutableStateOf(false) }
    var carregando by remember { mutableStateOf(false) }
    var erroMsg by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Criar Conta", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Cadastre-se para acessar o GENIA", fontSize = 12.sp, color = GenoGray600)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(Icons.Default.ArrowBack, "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GenoWhite)
            )
        },
        containerColor = GenoGray50
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Seção: Dados de acesso
            SectionLabel("Dados de Acesso")

            CadastroField(
                label = "Nome completo *",
                valor = nome,
                icon  = Icons.Default.Person,
                onChange = { nome = it; erroMsg = null }
            )

            CadastroField(
                label   = "E-mail *",
                valor   = email,
                icon    = Icons.Default.Email,
                teclado = KeyboardType.Email,
                onChange = { email = it; erroMsg = null }
            )

            OutlinedTextField(
                value         = senha,
                onValueChange = { senha = it; erroMsg = null },
                label         = { Text("Senha * (mínimo 6 caracteres)", fontSize = 13.sp) },
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth(),
                leadingIcon   = { Icon(Icons.Default.Lock, null, tint = GenoGreen700) },
                trailingIcon  = {
                    IconButton(onClick = { mostrarSenha = !mostrarSenha }) {
                        Icon(
                            if (mostrarSenha) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            null, tint = GenoGray400
                        )
                    }
                },
                visualTransformation = if (mostrarSenha) VisualTransformation.None
                                       else PasswordVisualTransformation(),
                shape  = RoundedCornerShape(12.dp),
                colors = cadFieldColors()
            )

            OutlinedTextField(
                value         = confirmarSenha,
                onValueChange = { confirmarSenha = it; erroMsg = null },
                label         = { Text("Confirmar senha *", fontSize = 13.sp) },
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth(),
                leadingIcon   = { Icon(Icons.Default.LockOpen, null, tint = GenoGreen700) },
                trailingIcon  = {
                    IconButton(onClick = { mostrarConfirmar = !mostrarConfirmar }) {
                        Icon(
                            if (mostrarConfirmar) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            null, tint = GenoGray400
                        )
                    }
                },
                visualTransformation = if (mostrarConfirmar) VisualTransformation.None
                                       else PasswordVisualTransformation(),
                isError = confirmarSenha.isNotEmpty() && senha != confirmarSenha,
                shape   = RoundedCornerShape(12.dp),
                colors  = cadFieldColors()
            )

            // Seção: Dados da fazenda
            SectionLabel("Dados da Fazenda")

            CadastroField(
                label    = "Nome da fazenda",
                valor    = nomeFazenda,
                icon     = Icons.Default.Home,
                onChange = { nomeFazenda = it }
            )

            CadastroField(
                label    = "Município *",
                valor    = municipio,
                icon     = Icons.Default.LocationCity,
                onChange = { municipio = it; erroMsg = null }
            )

            // Estado dropdown
            ExposedDropdownMenuBox(
                expanded        = expandidoEstado,
                onExpandedChange = { expandidoEstado = !expandidoEstado }
            ) {
                OutlinedTextField(
                    value         = estado,
                    onValueChange = {},
                    label         = { Text("Estado *", fontSize = 13.sp) },
                    readOnly      = true,
                    leadingIcon   = { Icon(Icons.Default.Map, null, tint = GenoGreen700) },
                    trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandidoEstado) },
                    modifier      = Modifier.fillMaxWidth().menuAnchor(),
                    shape         = RoundedCornerShape(12.dp),
                    colors        = cadFieldColors()
                )
                ExposedDropdownMenu(
                    expanded        = expandidoEstado,
                    onDismissRequest = { expandidoEstado = false }
                ) {
                    ESTADOS.forEach { uf ->
                        DropdownMenuItem(
                            text    = { Text(uf) },
                            onClick = { estado = uf; expandidoEstado = false }
                        )
                    }
                }
            }

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

            // Botão cadastrar
            Button(
                onClick = {
                    when {
                        nome.isBlank()      -> { erroMsg = "Informe seu nome."; return@Button }
                        email.isBlank()     -> { erroMsg = "Informe seu e-mail."; return@Button }
                        senha.length < 6    -> { erroMsg = "A senha deve ter pelo menos 6 caracteres."; return@Button }
                        senha != confirmarSenha -> { erroMsg = "As senhas não coincidem."; return@Button }
                        municipio.isBlank() -> { erroMsg = "Informe o município."; return@Button }
                    }
                    carregando = true
                    erroMsg = null
                    scope.launch {
                        val result = authRepo.cadastrar(
                            email       = email.trim(),
                            senha       = senha,
                            nome        = nome.trim(),
                            nomeFazenda = nomeFazenda.trim(),
                            municipio   = municipio.trim(),
                            estado      = estado
                        )
                        carregando = false
                        result.fold(
                            onSuccess = { onCadastrado() },
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
                        color       = GenoWhite,
                        modifier    = Modifier.size(22.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.PersonAdd, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("CRIAR CONTA", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SectionLabel(texto: String) {
    Text(
        texto,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 14.sp,
        color      = GenoGreen800,
        modifier   = Modifier.padding(top = 4.dp, bottom = 2.dp)
    )
    HorizontalDivider(color = GenoGreen100)
}

@Composable
private fun CadastroField(
    label: String,
    valor: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    teclado: KeyboardType = KeyboardType.Text,
    onChange: (String) -> Unit
) {
    OutlinedTextField(
        value           = valor,
        onValueChange   = onChange,
        label           = { Text(label, fontSize = 13.sp) },
        singleLine      = true,
        modifier        = Modifier.fillMaxWidth(),
        leadingIcon     = { Icon(icon, null, tint = GenoGreen700) },
        shape           = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(keyboardType = teclado),
        colors          = cadFieldColors()
    )
}

@Composable
private fun cadFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor      = GenoGreen700,
    unfocusedBorderColor    = GenoGray200,
    focusedContainerColor   = GenoWhite,
    unfocusedContainerColor = GenoWhite,
    focusedLabelColor       = GenoGreen700
)
