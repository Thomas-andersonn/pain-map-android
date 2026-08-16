package com.example.painmap.ui.navigation

sealed class PainMapRoute(val route: String) {
    data object Dashboard : PainMapRoute("dashboard")
    data object BodyMap : PainMapRoute("bodymap")
    data object TriageResult : PainMapRoute("triage_result")
}
