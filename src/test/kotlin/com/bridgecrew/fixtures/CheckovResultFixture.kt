package com.bridgecrew.fixtures

import com.bridgecrew.CheckovResult

fun createSastCheckovResult(): CheckovResult {
    return CheckovResult(
        check_id = "CKV3_SAST_13",
        bc_check_id = "",
        check_name = "Unsafe custom MessageDigest is implemented",
        file_path = "Assertions.fail.java",
        repo_file_path = "/sast-policies/tests/sast/source_code/java/BrokenCryptographicAlgorithm/Assertions.fail.java",
        file_abs_path = "/Users/user/sast-policies/tests/sast/source_code/java/BrokenCryptographicAlgorithm/fail.java",
        file_line_range = ArrayList(listOf(1, 3)),
        resource = "",
        severity = "MEDIUM",
        description = "",
        short_description = "",
        vulnerability_details = null,
        guideline = "",
        code_block = listOf(listOf(1.0, "class MyBadImplementation extends java.security.MessageDigest {], [2.0 ], [3.0, }")),
        cwe = arrayListOf("CWE-327: Use of a Broken or Risky Cryptographic Algorithm"),
        fixed_definition = ""
    ).apply {
        check_type =  "sast_java"
    }
}

fun createCdkCheckovResult(): CheckovResult {
    return CheckovResult(
        check_id = "CKV2_AWS_6",
        bc_check_id = "",
        check_name = "Ensure that S3 bucket has a Public Access block",
        file_path = "Assertions.fail.java",
        repo_file_path = "/cdkgoat/cdkgoat_stack.py",
        file_abs_path = "/Users/user/cdkgoat/cdkgoat/cdkgoat_stack.py",
        file_line_range = ArrayList(listOf(1, 3)),
        resource = "",
        severity = "",
        description = "",
        short_description = "",
        vulnerability_details = null,
        guideline = "",
        code_block = listOf(listOf(19, "s3.Bucket(self, bucket_name, removal_policy=RemovalPolicy.DESTROY)")),
        cwe = null,
        fixed_definition = ""
    ).apply {
        check_type = "cdk_python"
    }
}
