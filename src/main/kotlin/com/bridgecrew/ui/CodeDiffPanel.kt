package com.bridgecrew.ui

import com.bridgecrew.results.BaseCheckovResult
import com.bridgecrew.utils.*
import com.github.difflib.text.DiffRow
import com.github.difflib.text.DiffRowGenerator
import com.intellij.util.ui.JBUI
import org.slf4j.LoggerFactory
import java.awt.Dimension
import java.awt.Font
import javax.swing.*
import javax.swing.text.SimpleAttributeSet
import javax.swing.text.StyleConstants

class CodeDiffPanel(val result: BaseCheckovResult, isErrorBubble: Boolean): JPanel() {

    private val logger = LoggerFactory.getLogger(javaClass)
    var hasDiff = false
    private val codeFont = Font("JetBrains Mono", Font.BOLD, 12)

    init {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        border = BorderFactory.createEmptyBorder(0, 10, 0, 10)

        val fixHolder = JPanel()
        fixHolder.layout = BoxLayout(fixHolder, BoxLayout.Y_AXIS)
        fixHolder.background = if(isDarkMode()) BLOCK_BG_DARK else BLOCK_BG_LIGHT
        val generator = DiffRowGenerator.create()
                .inlineDiffByWord(true)
                .build()

        val oldCode = buildCodeBlock()
        var newCode = buildFixLines()
        if(!isErrorBubble){
            newCode=buildFix()
        }
        val rows = generator.generateDiffRows(oldCode, newCode)
        val firstDiffRow = rows.find { it.tag != DiffRow.Tag.EQUAL &&
            it.newLine.trim().isNotEmpty() && it.newLine.trim().toDoubleOrNull() == null }
        updateFirstDiffLine(firstDiffRow)
        rows.filter { it.tag != DiffRow.Tag.EQUAL }.forEach { row ->
            if(row.oldLine.trim().isNotEmpty()){
                val vulBlock = createCodeBlock(row.oldLine.trim())
                vulBlock.background = if(isDarkMode()) VULNERABLE_COLOR_DARK else VULNERABLE_COLOR_LIGHT
                fixHolder.add(vulBlock)
                hasDiff=true
            }
            if(row.newLine.trim().isNotEmpty() && row.newLine.trim().toDoubleOrNull() == null){
                val fixBlock = createCodeBlock(row.newLine.trim())
                fixBlock.background = if(isDarkMode()) FIX_COLOR_DARK else FIX_COLOR_LIGHT
                fixHolder.add(fixBlock)
                hasDiff=true
            }
        }
        fixHolder.add(Box.createVerticalGlue())
        add(fixHolder)
    }

    private fun updateFirstDiffLine(diffRow: DiffRow?) {
        if (diffRow == null) {
            return
        }

        try {
            result.codeDiffFirstLine = diffRow.newLine.split(" ")[0].toInt()
        } catch (e: Exception) {
            logger.debug("Could not update first diff line from new line \"${diffRow.newLine}\"", e)
        }
    }

    private fun createCodeBlock(innerText: String): JTextPane {
        val textArea = JTextPane()
        textArea.text = innerText
        textArea.font = codeFont
        textArea.isEditable = false
        val attributes = SimpleAttributeSet()
        StyleConstants.setLineSpacing(attributes, 0f)
        StyleConstants.setForeground(attributes, if(isDarkMode()) FONT_COLOR_DARK else FONT_COLOR_LIGHT)
        textArea.setParagraphAttributes(attributes, true)
        textArea.margin = JBUI.insets(5, 5, 2, 5)
        textArea.maximumSize = Dimension(Int.MAX_VALUE, 5)
        return textArea
    }

    private fun buildCodeBlock(): ArrayList<String> {
        val codeBlock = arrayListOf<String>()
            result.codeBlock.forEach { block ->
                var currentLine = (block[0] as Double).toInt()
                val code = block[1]
                codeBlock += "$currentLine\t$code".replace("\n", "")
            }

        return codeBlock
    }

    private fun buildFix(): ArrayList<String> {
        val fixWithRowNumber = arrayListOf<String>()
        if (result.codeBlock.isNotEmpty()) {
            var currentLine = (result.codeBlock[0][0] as Double).toInt()
            result.fixDefinition?.split("\n")?.forEach { codeRow ->
                fixWithRowNumber += "$currentLine\t$codeRow"
                currentLine++
            }
        }
        return fixWithRowNumber
    }

    private fun buildFixLines(): ArrayList<String> {
        val fixWithRowNumber = arrayListOf<String>()
        if (result.codeBlock.isNotEmpty()) {
            var currentLine = (result.codeBlock[0][0] as Double).toInt()
            result.fixDefinition?.split("\n")?.forEach { codeRow ->
                fixWithRowNumber += "$currentLine\t$codeRow".replace("\n", "")
                currentLine++
            }
        }
        return fixWithRowNumber
    }
}