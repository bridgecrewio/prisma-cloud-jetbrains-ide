package com.bridgecrew.services.installation

import com.bridgecrew.services.CliService
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

class PipenvInstallerCommandService : InstallerCommandService {

    override fun getInstallCommand(): GeneralCommandLine {
        return GeneralCommandLine(arrayListOf("pipenv", "--python", "3", "install", "checkov"))
    }

    override fun getVersion(project: Project): ArrayList<String> {
        return arrayListOf(project.service<CliService>().checkovPath, "-v")
    }

    companion object {
        fun getWinCommandsForFindingCheckovPath(): ArrayList<String> {
            return arrayListOf("pipenv", "run", "where", "python")
        }

        fun getUnixCommandsForFindingCheckovPath(): ArrayList<String> {
            return arrayListOf("pipenv", "run", "which", "python")
        }
    }
}