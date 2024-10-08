package com.bridgecrew.services.checkovScanCommandsService

import com.bridgecrew.services.CliService
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import org.apache.commons.io.FilenameUtils

class InstalledCheckovScanCommandsService(project: Project) : CheckovScanCommandsService(project) {
    override fun getCheckovRunningCommandByServiceType(outputFilePath: String): ArrayList<String> {
        return arrayListOf(project.service<CliService>().checkovPath)
    }

    override fun getDirectory(): String {
        return FilenameUtils.separatorsToSystem(project.basePath!!)
    }

    override fun getFilePath(originalFilePath: String): String {
        return FilenameUtils.separatorsToSystem(originalFilePath)
    }

    override fun getOutputFilePath(outputFilePath: String): String {
        return outputFilePath
    }

    override fun getCertPath(): String {
        return settings?.certificate!!
    }
}