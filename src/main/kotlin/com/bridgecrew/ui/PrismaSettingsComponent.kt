package com.bridgecrew.ui

import com.bridgecrew.api.PrismaApiClient
import com.bridgecrew.ui.components.ActionLink
import com.bridgecrew.utils.FULL_SCAN_RERO_LIMIT
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.options.Configurable
import com.intellij.ui.JBColor
import com.intellij.util.ui.JBUI
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.net.URL
import java.text.NumberFormat
import java.util.*
import javax.swing.*
import javax.swing.text.NumberFormatter

class PrismaSettingsComponent(private val configurable: Configurable) {

    private var rootPanel: JPanel = JPanel()
    val secretKeyField: JTextField = JTextField()
    val accessKeyField: JTextField = JTextField()
    val certificateField: JTextField = JTextField()
    val prismaURLField: JTextField = JTextField()
    var fullScanRepoLimitField: JFormattedTextField
    private val validationResults: JLabel = JLabel()

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
        createConnectionTestRow(settingsPanel, constraints, 3)
        createSettingsRow(settingsPanel, constraints, "CA-Certificate:", certificateField, 4)
        createSettingsRow(settingsPanel, constraints, "SAST full scan size limit (MB):", fullScanRepoLimitField, 5)

        constraints.gridy = 6
        constraints.gridx = 1
        validationResults.foreground = JBColor.RED
        settingsPanel.add(validationResults, constraints)

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

    private fun createSettingsRow(
        settingsPanel: JPanel,
        constraints: GridBagConstraints,
        keyText: String,
        inputField: JComponent,
        gridY: Int
    ) {
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

    private fun createConnectionTestRow(
        settingsPanel: JPanel,
        constraints: GridBagConstraints,
        gridY: Int
    ) {
        val resultText = JLabel()
        val testConnectionLink = ActionLink(
            "Test Connection",
            {
                val loginResponse = ApplicationManager.getApplication().getService(PrismaApiClient::class.java).login()
                constraints.gridx = 1
                constraints.gridy = gridY
                if (loginResponse?.token != null) {
                    resultText.text = "Connection successful"
                    resultText.foreground = JBColor.GREEN
                } else {
                    resultText.text = "Connection failed. Please check the log file."
                    resultText.foreground = JBColor.RED
                }
                settingsPanel.add(resultText, constraints)
            },
            preValidation@{
                resultText.text = ""
                if (!isValid()) {
                    return@preValidation false
                }
                configurable.apply()
                return@preValidation true
            }
        )
        constraints.gridx = 0
        constraints.gridy = gridY
        settingsPanel.add(testConnectionLink, constraints)
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

    fun isValid(): Boolean {
        try {
            val url = URL(prismaURLField.text)
            if (url.protocol.equals("http", ignoreCase = true)) {
                validationResults.text = "Prisma URL protocol must be https"
                return false
            }
            if (!url.host.startsWith("api", ignoreCase = true)) {
                validationResults.text = "Prisma URL hostname must begin in 'api'"
                return false
            }
            UUID.fromString(accessKeyField.text)
            validationResults.text = ""
            return true
        } catch (e: Exception) {
            validationResults.text = e.message
            return false
        }
    }
}