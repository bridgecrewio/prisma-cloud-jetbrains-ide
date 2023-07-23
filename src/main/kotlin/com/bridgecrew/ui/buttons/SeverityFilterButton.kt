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

class SeverityFilterButton(val project: Project, text: String, val severity: Severity): JButton(text) {

    init {
        addActionListener(SeverityFilterActions(project))
        preferredSize = Dimension(22, 22)
        border = null
        isOpaque = true
        ui = object: BasicButtonUI() {
            override fun paint(g: Graphics?, c: JComponent?) {
                val g2d = g?.create() as Graphics2D
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                g2d.color = getFilterColor()
                c?.height?.let { g2d.fillRect(0,0,  c.width, it) }
                val boldFont = Font(font.fontName, Font.BOLD, font.size)
                font = boldFont
                super.paint(g, c)
                g2d.dispose()
            }
        }

        isEnabled = shouldBeEnabled(severity, project)
    }

    private fun getFilterColor(): Color {
        var bgColor: Color = UIUtil.getEditorPaneBackground()
        if(shouldBeEnabled(severity, project) && isClicked()) {
            val color = if(isDarkMode()) Color.decode("#5C6164") else Color.decode("#CFCFCF")
            bgColor = Color(color.red, color.green, color.blue, 102)
        }
        return bgColor
    }

    private fun isClicked(): Boolean {
        return SeverityFilterActions.severityFilterState[text] == true
    }

    private fun shouldBeEnabled(severity: Severity, project: Project): Boolean {
        if (project.service<FullScanStateService>().isFullScanRunning)
            return false

        val filteredResults = CheckovResultsListUtils.filterResultsByCategoriesAndSeverities(project.service<ResultsCacheService>().getAdjustedCheckovResults(), null, listOf(severity))

        if (filteredResults.isEmpty()) {
            return false
        }


        if (!SeverityFilterActions.enabledSeverities.contains(severity)) {
            return false
        }

        return true
    }

}