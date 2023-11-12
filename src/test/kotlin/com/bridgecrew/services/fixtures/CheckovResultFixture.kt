package com.bridgecrew.services.fixtures

import com.bridgecrew.CheckovResult

fun createDefaultResults(): CheckovResult {
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
            check_type = "sast_java",
            fixed_definition = ""
    )
}

fun createSastCheckovResultResults(): CheckovResult {
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
            check_type = "sast_java",
            fixed_definition = ""
    )
}