package com.bridgecrew.services.installation

import com.bridgecrew.services.CliService
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

class PipInstallerCommandService : InstallerCommandService {

    override fun getInstallCommand(): GeneralCommandLine {
        val command = GeneralCommandLine(
            arrayListOf(
            "pip3",
            "install",
            "-U",
            "--user",
            "checkov",
            "-i",
            "https://pypi.org/simple/"
            )
        )
        command.environment["PIP_BREAK_SYSTEM_PACKAGES"] = "1"
        return command
    }

    override fun getVersion(project: Project): ArrayList<String> {
        return arrayListOf(project.service<CliService>().checkovPath, "-v")
    }

    companion object {
        fun getWinCommandsForFindingCheckovPath(): ArrayList<String> {
            return arrayListOf("pip3", "show", "checkov")
        }

        fun getUnixCommandsForFindingCheckovPath(): ArrayList<String> {
            return arrayListOf("python3", "-c", "import site; print(site.USER_BASE)")
        }
    }
}