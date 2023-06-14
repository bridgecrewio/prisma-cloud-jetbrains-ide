package com.bridgecrew.ui

import com.bridgecrew.utils.createGridRowCol
import com.intellij.icons.AllIcons
import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.uiDesigner.core.GridConstraints
import com.intellij.uiDesigner.core.GridLayoutManager
import com.intellij.util.ui.UIUtil
import com.intellij.util.ui.JBUI
import icons.CheckovIcons
import com.intellij.openapi.actionSystem.ActionManager
import icons.CheckovIcons.pluginLargeIcon
import java.awt.*
import javax.swing.*

class CheckovToolWindowDescriptionPanel(val project: Project) : SimpleToolWindowPanel(true, true) {
    private var descriptionPanel: JPanel = JPanel()

    init {
        initializationDescription()
    }

    /**
     * Create display of description before scanning.
     */

    fun emptyDescription(): JPanel {
        descriptionPanel = JPanel()
        descriptionPanel.background = UIUtil.getEditorPaneBackground() ?: descriptionPanel.background
        return descriptionPanel
    }

    fun noErrorsPanel(): JPanel {
        return createStatusScreenWithIcon("Great Job - Your Code Is Valid!")
    }

    fun initializationDescription(): JPanel {
        descriptionPanel = JPanel()
        descriptionPanel.layout = GridLayoutManager(2, 1, JBUI.emptyInsets(), -1, -1)
        descriptionPanel.background = UIUtil.getEditorPaneBackground() ?: descriptionPanel.background
        val imagePanel = createImagePanel()
        val scanningPanel = JPanel()
        scanningPanel.background = UIUtil.getEditorPaneBackground()
        scanningPanel.add(JLabel("Prisma Cloud is being initialized"),  createGridRowCol(1,0, GridConstraints.ANCHOR_NORTH))
        descriptionPanel.add(imagePanel, createGridRowCol(0,0,GridConstraints.ANCHOR_NORTHEAST))
        descriptionPanel.add(scanningPanel, createGridRowCol(1,0,GridConstraints.ANCHOR_NORTH))
        return descriptionPanel
    }

    fun preScanDescription(): JPanel {
        return createStatusScreenWithIcon("Let's Scan Your Project For Code Security Issues", true)
    }

    fun configurationDescription(): JPanel {
        descriptionPanel = JPanel()
        descriptionPanel.layout = GridLayoutManager(2, 1, JBUI.emptyInsets(), -1, -1)
        val imagePanel = createImagePanel()
        descriptionPanel.background = UIUtil.getEditorPaneBackground() ?: descriptionPanel.background
        val configPanel = JPanel()
        configPanel.background = UIUtil.getEditorPaneBackground()
        configPanel.add(CheckovSettingsPanel(project), GridConstraints.ANCHOR_CENTER)
        descriptionPanel.add(imagePanel, createGridRowCol(0,0,GridConstraints.ANCHOR_NORTHEAST))
        descriptionPanel.add(configPanel,  createGridRowCol(1,0,GridConstraints.ANCHOR_NORTH))
        return descriptionPanel
    }

    fun duringScanDescription(description: String): JPanel {
        descriptionPanel = JPanel()
        descriptionPanel.layout = GridLayoutManager(2, 1, JBUI.emptyInsets(), -1, -1)
        descriptionPanel.background = UIUtil.getEditorPaneBackground() ?: descriptionPanel.background
        val imagePanel = createImagePanel()
        val scanningPanel = JPanel()
        scanningPanel.add(JLabel(description), GridConstraints.ANCHOR_CENTER)
        scanningPanel.background = UIUtil.getEditorPaneBackground()
        descriptionPanel.add(imagePanel, createGridRowCol(0,0,GridConstraints.ANCHOR_NORTHEAST))
        descriptionPanel.add(scanningPanel, createGridRowCol(1,0,GridConstraints.ANCHOR_NORTH))
        return descriptionPanel
    }

    fun failedScanDescription(): JPanel {
        descriptionPanel = JPanel()
        descriptionPanel.layout = GridLayoutManager(2, 1, JBUI.emptyInsets(), -1, -1)
        descriptionPanel.background = UIUtil.getEditorPaneBackground() ?: descriptionPanel.background
        val imagePanel = createImagePanel()
        val scanningPanel = JPanel()
        scanningPanel.background = UIUtil.getEditorPaneBackground()
        scanningPanel.add(JLabel("Scan failed to run, please check the logs for further action"), GridConstraints.ANCHOR_CENTER)
        descriptionPanel.add(imagePanel, createGridRowCol(0,0,GridConstraints.ANCHOR_NORTHEAST))
        descriptionPanel.add(scanningPanel, createGridRowCol(1,0,GridConstraints.ANCHOR_NORTH))
        return descriptionPanel
    }

    private fun createImagePanel(): JPanel {
        val imagePanel = JPanel()
        imagePanel.layout = GridLayoutManager(2, 2, JBUI.emptyInsets(), -1, -1)
        imagePanel.background = UIUtil.getEditorPaneBackground()
        imagePanel.add(JLabel(pluginLargeIcon), createGridRowCol(0,0, GridConstraints.ANCHOR_CENTER))
        imagePanel.add(JLabel("Prisma Cloud"), createGridRowCol(1,0, GridConstraints.ANCHOR_NORTHEAST))
        imagePanel.add(JLabel("  "), createGridRowCol(0,1, GridConstraints.ANCHOR_NORTHEAST))
        return imagePanel
    }

    private fun createStatusScreenWithIcon(text: String, isAddScan: Boolean = false): JPanel {
        val mainPanel = JPanel()
        mainPanel.background = UIUtil.getEditorPaneBackground()
        val imagePanel = JPanel()
        imagePanel.layout = BoxLayout(imagePanel, BoxLayout.Y_AXIS)
        imagePanel.background = UIUtil.getEditorPaneBackground()
        val status = JLabel(text)
        status.alignmentX = CENTER_ALIGNMENT
        status.font = Font(status.font.name, Font.BOLD, 14)
        status.foreground = Color.decode("#7F8B91")
        val iconLabel = JLabel(CheckovIcons.prismaIcon)
        iconLabel.alignmentX = CENTER_ALIGNMENT
        val prismaText = JLabel("Prisma Cloud")
        prismaText.alignmentX = CENTER_ALIGNMENT
        prismaText.font = Font("SF Pro Text", Font.BOLD, 10)
        prismaText.foreground = Color.decode("#7F8B91")
        imagePanel.add(Box.createRigidArea(Dimension(0, 50)))
        imagePanel.add(Box.createVerticalGlue())
        imagePanel.add(status)
        imagePanel.add(Box.createRigidArea(Dimension(0, 15)))

        if (isAddScan) {
            val scanButton = createScanButton()
            imagePanel.add(scanButton)
            imagePanel.add(Box.createRigidArea(Dimension(0, 15)))
        }

        imagePanel.add(iconLabel)
        imagePanel.add(Box.createRigidArea(Dimension(0, 10)))
        imagePanel.add(prismaText)
        imagePanel.add(Box.createVerticalGlue())
        mainPanel.add(imagePanel, BorderLayout.CENTER)
        return mainPanel
    }

    private fun createScanButton(): JButton {
        val scanButton = JButton("Scan")
        scanButton.alignmentX = CENTER_ALIGNMENT
        scanButton.addActionListener {
            val scanAction = ActionManager.getInstance().getAction("com.bridgecrew.ui.actions.CheckovScanAction")
            val dataContext = DataManager.getInstance().getDataContext(scanButton)
            val action = AnActionEvent.createFromDataContext(ActionPlaces.UNKNOWN, null, dataContext)
            action.presentation.icon = AllIcons.Actions.Execute
            scanAction.actionPerformed(action)
        }
        return scanButton
    }
}