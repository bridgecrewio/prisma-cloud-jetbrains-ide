package com.bridgecrew.services.checkovScanCommandsService

import com.bridgecrew.utils.PLUGIN_ID
import com.bridgecrew.utils.toDockerFilePath
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.project.Project
import org.apache.commons.io.FilenameUtils

class DockerCheckovScanCommandsService(project: Project) : CheckovScanCommandsService(project) {

    private val image = "bridgecrew/checkov"
    private val volumeDirectory = getDockerUnixPath(project.basePath)
    private val volumeCertPath = "/usr/lib/ssl/cert.pem"
    override fun getCheckovRunningCommandByServiceType(outputFilePath: String): ArrayList<String> {
        val pluginVersion =
                PluginManagerCore.getPlugin(PluginId.getId(PLUGIN_ID))?.version ?: "UNKNOWN"

        val dockerCommand = arrayListOf("docker", "run", "--rm", "-a", "stdout", "-a", "stderr", "--env", "BC_SOURCE=jetbrains", "--env", "BC_SOURCE_VERSION=$pluginVersion", "--env", "LOG_LEVEL=DEBUG")
        val prismaUrl = settings?.prismaURL
        val certPath = settings?.certificate
        if (!prismaUrl.isNullOrEmpty()) {
            dockerCommand.addAll(arrayListOf("--env", "PRISMA_API_URL=${prismaUrl}"))
        }

        if (!certPath.isNullOrEmpty()) {
            val volumeCaFile = "$certPath:$volumeCertPath"
            dockerCommand.addAll(arrayListOf("--volume", volumeCaFile))
        }

        dockerCommand.addAll(arrayListOf("--volume", "$outputFilePath:/${getDockerUnixPath(outputFilePath)}"))

        val volumeDir = "${FilenameUtils.separatorsToUnix(project.basePath)}:/${volumeDirectory}"
        dockerCommand.addAll(arrayListOf("--volume", volumeDir, image))
        return dockerCommand

    }

    override fun getDirectory(): String {
        return volumeDirectory
    }

    private fun getDockerUnixPath(path: String?): String {
        return toDockerFilePath(FilenameUtils.separatorsToUnix(path));
    }


    override fun getOutputFilePath(outputFilePath: String): String {
        return getDockerUnixPath(outputFilePath)
    }

    override fun getFilePath(originalFilePath: String): String {
        return originalFilePath.replace(project.basePath!!, volumeDirectory)
    }

    override fun getCertPath(): String {
        return volumeCertPath
    }
}