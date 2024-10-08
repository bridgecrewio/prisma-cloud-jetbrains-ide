package com.bridgecrew.utils

import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import org.apache.commons.io.FilenameUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path

val logger: Logger = LoggerFactory.getLogger("FileUtils")
var checkovTempDirPath: Path = Files.createTempDirectory("checkov")

fun navigateToFile(project: Project, virtualFile: VirtualFile, startLine: Int = 0, startColumn: Int = 0) {
    val line = calculateLineOffset(startLine - 1, virtualFile)
    val column = calculateColumnOffset(startColumn, virtualFile, line)
    val fileDescriptor = OpenFileDescriptor(project, virtualFile, line, column)
    fileDescriptor.navigate(false)
}

fun navigateToFile(project: Project, filePath: String, startLine: Int = 0, startColumn: Int = 0) {
    val virtualFile: VirtualFile = LocalFileSystem.getInstance().findFileByPath(filePath)
            ?: return
    navigateToFile(project, virtualFile, startLine, startColumn)
}
fun calculateLineOffset(start: Int, virtualFile: VirtualFile): Int {
    if (start <= 0)
        return 0

    val document: Document? = FileDocumentManager.getInstance().getDocument(virtualFile)

    if (start > document!!.lineCount)
        return start

    return start
}

fun calculateColumnOffset(start: Int, virtualFile: VirtualFile, line: Int): Int {
    if (start <= 0)
        return 0

    val document: Document? = FileDocumentManager.getInstance().getDocument(virtualFile)

    if (start > document!!.getLineEndOffset(line))
        return start

    return start
}
/**
 * Helper function that validates url string.
 */
fun isUrl(url: String?): Boolean {
    if (url.isNullOrEmpty()) {
        return false
    }
    return try {
        URL(url)
        true
    } catch (e: Throwable) {
        false
    }
}

fun getFileType(filePath: String): FileType {
    val fileParts = filePath.split(".")
    return if(fileParts.size > 1){
        when(fileParts[1]) {
            "json" -> FileType.JSON
            "tf" -> FileType.TERRAFORM
            "js" -> FileType.JAVASCRIPT
            "ts" -> FileType.TYPESCRIPT
            "py" -> FileType.PYTHON
            "txt" -> FileType.TEXT
            "yml", "yaml" -> FileType.YAML
            "Dockerfile" -> FileType.DOCKERFILE
            "xml" -> FileType.XML
            "kt" -> FileType.KOTLIN
            "java", "jar" -> FileType.JAVA
            "mod" -> FileType.GOLANG
            "gradle" -> FileType.GRADLE
            "csproj" -> FileType.CSPROJ
            else -> FileType.UNKNOWN
        }
    } else {
        when(filePath) { //no "dot" in file name
            "Dockerfile" -> FileType.DOCKERFILE
            "Gemfile" -> FileType.GEMFILE
            else -> FileType.UNKNOWN
        }
    }
}

fun getGitIgnoreValues(project: Project): List<String> {

    try {
        val path = project.basePath + "/.gitignore"
        val gitignoreVirtualFile = LocalFileSystem.getInstance().findFileByPath(path)
        if (gitignoreVirtualFile == null) {
            logger.info("no .gitignore file in this project")
            return arrayListOf()
        }

        return String(gitignoreVirtualFile.contentsToByteArray())
                .split(System.lineSeparator()).filter { raw -> !(raw.trim().startsWith("#") || raw.trim().isEmpty() )}

    } catch (e: Exception) {
        logger.error("error while reading .gitignore file", e)
    }
    return arrayListOf()
}

fun extractFileNameFromPath(filePath: String): String {
    val filename: String = FilenameUtils.getName(filePath)
    val extension: String = FilenameUtils.getExtension(filename)
    return filename.removeSuffix(".$extension")
}

fun createCheckovTempFile(prefix: String, suffix: String): File {
    if (!Files.exists(checkovTempDirPath)) {
        checkovTempDirPath = Files.createTempDirectory("checkov")
    }
    return File.createTempFile(prefix,
            suffix,
            checkovTempDirPath.toFile())
}

fun getPackageJsonLockFile(filePath: String): String{
    return filePath.replace("package.json", "package-lock.json")
}

fun deleteCheckovTempDir() {
    try {
        if (!Files.exists(checkovTempDirPath)) {
            return
        }

        val listOfFiles = checkovTempDirPath.toFile().list()!!
        logger.info(
            "Checking if Checkov temp dir should be deleted, current files - ${
                checkovTempDirPath.toFile().list()?.map { path -> path.toString() }
            }"
        )
        if (listOfFiles.isEmpty() || listOfFiles.none { filePath -> filePath.startsWith("error") }) {
            checkovTempDirPath.toFile().deleteRecursively()
        }
    } catch (e: Exception) {
        logger.warn("could not delete temp directory in $checkovTempDirPath", e)
    }
}

fun toVirtualFilePath(project: Project, virtualFile: VirtualFile): String {
    return virtualFile.path.removePrefix(project.basePath!!).removePrefix(File.separator)
}

fun toDockerFilePath(path: String): String {
    return path.replace(":/", "[--colon--]")
}
fun fromDockerFilePath(path: String): String {
    return path.replace( "[--colon--]",":/")
}