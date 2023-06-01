package com.bridgecrew.ui

import com.bridgecrew.settings.CheckovSettingsConfigurable
import com.bridgecrew.utils.createGridRowCol
import com.intellij.openapi.application.ApplicationManager
import com.intellij.uiDesigner.core.GridLayoutManager
import com.intellij.uiDesigner.core.GridConstraints
import javax.swing.*
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.util.ui.JBUI

class CheckovSettingsPanel(project: Project): JPanel() {

    init {

        layout = GridLayoutManager(3, 1, JBUI.emptyInsets(), -1, -1)

        add(JLabel("Prisma Cloud Plugin would scan your infrastructure as code files."), createGridRowCol(0,0, GridConstraints.ANCHOR_CENTER))
        add(JLabel("Please configure a valid Prisma token in order to use this Plugin"), createGridRowCol(1,0, GridConstraints.ANCHOR_CENTER))
        val settingsButton = JButton("Open Settings")

        settingsButton.addActionListener {
            ApplicationManager.getApplication().invokeLater {
                ShowSettingsUtil.getInstance().showSettingsDialog(project, CheckovSettingsConfigurable::class.java)
            }
        }

        add(settingsButton, createGridRowCol(2,0, GridConstraints.ANCHOR_CENTER))

    }
}
