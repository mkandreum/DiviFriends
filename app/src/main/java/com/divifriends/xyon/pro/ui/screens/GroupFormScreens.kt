package com.divifriends.xyon.pro.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.divifriends.xyon.pro.data.model.Group
import com.divifriends.xyon.pro.ui.components.ButtonTone
import com.divifriends.xyon.pro.ui.components.DiviConfirmDialog
import com.divifriends.xyon.pro.ui.components.DiviLabel
import com.divifriends.xyon.pro.ui.components.DiviScreen
import com.divifriends.xyon.pro.ui.components.DiviTextField
import com.divifriends.xyon.pro.ui.components.GlassButton
import com.divifriends.xyon.pro.ui.components.GlassCard
import com.divifriends.xyon.pro.ui.components.MainButton
import com.divifriends.xyon.pro.ui.components.PageTitle
import com.divifriends.xyon.pro.ui.components.SectionHeader
import com.divifriends.xyon.pro.ui.theme.LocalDiviPalette

@Composable
fun CreateGroupScreen(
    userName: String,
    onBack: () -> Unit,
    onCreated: () -> Unit,
    onCreate: (String, List<String>) -> Unit
) {
    var step by remember { mutableIntStateOf(1) }
    var name by remember { mutableStateOf("") }
    var countText by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val names = remember { mutableStateListOf<String>() }

    DiviScreen {
        PageTitle(
            title = if (step == 1) "Crear Grupo (1/2)" else "Crear Grupo (2/2)",
            onBack = { if (step == 2) step = 1 else onBack() }
        )

        if (step == 1) {
            GlassCard {
                DiviTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "\uD83D\uDCDD Nombre del Grupo",
                    placeholder = "Ej: Viaje a la playa \uD83C\uDFD6\uFE0F"
                )
                DiviTextField(
                    value = countText,
                    onValueChange = { countText = it.filter { c -> c.isDigit() }.take(2) },
                    label = "\uD83D\uDC65 \u00bfCu\u00e1ntas personas sois?",
                    placeholder = "Ej: 4",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.padding(top = 16.dp)
                )
                error?.let { ErrorText(it) }
                MainButton(
                    text = "Siguiente \u27A1\uFE0F",
                    onClick = {
                        val count = countText.toIntOrNull() ?: 0
                        if (name.isBlank() || count < 2) {
                            error = "Introduce un nombre y al menos 2 personas."
                        } else {
                            error = null
                            names.clear()
                            names.add(userName)
                            repeat(count - 1) { names.add("") }
                            step = 2
                        }
                    },
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        } else {
            GlassCard {
                SectionHeader("Participantes de \"$name\"")
                Column(
                    modifier = Modifier.padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    names.forEachIndexed { index, value ->
                        DiviTextField(
                            value = value,
                            onValueChange = { names[index] = it },
                            label = "\uD83D\uDC64 Nombre Persona ${index + 1}",
                            placeholder = if (index == 0) userName else "Nombre...",
                            readOnly = index == 0
                        )
                    }
                }
                error?.let { ErrorText(it) }
                MainButton(
                    text = "\u2714\uFE0F Crear Grupo Definitivo",
                    onClick = {
                        val people = names.map { it.trim() }
                        when {
                            people.any { it.isEmpty() } ->
                                error = "Por favor, introduce un nombre para cada persona. \u261D\uFE0F"
                            people.distinct().size != people.size ->
                                error = "Los nombres deben ser \u00fanicos. \u261D\uFE0F"
                            else -> {
                                error = null
                                onCreate(name, people)
                                onCreated()
                            }
                        }
                    },
                    modifier = Modifier.padding(top = 20.dp)
                )
            }
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
    var nameText by remember(group.id) { mutableStateOf(group.name) }
    var newParticipant by remember { mutableStateOf("") }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var pendingRemoval by remember { mutableStateOf<String?>(null) }

    DiviScreen {
        PageTitle(title = "Editar Grupo", onBack = onBack)

        GlassCard {
            DiviTextField(
                value = nameText,
                onValueChange = { nameText = it },
                label = "\uD83D\uDCDD Nombre del Grupo"
            )
            MainButton(
                text = "Guardar Nombre",
                onClick = { onRename(nameText) },
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        GlassCard {
            SectionHeader("\uD83D\uDC65 Participantes")
            Column(
                modifier = Modifier.padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                group.people.forEach { person ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = person,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        if (person != userName) {
                            GlassButton(
                                text = "\u2715",
                                onClick = { pendingRemoval = person },
                                tone = ButtonTone.Danger,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                }
            }
            DiviTextField(
                value = newParticipant,
                onValueChange = { newParticipant = it },
                label = "A\u00f1adir Persona",
                placeholder = "Nuevo nombre...",
                modifier = Modifier.padding(top = 16.dp)
            )
            GlassButton(
                text = "\u2795 A\u00f1adir",
                onClick = {
                    onAddParticipant(newParticipant)
                    newParticipant = ""
                },
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        GlassCard {
            GlassButton(
                text = "\uD83D\uDDD1\uFE0F Eliminar Grupo",
                onClick = { showDeleteConfirm = true },
                tone = ButtonTone.Danger
            )
        }
    }

    if (showDeleteConfirm) {
        DiviConfirmDialog(
            title = "\uD83D\uDDD1\uFE0F Eliminar Grupo",
            message = "\u00bfSeguro que quieres eliminar el grupo \"${group.name}\"? Se perder\u00e1n todos sus gastos.",
            onConfirm = {
                showDeleteConfirm = false
                onDeleteGroup()
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }

    pendingRemoval?.let { person ->
        DiviConfirmDialog(
            title = "Quitar Participante",
            message = "\u00bfSeguro que quieres quitar a $person del grupo?",
            onConfirm = {
                onRemoveParticipant(person)
                pendingRemoval = null
            },
            onDismiss = { pendingRemoval = null }
        )
    }
}

@Composable
private fun ErrorText(text: String) {
    Text(
        text = text,
        color = LocalDiviPalette.current.danger,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 12.dp)
    )
}
