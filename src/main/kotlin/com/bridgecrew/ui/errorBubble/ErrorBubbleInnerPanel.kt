package com.bridgecrew.ui.errorBubble

import com.bridgecrew.results.BaseCheckovResult
import com.bridgecrew.results.Category
import com.bridgecrew.results.LicenseCheckovResult
import com.bridgecrew.results.VulnerabilityCheckovResult
import com.bridgecrew.ui.CodeDiffPanel
import com.bridgecrew.ui.rightPanel.dictionaryDetails.VulnerabilitiesDictionaryPanel
import com.bridgecrew.utils.UNKNOWN_LICENSES_DESCRIPTION
import com.bridgecrew.utils.VIOLATED_LICENSES_DESCRIPTION
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBScrollPane
import java.awt.*
import javax.swing.*


class ErrorBubbleInnerPanel(val project: Project, val result: BaseCheckovResult, private val vulnerabilityCount: Int, index: Int, total: Int, callback: navigationCallback) : JPanel() {

    companion object {
        const val MIN_INNER_PANEL_HEIGHT = 75
        const val PANEL_HEIGHT = 200
        const val PANEL_WIDTH = 550
        const val MAX_TITLE_TEXT_WIDTH = 420
    }

    init {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)

        val topPanel = ErrorBubbleTopPanel(result, getTitleByCategory(), index, total, callback)
        topPanel.alignmentX = Component.LEFT_ALIGNMENT
        add(topPanel)

        add(JSeparator(JSeparator.HORIZONTAL), BorderLayout.SOUTH)
        add(Box.createRigidArea(Dimension(0, 5)))

        addCenterPanelByCategory()

        add(Box.createRigidArea(Dimension(0, 5)))
        val actionsPanel = ErrorBubbleActionsPanel(project, result)
        actionsPanel.alignmentX = Component.LEFT_ALIGNMENT
        actionsPanel.border = BorderFactory.createEmptyBorder(0, 25, 0, 0)
        add(actionsPanel)

        preferredSize = Dimension(PANEL_WIDTH, PANEL_HEIGHT)
    }

    private fun getTitleByCategory(): String {
        return if (result.category == Category.VULNERABILITIES) {
            if (vulnerabilityCount > 1) {
                "Package contains $vulnerabilityCount vulnerabilities"
            } else {
                (result as VulnerabilityCheckovResult).violationId.toString()
            }
        } else if (result.category == Category.LICENSES){
            "${(result as LicenseCheckovResult).policy}/${result.licenseType}"
        } else {
            result.name
        }
    }

    private fun addCenterPanelByCategory() {
        when (result.category) {
            Category.VULNERABILITIES -> {
                val vulnerabilitiesPanel = VulnerabilitiesDictionaryPanel(result as VulnerabilityCheckovResult)
                vulnerabilitiesPanel.alignmentX = Component.LEFT_ALIGNMENT
                vulnerabilitiesPanel.border = BorderFactory.createEmptyBorder(5, 30, 0, 10)

                val scroll = JBScrollPane(vulnerabilitiesPanel)
                scroll.alignmentX = Component.LEFT_ALIGNMENT
                SwingUtilities.invokeLater {
                    scroll.viewport.viewPosition = Point(0, 0)
                }
                add(scroll)
            }

            Category.IAC -> {
                if(result.fixDefinition != null){
                    val codeDiffPanel = CodeDiffPanel(result, false)
                    if(codeDiffPanel.hasDiff){
                        val scroll = JBScrollPane(codeDiffPanel)
                        scroll.border = BorderFactory.createEmptyBorder(1, 30, 0, 0)
                        scroll.alignmentX = Component.LEFT_ALIGNMENT
                        SwingUtilities.invokeLater {
                            scroll.viewport.viewPosition = Point(0, 0)
                        }
                        add(scroll)
                    }else{
                        buildCenterPanel("No automated fix is available")
                    }
                } else {
                    buildCenterPanel("No automated fix is available")
                }
            }

            Category.SECRETS -> {
                buildCenterPanel(result.description ?: "")
            }

            Category.LICENSES -> {
                val text = when (result.id) {
                    "BC_LIC_1" -> {
                        VIOLATED_LICENSES_DESCRIPTION
                    }
                    "BC_LIC_2" -> {
                        UNKNOWN_LICENSES_DESCRIPTION
                    }
                    else -> {
                        "No description available for policy ${result.id}"
                    }
                }
                buildCenterPanel(text)
            }
        }
    }

    private fun buildCenterPanel(text: String) {
        val centerPanel = ErrorBubbleCenterPanel(text)
        centerPanel.alignmentX = Component.LEFT_ALIGNMENT
        centerPanel.border = BorderFactory.createEmptyBorder(5, 30, 0, 0)
        add(centerPanel)
    }
}