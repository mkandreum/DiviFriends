package com.divifriends.xyon.pro.ui.navigation

object Routes {
    const val GROUPS = "grupos"
    const val BALANCES = "saldos"
    const val REPORT = "informe"
    const val SETTINGS = "ajustes"
    const val CREATE = "crear"
    const val GROUP_DETAIL = "grupo/{groupId}"
    const val EDIT = "editar/{groupId}"

    fun groupDetail(id: String) = "grupo/$id"
    fun edit(id: String) = "editar/$id"

    val roots = setOf(GROUPS, BALANCES, REPORT, SETTINGS)
}
