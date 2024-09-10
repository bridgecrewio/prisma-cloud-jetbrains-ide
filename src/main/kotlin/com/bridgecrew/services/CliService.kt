package com.bridgecrew.services

import com.bridgecrew.ui.CheckovNotificationBalloon
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ScriptRunnerUtil
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import org.slf4j.LoggerFactory
import java.nio.charset.Charset
import javax.swing.SwingUtilities
import kotlin.reflect.KFunction3

@Service
class CliService {

    private val logger = LoggerFactory.getLogger(javaClass)
    var checkovPath: String = ""
    var checkovVersion: String = ""

    fun run(
            commands: ArrayList<String>,
            project: Project,
            onSuccessFunction: KFunction3<String, Int, Project, Unit>,
            onFailureFunction: ((output: String, exitCode: Int, project: Project) -> Unit)? =  null
        ) {
        val commandToPrint = commands.joinToString(" ")
        logger.info("Running command: $commandToPrint")
        try {

            val generalCommandLine = GeneralCommandLine(commands)
            generalCommandLine.charset = Charset.forName("UTF-8")
            generalCommandLine.setWorkDirectory(project.basePath)

            val processHandler: ProcessHandler = OSProcessHandler(generalCommandLine)
            val myBackgroundable =
                BackgroundableTask(project, "running cli command $commandToPrint", processHandler, onSuccessFunction)
            if (SwingUtilities.isEventDispatchThread()) {
                ProgressManager.getInstance().run(myBackgroundable)
            } else {
                ApplicationManager.getApplication().invokeLater {
                    ProgressManager.getInstance().run(myBackgroundable)
                }
            }
        } catch (e: Exception){
            logger.warn("Cli command $commandToPrint could not run due to exception: $e")
            if (onFailureFunction != null) {
                onFailureFunction("",  1, project)
            }
        }
    }

    private class BackgroundableTask(
        project: Project,
        title: String,
        val processHandler: ProcessHandler,
        val function: (output: String, exitCode: Int, project: Project) -> Unit,
    ) : Task.Backgroundable(project, title, true) {

        override fun run(indicator: ProgressIndicator) {
            indicator.isIndeterminate = false
            try {
                val output = ScriptRunnerUtil.getProcessOutput(processHandler,
                        ScriptRunnerUtil.STDOUT_OR_STDERR_OUTPUT_KEY_FILTER,
                    720000000)
                function(output,  processHandler.exitCode!!, project)
            } catch (e: Exception) {
                if (e.message?.contains("Script execution took") == true){
                    CheckovNotificationBalloon.showNotification(project,
                        "Exceeded allocated scanning time",
                        NotificationType.INFORMATION)
                }
                else {
                    throw e
                }
            }
        }

    }
}




