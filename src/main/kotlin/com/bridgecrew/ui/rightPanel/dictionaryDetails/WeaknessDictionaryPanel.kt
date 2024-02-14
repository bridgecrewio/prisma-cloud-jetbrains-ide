package com.bridgecrew.ui.rightPanel.dictionaryDetails

import com.bridgecrew.DataFlow
import com.bridgecrew.results.WeaknessCheckovResult
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.LogicalPosition
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.ui.JBColor
import com.intellij.util.ui.JBUI
import java.awt.Cursor
import java.awt.Dimension
import java.awt.Font
import java.awt.GridBagConstraints
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import javax.swing.JLabel

data class DataFlowDictionary(
        val fileName: String,
        val path: String,
        val codeBlock: String,
        val column: Int,
        val row: Int,
)

class WeaknessDictionaryPanel(private val result: WeaknessCheckovResult, private val project: Project) : DictionaryExtraInfoPanel() {
    override var fieldsMap: MutableMap<String, Any?> = mutableMapOf(
            "Description" to result.description,
            "Code" to extractCode(result),
            "Data flow" to extractDataFlow(result)
    )

    init {
        setFieldsMap(result)
        addCustomPolicyGuidelinesIfNeeded(result)
        createDictionaryLayout()
        createDataFlowLayout()
    }

    private fun setFieldsMap(result: WeaknessCheckovResult){
        if (!result.owasp.isNullOrEmpty()) {
            fieldsMap["OWASP Top 10"] = result.owasp.joinToString(", ")
        }

        if (!result.cwe.isNullOrEmpty()) {
            fieldsMap["CWE(s)"] = result.cwe.joinToString(", ")
        }
    }

    private fun extractCode(result: WeaknessCheckovResult): Any {
        return try {
            result.codeBlock[0][1].toString().trim()
        } catch (e: Exception) {
            ""
        }
    }

    private fun extractDataFlow(result: WeaknessCheckovResult): String? {
        val dataFlow = result.metadata?.code_locations ?: result.metadata?.taint_mode?.data_flow
        if (dataFlow !== null) {
            return this.calculateDataFlow(dataFlow);
        }
        return null
    }

    private fun calculateDataFlow(dataFlowList: List<DataFlow>): String {
        val filesCount = dataFlowList.map { it.path }.distinct().count()
        val stepsCount = dataFlowList.count()
        return "$stepsCount steps in $filesCount file(s)"
    }


    private fun getDataFlowDictionary(result: WeaknessCheckovResult): Array<DataFlowDictionary>? {
        val dataFlow = result.metadata?.code_locations ?: result.metadata?.taint_mode?.data_flow

        if (dataFlow !== null) {
            return dataFlow.map {
                val line = it.start.row.toString() + if (it.start.row != it.end.row) "-${it.end.row}" else ""
                DataFlowDictionary("${File(it.path).name}: $line", it.path, it.code_block, it.start.column, it.start.row)
            }.toTypedArray()
        }
        return null
    }

    private fun openFileAtLine(project: Project, absPath: String, line: Int, column: Int) {
        val virtualFile = LocalFileSystem.getInstance().findFileByPath(absPath)
        if (virtualFile != null) {
            val editor: Editor? = FileEditorManager.getInstance(project).openTextEditor(OpenFileDescriptor(project, virtualFile, line, column), true)
            editor?.caretModel?.moveToLogicalPosition(LogicalPosition(line, column))
        }
    }


    private fun createDataFlowLayout() {
        val dataArray = this.getDataFlowDictionary(this.result) ?: return
        val dictionaryFont = Font("SF Pro Text", Font.BOLD, 12)

        val maxKeyWidth = dataArray.maxOfOrNull {
            getFontMetrics(dictionaryFont).stringWidth(it.fileName)
        } ?: 0

        val keyConstraints = GridBagConstraints().apply {
            weightx = 0.0
            fill = GridBagConstraints.VERTICAL
            anchor = GridBagConstraints.LINE_START
            insets = JBUI.insets(1, 0, 1, 15)
        }

        val valueConstraints = GridBagConstraints().apply {
            weightx = 1.0
            fill = GridBagConstraints.HORIZONTAL
            gridwidth = GridBagConstraints.REMAINDER
            insets = JBUI.insetsBottom(10)
        }

        val boldFont = Font(dictionaryFont.name, Font.BOLD, dictionaryFont.size)

        for (item in dataArray) {
            val valueAsString = if (item.codeBlock.isEmpty()) "---" else item.codeBlock.trim()
            // Create a clickable JLabel for the key
            val keyLabel = JLabel("<html><a href=''>${item.fileName}</a></html>")
            keyLabel.font = boldFont
            keyLabel.preferredSize = Dimension(maxKeyWidth + 50, keyLabel.preferredSize.height)
            keyLabel.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
            keyLabel.toolTipText = item.fileName
            keyLabel.setOpaque(true); // Make the JLabel opaque to show the background color
            keyLabel.setBackground(JBColor.PanelBackground);
            // Add mouse listener to the key label
            keyLabel.addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    openFileAtLine(project, item.path, item.row - 1, item.column)
                }
            })

            add(keyLabel, keyConstraints)
            val valueLabel = JLabel(valueAsString)
            valueLabel.font = dictionaryFont.deriveFont(Font.PLAIN)
            valueLabel.toolTipText = valueAsString
            add(valueLabel, valueConstraints)
        }
    }

}