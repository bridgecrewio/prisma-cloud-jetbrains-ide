package com.bridgecrew.services.scan

import com.bridgecrew.analytics.AnalyticsService
import com.bridgecrew.listeners.CheckovScanListener
import com.bridgecrew.utils.DEFAULT_FILE_TIMEOUT
import com.bridgecrew.utils.DEFAULT_FRAMEWORK_TIMEOUT
import com.bridgecrew.utils.createCheckovTempFile
import com.bridgecrew.utils.extractFileNameFromPath
import com.intellij.execution.ExecutionException
import com.intellij.execution.process.ProcessAdapter
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessOutputTypes
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener
import com.intellij.openapi.util.Key
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

data class ScanTaskResult(
        val checkovResult: File,
        val debugOutput: File,
        val errorReason: String)
{
    fun deleteResultsFile() {
        if (checkovResult.exists())
            checkovResult.delete()
        if (debugOutput.exists())
            debugOutput.delete()
    }
}

abstract class ScanTask(
    project: Project,
    title: String,
    private val sourceName: String,
    private val processHandler: ProcessHandler,
    val checkovResultFile: File
) : Task.Backgroundable(project, title, true) {

    protected val logger: Logger = LoggerFactory.getLogger(javaClass)

    val debugOutputFile: File = createCheckovTempFile("${sourceName}-debug-output", ".txt")
    var errorReason = ""

    protected var indicator: ProgressIndicator? = null

    protected fun getScanOutputs(timeout: Long): ScanTaskResult {
        if (!processHandler.isStartNotified) {
            logger.error("Assertion failed: processHandler.isStartNotified = ${processHandler.isStartNotified}")
        }
        processHandler.addProcessListener(object : ProcessAdapter() {

            override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
                try {
                    if (processHandler.isProcessTerminated || processHandler.isProcessTerminating) {
                        logger.info("Process is terminating for $sourceName")
                        return
                    }

                    indicator!!.checkCanceled()

                    if (outputType == ProcessOutputTypes.SYSTEM) {
                        return
                    }

                    val text = event.text

                    if (outputType == ProcessOutputTypes.STDERR) {
                        debugOutputFile.appendText(text)
                        errorReason = updateErrorReason(text)
                    }

                    logger.debug(text)
                } catch (e: ProcessCanceledException) {
                    logger.info(
                        "Process was canceled for $sourceName during onTextAvailable, destroying process for processHandler",
                        e
                    )
                    processHandler.destroyProcess()
                }

            }
        })

        processHandler.startNotify()
        if (!processHandler.waitFor(timeout)) {
            throw ExecutionException("Script execution took more than ${(timeout / 1000)} seconds")
        }

        return ScanTaskResult(checkovResultFile, debugOutputFile, errorReason)
    }

    private fun updateErrorReason(text: String): String {
       if (text.contains("[ERROR]")) {
           return text.substring(text.indexOf("ERROR"))
       }

        if (text.contains("Please check your API token")) {
            return text
        }

        return ""
    }

    fun cancelTask() {
        if (this.indicator != null) {
            logger.info("Going to cancel task for $sourceName")
            this.indicator!!.cancel()
            ProgressManager.canceled(indicator!!)
            processHandler.destroyProcess()
            deleteResultsFile()
            logger.info("Task was canceled for $sourceName")
        }
    }

    fun deleteResultsFile() {
        if (checkovResultFile.exists())
            checkovResultFile.delete()
        if (debugOutputFile.exists())
            debugOutputFile.delete()
    }

    class FrameworkScanTask(
        project: Project,
        title: String,
        private val framework: String,
        private val processHandler: ProcessHandler,
        checkovResultOutputFile: File
    ) : ScanTask(project, title, framework, processHandler, checkovResultOutputFile), ProjectManagerListener {

        override fun run(indicator: ProgressIndicator) {
            try {
                this.indicator = indicator
                checkOnCancel()
                logger.info("Going to scan for framework $framework")
                indicator.isIndeterminate = false

                val scanTaskResult: ScanTaskResult = getScanOutputs(DEFAULT_FRAMEWORK_TIMEOUT)
                indicator.checkCanceled()

                logger.info("Checkov scan task finished successfully for framework $framework")

                project.service<AnalyticsService>().fullScanByFrameworkFinished(framework)

                project.service<CheckovScanService>().analyzeFrameworkScan(scanTaskResult, processHandler.exitCode!!, project, framework)

            } catch (e: ProcessCanceledException) {
                logger.info("Task for framework $framework was canceled ", e)
                processHandler.destroyProcess()
                deleteResultsFile()
                project.service<FullScanStateService>().frameworkWasCancelled()
                project.messageBus.syncPublisher(CheckovScanListener.SCAN_TOPIC).scanningFinished(CheckovScanService.ScanSourceType.FRAMEWORK)
            } catch (error: Exception) {
                logger.error("error while scanning framework $framework", error)
                project.service<AnalyticsService>().fullScanByFrameworkFinished(framework)
                project.service<FullScanStateService>().frameworkFinishedWithErrors(framework, ScanTaskResult(checkovResultFile, debugOutputFile, errorReason))
                throw error
            }
        }

        private fun checkOnCancel() {
            if (project.service<FullScanStateService>().onCancel) {
                throw ProcessCanceledException(Exception("Could not start process on cancel state"))
            }
            indicator!!.checkCanceled()
        }
    }

    class FileScanTask(project: Project, title: String, private val filePath: String, private val processHandler: ProcessHandler, checkovResultFile: File):
            ScanTask(project, title, extractFileNameFromPath(filePath), processHandler, checkovResultFile) {
        override fun run(indicator: ProgressIndicator) {
            try {

                this.indicator = indicator
                indicator.checkCanceled()
                logger.info("Going to scan for file $filePath")
                indicator.isIndeterminate = false

                val scanTaskResult: ScanTaskResult = getScanOutputs(DEFAULT_FILE_TIMEOUT)
                indicator.checkCanceled()

                logger.info("Checkov scan task finished successfully for file $filePath")

                project.service<CheckovScanService>().analyzeFileScan(scanTaskResult, processHandler.exitCode!!, project, filePath)

            } catch (e: ProcessCanceledException) {
                logger.info("Task for file $filePath was canceled", e)
                processHandler.destroyProcess()
                deleteResultsFile()
            }
            catch (error: Exception) {
                logger.error("error while scanning file $filePath", error)
                throw error
            }
        }
    }
}