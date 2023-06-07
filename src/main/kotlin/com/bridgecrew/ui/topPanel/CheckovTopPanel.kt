package com.bridgecrew.ui.topPanel

import com.bridgecrew.results.Severity
import com.bridgecrew.ui.buttons.SeverityFilterButton
import com.bridgecrew.utils.*
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import java.awt.*
import javax.swing.*
import javax.swing.plaf.basic.BasicSeparatorUI

class CheckovTopPanel(val project: Project) : SimpleToolWindowPanel(true, true), Disposable {

    init {
        val actionToolbarPanel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0))

        createActionGroupPanel(actionToolbarPanel)
        createSeparator(actionToolbarPanel)
        addSeverityLabel(actionToolbarPanel)
        addFilterActions(actionToolbarPanel)

        add(actionToolbarPanel, BorderLayout.NORTH)
        val separatorColor = if(isDarkMode()) separatorColorDark else separatorColorLight
        border = BorderFactory.createMatteBorder(0,0,1,0, separatorColor)
        toolbar = actionToolbarPanel
    }

    private fun createActionGroupPanel(actionToolbarPanel: JPanel) {
        actionToolbarPanel.add(CheckovActionToolbar.actionToolBar.component)
    }

    private fun addSeverityLabel(actionToolbarPanel: JPanel) {
        actionToolbarPanel.add(JLabel("Severity:"))
        actionToolbarPanel.add(Box.createRigidArea(Dimension(5, 20)))
    }

    private fun createSeparator(actionToolbarPanel: JPanel) {
        val separator = JSeparator(JSeparator.VERTICAL)
        separator.preferredSize = Dimension(2, 20)
        separator.setUI(object : BasicSeparatorUI() {
            override fun paint(g: Graphics, c: JComponent) {
                val g2d = g.create() as Graphics2D
                g2d.paint = if(isDarkMode()) separatorColorDark else separatorColorLight
                g2d.fillRect(0, 0, c.width, c.height)
                g2d.dispose()
            }
        })
        actionToolbarPanel.add(separator)
        actionToolbarPanel.add(Box.createRigidArea(Dimension(5, 20)))
    }

    private fun addFilterActions(actionToolbarPanel: JPanel) {
        actionToolbarPanel.add(SeverityFilterButton(project, "I", Severity.INFO))
        actionToolbarPanel.add(Box.createRigidArea(Dimension(3, 0)))
        actionToolbarPanel.add(SeverityFilterButton(project,"L", Severity.LOW))
        actionToolbarPanel.add(Box.createRigidArea(Dimension(3, 0)))
        actionToolbarPanel.add(SeverityFilterButton(project,"M", Severity.MEDIUM))
        actionToolbarPanel.add(Box.createRigidArea(Dimension(3, 0)))
        actionToolbarPanel.add(SeverityFilterButton(project,"H", Severity.HIGH))
        actionToolbarPanel.add(Box.createRigidArea(Dimension(3, 0)))
        actionToolbarPanel.add(SeverityFilterButton(project,"C", Severity.CRITICAL))
    }

    override fun dispose() = Unit
}