package com.genoboi.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.genoboi.domain.model.TipoAlerta
import com.genoboi.ui.navigation.Screen
import com.genoboi.ui.theme.*

// ─── Top App Bar ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenoTopBar(
    titulo: String = "",
    subtitulo: String = "",
    showBack: Boolean = false,
    onBack: () -> Unit = {},
    badgeCount: Int = 0,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            if (titulo.isEmpty()) {
                // Logo estilo GENIA
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text       = "GENIA",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize   = 22.sp,
                        color      = GenoGreen800
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        imageVector        = Icons.Default.Grass,
                        contentDescription = null,
                        tint               = GenoGreen600,
                        modifier           = Modifier.size(20.dp)
                    )
                    if (subtitulo.isNotEmpty()) {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text     = subtitulo,
                            fontSize = 13.sp,
                            color    = GenoGray600,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                Column {
                    Text(titulo, fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
                    if (subtitulo.isNotEmpty())
                        Text(subtitulo, fontSize = 12.sp, color = GenoGray400)
                }
            }
        },
        navigationIcon = {
            if (showBack) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                }
            }
        },
        actions = {
            if (badgeCount > 0) {
                BadgedBox(badge = {
                    Badge { Text(badgeCount.toString()) }
                }) {
                    Icon(Icons.Default.Notifications, contentDescription = "Alertas")
                }
                Spacer(Modifier.width(12.dp))
            }
            actions()
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = GenoWhite
        )
    )
}

// ─── Bottom Navigation Bar ────────────────────────────────────────────────────

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector,
    val iconSelected: ImageVector = icon
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Dashboard,  "Dashboard", Icons.Default.Home),
    BottomNavItem(Screen.Animais,    "Animais",   Icons.Default.Pets),
    BottomNavItem(Screen.Match,      "Match",     Icons.Default.Favorite),
    BottomNavItem(Screen.Calendario, "Calendário",Icons.Default.CalendarMonth),
)

@Composable
fun GenoBottomBar(
    currentRoute: String?,
    onNavigate: (Screen) -> Unit
) {
    NavigationBar(
        containerColor = GenoWhite,
        tonalElevation = 0.dp,
        modifier        = Modifier.border(1.dp, GenoGray200, shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
    ) {
        bottomNavItems.forEach { item ->
            val selected = currentRoute == item.screen.route
            NavigationBarItem(
                selected = selected,
                onClick  = { onNavigate(item.screen) },
                icon     = {
                    Icon(
                        imageVector        = item.icon,
                        contentDescription = item.label,
                        modifier           = Modifier.size(22.dp)
                    )
                },
                label    = { Text(item.label, fontSize = 10.sp) },
                colors   = NavigationBarItemDefaults.colors(
                    selectedIconColor   = GenoGreen800,
                    selectedTextColor   = GenoGreen800,
                    unselectedIconColor = GenoGray400,
                    unselectedTextColor = GenoGray400,
                    indicatorColor      = GenoGreen100
                )
            )
        }
    }
}

// ─── Stat Card (dashboard) ────────────────────────────────────────────────────

@Composable
fun StatCard(
    icon: ImageVector,
    valor: String,
    label: String,
    iconTint: Color = GenoGreen800,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape    = RoundedCornerShape(12.dp),
        colors   = CardDefaults.cardColors(containerColor = GenoWhite),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            Modifier.padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(4.dp))
            Text(valor, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = GenoGray900)
            Text(label, fontSize = 11.sp, color = GenoGray600)
        }
    }
}

// ─── Alerta Item ──────────────────────────────────────────────────────────────

@Composable
fun AlertaRow(
    animalNome: String,
    descricao: String,
    tipo: TipoAlerta,
    onClick: () -> Unit = {}
) {
    val (color, icon) = when (tipo) {
        TipoAlerta.CIO        -> Pair(GenoGreen600, Icons.Default.Circle)
        TipoAlerta.INSEMINACAO -> Pair(GenoBlue,    Icons.Default.Vaccines)
        TipoAlerta.PARTO      -> Pair(GenoOrange,   Icons.Default.ChildCare)
        TipoAlerta.DIAGNOSTICO -> Pair(GenoBlue,    Icons.Default.MedicalServices)
        TipoAlerta.GENETICO   -> Pair(GenoRed,      Icons.Default.Warning)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(animalNome, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(descricao,  fontSize = 12.sp, color = GenoGray600)
        }
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
    }
}

// ─── Section Header ───────────────────────────────────────────────────────────

@Composable
fun SectionHeader(
    titulo: String,
    acaoLabel: String = "",
    onAcao: () -> Unit = {}
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(titulo, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = GenoGray900)
        if (acaoLabel.isNotEmpty()) {
            TextButton(onClick = onAcao, contentPadding = PaddingValues(0.dp)) {
                Text(acaoLabel, fontSize = 12.sp, color = GenoGreen700)
            }
        }
    }
}

// ─── Chip de espécie ──────────────────────────────────────────────────────────

@Composable
fun EspecieChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val bg    = if (selected) GenoGreen800 else GenoGray100
    val fg    = if (selected) GenoWhite    else GenoGray600
    Surface(
        shape    = RoundedCornerShape(20.dp),
        color    = bg,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text     = label,
            fontSize = 12.sp,
            color    = fg,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
        )
    }
}

// ─── Tag RFID ─────────────────────────────────────────────────────────────────

@Composable
fun RfidTag(rfid: String, modifier: Modifier = Modifier) {
    Surface(
        shape  = RoundedCornerShape(6.dp),
        color  = GenoGreen100,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(Icons.Default.Nfc, contentDescription = null, tint = GenoGreen700, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text(rfid, fontSize = 11.sp, color = GenoGreen800, fontWeight = FontWeight.Medium)
        }
    }
}

// ─── Probabilidade badge ──────────────────────────────────────────────────────

@Composable
fun ProbabilidadeBadge(valor: Int, modifier: Modifier = Modifier) {
    val color = when {
        valor >= 75 -> GenoGreen700
        valor >= 50 -> GenoAmber
        else        -> GenoRed
    }
    val label = when {
        valor >= 75 -> "Alta"
        valor >= 50 -> "Média"
        else        -> "Baixa"
    }
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Text("$valor%", fontWeight = FontWeight.ExtraBold, fontSize = 32.sp, color = color)
        Spacer(Modifier.width(6.dp))
        Text(label, fontSize = 13.sp, color = color, fontWeight = FontWeight.Medium)
    }
}
