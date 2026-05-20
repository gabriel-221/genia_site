# 🐄 GenoBoi — Android App

Sistema Inteligente de Gestão Genética e Reprodutiva  
Hackathon Expoagro Crateús 2026

---

## 🏗️ Arquitetura

```
Presentation Layer (Compose UI)
    │
    ├── DashboardScreen       → KPIs, gráfico, alertas, recomendação IA
    ├── AnimaisScreen         → Lista com filtros + FAB cadastro
    ├── CadastroAnimalScreen  → Wizard 5 passos (Básico→Pedigree→Rep→IA→Revisão)
    ├── GeneMatchScreen       → Card swipe de reprodutores compatíveis
    └── CalendarioScreen      → Calendário reprodutivo + alertas
         │
Domain Layer (Models puros)
    ├── Animal, EventoReprodutivo, CicloCio
    ├── GeneMatchResult, AlertaItem, DashboardResumo
    └── MockData              → dados mock para dashboard e match
         │
Data Layer
    ├── Room Database (AppDatabase)
    │   ├── AnimalEntity + AnimalDao
    │   ├── EventoReprodutivoEntity + EventoReprodutivoDao
    │   └── CicloCioEntity + CicloCioDao
    ├── Mappers (Entity ↔ Domain)
    └── AnimalRepository      → single source of truth
```

---

## 📦 Stack

| Camada       | Tecnologia                        |
|--------------|-----------------------------------|
| UI           | Jetpack Compose + Material 3      |
| Navegação    | Navigation Compose                |
| Estado       | ViewModel + StateFlow + collectAsState |
| Banco local  | Room 2.6                          |
| Imagens      | Coil                              |
| Build        | Kotlin 1.9 + Gradle 8.2           |

---

## 📁 Estrutura de arquivos

```
app/src/main/java/com/genoboi/
├── MainActivity.kt                    ← Entry point + Scaffold global
│
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt             ← Room database singleton
│   │   ├── dao/Daos.kt                ← AnimalDao, EventoDao, CicloDao
│   │   └── entity/
│   │       ├── Entities.kt            ← @Entity classes para Room
│   │       └── Mappers.kt             ← Entity ↔ Domain conversions
│   └── repository/
│       └── AnimalRepository.kt        ← Abstrai acesso aos DAOs
│
├── domain/
│   └── model/
│       ├── Models.kt                  ← Animal, Evento, enums, etc.
│       └── MockData.kt                ← Dados mock (dashboard, match)
│
└── ui/
    ├── theme/
    │   ├── Color.kt                   ← Paleta verde GenoBoi
    │   └── Theme.kt                   ← MaterialTheme config
    ├── navigation/
    │   └── NavGraph.kt                ← Rotas + NavHost
    ├── components/
    │   └── Components.kt              ← TopBar, BottomBar, cards, chips...
    ├── dashboard/
    │   └── DashboardScreen.kt
    ├── animais/
    │   └── AnimaisScreen.kt
    ├── cadastro/
    │   └── CadastroAnimalScreen.kt    ← Wizard 5 passos
    ├── match/
    │   └── GeneMatchScreen.kt
    └── calendario/
        └── CalendarioScreen.kt
```

---

## 🚀 Como compilar

1. Abra o projeto no **Android Studio Hedgehog** ou superior
2. Sync Gradle (`File → Sync Project with Gradle Files`)
3. Run no emulador ou device Android 8+ (API 26+)

---

## 📋 Estado atual dos dados

| Tela         | Dados               | Fonte        |
|--------------|---------------------|--------------|
| Dashboard    | Resumo + alertas    | MockData.kt  |
| Animais      | Lista de animais    | Room DB      |
| Cadastro     | Formulário 5 passos | Room DB (salva) |
| GeneMatch    | Reprodutores        | MockData.kt  |
| Calendário   | Eventos e alertas   | MockData.kt  |

---

## 🔜 Próximas etapas sugeridas

- [ ] ViewModel para cada tela (separar state management)
- [ ] AnimalDetalheScreen (perfil completo + timeline reprodutiva)
- [ ] IA local com TensorFlow Lite (modelo Random Forest exportado)
- [ ] Sincronização com backend FastAPI
- [ ] Leitura de tag NFC/RFID via Web NFC / Android NFC API
- [ ] Exportação CSV/SGR Embrapa
- [ ] Push notifications para alertas de cio/parto
