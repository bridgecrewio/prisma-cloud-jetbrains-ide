package com.bridgecrew.ui.topPanel

import com.bridgecrew.analytics.AnalyticsService
import com.bridgecrew.results.Category
import com.bridgecrew.results.Severity
import com.bridgecrew.services.CheckovResultsListUtils
import com.bridgecrew.services.ResultsCacheService
import com.bridgecrew.services.scan.FullScanStateService
import com.bridgecrew.ui.buttons.SeverityFilterButton
import com.bridgecrew.utils.*
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import java.awt.*
import java.time.Duration
import java.time.Instant
import javax.swing.*
import javax.swing.plaf.basic.BasicSeparatorUI

data class ScanResultMetadata(
        val totalIssues: Int,
        val totalPassed: Int,
        val scanDuration: Long
)

class CheckovTopPanel(val project: Project) : SimpleToolWindowPanel(true, true), Disposable {

    init {
        val actionToolbarPanel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0))

        createActionGroupPanel(actionToolbarPanel)
        createSeparator(actionToolbarPanel)
        addSeverityLabel(actionToolbarPanel)
        addFilterActions(actionToolbarPanel)

        add(actionToolbarPanel, BorderLayout.NORTH)
        toolbar = actionToolbarPanel
    }

    private fun createActionGroupPanel(actionToolbarPanel: JPanel) {
        actionToolbarPanel.add(CheckovActionToolbar.actionToolBar.component)
    }

    private fun addSeverityLabel(actionToolbarPanel: JPanel) {
        actionToolbarPanel.add(JLabel("Severity:"))
        actionToolbarPanel.add(Box.createRigidArea(Dimension(5, 24)))
    }

    private fun createSeparator(actionToolbarPanel: JPanel) {
        val separator = JSeparator(JSeparator.VERTICAL)
        separator.preferredSize = Dimension(2, 24)
        separator.setUI(object : BasicSeparatorUI() {
            override fun paint(g: Graphics, c: JComponent) {
                val g2d = g.create() as Graphics2D
                g2d.paint = if(isDarkMode()) separatorColorDark else separatorColorLight
                g2d.fillRect(0, 0, c.width, c.height)
                g2d.dispose()
            }
        })
        actionToolbarPanel.add(separator)
        actionToolbarPanel.add(Box.createRigidArea(Dimension(5, 24)))
    }

    private fun addFilterActions(actionToolbarPanel: JPanel) {
        actionToolbarPanel.add(SeverityFilterButton(project, "I", Severity.INFO))
        actionToolbarPanel.add(SeverityFilterButton(project,"L", Severity.LOW))
        actionToolbarPanel.add(SeverityFilterButton(project,"M", Severity.MEDIUM))
        actionToolbarPanel.add(SeverityFilterButton(project,"H", Severity.HIGH))
        actionToolbarPanel.add(SeverityFilterButton(project,"C", Severity.CRITICAL))
    }

    override fun dispose() = Unit
}