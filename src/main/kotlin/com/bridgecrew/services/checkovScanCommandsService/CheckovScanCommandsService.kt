package com.bridgecrew.services.checkovScanCommandsService

import com.bridgecrew.listeners.CheckovSettingsListener
import com.bridgecrew.settings.PrismaSettingsState
import com.bridgecrew.utils.*
import com.intellij.openapi.project.Project
import org.apache.commons.lang.StringUtils

abstract class CheckovScanCommandsService(val project: Project) {
    protected val settings = PrismaSettingsState().getInstance()
    private var gitRepo = getRepoName()

    fun getExecCommandForSingleFile(filePaths: List<String>, outputFilePath: String): ArrayList<String> {
        val cmds = ArrayList<String>()
        cmds.addAll(getCheckovRunningCommandByServiceType(outputFilePath))
        cmds.addAll(getCheckovCliArgsForExecCommand(getOutputFilePath(outputFilePath)))

        filePaths.forEach{ path -> cmds.add("-f"); cmds.add(getFilePath(path)) }

        return cmds
    }

    fun getExecCommandsForRepositoryByFramework(framework: String, outputFilePath: String): ArrayList<String> {

        val baseCmds = ArrayList<String>()
        baseCmds.addAll(getCheckovRunningCommandByServiceType(outputFilePath))

        baseCmds.add("-d")

        baseCmds.add(getDirectory())

        baseCmds.addAll(getExcludePathCommand())

        val cmdByFramework = arrayListOf<String>()
        cmdByFramework.addAll(baseCmds)
        cmdByFramework.addAll(getCheckovCliArgsForExecCommand(getOutputFilePath(outputFilePath)))
        cmdByFramework.addAll(getCheckovNoFailOnCrash(framework))

        cmdByFramework.add("--framework")
        cmdByFramework.add(framework)

        return cmdByFramework
    }
    private fun getCheckovNoFailOnCrash(framework: String): ArrayList<String> {
        val command = ArrayList<String>()
        if (framework === "sast") {
            command.add("--no-fail-on-crash")
        }

        return command
    }

    private fun getCheckovCliArgsForExecCommand(outputFilePath: String): ArrayList<String> {
        val apiToken = settings?.getApiKey()
        if (apiToken.isNullOrEmpty()) {
            project.messageBus.syncPublisher(CheckovSettingsListener.SETTINGS_TOPIC).settingsUpdated()
            throw Exception("Wasn't able to get api token\n" +
                    "Please insert an Api Token to continue")
        }

        val command = arrayListOf("-s", "--bc-api-key", apiToken, "--repo-id", gitRepo, "--quiet", "-o", "cli", "-o", "json",
                "--output-file-path", "console,$outputFilePath")
        command.addAll(getCertParams())

        val prismaUrl = settings?.prismaURL
        if (!prismaUrl.isNullOrEmpty()) {
            command.addAll(arrayListOf("--prisma-api-url", prismaUrl))
        }

        return command
    }

    private fun getExcludePathCommand(): ArrayList<String> {
        val cmds = ArrayList<String>()

        val excludedPaths = (getGitIgnoreValues(project) + FULL_SCAN_EXCLUDED_PATHS).distinct()

        for (excludePath in excludedPaths) {
            cmds.add("--skip-path")
            cmds.add(getNormalizedExcludePath(excludePath))
        }

        return cmds
    }

    private fun getNormalizedExcludePath(excludePath: String): String {
        if (isWindows()) {
            var winExcludePath = StringUtils.removeEnd(excludePath, "\\")
            winExcludePath = StringUtils.removeStart(winExcludePath, "**/")
            winExcludePath = StringUtils.removeStart(winExcludePath, "*")
            return winExcludePath
        }

        return StringUtils.removeEnd(excludePath, "/")
    }

    private fun getCertParams(): ArrayList<String> {
        val cmds = ArrayList<String>()
        val certPath = settings?.certificate
        if (!certPath.isNullOrEmpty()) {
            cmds.add("--ca-certificate")
            cmds.add(getCertPath())
            return cmds
        }
        return cmds
    }

    abstract fun getCheckovRunningCommandByServiceType(outputFilePath: String): ArrayList<String>
    abstract fun getDirectory(): String
    abstract fun getFilePath(originalFilePath: String): String
    abstract fun getCertPath(): String
    abstract fun getOutputFilePath(outputFilePath: String): String

}