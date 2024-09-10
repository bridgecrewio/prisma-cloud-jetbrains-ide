package com.bridgecrew.services.installation

import com.bridgecrew.listeners.CheckovInstallerListener
import com.bridgecrew.services.CliService
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ScriptRunnerUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import org.slf4j.LoggerFactory
import java.nio.charset.Charset
import javax.swing.SwingUtilities

@Service
class CheckovInstallerService {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun install(
            project: Project,
    ) {
        val commands = ArrayList<Pair<InstallerCommandService, ProcessHandler>>()
        val checkovServices = arrayOf(DockerInstallerCommandService(), PipInstallerCommandService(), PipenvInstallerCommandService())
        for (service in checkovServices) {
            try {
                val generalCommandLine = service.getInstallCommand()
                generalCommandLine.charset = Charset.forName("UTF-8")
                val processHandler: ProcessHandler = OSProcessHandler(generalCommandLine)
                commands.add(Pair(service, processHandler))
            } catch (e: Exception) {
                logger.info("Process is not installed in the machine, will not try to install $e")
                continue
            }
        }
        if (commands.isEmpty()) {
            logger.error("Checkov could not be installed, your machine is missing all 3 installation options.\n Please install docker | pip | pipenv")
        }
        val installerTask = InstallerTask(project, "Installing checkov", commands)
        if (SwingUtilities.isEventDispatchThread()) {
            ProgressManager.getInstance().run(installerTask)
        } else {
            ApplicationManager.getApplication().invokeLater {
                ProgressManager.getInstance().run(installerTask)
            }
        }
    }

    private class InstallerTask(
        project: Project,
        title: String,
        val services: ArrayList<Pair<InstallerCommandService, ProcessHandler>>
    ) : Task.Backgroundable(project, title, true) {

        private val logger = LoggerFactory.getLogger(javaClass)

        override fun run(indicator: ProgressIndicator) {
            indicator.isIndeterminate = false
            for (service in services) {
                val serviceObject = service.first
                val handler = service.second
                val output = ScriptRunnerUtil.getProcessOutput(handler,
                        ScriptRunnerUtil.STDOUT_OR_STDERR_OUTPUT_KEY_FILTER,
                        720000000)
                if (handler.exitCode != 0 || output.contains("[ERROR]")) {
                    logger.info("Failed to install using: ${serviceObject.javaClass.kotlin}")
                    continue
                }
                logger.info("Checkov installed successfully using ${serviceObject.javaClass.kotlin}")
                project.messageBus.syncPublisher(CheckovInstallerListener.INSTALLER_TOPIC).installerFinished(serviceObject)
                project.service<CliService>().run(serviceObject.getVersion(project), project, ::printCheckovVersion)
                break
            }
        }

        fun printCheckovVersion(output: String, exitCode: Int, project: Project) {
            if (exitCode != 0 || output.contains("[ERROR]")) {
                logger.warn("Failed to get checkov version")
                return
            }
            project.service<CliService>().checkovVersion = output.trim().replace("\n", "")
            logger.info("Checkov was installed version: $output")
        }
    }
}




