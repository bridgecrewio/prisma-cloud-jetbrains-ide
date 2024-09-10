package com.bridgecrew.services

import com.bridgecrew.CheckovResult
import com.bridgecrew.results.*
import com.bridgecrew.settings.CheckovGlobalState
import com.bridgecrew.utils.CheckovUtils
import com.bridgecrew.utils.fromDockerFilePath
import com.bridgecrew.utils.isWindows
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Paths

@Service
class ResultsCacheService(val project: Project) {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)
    var checkovResults: MutableList<BaseCheckovResult> = mutableListOf()
    var modifiedResults: MutableList<BaseCheckovResult> = mutableListOf()
    private val baseDir: String = project.basePath!!

    // This function returns `checkovResults` after accounting for changes that were done between scans
    // For example, after fixing or suppressing a resource, we want to clean those entries from all client facing usages.
    // TODO if this causes performance issues we can optimize by using a hashmap with a key for each result (value is the BaseCheckovResult)
    fun getAdjustedCheckovResults(): List<BaseCheckovResult> {
        if (modifiedResults.isEmpty()) {
            modifiedResults = this.checkovResults
        }

        if (CheckovGlobalState.shouldRecalculateResult) {
            var changedResults: MutableList<BaseCheckovResult> = mutableListOf()
            CheckovGlobalState.modifiedCheckovResults.forEach { modifiedResult ->
                val originalResult = modifiedResults.find { res -> res.mostlyEquals(modifiedResult) }
                if (originalResult != null) {
                    val index = modifiedResults.indexOf(originalResult)
                    modifiedResults.removeAt(index) //FYI working with `list[index] = newVal` DIDNT WORK!!! very messy bug so don't refactor this back
                    changedResults.add(modifiedResult)
                }
            }
            modifiedResults.addAll(changedResults)
            CheckovGlobalState.shouldRecalculateResult = false
        }

        modifiedResults.removeAll(CheckovGlobalState.suppressedVulnerabilitiesToIgnore)
        return modifiedResults
    }

    fun addCheckovResultFromFileScan(newCheckovResults: List<CheckovResult>, filePath: String) {
        newCheckovResults.forEach { newCheckovResult ->
            run {
                newCheckovResult.file_abs_path = fromDockerFilePath(newCheckovResult.file_abs_path)

                if ( newCheckovResult.file_abs_path != filePath &&
                        filePath.contains(newCheckovResult.file_abs_path)) {
                    newCheckovResult.file_abs_path = filePath
                }

                checkovResults.removeIf { savedCheckovResult -> savedCheckovResult.absoluteFilePath == newCheckovResult.file_abs_path }

            }
        }
        setCheckovResultsFromResultsList(newCheckovResults)
    }

    fun removeCheckovResultByFilePath(filePath: String) {
        checkovResults.removeIf { savedCheckovResult ->
            savedCheckovResult.absoluteFilePath == filePath
        }
    }

    fun deleteAllCheckovResults() {
        checkovResults.clear()
    }

    private fun getCheckType(checkType: String): CheckType {
        val typePart = checkType.split("_").first().uppercase()
        when {
            typePart == CheckType.SAST.toString() -> {
                return CheckType.SAST
            }
            typePart == CheckType.CDK.toString() -> {
                return CheckType.CDK
            }
            else -> {
                return CheckType.valueOf(checkType.uppercase())
            }
        }
    }

    fun setCheckovResultsFromResultsList(results: List<CheckovResult>) {
        for (result in results) {
            try {
                result.file_abs_path = fromDockerFilePath(result.file_abs_path)

                if (isWindows()) {
                    result.file_abs_path = result.file_abs_path.replace("\\", "/");
                }

                val category: Category = mapCheckovCheckTypeToScanType(result.check_type, result.check_id)
                val checkType = this.getCheckType(result.check_type)
                val resource: String = CheckovUtils.extractResource(result, category, checkType)
                val name: String = getResourceName(result, category)
                val severity = runCatching { Severity.valueOf(result.severity.uppercase()) }.getOrDefault(Severity.INFO)
                val description = if(!result.description.isNullOrEmpty()) result.description else result.short_description
                val filePath = result.file_abs_path.replace(baseDir, "").replace("//", "/")
                val fileAbsPath = if (!result.file_abs_path.contains(baseDir)) Paths.get(baseDir, File.separator, result.file_abs_path).toString() else result.file_abs_path

                when (category) {
                    Category.VULNERABILITIES -> {
                        if (result.vulnerability_details == null) {
                            throw Exception("type is vulnerability but no vulnerability_details")
                        }
                        val vulnerabilityCheckovResult = VulnerabilityCheckovResult(
                                checkType, filePath,
                                resource, name, result.check_id, severity, description,
                                result.guideline, fileAbsPath, result.file_line_range, result.fixed_definition,
                                result.code_block,
                                result.vulnerability_details.cvss,
                                result.vulnerability_details.package_name,
                                result.vulnerability_details.package_version,
                                result.vulnerability_details.lowest_fixed_version,
                                result.vulnerability_details.link,
                                result.vulnerability_details.published_date,
                                result.vulnerability_details.vector,
                                result.vulnerability_details.id,
                                result.file_path,
                                result.vulnerability_details.risk_factors,
                                result.vulnerability_details.root_package_name,
                                result.vulnerability_details.root_package_version,
                                result.vulnerability_details.root_package_fix_version,
                                result.vulnerability_details.fix_command
                                )
                        checkovResults.add(vulnerabilityCheckovResult)

                        continue
                    }
                    Category.SECRETS -> {
                        val secretCheckovResult = SecretsCheckovResult(checkType, filePath,
                                resource, name, result.check_id, severity, description,
                                result.guideline, fileAbsPath, result.file_line_range, result.fixed_definition,
                                result.code_block, result.check_name)
                        checkovResults.add(secretCheckovResult)
                        continue
                    }
                    Category.IAC -> {
                        val iacCheckovResult = IacCheckovResult(checkType, filePath,
                                resource, name, result.check_id, severity, description,
                                result.guideline, fileAbsPath, result.file_line_range, result.fixed_definition,
                                result.code_block, result.check_name)
                        checkovResults.add(iacCheckovResult)
                        continue
                    }
                    Category.LICENSES -> {
                        if (result.vulnerability_details == null) {
                            throw Exception("type is license but no vulnerability_details")
                        }

                        val licenseCheckovResult = LicenseCheckovResult(checkType, filePath,
                                resource, name, result.check_id, severity, description,
                                result.guideline, fileAbsPath, result.file_line_range, result.fixed_definition,
                                result.code_block,
                                result.vulnerability_details.package_name,
                                result.vulnerability_details.license,
                                result.check_id.uppercase() == "BC_LIC_1"
                        )
                        checkovResults.add(licenseCheckovResult)
                        continue
                    }

                    Category.WEAKNESSES -> {
                        val weaknessCheckovResult = WeaknessCheckovResult(checkType, filePath,
                                resource, name, result.check_id, severity, description,
                                result.guideline, fileAbsPath, result.file_line_range, result.fixed_definition,
                                result.code_block, result.check_name, result.cwe, result.owasp, result.metadata)
                        checkovResults.add(weaknessCheckovResult)
                        continue
                    }
                }
            } catch (e: Exception) {
                logger.info("Error while adding checkov result $result", e)
            }

        }
    }

    private fun mapCheckovCheckTypeToScanType(checkType: String, checkId: String): Category {
        when  {
            arrayOf("ansible", "arm", "bicep", "cloudformation", "dockerfile", "helm", "json",
                    "yaml", "kubernetes", "kustomize", "openapi", "serverless", "terraform", "terraform_plan").contains(checkType) -> {
                return Category.IAC
            }

            checkType == "secrets" -> {
                return Category.SECRETS
            }

            arrayOf("sca_package", "sca_image").contains(checkType) -> {
                if (checkId.uppercase().startsWith("BC_LIC")) {
                    return Category.LICENSES
                } else if (checkId.uppercase().startsWith("BC_VUL")) {
                    return Category.VULNERABILITIES
                }
            }

            checkType.startsWith("cdk_") || checkType.startsWith("sast_") -> {
                return Category.WEAKNESSES
            }
        }

        throw Exception("Scan type is not found in the result!")
    }

    private fun getResourceName(result: CheckovResult, category: Category): String {
        return when (category) {
            Category.IAC, Category.SECRETS -> {
                getIACResourceName(result.check_name, result.file_line_range[0], result.file_line_range[1])
            }

            Category.LICENSES -> {
                result.vulnerability_details?.license ?: "NOT FOUND"
            }

            Category.VULNERABILITIES -> {
                "${result.vulnerability_details?.package_name}:${result.vulnerability_details?.package_version}  (${result.vulnerability_details?.id})"
            }

            Category.WEAKNESSES ->{
                getIACResourceName(result.check_name, result.file_line_range[0], result.file_line_range[1])
            }
        }
    }

    fun getIACResourceName(checkName: String, firstLine: Int, lastLine: Int): String {
        return "$checkName (${firstLine} - ${lastLine})"
    }
}
