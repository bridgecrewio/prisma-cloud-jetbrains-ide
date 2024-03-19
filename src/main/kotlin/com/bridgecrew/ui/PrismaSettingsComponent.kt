package com.bridgecrew.ui

import com.bridgecrew.utils.FULL_SCAN_RERO_LIMIT
import com.intellij.util.ui.JBUI
import java.awt.*
import java.text.NumberFormat
import javax.swing.JFormattedTextField
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.text.NumberFormatter

class PrismaSettingsComponent {
    private var rootPanel: JPanel = JPanel()
    val secretKeyField: JTextField = JTextField()
    val accessKeyField: JTextField = JTextField()
    val certificateField: JTextField = JTextField()
    val prismaURLField: JTextField = JTextField()
    var fullScanRepoLimitField: JFormattedTextField

    init {
        rootPanel.layout = GridBagLayout()
        fullScanRepoLimitField = initFullScanRepoLimitField()
        val settingsPanel = JPanel(GridBagLayout())

        val constraints = GridBagConstraints()
        constraints.fill = GridBagConstraints.HORIZONTAL
        constraints.anchor = GridBagConstraints.NORTHWEST
        constraints.insets = JBUI.insets(0, 0, 5, 20)

        createSettingsRow(settingsPanel, constraints, "Access Key (Required):", accessKeyField, 0)
        createSettingsRow(settingsPanel, constraints, "Secret Key (Required):", secretKeyField, 1)
        createSettingsRow(settingsPanel, constraints, "Prisma URL (Required):", prismaURLField, 2)
        createSettingsRow(settingsPanel, constraints, "CA-Certificate:", certificateField, 3)
        createSettingsRow(settingsPanel, constraints, "SAST full scan size limit (MB):", fullScanRepoLimitField, 4)

        constraints.gridx = 0
        constraints.gridy = 0
        constraints.fill = GridBagConstraints.VERTICAL
        rootPanel.add(settingsPanel, constraints)

        //add an empty panel to place the settings on the top left
        constraints.gridy = 1
        constraints.weighty = 1.0
        constraints.weightx = 1.0
        rootPanel.add(JPanel(), constraints)
    }

    private fun createSettingsRow(settingsPanel: JPanel, constraints: GridBagConstraints, keyText: String, inputField: JTextField, gridY: Int) {
        constraints.gridx = 0
        constraints.gridy = gridY
        constraints.ipady = 10
        val accessKeyLabel = JLabel(keyText)
        accessKeyLabel.labelFor = accessKeyField
        settingsPanel.add(accessKeyLabel, constraints)
        constraints.gridx = 1
        constraints.ipady = 0
        inputField.preferredSize = Dimension(380, inputField.preferredSize.height)
        settingsPanel.add(inputField, constraints)
    }

    fun getPanel(): JPanel {
        return rootPanel
    }

    private fun initFullScanRepoLimitField(): JFormattedTextField {
        val format = NumberFormat.getIntegerInstance()
        format.setGroupingUsed(false)

        val formatter = NumberFormatter(format)
        formatter.setMinimum(0)
        formatter.setMaximum(Int.MAX_VALUE)
        formatter.setCommitsOnValidEdit(true)

        val field = JFormattedTextField(formatter)

        field.value = FULL_SCAN_RERO_LIMIT

        return field
    }
}