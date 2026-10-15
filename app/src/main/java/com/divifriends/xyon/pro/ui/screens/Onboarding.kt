package com.divifriends.xyon.pro.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.divifriends.xyon.pro.ui.components.DiviDialog
import com.divifriends.xyon.pro.ui.components.DiviTextField
import com.divifriends.xyon.pro.ui.components.MainButton
import com.divifriends.xyon.pro.ui.theme.TextSecondary

@Composable
fun OnboardingDialog(onConfirm: (String) -> Unit) {
    var name by remember { mutableStateOf("") }

    DiviDialog(onDismiss = { }) {
        Text(
            text = "\uD83D\uDC4B \u00a1Bienvenido a DiviFriends!",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Para empezar, dinos tu nombre. Se usar\u00e1 para a\u00f1adirte autom\u00e1ticamente a los grupos que crees.",
            color = TextSecondary,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 12.dp, bottom = 16.dp)
        )
        DiviTextField(
            value = name,
            onValueChange = { name = it },
            label = "Tu nombre",
            placeholder = "Tu nombre aqu\u00ed..."
        )
        MainButton(
            text = "\uD83D\uDE80 \u00a1A repartir gastos!",
            onClick = { onConfirm(name) },
            modifier = Modifier.padding(top = 20.dp)
        )
    }
}
