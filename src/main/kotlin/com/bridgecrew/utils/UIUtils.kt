package com.bridgecrew.utils

import com.bridgecrew.results.Severity
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.uiDesigner.core.GridConstraints
import icons.CheckovIcons
import java.awt.Color
import javax.swing.Icon

val separatorColorDark: Color = Color.decode("#323232")
val separatorColorLight: Color = Color.decode("#D1D1D1")

val FIX_COLOR_DARK: Color = Color.decode("#49544A")
val FIX_COLOR_LIGHT: Color = Color.decode("#49544A")
val VULNERABLE_COLOR_DARK: Color = Color.decode("#704745")
val VULNERABLE_COLOR_LIGHT: Color = Color.decode("#704745")

fun createGridRowCol(row: Int, col: Int = 0, align: Int = 0, fill: Int = 0): GridConstraints {
    return GridConstraints(
        row, col, 1, 1, align, fill, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null,
        null, 1, false
    )
}

private val severityIconMap: Map<Severity, Icon> = mapOf(
        Severity.CRITICAL to CheckovIcons.SeverityCritical,
        Severity.HIGH to CheckovIcons.SeverityHigh,
        Severity.MEDIUM to CheckovIcons.SeverityMedium,
        Severity.LOW to CheckovIcons.SeverityLow,
        Severity.INFO to CheckovIcons.SeverityInfo,
        Severity.UNKNOWN to CheckovIcons.SeverityInfo
)

enum class IDEColorMode {
    DARK,
    LIGHT
}

fun getSeverityIconBySeverity(severity: Severity): Icon {
    return severityIconMap[severity] ?: CheckovIcons.SeverityInfo
}

fun getIDEColorMode(): IDEColorMode {
    val editorColorsManager = EditorColorsManager.getInstance()
    val currentScheme = editorColorsManager.globalScheme
    return when(currentScheme.name) {
        "_@user_High contrast" -> IDEColorMode.DARK
        "_@user_Darcula" -> IDEColorMode.DARK
        "@user_IntelliJ Light" -> IDEColorMode.LIGHT
        "_@user_Default" -> IDEColorMode.LIGHT
        else -> IDEColorMode.DARK
    }
}

fun isDarkMode(): Boolean {
    return getIDEColorMode() == IDEColorMode.DARK
}