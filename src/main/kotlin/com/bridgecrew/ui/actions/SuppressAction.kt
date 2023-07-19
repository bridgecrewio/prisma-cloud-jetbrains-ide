package com.bridgecrew.ui.actions

import com.bridgecrew.results.BaseCheckovResult
import com.bridgecrew.settings.CheckovGlobalState
import com.bridgecrew.ui.SuppressionDialog
import com.bridgecrew.ui.buttons.SuppressionLinkButton
import com.bridgecrew.utils.FileType
import com.bridgecrew.utils.getFileType
import com.bridgecrew.utils.navigateToFile
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.LocalFileSystem
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
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
        val lineText = document.getText(TextRange(lineStartOffset, lineEndOffset)).trimEnd()
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
        val comment = "checkov:skip=${result.id}: $reason"
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
        CheckovGlobalState.suppressedFileToIgnore = result.filePath
        WriteCommandAction.runWriteCommandAction(null) {
            val editor = EditorFactory.getInstance().createEditor(document, null)
            val newLineText = "${suppressionComment}\n"

            val project = ProjectManager.getInstance().defaultProject

            document.insertString(insertionOffset, newLineText)
            editor.caretModel.moveToOffset(insertionOffset + newLineText.length)
            navigateToFile(project, result.absoluteFilePath, lineNumber + 1)

        }

        FileDocumentManager.getInstance().saveDocument(document)
    }
}