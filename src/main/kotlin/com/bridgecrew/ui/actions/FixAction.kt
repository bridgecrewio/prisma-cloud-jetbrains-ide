package com.bridgecrew.ui.actions

import com.bridgecrew.FixCommand
import com.bridgecrew.listeners.CheckovScanListener
import com.bridgecrew.listeners.ErrorBubbleFixListener
import com.bridgecrew.results.BaseCheckovResult
import com.bridgecrew.results.Category
import com.bridgecrew.results.VulnerabilityCheckovResult
import com.bridgecrew.services.scan.CheckovScanService
import com.bridgecrew.settings.CheckovGlobalState
import com.bridgecrew.utils.navigateToFile
import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.DataKey
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import org.slf4j.LoggerFactory
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.JButton

class FixAction(private val buttonInstance: JButton, val result: BaseCheckovResult) : ActionListener {

    init {
        DataManager.getInstance().dataContextFromFocusAsync.then { dataContext ->
            dataContext.getData(DataKey.create<Project>("project"))?.let {
                val connection = it.messageBus.connect()
                val inScanMsg = "Scan in progress. Please wait for completion before retrying."
                buttonInstance.isEnabled = !CheckovGlobalState.scanInProgress
                connection.subscribe(CheckovScanListener.SCAN_TOPIC, object : CheckovScanListener {
                    override fun fileScanningStarted() {
                        buttonInstance.isEnabled = false
                        buttonInstance.toolTipText = inScanMsg
                        CheckovGlobalState.scanInProgress = true
                    }

                    override fun projectScanningStarted() {
                        buttonInstance.isEnabled = false
                        buttonInstance.toolTipText = inScanMsg
                        CheckovGlobalState.scanInProgress = true
                    }

                    override fun scanningFinished(scanSourceType: CheckovScanService.ScanSourceType) {
                        buttonInstance.isEnabled = true
                        buttonInstance.toolTipText = ""
                        CheckovGlobalState.scanInProgress = false
                    }

                    override fun fullScanFailed() {
                        buttonInstance.isEnabled = true
                        buttonInstance.toolTipText = ""
                        CheckovGlobalState.scanInProgress = false
                    }
                })
            }
        }
    }

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun actionPerformed(e: ActionEvent?) {
        ApplicationManager.getApplication().invokeLater {
            if (result.category == Category.VULNERABILITIES) {
                handleSCEFixes()
            } else {
                buttonInstance.isEnabled = false
                applyFixDefinition()
                val project = ProjectManager.getInstance().defaultProject
                project.messageBus.syncPublisher(ErrorBubbleFixListener.ERROR_BUBBLE_FIX_TOPIC).fixClicked()
            }
        }

    }

    private fun applyFixDefinition(): Boolean {
        try {
            val startLine: Int = getLineByIndex(0)
            val endLine: Int = getLineByIndex(1)

            val virtualFile: VirtualFile = LocalFileSystem.getInstance().findFileByPath(result.absoluteFilePath)
                    ?: return false
            val document: Document? = FileDocumentManager.getInstance().getDocument(virtualFile)

            val startOffset = document!!.getLineStartOffset(startLine)
            val endOffset = document.getLineEndOffset(endLine)

            DataManager.getInstance().dataContextFromFocusAsync.then { dataContext ->
                dataContext.getData(DataKey.create<Project>("project"))?.let {
                    WriteCommandAction.runWriteCommandAction(it) {
                        document.replaceString(startOffset, endOffset, result.fixDefinition!!)
                        FileDocumentManager.getInstance().saveDocument(document)
                        navigateToFile(it, virtualFile, result.codeDiffFirstLine)
                    }
                }
            }

        } catch (e: Exception) {
            logger.warn("error while trying to apply fix", e)
            buttonInstance.isEnabled = true
            return false
        }
        return true
    }

    private fun getLineByIndex(index: Int): Int {
        val line: Int = result.fileLineRange.getOrElse(index) { 0 }
        return if(line > 0) line - 1 else line
    }

    private fun handleSCEFixes() {
        try {
            val fixCommand = (result as VulnerabilityCheckovResult).fixCommand
            if(fixCommand != null) {
                CheckovGlobalState.filesNotToScanAfterFix.add(result.absoluteFilePath)
                if(fixCommand.manualCodeFix) {
                    val isSuccess = applyFixDefinition()
                    showSCAFixModal(fixCommand, isSuccess)
                } else {
                    showSCAFixModal(fixCommand, true)
                }
            }
        } catch (e: Exception) {
            logger.warn("error while trying to apply SCA fix", e)
            buttonInstance.isEnabled = true
        }
    }

    private fun showSCAFixModal(fixCommand: FixCommand, isSuccess: Boolean) {
        DataManager.getInstance().dataContextFromFocusAsync.then { dataContext ->
            dataContext.getData(DataKey.create<Project>("project"))?.let {
                Messages.showInfoMessage(
                    it,
                    buildModalMessage(fixCommand, isSuccess),
                    "SCA Fix - Additional Action Required"
                )
            }
        }
    }

    private fun buildModalMessage(fixCommand: FixCommand, isSuccess: Boolean): String {
        var message = "${fixCommand.msg}\n${fixCommand.cmds.joinToString("\n")}"
        if(fixCommand.manualCodeFix) {
            message = if(isSuccess) {
                val firstLine = getLineByIndex(0)
                val lastLine = getLineByIndex(1)
                val lineMsg = if(firstLine == lastLine) "in line $firstLine" else "between lines $firstLine and $lastLine"
                "We added a change to ${result.filePath} $lineMsg\n" + message
            } else {
                "Couldn't apply a code fix to this SCA vulnerability"
            }
        }
        return message
    }
}
