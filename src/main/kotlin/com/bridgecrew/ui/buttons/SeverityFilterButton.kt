package com.bridgecrew.ui.buttons

import com.bridgecrew.results.Severity
import com.bridgecrew.services.CheckovResultsListUtils
import com.bridgecrew.services.ResultsCacheService
import com.bridgecrew.services.scan.FullScanStateService
import com.bridgecrew.ui.actions.SeverityFilterActions
import com.bridgecrew.utils.isDarkMode
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import java.awt.*
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.plaf.basic.BasicButtonUI

class SeverityFilterButton(val project: Project, text: String, val severity: Severity, isActive: Boolean): JButton(text) {
    private var isActive = false

    init {
        this.isActive = isActive
        preferredSize = Dimension(22, 22)
        border = null
        isOpaque = true
        val boldFont = Font(font.fontName, Font.BOLD, font.size)
        font = boldFont

        updateButtonAppearance()
        // Add an ActionListener to change the text when the button is toggled
        addActionListener {
            updateButtonAppearance()
        }
        addActionListener(SeverityFilterActions(project))

        isEnabled = shouldBeEnabled(severity, project)

        ui = object: BasicButtonUI() {
            override fun paint(g: Graphics?, c: JComponent?) {
                if (c is SeverityFilterButton) {
                    super.paint(g, c)
                }
            }
        }
    }

    private fun updateButtonAppearance() {
        background = if (isActive) {
            val color = if(isDarkMode()) Color.decode("#5C6164") else Color.decode("#CFCFCF")
            Color(color.red, color.green, color.blue, 102)
        } else {
            null
        }
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