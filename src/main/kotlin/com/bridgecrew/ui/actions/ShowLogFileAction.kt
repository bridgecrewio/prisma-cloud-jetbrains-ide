package com.bridgecrew.ui.actions

import com.bridgecrew.icons.CheckovIcons
import com.bridgecrew.log.LoggerService
import com.bridgecrew.utils.ApplicationServiceUtil
import com.intellij.ide.actions.RevealFileAction
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.project.DumbAware
import java.io.File

class ShowLogFileAction : AnAction(), DumbAware {

    override fun update(event: AnActionEvent) {
        val presentation: Presentation = event.presentation
        presentation.icon = CheckovIcons.showLogsIcon
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.EDT
    }

    override fun actionPerformed(event: AnActionEvent) {
        val logFilePath = ApplicationServiceUtil.getService(LoggerService::class.java).getLogFilePath()
        RevealFileAction.openFile(File(logFilePath))
    }
}