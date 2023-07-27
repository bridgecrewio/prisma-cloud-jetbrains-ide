package com.bridgecrew.services

import com.bridgecrew.results.*
import com.bridgecrew.settings.CheckovGlobalState
import com.bridgecrew.ui.CheckovToolWindowFactory
import com.bridgecrew.ui.actions.SeverityFilterActions
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import org.jetbrains.rpc.LOG
import java.io.File

class CheckovResultsListUtils {
    companion object {
        private val checkovResultsComparator: Comparator<BaseCheckovResult> = CheckovResultsComparatorGenerator.generateCheckovResultComparator()

        private fun filterResultsByCategories(sourceList: List<BaseCheckovResult>, categories: List<Category>?): List<BaseCheckovResult> {
            if (!categories.isNullOrEmpty()) {
                return sourceList.filter { baseCheckovResult ->
                    categories.contains(baseCheckovResult.category)
                }.toMutableList()
            }

            val selectedCategory = CheckovToolWindowFactory.lastSelectedCategory

            if (selectedCategory == null) {
                return sourceList.toMutableList()
            }

            return sourceList.filter { baseCheckovResult -> selectedCategory == baseCheckovResult.category }
        }

        private fun filterResultBySeverities(sourceList: List<BaseCheckovResult>, severities: List<Severity>?): List<BaseCheckovResult> {
            if (!severities.isNullOrEmpty()) {
                return sourceList.filter { baseCheckovResult ->
                    severities.contains(baseCheckovResult.severity)
                }
            }

            return sourceList.filter { baseCheckovResult ->
                SeverityFilterActions.currentSelectedSeverities.contains(baseCheckovResult.severity)
            }
        }

        fun sortResults(sourceList: MutableList<BaseCheckovResult>) {
            try {
                sourceList.sortWith(checkovResultsComparator)
            } catch (e: Exception) {
                LOG.warn("error while sorting list", e)
            }
        }

        fun filterResultsByCategoriesAndSeverities(sourceList: List<BaseCheckovResult>, categories: List<Category>? = null, severities: List<Severity>? = null): List<BaseCheckovResult> {
            val checkovResults = filterResultsByCategories(sourceList, categories)
            return filterResultBySeverities(checkovResults, severities)
        }

        fun getCheckovResultsByPath(sourceList: List<BaseCheckovResult>, filePath: String): List<BaseCheckovResult> {
            return sourceList.filter { baseCheckovResult ->
                baseCheckovResult.filePath == "/${filePath}"
            }
        }

        fun sortAndGroupResultsByPath(sourceList: List<BaseCheckovResult>): Map<String, List<BaseCheckovResult>> {
            val filteredList = filterResultsByCategoriesAndSeverities(sourceList)
            sortResults(filteredList.toMutableList())
            return filteredList.groupBy { it.filePath }
        }

        fun cleanUpFileStateBeforeChanging(filePath: String) {
            CheckovGlobalState.modifiedCheckovResults = CheckovGlobalState.modifiedCheckovResults.filter { result -> result.filePath != filePath }.toMutableList()
        }

        fun modifyBaseCheckovResultLineNumbers(project: Project, original: BaseCheckovResult, startLine: Int, range: Int) {
            val allResults = project.service<ResultsCacheService>().getAdjustedCheckovResults()
            val resultsByPath = getCheckovResultsByPath(allResults, original.filePath.removePrefix(File.separator))
            resultsByPath.forEach { resultInFile ->
                val fileLineRange = ArrayList<Int>()
                val codeBlock = ArrayList<ArrayList<Any>>()

                resultInFile.fileLineRange.forEach { line->
                    val newLine = getNewLine(line, startLine, range)
                    fileLineRange.add(newLine)
                }
                resultInFile.codeBlock.forEach { block ->
                    val newBlock = ArrayList<Any>()
                    val newLine = getNewLine((block[0] as Double).toInt(), startLine, range)
                    newBlock.add(newLine.toDouble())
                    newBlock.add(block[1])
                    codeBlock.add(newBlock)
                }
                val modifiedResult = cloneCheckovResultWithModifiedFields(project, resultInFile, fileLineRange, codeBlock)
                CheckovGlobalState.modifiedCheckovResults.add(modifiedResult)
            }
            CheckovGlobalState.shouldRecalculateResult = resultsByPath.isNotEmpty()
        }

        private fun getNewLine(currLine: Int, startLine: Int, range: Int): Int {
            return if(currLine >= startLine) {
                currLine + range
            } else {
                currLine
            }
        }

        fun cloneCheckovResultWithModifiedFields(project: Project, checkovResult: BaseCheckovResult, fileLineRange: List<Int>, codeBlock: List<List<Any>>): BaseCheckovResult {
            var cloned = checkovResult

            when (checkovResult.category) {
                Category.IAC -> {
                    val newName = project.service<ResultsCacheService>().getIACResourceName((checkovResult as IacCheckovResult).checkName, fileLineRange[0], fileLineRange[1])
                    cloned = IacCheckovResult(
                            checkovResult.checkType, checkovResult.filePath,
                            checkovResult.resource, newName, checkovResult.id, checkovResult.severity, checkovResult.description,
                            checkovResult.guideline, checkovResult.absoluteFilePath, fileLineRange, checkovResult.fixDefinition,
                            codeBlock, checkovResult.checkName
                    )
                }

                Category.VULNERABILITIES -> {
                    cloned = VulnerabilityCheckovResult(
                            checkovResult.checkType, checkovResult.filePath, checkovResult.resource, checkovResult.name,
                            checkovResult.id, checkovResult.severity, checkovResult.description,
                            checkovResult.guideline, checkovResult.absoluteFilePath, fileLineRange, checkovResult.fixDefinition,
                            codeBlock, (checkovResult as VulnerabilityCheckovResult).cvss,
                            checkovResult.packageName, checkovResult.packageVersion, checkovResult.fixVersion, checkovResult.cveLink,
                            checkovResult.publishedDate, checkovResult.vector, checkovResult.violationId,
                            checkovResult.resourceId, checkovResult.riskFactors, checkovResult.rootPackageName,
                            checkovResult.rootPackageVersion, checkovResult.rootPackageFixVersion, checkovResult.fixCommand
                    )
                }

                Category.SECRETS -> {
                    cloned = SecretsCheckovResult(
                            checkovResult.checkType, checkovResult.filePath,
                            checkovResult.resource, checkovResult.name, checkovResult.id, checkovResult.severity, checkovResult.description,
                            checkovResult.guideline, checkovResult.absoluteFilePath, fileLineRange, checkovResult.fixDefinition,
                            codeBlock, (checkovResult as SecretsCheckovResult).checkName
                    )
                }

                Category.LICENSES -> {
                    cloned = LicenseCheckovResult(
                            checkovResult.checkType, checkovResult.filePath,
                            checkovResult.resource, checkovResult.name, checkovResult.id, checkovResult.severity, checkovResult.description,
                            checkovResult.guideline, checkovResult.absoluteFilePath, fileLineRange, checkovResult.fixDefinition,
                            codeBlock, (checkovResult as LicenseCheckovResult).policy, checkovResult.licenseType, checkovResult.approvedSPDX
                    )
                }
            }
            return cloned
        }
    }
}