package com.divifriends.xyon.pro.ui

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.divifriends.xyon.pro.data.FileAppDataStore
import com.divifriends.xyon.pro.data.model.Group
import com.divifriends.xyon.pro.nfc.NfcController
import com.divifriends.xyon.pro.ui.components.ButtonTone
import com.divifriends.xyon.pro.ui.components.DiviBottomBar
import com.divifriends.xyon.pro.ui.components.DiviConfirmDialog
import com.divifriends.xyon.pro.ui.components.DiviDialog
import com.divifriends.xyon.pro.ui.components.DiviNavItem
import com.divifriends.xyon.pro.ui.components.DiviToastHost
import com.divifriends.xyon.pro.ui.components.GlassButton
import com.divifriends.xyon.pro.ui.components.MoneyConfetti
import com.divifriends.xyon.pro.ui.components.NeonBackground
import com.divifriends.xyon.pro.ui.navigation.Routes
import com.divifriends.xyon.pro.ui.screens.BalancesScreen
import com.divifriends.xyon.pro.ui.screens.CreateGroupScreen
import com.divifriends.xyon.pro.ui.screens.EditGroupScreen
import com.divifriends.xyon.pro.ui.screens.GroupDetailScreen
import com.divifriends.xyon.pro.ui.screens.GroupListScreen
import com.divifriends.xyon.pro.ui.screens.OnboardingDialog
import com.divifriends.xyon.pro.ui.screens.ReportScreen
import com.divifriends.xyon.pro.ui.screens.SettingsScreen
import com.divifriends.xyon.pro.ui.theme.DiviTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

private sealed interface NfcUiState {
    data object EnableRequested : NfcUiState
    data object SelectGroup : NfcUiState
    data class ReadyToSend(val groupName: String) : NfcUiState
    data object WaitingToReceive : NfcUiState
    data object Unavailable : NfcUiState
}

private val NAV_ITEMS = listOf(
    DiviNavItem(Routes.GROUPS, "Grupos", "\uD83E\uDDD1\u200D\uD83E\uDD1D\u200D\uD83E\uDDD1"),
    DiviNavItem(Routes.BALANCES, "Saldos", "\u2696\uFE0F"),
    DiviNavItem(Routes.REPORT, "Informe", "\uD83D\uDCC8"),
    DiviNavItem(Routes.SETTINGS, "Ajustes", "\u2699\uFE0F")
)

@Composable
fun DiviAppRoot(viewModel: DiviViewModel) {
    val appData by viewModel.appData.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var toastMessage by remember { mutableStateOf<String?>(null) }
    var toastKey by remember { mutableIntStateOf(0) }
    var confettiKey by remember { mutableIntStateOf(0) }
    var nfcState by remember { mutableStateOf<NfcUiState?>(null) }
    var pendingCsv by remember { mutableStateOf<String?>(null) }

    fun showToast(message: String) {
        toastMessage = message
        toastKey++
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is UiEvent.Toast -> showToast(event.text)
                UiEvent.MoneyRain -> confettiKey++
            }
        }
    }
    LaunchedEffect(toastKey) {
        if (toastKey > 0) {
            delay(2600)
            toastMessage = null
        }
    }

    val activity = context as? Activity
    val nfcController = remember(activity) { activity?.let { NfcController(it) } }
    DisposableEffect(activity) {
        onDispose { nfcController?.stopReceiving() }
    }
    SideEffect {
        nfcController?.onReceived = { json ->
            nfcController?.stopReceiving()
            nfcState = null
            val group = runCatching {
                FileAppDataStore.json().decodeFromString(Group.serializer(), json)
            }.getOrNull()
            if (group == null) showToast("Error al leer la invitaci\u00f3n. \uD83D\uDE15")
            else viewModel.applyInvitation(group)
        }
        nfcController?.onError = { message ->
            nfcController?.stopReceiving()
            nfcState = null
            showToast(message)
        }
    }

    val backupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            val json = viewModel.backupJson()
            scope.launch {
                val ok = withContext(Dispatchers.IO) {
                    runCatching {
                        context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) }
                    }.isSuccess
                }
                showToast(if (ok) "\u00a1Respaldo guardado con \u00e9xito!" else "Error al guardar el archivo")
            }
        }
    }

    val restoreLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val text = withContext(Dispatchers.IO) {
                    runCatching {
                        context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                    }.getOrNull()
                }
                when {
                    text == null -> showToast("\u274C Error al leer el archivo.")
                    viewModel.restoreFromJson(text) -> showToast("\uD83D\uDD04 \u00a1Datos restaurados!")
                    else -> showToast("\u274C Archivo de respaldo no v\u00e1lido.")
                }
            }
        }
    }

    val csvLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        val content = pendingCsv
        pendingCsv = null
        if (uri != null && content != null) {
            scope.launch {
                val ok = withContext(Dispatchers.IO) {
                    runCatching {
                        context.contentResolver.openOutputStream(uri)?.use { it.write(content.toByteArray()) }
                    }.isSuccess
                }
                showToast(if (ok) "CSV exportado \uD83D\uDCBE" else "Error al exportar CSV")
            }
        }
    }

    fun shareText(text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, "Compartir resumen"))
    }

    fun requestNfcSend() {
        val controller = nfcController
        if (controller == null || !controller.available) {
            showToast("Este dispositivo no dispone de NFC.")
        } else if (!controller.enabled) {
            nfcState = NfcUiState.EnableRequested
        } else if (appData.groups.isEmpty()) {
            showToast("Primero debes crear un grupo para poder invitar.")
        } else {
            nfcState = NfcUiState.SelectGroup
        }
    }

    fun requestNfcReceive() {
        val controller = nfcController
        if (controller == null || !controller.available) {
            showToast("Este dispositivo no dispone de NFC.")
        } else if (!controller.enabled) {
            nfcState = NfcUiState.EnableRequested
        } else {
            controller.startReceiving()
            nfcState = NfcUiState.WaitingToReceive
        }
    }

    DiviTheme(paletteKey = appData.config.theme) {
        NeonBackground {
            DiviNavHost(
                appData = appData,
                viewModel = viewModel,
                onNfcSend = ::requestNfcSend,
                onNfcReceive = ::requestNfcReceive,
                onBackup = {
                    backupLauncher.launch("divifriends_backup_${LocalDate.now()}.json")
                },
                onRestore = { restoreLauncher.launch(arrayOf("application/json", "text/plain", "*/*")) },
                onExportCsv = { group ->
                    pendingCsv = viewModel.exportGroupAsCsv(group)
                    csvLauncher.launch("gastos_${group.name.replace(" ", "_")}.csv")
                },
                onExportText = { group ->
                    shareText(viewModel.exportGroupAsText(group))
                }
            )

            DiviToastHost(
                message = toastMessage,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 110.dp)
            )
            MoneyConfetti(trigger = confettiKey)
        }

        if (appData.config.userName.isBlank()) {
            OnboardingDialog(onConfirm = viewModel::setUserName)
        }

        NfcDialogs(
            state = nfcState,
            groups = appData.groups.values.toList(),
            onDismiss = {
                nfcController?.stopReceiving()
                nfcState = null
            },
            onGoToSettings = {
                nfcController?.openSettings()
                nfcState = null
            },
            onSelectGroup = { group ->
                val payload = Group(id = group.id, name = group.name, people = group.people, expenses = emptyList())
                val json = FileAppDataStore.json().encodeToString(Group.serializer(), payload)
                nfcController?.share(json)
                nfcState = NfcUiState.ReadyToSend(group.name)
            }
        )
    }
}

@Composable
private fun DiviNavHost(
    appData: com.divifriends.xyon.pro.data.model.AppData,
    viewModel: DiviViewModel,
    onNfcSend: () -> Unit,
    onNfcReceive: () -> Unit,
    onBackup: () -> Unit,
    onRestore: () -> Unit,
    onExportCsv: (Group) -> Unit,
    onExportText: (Group) -> Unit
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val route = backStackEntry?.destination?.route ?: Routes.GROUPS
    val showBottomBar = route in Routes.roots

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        NavHost(
            navController = navController,
            startDestination = Routes.GROUPS,
            modifier = Modifier.weight(1f)
        ) {
            composable(Routes.GROUPS) {
                GroupListScreen(
                    appData = appData,
                    onOpenGroup = { navController.navigate(Routes.groupDetail(it)) },
                    onCreateGroup = { navController.navigate(Routes.CREATE) },
                    onNfcSend = onNfcSend,
                    onNfcReceive = onNfcReceive
                )
            }
            composable(Routes.GROUP_DETAIL) { entry ->
                val groupId = entry.arguments?.getString("groupId")
                val group = groupId?.let { appData.groups[it] }
                if (group == null) {
                    LaunchedEffect(Unit) { navController.popBackStack() }
                } else {
                    GroupDetailScreen(
                        group = group,
                        onBack = { navController.popBackStack() },
                        onEdit = { navController.navigate(Routes.edit(group.id)) },
                        onAddExpense = { description, amount, paidBy, participants, category ->
                            viewModel.addExpense(group.id, description, amount, paidBy, participants, category)
                        },
                        onUpdateExpense = { id, description, amount, paidBy, participants, category ->
                            viewModel.updateExpense(group.id, id, description, amount, paidBy, participants, category)
                        },
                        onDeleteExpense = { viewModel.deleteExpense(group.id, it) },
                        onSettle = { from, to, amount -> viewModel.settleDebt(group.id, from, to, amount) },
                        onExportText = { onExportText(group) },
                        onExportCsv = { onExportCsv(group) }
                    )
                }
            }
            composable(Routes.CREATE) {
                CreateGroupScreen(
                    userName = appData.config.userName,
                    onBack = { navController.popBackStack() },
                    onCreated = { navController.popBackStack(Routes.GROUPS, inclusive = false) },
                    onCreate = viewModel::createGroup
                )
            }
            composable(Routes.EDIT) { entry ->
                val groupId = entry.arguments?.getString("groupId")
                val group = groupId?.let { appData.groups[it] }
                if (group == null) {
                    LaunchedEffect(Unit) { navController.popBackStack() }
                } else {
                    EditGroupScreen(
                        group = group,
                        userName = appData.config.userName,
                        onBack = { navController.popBackStack() },
                        onRename = { viewModel.renameGroup(group.id, it) },
                        onAddParticipant = { viewModel.addParticipant(group.id, it) },
                        onRemoveParticipant = { viewModel.removeParticipant(group.id, it) },
                        onDeleteGroup = {
                            viewModel.deleteGroup(group.id)
                            navController.popBackStack(Routes.GROUPS, inclusive = false)
                        }
                    )
                }
            }
            composable(Routes.BALANCES) { BalancesScreen(appData) }
            composable(Routes.REPORT) { ReportScreen(appData) }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    currentTheme = appData.config.theme,
                    onThemeChange = { viewModel.changeTheme(it) },
                    onBackup = onBackup,
                    onRestore = onRestore,
                    onDeleteAll = { viewModel.deleteAllData() }
                )
            }
        }

        if (showBottomBar) {
            DiviBottomBar(
                items = NAV_ITEMS,
                currentRoute = route,
                onSelect = { target ->
                    if (target != route) {
                        navController.navigate(target) {
                            popUpTo(Routes.GROUPS) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun NfcDialogs(
    state: NfcUiState?,
    groups: List<Group>,
    onDismiss: () -> Unit,
    onGoToSettings: () -> Unit,
    onSelectGroup: (Group) -> Unit
) {
    when (state) {
        NfcUiState.EnableRequested -> DiviConfirmDialog(
            title = "Activar NFC",
            message = "El NFC de tu dispositivo est\u00e1 desactivado. \u00bfQuieres ir a los ajustes para activarlo?",
            confirmText = "Ir a Ajustes",
            cancelText = "Ahora no",
            onConfirm = onGoToSettings,
            onDismiss = onDismiss
        )

        NfcUiState.SelectGroup -> DiviDialog(onDismiss = onDismiss) {
            Text(
                text = "Enviar Invitaci\u00f3n NFC",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Selecciona el grupo al que quieres invitar:",
                style = MaterialTheme.typography.bodyMedium,
                color = com.divifriends.xyon.pro.ui.theme.TextSecondary,
                modifier = Modifier.padding(vertical = 12.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                groups.forEach { group ->
                    GlassButton(
                        text = group.name,
                        onClick = { onSelectGroup(group) }
                    )
                }
            }
        }

        is NfcUiState.ReadyToSend -> DiviDialog(onDismiss = onDismiss) {
            Text(
                text = "Listo para Enviar",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Acerca la parte trasera de tu m\u00f3vil a la del otro dispositivo para enviar \"${state.groupName}\".",
                style = MaterialTheme.typography.bodyMedium,
                color = com.divifriends.xyon.pro.ui.theme.TextSecondary,
                modifier = Modifier.padding(vertical = 12.dp)
            )
            Text("\uD83D\uDCF2", style = MaterialTheme.typography.displaySmall)
            GlassButton(
                text = "Cerrar",
                onClick = onDismiss,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        NfcUiState.WaitingToReceive -> DiviDialog(onDismiss = onDismiss) {
            Text(
                text = "Esperando Invitaci\u00f3n",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Acerca tu m\u00f3vil al otro dispositivo para recibir la invitaci\u00f3n por NFC.",
                style = MaterialTheme.typography.bodyMedium,
                color = com.divifriends.xyon.pro.ui.theme.TextSecondary,
                modifier = Modifier.padding(vertical = 12.dp)
            )
            Text("\uD83D\uDCE5", style = MaterialTheme.typography.displaySmall)
            GlassButton(
                text = "Cancelar",
                onClick = onDismiss,
                tone = ButtonTone.Danger,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        NfcUiState.Unavailable -> DiviConfirmDialog(
            title = "NFC no disponible",
            message = "Este dispositivo no dispone de NFC.",
            confirmText = "Entendido",
            cancelText = "Cerrar",
            onConfirm = onDismiss,
            onDismiss = onDismiss
        )

        null -> Unit
    }
}
