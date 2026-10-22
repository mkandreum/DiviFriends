package com.divifriends.xyon.pro.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.divifriends.xyon.pro.data.model.Group
import com.divifriends.xyon.pro.ui.components.Avatar
import com.divifriends.xyon.pro.ui.components.AvatarStack
import com.divifriends.xyon.pro.ui.components.CircleIconButton
import com.divifriends.xyon.pro.ui.components.DiviConfirmDialog
import com.divifriends.xyon.pro.ui.components.DiviPillShape
import com.divifriends.xyon.pro.ui.components.DiviTextField
import com.divifriends.xyon.pro.ui.components.FieldLabel
import com.divifriends.xyon.pro.ui.components.HeroScaffold
import com.divifriends.xyon.pro.ui.components.HeroTopBar
import com.divifriends.xyon.pro.ui.components.PrimaryButton
import com.divifriends.xyon.pro.ui.components.SectionHeader
import com.divifriends.xyon.pro.ui.components.SoftButton
import com.divifriends.xyon.pro.ui.components.Tone
import com.divifriends.xyon.pro.ui.haptics.LocalHaptics
import com.divifriends.xyon.pro.ui.motion.bounceClick
import com.divifriends.xyon.pro.ui.motion.floating
import com.divifriends.xyon.pro.ui.motion.shakeOnError
import com.divifriends.xyon.pro.ui.theme.LocalDiviPalette

private val NAME_SUGGESTIONS = listOf(
    "✈️ Viaje", "🏠 Piso", "🍕 Cena", "🍻 Fiesta", "🏖️ Vacaciones", "🎁 Regalo"
)

@Composable
fun CreateGroupScreen(
    userName: String,
    onBack: () -> Unit,
    onCreated: () -> Unit,
    onCreate: (String, List<String>) -> Unit
) {
    val palette = LocalDiviPalette.current
    val haptics = LocalHaptics.current
    var step by rememberSaveable { mutableIntStateOf(0) }
    var name by rememberSaveable { mutableStateOf("") }
    var input by rememberSaveable { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var shake by remember { mutableIntStateOf(0) }
    val participants = remember {
        mutableStateListOf<String>().apply { if (userName.isNotBlank()) add(userName) }
    }
    val progress by animateFloatAsState(
        if (step == 0) 0.5f else 1f,
        spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessLow),
        label = "createProgress"
    )

    fun fail(message: String) {
        haptics?.error()
        shake++
        error = message
    }

    fun addPerson() {
        val value = input.trim()
        when {
            value.isEmpty() -> fail("Escribe un nombre")
            participants.any { it.equals(value, ignoreCase = true) } -> fail("$value ya está en el grupo")
            else -> {
                haptics?.success()
                participants.add(value)
                input = ""
                error = null
            }
        }
    }

    HeroScaffold(
        compactTitle = "Nuevo grupo",
        onBack = { if (step == 0) onBack() else step = 0 },
        hero = {
            HeroTopBar(
                title = "Nuevo grupo",
                caption = "Paso ${step + 1} de 2",
                onBack = { if (step == 0) onBack() else step = 0 }
            )
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(DiviPillShape)
                    .background(palette.heroChip)
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(progress)
                        .height(6.dp)
                        .clip(DiviPillShape)
                        .background(palette.sun)
                )
            }
            Spacer(Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(
                    name = name.ifBlank { "?" },
                    size = 72.dp,
                    shape = com.divifriends.xyon.pro.ui.components.Cookie,
                    modifier = Modifier.floating(amplitudeDp = 4f)
                )
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = name.ifBlank { "Tu grupo" },
                        style = MaterialTheme.typography.headlineSmall,
                        color = palette.onHero,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(6.dp))
                    if (participants.isEmpty()) {
                        Text("Aún sin participantes", style = MaterialTheme.typography.labelMedium, color = palette.onHeroMuted)
                    } else {
                        AvatarStack(names = participants.toList(), size = 30.dp, max = 5, ringColor = palette.heroEnd)
                    }
                }
            }
        },
        sheet = {
            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    val dir = if (targetState > initialState) 1 else -1
                    (slideInHorizontally(spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow)) { it * dir } + fadeIn())
                        .togetherWith(slideOutHorizontally(tween(200)) { -it * dir } + fadeOut(tween(150)))
                },
                label = "createStep"
            ) { current ->
                Column(Modifier.fillMaxWidth()) {
                    if (current == 0) {
                        SectionHeader("¿Cómo se llama?")
                        DiviTextField(
                            value = name,
                            onValueChange = {
                                name = it
                                error = null
                            },
                            placeholder = "Ej. Viaje a Lisboa",
                            leadingIcon = Icons.Rounded.Groups,
                            imeAction = ImeAction.Next,
                            onImeAction = { if (name.isNotBlank()) step = 1 },
                            modifier = Modifier.shakeOnError(shake)
                        )
                        Spacer(Modifier.height(14.dp))
                        FieldLabel("Ideas")
                        Row(
                            Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            NAME_SUGGESTIONS.forEach { suggestion ->
                                Text(
                                    text = suggestion,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = palette.onLime,
                                    modifier = Modifier
                                        .bounceClick(pressedScale = 0.9f, feedback = { it.toggle() }) {
                                            name = suggestion.substringAfter(' ')
                                            error = null
                                        }
                                        .clip(DiviPillShape)
                                        .background(palette.lime)
                                        .padding(horizontal = 14.dp, vertical = 9.dp)
                                )
                            }
                        }
                        ErrorLine(error)
                        Spacer(Modifier.height(26.dp))
                        PrimaryButton(
                            text = "Continuar",
                            leadingIcon = Icons.AutoMirrored.Rounded.ArrowForward,
                            onClick = {
                                if (name.isBlank()) fail("Ponle un nombre al grupo")
                                else {
                                    error = null
                                    step = 1
                                }
                            }
                        )
                    } else {
                        SectionHeader("¿Quién participa?")
                        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            DiviTextField(
                                value = input,
                                onValueChange = {
                                    input = it
                                    error = null
                                },
                                placeholder = "Nombre del amigo",
                                leadingIcon = Icons.Rounded.PersonAdd,
                                onImeAction = ::addPerson,
                                modifier = Modifier
                                    .weight(1f)
                                    .shakeOnError(shake)
                            )
                            CircleIconButton(
                                icon = Icons.Rounded.Add,
                                onClick = ::addPerson,
                                tone = Tone.Accent,
                                contentDescription = "Añadir participante",
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        ErrorLine(error)
                        Spacer(Modifier.height(10.dp))
                        Column(Modifier.animateContentSize(spring(dampingRatio = 0.8f))) {
                            participants.forEach { person ->
                                PersonLine(
                                    name = person,
                                    isYou = person == userName,
                                    onRemove = {
                                        haptics?.heavy()
                                        participants.remove(person)
                                    }
                                )
                            }
                        }
                        Text(
                            text = if (participants.size < 2) "Añade al menos 2 personas" else "${participants.size} personas",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp, start = 4.dp)
                        )
                        Spacer(Modifier.height(22.dp))
                        PrimaryButton(
                            text = "Crear grupo",
                            leadingIcon = Icons.Rounded.Check,
                            enabled = participants.size >= 2,
                            onClick = {
                                if (participants.size < 2) fail("Añade al menos 2 personas")
                                else {
                                    onCreate(name, participants.toList())
                                    onCreated()
                                }
                            }
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun ErrorLine(error: String?) {
    AnimatedVisibility(visible = error != null, enter = expandVertically() + fadeIn(), exit = fadeOut()) {
        Text(
            text = error.orEmpty(),
            style = MaterialTheme.typography.labelLarge,
            color = LocalDiviPalette.current.negative,
            modifier = Modifier.padding(top = 10.dp, start = 4.dp)
        )
    }
}

@Composable
private fun PersonLine(name: String, isYou: Boolean, onRemove: (() -> Unit)?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Avatar(name = name, size = 42.dp)
        Column(Modifier.weight(1f)) {
            Text(name, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
            if (isYou) Text("Tú", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (onRemove != null) {
            CircleIconButton(icon = Icons.Rounded.Close, onClick = onRemove, contentDescription = "Quitar a $name")
        }
    }
}

@Composable
fun EditGroupScreen(
    group: Group,
    userName: String,
    onBack: () -> Unit,
    onRename: (String) -> Unit,
    onAddParticipant: (String) -> Unit,
    onRemoveParticipant: (String) -> Unit,
    onDeleteGroup: () -> Unit
) {
    val palette = LocalDiviPalette.current
    var nameText by rememberSaveable(group.id) { mutableStateOf(group.name) }
    var newParticipant by rememberSaveable { mutableStateOf("") }
    var confirmDelete by remember { mutableStateOf(false) }

    HeroScaffold(
        compactTitle = "Editar grupo",
        onBack = onBack,
        hero = {
            HeroTopBar(title = "Editar grupo", caption = group.name, onBack = onBack)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(name = group.name, emoji = group.signatureEmoji(), size = 64.dp, modifier = Modifier.floating(amplitudeDp = 4f))
                Spacer(Modifier.width(16.dp))
                AvatarStack(names = group.people, size = 34.dp, max = 5, ringColor = palette.heroEnd)
            }
        },
        sheet = {
            SectionHeader("Nombre")
            DiviTextField(value = nameText, onValueChange = { nameText = it }, leadingIcon = Icons.Rounded.Groups)
            AnimatedVisibility(visible = nameText.trim() != group.name && nameText.isNotBlank()) {
                SoftButton(
                    text = "Guardar nombre",
                    onClick = { onRename(nameText) },
                    tone = Tone.Lime,
                    leadingIcon = Icons.Rounded.Check,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            SectionHeader("Participantes · ${group.people.size}")
            Column(Modifier.animateContentSize(spring(dampingRatio = 0.8f))) {
                group.people.forEach { person ->
                    PersonLine(name = person, isYou = person == userName, onRemove = { onRemoveParticipant(person) })
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DiviTextField(
                    value = newParticipant,
                    onValueChange = { newParticipant = it },
                    placeholder = "Añadir participante",
                    leadingIcon = Icons.Rounded.PersonAdd,
                    onImeAction = {
                        if (newParticipant.isNotBlank()) {
                            onAddParticipant(newParticipant)
                            newParticipant = ""
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
                CircleIconButton(
                    icon = Icons.Rounded.Add,
                    tone = Tone.Accent,
                    enabled = newParticipant.isNotBlank(),
                    contentDescription = "Añadir participante",
                    onClick = {
                        onAddParticipant(newParticipant)
                        newParticipant = ""
                    }
                )
            }

            SectionHeader("Zona peligrosa")
            SoftButton(
                text = "Eliminar grupo",
                onClick = { confirmDelete = true },
                tone = Tone.Danger,
                leadingIcon = Icons.Rounded.DeleteForever,
                heavy = true
            )
        }
    )

    if (confirmDelete) {
        DiviConfirmDialog(
            title = "Eliminar \"${group.name}\"",
            message = "Se borrarán el grupo y sus ${group.expenses.size} gastos. Esta acción no se puede deshacer.",
            icon = Icons.Rounded.DeleteForever,
            confirmText = "Eliminar grupo",
            onConfirm = {
                confirmDelete = false
                onDeleteGroup()
            },
            onDismiss = { confirmDelete = false }
        )
    }
}
