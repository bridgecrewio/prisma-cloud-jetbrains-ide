package com.bridgecrew.utils

const val PLUGIN_ID = "com.github.bridgecrewio.prismacloud"

object PANELTYPE {
    const val AUTO_CHOOSE_PANEL = 0
    const val CHECKOV_FILE_SCAN_FINISHED = 1
    const val CHECKOV_FRAMEWORK_SCAN_FINISHED = 2
    const val CHECKOV_REPOSITORY_SCAN_STARTED = 3
    const val CHECKOV_REPOSITORY_SCAN_FAILED = 4
    const val CHECKOV_INITIALIZATION_PROGRESS = 5
    const val CHECKOV_LOAD_TABS_CONTENT = 6
}

const val DEFAULT_FILE_TIMEOUT: Long = 80000 // 1.3 minutes
const val DEFAULT_FRAMEWORK_TIMEOUT: Long = 720000 // 12 minutes

const val GIT_DEFAULT_REPOSITORY_NAME = "jetbrains/extension"

val FULL_SCAN_FRAMEWORKS = arrayListOf("ansible", "arm", "bicep", "cloudformation", "dockerfile", "helm", "json",
        "yaml", "kubernetes", "kustomize", "openapi", "sca_package", "sca_image", "secrets", "serverless", "terraform",
        "terraform_plan", "sast", "cdk")

val PARTIAL_SCAN_FRAMEWORKS = arrayListOf("ansible", "arm", "bicep", "cloudformation", "dockerfile", "helm", "json",
    "yaml", "kubernetes", "kustomize", "openapi", "sca_package", "sca_image", "secrets", "serverless", "terraform",
    "terraform_plan")

val FULL_SCAN_EXCLUDED_PATHS = arrayListOf("node_modules")
val EXCLUDED_FILE_NAMES = arrayListOf("package-lock.json")
const val FULL_SCAN_STATE_FILE = "full_scan_state"

var DESIRED_NUMBER_OF_FRAMEWORK_FOR_FULL_SCAN = FULL_SCAN_FRAMEWORKS.size
const val DESIRED_NUMBER_OF_SINGLE_FILE_SCANS = 10

enum class FileType {
    JSON,
    TERRAFORM,
    YAML,
    DOCKERFILE,
    JAVASCRIPT,
    TYPESCRIPT,
    JAVA,
    KOTLIN,
    PYTHON,
    TEXT,
    XML,
    GOLANG,
    GRADLE,
    CSPROJ,
    GEMFILE,
    UNKNOWN
}

// license
const val VIOLATED_LICENSES_TITLE = "Non-Complaint license"
const val UNKNOWN_LICENSES_TITLE = "Unknown license"

const val VIOLATED_LICENSES_DESCRIPTION = "At least one of the license(s) used by this package and/or its dependencies are not OSI approved."
const val UNKNOWN_LICENSES_DESCRIPTION = "At least one of the license(s) used by this package and/or its dependencies are not recognized under the SPDX, are unidentifiable or belong to a proprietary open-source license. Ensure these packages are compliant."

const val FULL_SCAN_RERO_LIMIT = 512
