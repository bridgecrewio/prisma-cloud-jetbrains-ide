package com.bridgecrew.ui.actions

import com.bridgecrew.listeners.CheckovScanListener
import com.bridgecrew.listeners.ErrorBubbleFixListener
import com.bridgecrew.results.BaseCheckovResult
import com.bridgecrew.results.Category
import com.bridgecrew.services.scan.CheckovScanService
import com.bridgecrew.utils.navigateToFile
import com.intellij.ide.DataManager
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.JButton
import com.bridgecrew.settings.CheckovGlobalState
import com.intellij.openapi.ui.Messages

class FixAction(private val buttonInstance: JButton, val result: BaseCheckovResult) : ActionListener {

    init {
        val dataContext = DataManager.getInstance().dataContext
        val project = dataContext.getData("project") as Project
        val connection = project.messageBus.connect()
        val inScanMsg = "Scan in progress. Please wait for completion before retrying."
        buttonInstance.isEnabled = !CheckovGlobalState.scanInProgress
        connection.subscribe(CheckovScanListener.SCAN_TOPIC, object : CheckovScanListener {
            override fun fileScanningStarted(){
                buttonInstance.isEnabled = false
                buttonInstance.toolTipText = inScanMsg
                CheckovGlobalState.scanInProgress = true
            }
            override fun projectScanningStarted(){
                buttonInstance.isEnabled = false
                buttonInstance.toolTipText = inScanMsg
                CheckovGlobalState.scanInProgress = true
            }
            override fun scanningFinished(scanSourceType: CheckovScanService.ScanSourceType){
                buttonInstance.isEnabled = true
                buttonInstance.toolTipText = ""
                CheckovGlobalState.scanInProgress = false
            }
            override fun fullScanFailed(){
                buttonInstance.isEnabled = true
                buttonInstance.toolTipText = ""
                CheckovGlobalState.scanInProgress = false
            }
        })


    }

    private val LOG = logger<FixAction>()

    override fun actionPerformed(e: ActionEvent?) {
        buttonInstance.isEnabled = false
        ApplicationManager.getApplication().invokeLater {
            applyFixDefinition()
        }
        val project = ProjectManager.getInstance().defaultProject
        project.messageBus.syncPublisher(ErrorBubbleFixListener.ERROR_BUBBLE_FIX_TOPIC).fixClicked()
    }

    private fun applyFixDefinition() {
        try {
            val startLine: Int = result.fileLineRange.getOrElse(0) { 1 } - 1
            val endLine: Int = result.fileLineRange.getOrElse(1) { 1 } - 1

            val virtualFile: VirtualFile = LocalFileSystem.getInstance().findFileByPath(result.absoluteFilePath)
                    ?: return
            val document: Document? = FileDocumentManager.getInstance().getDocument(virtualFile)

            val startOffset = document!!.getLineStartOffset(startLine)
            val endOffset = document.getLineEndOffset(endLine)

            val dataContext = DataManager.getInstance().dataContext
            val project = dataContext.getData("project") as Project

            if (result.category == Category.VULNERABILITIES) {
                CheckovGlobalState.filesNotToScanAfterFix.add(result.absoluteFilePath)
                alertManualFixNeeded()
            }

            WriteCommandAction.runWriteCommandAction(project) {
                document.replaceString(startOffset, endOffset, result.fixDefinition!!)
                FileDocumentManager.getInstance().saveDocument(document)
                navigateToFile(project, virtualFile, result.codeDiffFirstLine)
            }
        } catch (e: Exception) {
            LOG.warn("error while trying to apply fix", e)
            buttonInstance.isEnabled = true
        }
    }

    private fun alertManualFixNeeded() {
        val dataContext = DataManager.getInstance().dataContext
        val project = dataContext.getData("project") as Project
        Messages.showInfoMessage(project,
                "In order for the fix to take effect, you need to manualy run `yarn install`. Without it, the fix is not complete",
                "Additional Action Required"
        )
    }
}