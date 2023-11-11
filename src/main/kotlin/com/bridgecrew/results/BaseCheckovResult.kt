package com.bridgecrew.results

enum class Category(category: String) {
    SECRETS("Secrets"),
    IAC("IAC"),
    VULNERABILITIES("Vulnerabilities"),
    LICENSES("Licenses"),
    WEAKNESSES("Weaknesses")
}

enum class CheckType(checkType: String) {
    ANSIBLE("ansible"),
    ARM("arm"),
    BICEP("bicep"),
    CLOUDFORMATION("cloudformation"),
    DOCKERFILE("dockerfile"),
    HELM("helm"),
    JSON("json"),
    YAML("yaml"),
    KUBERNETES("kubernetes"),
    KUSTOMIZE("kustomize"),
    OPENAPI("openapi"),
    SCA_PACKAGE("sca_package"),
    SCA_IMAGE("sca_image"),
    SECRETS("secrets"),
    SERVERLESS("serverless"),
    TERRAFORM("terraform"),
    TERRAFORM_PLAN("terraform_plan"),
    SAST("sast")
}

enum class Severity {
    CRITICAL,
    HIGH,
    MEDIUM,
    LOW,
    INFO,
    UNKNOWN
}

open class BaseCheckovResult(
        val category: Category,
        val checkType: CheckType,
        val filePath: String,
        val resource: String,
        val name: String,
        val id: String,
        val severity: Severity,
        val description: String?,
        val guideline: String?,
        val absoluteFilePath: String,
        val fileLineRange: List<Int>,
        val fixDefinition: String?,
        val codeBlock: List<List<Any>>,
        var codeDiffFirstLine: Int = fileLineRange[0]
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BaseCheckovResult) return false
        return this.category == other.category &&
                this.checkType == other.checkType &&
                this.filePath == other.filePath &&
                this.resource == other.resource &&
                this.name == other.name &&
                this.id == other.id &&
                this.severity == other.severity &&
                this.description == other.description &&
                this.guideline == other.guideline &&
                this.absoluteFilePath == other.absoluteFilePath &&
                this.fileLineRange == other.fileLineRange &&
                this.fixDefinition == other.fixDefinition &&
                this.codeBlock == other.codeBlock &&
                this.codeDiffFirstLine == other.codeDiffFirstLine
    }

    // this is to compare results that are originally the same, but not entirely equal since we modified them
    fun mostlyEquals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BaseCheckovResult) return false
        return this.id == other.id &&
                this.checkType == other.checkType &&
                this.filePath == other.filePath &&
                this.resource == other.resource &&
                this.category == other.category &&
                this.severity == other.severity
    }

    override fun hashCode(): Int {
        var result = category.hashCode()
        result = 31 * result + checkType.hashCode()
        result = 31 * result + filePath.hashCode()
        result = 31 * result + resource.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + severity.hashCode()
        return result
    }
}