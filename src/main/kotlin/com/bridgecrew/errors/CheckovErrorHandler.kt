package com.bridgecrew.errors

import com.bridgecrew.services.scan.CheckovScanService
import com.bridgecrew.services.scan.FullScanStateService
import com.bridgecrew.services.scan.ScanTaskResult
import com.bridgecrew.ui.CheckovNotificationBalloon
import com.bridgecrew.utils.extractFileNameFromPath
import com.intellij.notification.NotificationType
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import org.slf4j.LoggerFactory
import java.io.File

@Service
class CheckovErrorHandlerService(val project: Project) {

    private val logger = LoggerFactory.getLogger(javaClass)

    private fun notifyAboutScanError(scanTaskResult: ScanTaskResult, dataSourceValue: String, error: Exception, scanSourceType: CheckovScanService.ScanSourceType) {
        try {
            val dataSourceValueCleaned = if (scanSourceType == CheckovScanService.ScanSourceType.FILE) extractFileNameFromPath(dataSourceValue) else dataSourceValue
            val errorMessagePrefix = if (error.message != null) {
                "Error while scanning ${scanSourceType.toString().lowercase()} ${dataSourceValue.replace(project.basePath!!, "")}, original error message - ${error.message}"
            } else "Error while scanning $dataSourceValue"

            val checkResultPath = scanTaskResult.checkovResult.path.split(dataSourceValueCleaned)
            val debugOutputPath = scanTaskResult.debugOutput.path.split(dataSourceValueCleaned)
            val errorCheckovResultFile = File("${checkResultPath[0]}/error-$dataSourceValueCleaned-${checkResultPath[1]}")
            val errorDebugOutputFile = File("${debugOutputPath[0]}/error-$dataSourceValueCleaned-${debugOutputPath[1]}")
            scanTaskResult.debugOutput.renameTo(errorDebugOutputFile)
            scanTaskResult.checkovResult.renameTo(errorCheckovResultFile)

            val errorMessage = "${errorMessagePrefix}.\n " +
                    "Please check the log file in ${errorDebugOutputFile.path}.\n" +
                    "Prisma Cloud result can be found in ${errorCheckovResultFile.path}.\n" +
                    "To report: open a issue at https://github.com/bridgecrewio/prisma-cloud-jetbrains-ide/issues\n"

            logger.error(errorMessage, error)

            CheckovNotificationBalloon.showNotification(project,
                    errorMessage,
                    NotificationType.ERROR)
        } catch (e: Exception) {
            logger.error("Error while notifying about original exception - $error", e)
        }
    }

    fun notifyAboutParsingError(scanningSource: String, scanSourceType: CheckovScanService.ScanSourceType) {
        val errorMessage = "Error while scanning ${scanSourceType.toString().lowercase()} ${scanningSource.replace(project.basePath!!, "")} - file was found as invalid"

        logger.warn(errorMessage)

        CheckovNotificationBalloon.showNotification(project,
                errorMessage,
                NotificationType.WARNING)
    }

    fun scanningError(scanTaskResult: ScanTaskResult, source: String, error: Exception, scanSourceType: CheckovScanService.ScanSourceType) {

        when (scanSourceType) {
            CheckovScanService.ScanSourceType.FILE -> {
                notifyAboutScanError(scanTaskResult, source, error, scanSourceType)
            }

            CheckovScanService.ScanSourceType.FRAMEWORK -> {
                notifyAboutScanError(scanTaskResult, source, error, scanSourceType)
                project.service<FullScanStateService>().frameworkFinishedWithErrors(source, scanTaskResult)
            }
        }

    }

}