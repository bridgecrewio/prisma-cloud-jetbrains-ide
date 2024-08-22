package com.bridgecrew.services.installation

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.project.Project

interface InstallerCommandService {
    fun getInstallCommand(): GeneralCommandLine
    fun getVersion(project: Project): ArrayList<String>
}