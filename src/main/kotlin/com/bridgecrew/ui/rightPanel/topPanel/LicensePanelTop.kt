package com.bridgecrew.ui.rightPanel.topPanel

import com.bridgecrew.results.BaseCheckovResult
import com.bridgecrew.ui.buttons.DocumentationButton
import com.bridgecrew.utils.CheckovUtils
import com.intellij.openapi.project.Project
import java.awt.GridBagConstraints
import javax.swing.JPanel

class LicensePanelTop(project: Project, result: BaseCheckovResult): CheckovDescriptionPanelTop(project, result) {

    init {
        val gbc = GridBagConstraints()
        gbc.fill = GridBagConstraints.HORIZONTAL
        gbc.weightx = 1.0
        val title = createTitleAndIcon(CheckovUtils.createLicenseTitle(result), result.severity)
        add(title, gbc)

        gbc.fill = GridBagConstraints.NONE
        gbc.weightx = 0.0
        gbc.gridx = 1
        val actions = createDescriptionPanelTitleActions()
        add(actions, gbc)
    }

    private fun createDescriptionPanelTitleActions(): JPanel {
        val panel = createActionsPanel()
        if (isShowDocumentationButton(result)) {
            panel.add(result.guideline?.let { DocumentationButton(it) })
        }
        createSuppressionButton(panel)
        return panel
    }
}