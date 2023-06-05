package com.bridgecrew.ui.buttons

import com.bridgecrew.results.Severity
import com.bridgecrew.services.CheckovResultsListUtils
import com.bridgecrew.services.ResultsCacheService
import com.bridgecrew.services.scan.FullScanStateService
import com.bridgecrew.ui.actions.SeverityFilterActions
import com.bridgecrew.utils.isDarkMode
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.util.ui.UIUtil
import java.awt.*
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.plaf.basic.BasicButtonUI

class SeverityFilterButton(project: Project, text: String, severity: Severity): JButton(text) {

    init {
        addActionListener(SeverityFilterActions(project))
        preferredSize = Dimension(22, 22)
        border = null
        isOpaque = true
        ui = object: BasicButtonUI() {
            override fun paint(g: Graphics?, c: JComponent?) {
                val g2d = g?.create() as Graphics2D
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                g2d.color = getBackgroundColor()
                c?.foreground = getTextColor()
                c?.height?.let { g2d.fillRect(0,0,  c.width, it) }
                val boldFont = Font(font.fontName, Font.BOLD, font.size)
                font = boldFont
                super.paint(g, c)
                g2d.dispose()
            }
        }

        isEnabled = shouldBeEnabled(severity, project)
        background = getBackgroundColor()
    }

    private fun getTextColor(): Color {
        return if(isClicked()) foreground else foreground //TODO put here the color when provided
    }
    private fun getBackgroundColor(): Color {
        var bgColor = UIUtil.getEditorPaneBackground()
        if(isEnabled) {
            val color = if(isDarkMode()) Color.decode("#6E6E6E") else Color.decode("#AFB1B3")
            bgColor = Color(color.red, color.green, color.blue, 102)
        }
        return bgColor
    }

    private fun isClicked(): Boolean {
        return SeverityFilterActions.severityFilterState[text] == true
    }

    private fun shouldBeEnabled(severity: Severity, project: Project): Boolean {
        if (project.service<FullScanStateService>().isFullScanRunning && !project.service<FullScanStateService>().isFrameworkResultsWereDisplayed)
            return false

        val filteredResults = CheckovResultsListUtils.filterResultsByCategoriesAndSeverities(project.service<ResultsCacheService>().checkovResults, null, listOf(severity))

        if (filteredResults.isEmpty()) {
            return false
        }


        if (!SeverityFilterActions.enabledSeverities.contains(severity)) {
            return false
        }

        return true
    }

}