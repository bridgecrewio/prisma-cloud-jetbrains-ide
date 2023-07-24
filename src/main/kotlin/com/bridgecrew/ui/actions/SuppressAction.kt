package com.bridgecrew.ui.actions

import com.bridgecrew.results.BaseCheckovResult
import com.bridgecrew.services.CheckovResultsListUtils
import com.bridgecrew.results.Category
import com.bridgecrew.results.VulnerabilityCheckovResult
import com.bridgecrew.settings.CheckovGlobalState
import com.bridgecrew.ui.CheckovToolWindowManagerPanel
import com.bridgecrew.ui.SuppressionDialog
import com.bridgecrew.ui.buttons.SuppressionLinkButton
import com.bridgecrew.utils.FileType
import com.bridgecrew.utils.PANELTYPE
import com.bridgecrew.utils.getFileType
import com.bridgecrew.utils.navigateToFile
import com.intellij.ide.DataManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.LocalFileSystem
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.io.File
import javax.swing.JButton

class SuppressAction(private val buttonInstance: JButton, private var result: BaseCheckovResult) : ActionListener {
    private var isOpenDialog: Boolean = true

    override fun actionPerformed(e: ActionEvent?) {
        val fileType = getFileType(result.filePath)

        val dialog = SuppressionDialog()
        if (isOpenDialog) {
            dialog.show()
            isOpenDialog = false
            disableButton()
            if (dialog.exitCode == DialogWrapper.OK_EXIT_CODE) {
                buttonInstance.isEnabled = false
                generateComment(fileType, dialog.userJustification)
            } else if (dialog.exitCode == DialogWrapper.CANCEL_EXIT_CODE) {
                isOpenDialog = true
                enableButton()
            }
        }
    }

    private fun disableButton() {
        if (buttonInstance is SuppressionLinkButton) {
            buttonInstance.setDisabledLook()
        } else {
            buttonInstance.isEnabled = false
        }
    }

    private fun enableButton() {
        if (buttonInstance is SuppressionLinkButton) {
            buttonInstance.setEnabledLook()
        } else {
            buttonInstance.isEnabled = true
        }
    }

    private fun generateComment(fileType: FileType, userReason: String?) {
        val suppressionComment = generateCheckovSuppressionComment(userReason, fileType)
        val document = getDocument(result.absoluteFilePath)
        val lineNumber = getLineNumber(fileType)
        if (document != null && !isSuppressionExists(document, lineNumber, suppressionComment) && !isSuppressionExists(document, lineNumber + 1, suppressionComment)) {
            addTextToFile(document, lineNumber, suppressionComment)
        }
    }

    private fun getDocument(filePath: String): Document? {
        val file = LocalFileSystem.getInstance().findFileByPath(filePath) ?: return null
        return FileDocumentManager.getInstance().getDocument(file)
    }

    private fun isSuppressionExists(document: Document, lineNumber: Int, suppressionComment: String): Boolean {
        val checkLineNumber = if (lineNumber == 0) 0 else lineNumber - 1
        val lineStartOffset = document.getLineStartOffset(checkLineNumber)
        val lineEndOffset = document.getLineEndOffset(checkLineNumber)
        val lineText = document.getText(TextRange(lineStartOffset, lineEndOffset)).trim()
        val existingList = lineText.split(" ").filter { existingWord -> suppressionComment.split(" ").contains(existingWord) && existingWord.lowercase().contains("checkov") }
        return existingList.isNotEmpty()
    }

    private fun getLineNumber(fileType: FileType): Int {
        if (fileType == FileType.DOCKERFILE) {
            return 0
        }
        return result.fileLineRange[0]
    }

    private fun generateCheckovSuppressionComment(userReason: String?, fileType: FileType): String {
        val reason = if (userReason.isNullOrEmpty()) "ADD REASON" else userReason
        val skip = if (result.category == Category.VULNERABILITIES) (result as VulnerabilityCheckovResult).violationId else result.id
        val comment = "checkov:skip=${skip}: $reason"
        return when(fileType) {
            FileType.TEXT, FileType.GEMFILE -> "#$comment"
            FileType.XML, FileType.CSPROJ -> "<!--$comment-->"
            FileType.GOLANG, FileType.KOTLIN, FileType.GRADLE  -> "//$comment"
            FileType.JSON -> comment
            else -> "#$comment"
        }
    }

    private fun addTextToFile(document: Document, lineNumber: Int, suppressionComment: String) {
        val insertionOffset = document.getLineStartOffset(lineNumber)
        val dataContext = DataManager.getInstance().dataContext
        val project = dataContext.getData("project") as Project

        val textLine = document.getText(TextRange(insertionOffset, document.getLineStartOffset(lineNumber)))
        val matchSpacesBeforeComment = Regex("^[\\s\\t]+").find(textLine)
        val addSpacesBeforeComment = if(matchSpacesBeforeComment?.value !== null) matchSpacesBeforeComment.value else ""

        WriteCommandAction.runWriteCommandAction(null) {
            val editor = EditorFactory.getInstance().createEditor(document, null)
            val newLineText = "${addSpacesBeforeComment}${suppressionComment}\n"
            document.insertString(insertionOffset, newLineText)
            editor.caretModel.moveToOffset(insertionOffset + newLineText.length)
            navigateToFile(project, result.absoluteFilePath, lineNumber)
        }

        CheckovGlobalState.suppressedVulnerabilitiesToIgnore.add(CheckovResultsListUtils.cloneCheckovResultWithModifiedFields(project, result, result.fileLineRange, result.codeBlock))
        CheckovGlobalState.filePathsToIgnore[result.filePath.removePrefix(File.separator)] = System.currentTimeMillis()
        CheckovResultsListUtils.modifyBaseCheckovResultLineNumbers(project, result, lineNumber, 1)
        project.service<CheckovToolWindowManagerPanel>().loadMainPanel(PANELTYPE.CHECKOV_FILE_SCAN_FINISHED, result.filePath)
//        FileDocumentManager.getInstance().saveDocument(document)
    }
}