package com.bridgecrew.testing

import com.intellij.openapi.ui.SimpleToolWindowPanel
import java.io.File
import java.io.InputStream

class CheckovRunnerTesting : SimpleToolWindowPanel(false, true) {
    val results: List<String> = mutableListOf("""{
        "check_id": "CKV2_AWS_12",
        "bc_check_id": "BC_AWS_LOGGING_10",
        "check_name": "Ensure VPC flow logging is enabled in all VPCs",
        "file_path": "src/main/java/com/jfrog/ide/idea/ui/ComponentIssueDetails.java",
        "file_abs_path": "/Users/yorhov/development/test1.tf",
        "repo_file_path": "/test.tf",
        "file_line_range": [
        1,
        10
        ],
        "resource": "aws_vpc.eks_vpc",
        "evaluations": null,
        "check_class": "checkov.common.graph.checks_infra.base_check",
        "fixed_definition": null,

        "caller_file_path": null,
        "caller_file_line_range": null,
        "guideline": "https://docs.bridgecrew.io/docs/logging_9-enable-vpc-flow-logging"
    }""",
        """{
        "check_id": "CKV2_AWS_11",
        "bc_check_id": "BC_AWS_LOGGING_9",
        "check_name": "Ensure VPC flow logging is enabled in all VPCs",
        "file_path": "src/main/java/com/jfrog/ide/idea/ui/ComponentsTree.java",
        "file_abs_path": "/Users/yorhov/development/test.tf",
        "repo_file_path": "/test.tf",
        "file_line_range": [
        1,
        59
        ],
        "resource": "aws_vpc.eks_vpc",
        "evaluations": null,
        "check_class": "checkov.common.graph.checks_infra.base_check",
        "fixed_definition": null,

        "caller_file_path": null,
        "caller_file_line_range": null,
        "guideline": "https://docs.bridgecrew.io/docs/logging_9-enable-vpc-flow-logging"
    }""",
        """{
        "check_id": "CKV2_AWS_13",
        "bc_check_id": "BC_AWS_LOGGING_13",
        "check_name": "Ensure VPC flow logging is enabled in all VPCs",
        "file_path": "src/main/java/com/jfrog/ide/idea/ui/ComponentsTree.java",
        "file_abs_path": "/Users/yorhov/development/test.tf",
        "repo_file_path": "/test.tf",
        "file_line_range": [
        43,
        59
        ],
        "resource": "aws_vpc.eks_vpc",
        "evaluations": null,
        "check_class": "checkov.common.graph.checks_infra.base_check",
        "fixed_definition": null,

        "caller_file_path": null,
        "caller_file_line_range": null,
        "guideline": "https://docs.bridgecrew.io/docs/logging_9-enable-vpc-flow-logging"
    }""")

    fun readFileAsLinesUsingUseLines(fileName: String): String {
        val inputStream: InputStream = File(fileName).inputStream()
        return inputStream.bufferedReader().use { it.readText() }
    }
}
