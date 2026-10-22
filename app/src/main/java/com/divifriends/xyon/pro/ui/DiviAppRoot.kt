package com.divifriends.xyon.pro.ui

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Contactless
import androidx.compose.material.icons.rounded.GroupAdd
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.Nfc
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.divifriends.xyon.pro.data.FileAppDataStore
import com.divifriends.xyon.pro.data.model.AppData
import com.divifriends.xyon.pro.data.model.Group
import com.divifriends.xyon.pro.nfc.NfcController
import com.divifriends.xyon.pro.ui.components.ActionTile
import com.divifriends.xyon.pro.ui.components.ConfettiBurst
import com.divifriends.xyon.pro.ui.components.DiviBackground
import com.divifriends.xyon.pro.ui.components.DiviConfirmDialog
import com.divifriends.xyon.pro.ui.components.DiviDialog
import com.divifriends.xyon.pro.ui.components.DiviFloatingNav
import com.divifriends.xyon.pro.ui.components.DiviNavItem
import com.divifriends.xyon.pro.ui.components.DiviSheet
import com.divifriends.xyon.pro.ui.components.DiviToastHost
import com.divifriends.xyon.pro.ui.components.PulseRings
import com.divifriends.xyon.pro.ui.components.SoftButton
import com.divifriends.xyon.pro.ui.components.Tone
import com.divifriends.xyon.pro.ui.components.TransactionRow
import com.divifriends.xyon.pro.ui.haptics.DiviHaptics
import com.divifriends.xyon.pro.ui.haptics.LocalHaptics
import com.divifriends.xyon.pro.ui.motion.EmphasizedDecelerate
import com.divifriends.xyon.pro.ui.navigation.Routes
import com.divifriends.xyon.pro.ui.screens.BalancesScreen
import com.divifriends.xyon.pro.ui.screens.CreateGroupScreen
import com.divifriends.xyon.pro.ui.screens.EditGroupScreen
import com.divifriends.xyon.pro.ui.screens.GroupDetailScreen
import com.divifriends.xyon.pro.ui.screens.GroupListScreen
import com.divifriends.xyon.pro.ui.screens.HomeScreen
import com.divifriends.xyon.pro.ui.screens.OnboardingDialog
import com.divifriends.xyon.pro.ui.screens.ReportScreen
import com.divifriends.xyon.pro.ui.screens.SettingsScreen
import com.divifriends.xyon.pro.ui.screens.signatureEmoji
import com.divifriends.xyon.pro.ui.screens.totalSpent
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
}

private sealed interface NavIntent {
    data class OpenGroup(val id: String, val add: Boolean = false) : NavIntent
    data object CreateGroup : NavIntent
}

private val NAV_ITEMS = listOf(
    DiviNavItem(Routes.HOME, "Inicio", Icons.Rounded.Home),
    DiviNavItem(Routes.GROUPS, "Grupos", Icons.Rounded.Groups),
    DiviNavItem(Routes.ADD, "Añadir", Icons.Rounded.Add, isPrimary = true),
    DiviNavItem(Routes.BALANCES, "Saldos", Icons.Rounded.AccountBalanceWallet),
    DiviNavItem(Routes.REPORT, "Informe", Icons.Rounded.Insights)
)

@Composable
fun DiviAppRoot(viewModel: DiviViewModel) {
    val appData by viewModel.appData.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val haptics = remember(context) { DiviHaptics(context) }
    SideEffect { haptics.userEnabled = appData.config.haptics }

    var toastMessage by remember { mutableStateOf<String?>(null) }
    var toastKey by remember { mutableIntStateOf(0) }
    var confettiKey by remember { mutableIntStateOf(0) }
    var nfcState by remember { mutableStateOf<NfcUiState?>(null) }
    var pendingCsv by remember { mutableStateOf<String?>(null) }
    var actionSheetOpen by remember { mutableStateOf(false) }
    var navIntent by remember { mutableStateOf<NavIntent?>(null) }

    fun showToast(message: String) {
        toastMessage = message
        toastKey++
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is UiEvent.Toast -> showToast(event.text)
                UiEvent.MoneyRain -> {
                    confettiKey++
                    haptics.celebrate()
                }
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
    val view = LocalView.current
    SideEffect {
        activity?.let { act ->
            val controller = WindowCompat.getInsetsController(act.window, view)
            // The gradient hero sits under the status bar: always light icons there.
            controller.isAppearanceLightStatusBars = false
            controller.isAppearanceLightNavigationBars = !appData.config.darkMode
        }
    }
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
            if (group == null) {
                haptics.error()
                showToast("Error al leer la invitación. 😕")
            } else {
                viewModel.applyInvitation(group)
                confettiKey++
                haptics.celebrate()
            }
        }
        nfcController?.onError = { message ->
            nfcController?.stopReceiving()
            nfcState = null
            haptics.error()
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
                if (ok) haptics.success() else haptics.error()
                showToast(if (ok) "¡Respaldo guardado con éxito!" else "Error al guardar el archivo")
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
                    text == null -> {
                        haptics.error()
                        showToast("❌ Error al leer el archivo.")
                    }
                    viewModel.restoreFromJson(text) -> {
                        haptics.success()
                        showToast("🔄 ¡Datos restaurados!")
                    }
                    else -> {
                        haptics.error()
                        showToast("❌ Archivo de respaldo no válido.")
                    }
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
                if (ok) haptics.success() else haptics.error()
                showToast(if (ok) "CSV exportado 💾" else "Error al exportar CSV")
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
        when {
            controller == null || !controller.available -> {
                haptics.warning()
                showToast("Este dispositivo no dispone de NFC.")
            }
            !controller.enabled -> nfcState = NfcUiState.EnableRequested
            appData.groups.isEmpty() -> {
                haptics.warning()
                showToast("Primero debes crear un grupo para poder invitar.")
            }
            else -> nfcState = NfcUiState.SelectGroup
        }
    }

    fun requestNfcReceive() {
        val controller = nfcController
        when {
            controller == null || !controller.available -> {
                haptics.warning()
                showToast("Este dispositivo no dispone de NFC.")
            }
            !controller.enabled -> nfcState = NfcUiState.EnableRequested
            else -> {
                controller.startReceiving()
                nfcState = NfcUiState.WaitingToReceive
            }
        }
    }

    /** "Nuevo gasto" from anywhere: straight in with one group, otherwise pick. */
    fun startNewExpense(): Boolean {
        return when (appData.groups.size) {
            0 -> {
                haptics.warning()
                showToast("Crea un grupo primero 👫")
                navIntent = NavIntent.CreateGroup
                true
            }
            1 -> {
                navIntent = NavIntent.OpenGroup(appData.groups.keys.first(), add = true)
                true
            }
            else -> false
        }
    }

    CompositionLocalProvider(LocalHaptics provides haptics) {
        DiviTheme(dark = appData.config.darkMode) {
            DiviBackground {
                DiviNavHost(
                    appData = appData,
                    viewModel = viewModel,
                    onNfcSend = ::requestNfcSend,
                    onNfcReceive = ::requestNfcReceive,
                    actionSheetOpen = actionSheetOpen,
                    onPrimaryAction = { actionSheetOpen = !actionSheetOpen },
                    onNewExpense = { if (!startNewExpense()) actionSheetOpen = true },
                    onBackup = { backupLauncher.launch("divifriends_backup_${LocalDate.now()}.json") },
                    onRestore = { restoreLauncher.launch(arrayOf("application/json", "text/plain", "*/*")) },
                    onExportCsv = { group ->
                        pendingCsv = viewModel.exportGroupAsCsv(group)
                        csvLauncher.launch("gastos_${group.name.replace(" ", "_")}.csv")
                    },
                    onExportText = { group -> shareText(viewModel.exportGroupAsText(group)) },
                    navIntent = navIntent,
                    onNavIntentConsumed = { navIntent = null },
                    modifier = Modifier.fillMaxSize()
                )

                DiviToastHost(message = toastMessage, modifier = Modifier.align(Alignment.TopCenter))
                ConfettiBurst(trigger = confettiKey)

                if (!appData.config.onboarded && appData.config.userName.isBlank()) {
                    OnboardingDialog(onConfirm = viewModel::completeOnboarding)
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
                        haptics.success()
                        nfcState = NfcUiState.ReadyToSend(group.name)
                    }
                )

                if (actionSheetOpen) {
                    PrimaryActionSheet(
                        groups = appData.groups.values.sortedBy { it.name.lowercase() },
                        onDismiss = { actionSheetOpen = false },
                        onNewExpenseDirect = {
                            if (startNewExpense()) actionSheetOpen = false
                        },
                        onPickGroup = { id ->
                            actionSheetOpen = false
                            navIntent = NavIntent.OpenGroup(id, add = true)
                        },
                        onNewGroup = {
                            actionSheetOpen = false
                            navIntent = NavIntent.CreateGroup
                        },
                        onNfcInvite = {
                            actionSheetOpen = false
                            requestNfcSend()
                        },
                        onNfcScan = {
                            actionSheetOpen = false
                            requestNfcReceive()
                        }
                    )
                }
            }
        }
    }
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.isTabSwitch(): Boolean =
    initialState.destination.route in Routes.roots && targetState.destination.route in Routes.roots

@Composable
private fun DiviNavHost(
    appData: AppData,
    viewModel: DiviViewModel,
    onNfcSend: () -> Unit,
    onNfcReceive: () -> Unit,
    actionSheetOpen: Boolean,
    onPrimaryAction: () -> Unit,
    onNewExpense: () -> Unit,
    onBackup: () -> Unit,
    onRestore: () -> Unit,
    onExportCsv: (Group) -> Unit,
    onExportText: (Group) -> Unit,
    navIntent: NavIntent?,
    onNavIntentConsumed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val route = backStackEntry?.destination?.route ?: Routes.HOME
    val showNav = route in Routes.roots

    LaunchedEffect(navIntent) {
        when (val intent = navIntent) {
            is NavIntent.OpenGroup -> navController.navigate(Routes.groupDetail(intent.id, intent.add))
            NavIntent.CreateGroup -> navController.navigate(Routes.CREATE)
            null -> Unit
        }
        if (navIntent != null) onNavIntentConsumed()
    }

    val pushSpec = tween<androidx.compose.ui.unit.IntOffset>(420, easing = EmphasizedDecelerate)

    Box(modifier = modifier) {
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            enterTransition = {
                if (isTabSwitch()) fadeIn(tween(260, delayMillis = 60)) + scaleIn(tween(320, easing = EmphasizedDecelerate), initialScale = 0.96f)
                else slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, pushSpec) { it / 3 } + fadeIn(tween(300))
            },
            exitTransition = {
                if (isTabSwitch()) fadeOut(tween(120))
                else slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, pushSpec) { it / 6 } +
                    fadeOut(tween(260)) + scaleOut(tween(300), targetScale = 0.96f)
            },
            popEnterTransition = {
                if (isTabSwitch()) fadeIn(tween(260)) as EnterTransition
                else slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, pushSpec) { it / 6 } +
                    fadeIn(tween(300)) + scaleIn(tween(320), initialScale = 0.96f)
            },
            popExitTransition = {
                if (isTabSwitch()) fadeOut(tween(120)) as ExitTransition
                else slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, pushSpec) { it / 3 } + fadeOut(tween(220))
            }
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    appData = appData,
                    onOpenGroup = { navController.navigate(Routes.groupDetail(it)) },
                    onCreateGroup = { navController.navigate(Routes.CREATE) },
                    onOpenGroups = { navController.navigate(Routes.GROUPS) { launchSingleTop = true } },
                    onAddExpense = { groupId -> navController.navigate(Routes.groupDetail(groupId, add = true)) },
                    onOpenReport = { navController.navigate(Routes.REPORT) { launchSingleTop = true } },
                    onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                    onNfcInvite = onNfcSend,
                    onNfcScan = onNfcReceive
                )
            }
            composable(Routes.GROUPS) {
                GroupListScreen(
                    appData = appData,
                    onOpenGroup = { navController.navigate(Routes.groupDetail(it)) },
                    onCreateGroup = { navController.navigate(Routes.CREATE) },
                    onNfcSend = onNfcSend,
                    onNfcReceive = onNfcReceive
                )
            }
            composable(
                Routes.GROUP_DETAIL,
                arguments = listOf(
                    navArgument("groupId") { type = NavType.StringType },
                    navArgument("add") {
                        type = NavType.BoolType
                        defaultValue = false
                    }
                )
            ) { entry ->
                val groupId = entry.arguments?.getString("groupId")
                val openAdd = entry.arguments?.getBoolean("add") ?: false
                val group = groupId?.let { appData.groups[it] }
                if (group == null) {
                    LaunchedEffect(Unit) { navController.popBackStack() }
                } else {
                    GroupDetailScreen(
                        group = group,
                        userName = appData.config.userName,
                        openAddOnStart = openAdd,
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
                    onCreated = {
                        if (!navController.popBackStack(Routes.GROUPS, inclusive = false)) {
                            navController.popBackStack()
                            navController.navigate(Routes.GROUPS) { launchSingleTop = true }
                        }
                    },
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
                            if (!navController.popBackStack(Routes.GROUPS, inclusive = false)) {
                                navController.popBackStack(Routes.HOME, inclusive = false)
                            }
                        }
                    )
                }
            }
            composable(Routes.BALANCES) {
                BalancesScreen(
                    appData = appData,
                    onOpenGroup = { navController.navigate(Routes.groupDetail(it)) },
                    onAddExpense = onNewExpense,
                    onSettle = { groupId, from, to, amount -> viewModel.settleDebt(groupId, from, to, amount) }
                )
            }
            composable(Routes.REPORT) { ReportScreen(appData) }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    userName = appData.config.userName,
                    groupCount = appData.groups.size,
                    expenseCount = appData.groups.values.sumOf { g -> g.expenses.count { !it.isSettlement } },
                    darkMode = appData.config.darkMode,
                    hapticsEnabled = appData.config.haptics,
                    onDarkModeChange = viewModel::setDarkMode,
                    onHapticsChange = viewModel::setHaptics,
                    onBack = { navController.popBackStack() },
                    onBackup = onBackup,
                    onRestore = onRestore,
                    onDeleteAll = { viewModel.deleteAllData() }
                )
            }
        }

        AnimatedVisibility(
            visible = showNav,
            enter = slideInVertically(spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMediumLow)) { it * 2 } + fadeIn(),
            exit = slideOutVertically(tween(220)) { it * 2 } + fadeOut(tween(160)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 14.dp)
        ) {
            DiviFloatingNav(
                items = NAV_ITEMS,
                currentRoute = route,
                primaryOpen = actionSheetOpen,
                onSelect = { target ->
                    if (target != route) {
                        navController.navigate(target) {
                            popUpTo(Routes.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                onPrimaryAction = onPrimaryAction
            )
        }
    }
}

@Composable
private fun PrimaryActionSheet(
    groups: List<Group>,
    onDismiss: () -> Unit,
    onNewExpenseDirect: () -> Unit,
    onPickGroup: (String) -> Unit,
    onNewGroup: () -> Unit,
    onNfcInvite: () -> Unit,
    onNfcScan: () -> Unit
) {
    var picking by remember { mutableStateOf(false) }
    DiviSheet(onDismiss = onDismiss) {
        AnimatedContent(
            targetState = picking,
            transitionSpec = {
                (fadeIn(tween(220)) + scaleIn(tween(260), initialScale = 0.94f))
                    .togetherWith(fadeOut(tween(120)))
            },
            label = "actionSheet"
        ) { isPicking ->
            Column(Modifier.fillMaxWidth()) {
                if (!isPicking) {
                    Text(
                        text = "¿Qué quieres hacer?",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(18.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        ActionTile(
                            label = "Nuevo gasto",
                            icon = Icons.AutoMirrored.Rounded.ReceiptLong,
                            tone = Tone.Mint,
                            onClick = {
                                if (groups.size > 1) picking = true else onNewExpenseDirect()
                            },
                            modifier = Modifier.weight(1f)
                        )
                        ActionTile(
                            label = "Nuevo grupo",
                            icon = Icons.Rounded.GroupAdd,
                            tone = Tone.Aqua,
                            phase = 0.3f,
                            onClick = onNewGroup,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        ActionTile(
                            label = "Invitar",
                            caption = "Por NFC",
                            icon = Icons.Rounded.Nfc,
                            tone = Tone.Aqua,
                            phase = 0.6f,
                            onClick = onNfcInvite,
                            modifier = Modifier.weight(1f)
                        )
                        ActionTile(
                            label = "Unirme",
                            caption = "Recibir grupo",
                            icon = Icons.Rounded.Contactless,
                            tone = Tone.Mint,
                            phase = 0.9f,
                            onClick = onNfcScan,
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    Text(
                        text = "¿En qué grupo?",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(10.dp))
                    groups.forEach { group ->
                        TransactionRow(
                            name = group.name,
                            emoji = group.signatureEmoji(),
                            title = group.name,
                            subtitle = "${group.people.size} personas",
                            amount = formatCurrency(group.totalSpent()),
                            amountColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            onClick = { onPickGroup(group.id) }
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    SoftButton(text = "Atrás", onClick = { picking = false }, tone = Tone.Raised)
                }
            }
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
            title = "Activa el NFC",
            message = "El NFC de tu móvil está apagado. ¿Vamos a los ajustes para activarlo?",
            icon = Icons.Rounded.Nfc,
            confirmText = "Ir a Ajustes",
            cancelText = "Ahora no",
            destructive = false,
            onConfirm = onGoToSettings,
            onDismiss = onDismiss
        )

        NfcUiState.SelectGroup -> DiviDialog(onDismiss = onDismiss) {
            Text(
                text = "Invitar por NFC",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Elige el grupo que quieres compartir:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            groups.forEach { group ->
                TransactionRow(
                    name = group.name,
                    emoji = group.signatureEmoji(),
                    title = group.name,
                    subtitle = "${group.people.size} personas",
                    amount = "",
                    amountColor = MaterialTheme.colorScheme.onSurface,
                    onClick = { onSelectGroup(group) }
                )
            }
        }

        is NfcUiState.ReadyToSend -> NfcRadarDialog(
            icon = Icons.Rounded.Nfc,
            title = "Listo para enviar",
            message = "Junta la parte trasera de los dos móviles para compartir \"${state.groupName}\".",
            button = "Cerrar",
            onDismiss = onDismiss
        )

        NfcUiState.WaitingToReceive -> NfcRadarDialog(
            icon = Icons.Rounded.Contactless,
            title = "Buscando invitación…",
            message = "Acerca tu móvil al de tu amigo para unirte a su grupo.",
            button = "Cancelar",
            onDismiss = onDismiss
        )

        null -> Unit
    }
}

@Composable
private fun NfcRadarDialog(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    message: String,
    button: String,
    onDismiss: () -> Unit
) {
    DiviDialog(onDismiss = onDismiss) {
        PulseRings(icon = icon, modifier = Modifier.align(Alignment.CenterHorizontally))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        SoftButton(text = button, onClick = onDismiss, tone = Tone.Raised)
    }
}
