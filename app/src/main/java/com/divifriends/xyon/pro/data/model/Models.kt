package com.divifriends.xyon.pro.data.model

import kotlinx.serialization.Serializable

/**
 * Root document persisted as JSON. The schema is intentionally identical to the
 * legacy WebView version so existing backups can still be restored.
 */
@Serializable
data class AppData(
    val config: AppConfig = AppConfig(),
    val groups: Map<String, Group> = emptyMap(),
    val activities: List<ActivityLog> = emptyList()
)

@Serializable
data class AppConfig(
    val userName: String = "",
    /** Legacy field kept only so old backups keep restoring. No longer used. */
    val theme: String = "",
    /** Appearance: false = claro (por defecto), true = oscuro. */
    val darkMode: Boolean = false,
    /** Whether the welcome flow has been completed or skipped. */
    val onboarded: Boolean = false,
    /** Haptic feedback (vibration) on taps, swipes and confirmations. */
    val haptics: Boolean = true
)

@Serializable
data class Group(
    val id: String,
    val name: String,
    val people: List<String> = emptyList(),
    val expenses: List<Expense> = emptyList()
)

@Serializable
data class Expense(
    val id: String,
    val description: String,
    val amount: Double,
    val paidBy: String,
    val participants: List<String> = emptyList(),
    val category: String = CATEGORY_OTHER,
    val date: String,
    val isSettlement: Boolean = false
)

@Serializable
data class ActivityLog(
    val text: String,
    val date: String
)

/** A suggested transfer that moves everyone closer to a zero balance. */
data class Debt(
    val from: String,
    val to: String,
    val amount: Double
)

const val CATEGORY_SETTLEMENT = "\uD83E\uDD1D"
const val CATEGORY_OTHER = "\uD83D\uDCB8"

/**
 * Expense categories. Key = emoji shown in the picker, value = human label.
 * Settlement is deliberately excluded from the picker (it is created automatically).
 */
val EXPENSE_CATEGORIES: List<Pair<String, String>> = listOf(
    "\uD83C\uDF55" to "Comida",
    "\uD83D\uDE97" to "Transporte",
    "\uD83D\uDED2" to "Supermercado",
    "\uD83C\uDFE0" to "Alojamiento",
    "\uD83C\uDFAC" to "Ocio",
    "\uD83C\uDF81" to "Regalos",
    "\u2708\uFE0F" to "Viajes",
    "\u2615" to "Caf\u00e9/Bebidas",
    "\uD83C\uDF7B" to "Fiesta",
    "\uD83C\uDF9F\uFE0F" to "Entradas",
    "\uD83D\uDC8A" to "Salud",
    "\uD83D\uDCA1" to "Servicios",
    "\uD83D\uDECD\uFE0F" to "Compras",
    CATEGORY_OTHER to "Otro"
)

fun categoryLabel(category: String): String =
    EXPENSE_CATEGORIES.firstOrNull { it.first == category }?.second
        ?: if (category == CATEGORY_SETTLEMENT) "Liquidaci\u00f3n" else "Otro"
