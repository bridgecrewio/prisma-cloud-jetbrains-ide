package com.bridgecrew.ui.actions

import com.bridgecrew.icons.CheckovIcons
import com.bridgecrew.log.LoggerService
import com.bridgecrew.util.ApplicationServiceUtil
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.project.DumbAware
import org.slf4j.LoggerFactory
import java.awt.Desktop
import java.io.File
import java.io.IOException

class ShowLogFileAction : AnAction(), DumbAware {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun update(event: AnActionEvent) {
        val presentation: Presentation = event.presentation
        presentation.icon = CheckovIcons.showLogsIcon
    }

//    TODO: Uncomment when moving to Kotlin 2 + Java 21
//    override fun getActionUpdateThread(): ActionUpdateThread {
//        return ActionUpdateThread.EDT
//    }

    override fun actionPerformed(event: AnActionEvent) {
        val logFilePath = ApplicationServiceUtil.getService(LoggerService::class.java).getLogFilePath()
        val file = File(logFilePath)

        if (!file.exists()) {
            logger.error("File does not exist: $logFilePath")
            return
        }

        if (!Desktop.isDesktopSupported()) {
            logger.error("Desktop is not supported on this platform.")
            return
        }

        val desktop = Desktop.getDesktop()
        if (!desktop.isSupported(Desktop.Action.OPEN)) {
            logger.error("Open action is not supported on this platform.")
            return
        }

        try {
            desktop.open(file)
        } catch (e: IOException) {
            logger.error("An error occurred while trying to open the file: ${e.message}", e)
        }
    }
}