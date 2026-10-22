package com.divifriends.xyon.pro.ui.navigation

object Routes {
    const val HOME = "inicio"
    const val GROUPS = "grupos"
    const val BALANCES = "saldos"
    const val REPORT = "informe"
    const val SETTINGS = "ajustes"
    const val CREATE = "crear"
    /** Virtual route used by the navigation bar's primary action (opens a sheet). */
    const val ADD = "anadir"
    /** `add=true` opens the new-expense sheet as soon as the group shows. */
    const val GROUP_DETAIL = "grupo/{groupId}?add={add}"
    const val EDIT = "editar/{groupId}"

    fun groupDetail(id: String, add: Boolean = false) = "grupo/$id?add=$add"
    fun edit(id: String) = "editar/$id"

    /** Destinations shown inside the floating navigation bar. */
    val roots = setOf(HOME, GROUPS, BALANCES, REPORT)
}
