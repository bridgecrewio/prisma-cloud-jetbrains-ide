package com.bridgecrew.utils

import com.bridgecrew.results.Severity
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.uiDesigner.core.GridConstraints
import icons.CheckovIcons
import java.awt.Color
import java.awt.Font
import java.awt.Insets
import java.text.DecimalFormat
import javax.swing.Icon

val separatorColorDark: Color = Color.decode("#323232")
val separatorColorLight: Color = Color.decode("#D1D1D1")

val BLOCK_BG_DARK: Color = Color.decode("#313335")
val BLOCK_BG_LIGHT: Color = Color.decode("#FFFFFF")
val FIX_COLOR_DARK: Color = Color.decode("#49544A")
val FIX_COLOR_LIGHT: Color = Color.decode("#E9F5E6")
val VULNERABLE_COLOR_DARK: Color = Color.decode("#704745")
val VULNERABLE_COLOR_LIGHT: Color = Color.decode("#F5E6E7")
val FONT_COLOR_DARK: Color = Color.decode("#BABABA")
val FONT_COLOR_LIGHT: Color = Color.decode("#080808")

val ERROR_BUBBLE_ACTIONS_FONT: Font = Font("SF Pro Text", Font.PLAIN, 13)
val ERROR_BUBBLE_ACTIONS_COLOR_DARK: Color = Color.decode("#589DF6")
val ERROR_BUBBLE_ACTIONS_COLOR_LIGHT: Color = Color.decode("#2470B3")
val ERROR_BUBBLE_ACTIONS_MARGIN: Insets = Insets(6, 0, 0, 10)

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

fun formatNumberWithCommas(number: Int): String {
    val formatter = DecimalFormat("#,###")
    return formatter.format(number)
}