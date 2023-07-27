package com.bridgecrew.cache

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import com.intellij.openapi.vfs.LocalFileSystem

class CacheDataAnalyticsStorage (private val project: Project){

    private val ideaDirectory: VirtualFile? = getIdeaDirectory(project)
    private val cacheFile: String = "prisma_data_analytics.txt"

    fun writeDataToFile(data: String) {
        ideaDirectory?.let { ideaDir ->
            val dataFile = File(ideaDir.path, cacheFile)
            BufferedWriter(FileWriter(dataFile)).use { writer ->
                writer.write(data)
            }
        }
    }

    fun readDataFromFile(): String? {
        ideaDirectory?.let { ideaDir ->
            val dataFile = File(ideaDir.path, cacheFile)
            if (dataFile.exists()) {
                return dataFile.readText()
            }
        }
        return null
    }

    fun clear(){
        writeDataToFile("")
    }

    private fun getIdeaDirectory(project: Project): VirtualFile? {
        val basePath = project.basePath ?: return null
        val ideaDirectoryPath = "$basePath/.idea"
        return LocalFileSystem.getInstance().findFileByPath(ideaDirectoryPath)
    }

}
