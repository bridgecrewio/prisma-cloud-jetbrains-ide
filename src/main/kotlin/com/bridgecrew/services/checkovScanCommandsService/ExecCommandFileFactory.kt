package com.bridgecrew.services.checkovScanCommandsService

import com.bridgecrew.utils.getPackageJsonLockFile
import java.io.File

class ExecCommandSingleFileBuilder(val filePath: String) {

    fun buildExecCommand(): List<String> {
        return when {
            filePath.endsWith("package.json") -> buildPackageJsonExecCommand(filePath)
            else -> listOf(filePath)
        }
    }

    private fun buildPackageJsonExecCommand(filePath: String): List<String> {
        val lockFile = getPackageJsonLockFile(filePath);
        if(File(lockFile).exists()){
            return listOf(filePath, lockFile)
        }

        return listOf(filePath)
    }
}