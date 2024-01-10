package com.bridgecrew.fixtures

import com.bridgecrew.results.WeaknessCheckovResult
import com.google.gson.Gson



const val metadataCodeCollectionJson = """
    "metadata": {
         "code_locations": [{
             "path": "/Users/sast-core/tests_policies/src/python/EncryptionKeySize2.py",
             "start": {
                "row": 13,
                "column": 0
             },
             "end": {
                "row": 13,
                "column": 66
             },
             "code_block": "cryptography.hazmat.primitives.asymmetric.rsa.generate_private_key"
         }, {
             "path": "/Users/sast-core/tests_policies/src/python/EncryptionKeySize.py",
             "start": {
                "row": 14,
                "column": 73
             },
             "end": {
                "row": 15,
                "column": 86
             },
             "code_block": "key_size=size"
         }]
     },
"""

const val metadataTaintModeJson = """
    "metadata": {
         "taint_mode": [{
             "path": "/Users/sast-core/tests_policies/src/python/EncryptionKeySize.py",
             "start": {
                "row": 13,
                "column": 0
             },
             "end": {
                "row": 13,
                "column": 66
             },
             "code_block": "cryptography.hazmat.primitives.asymmetric.rsa.generate_private_key"
         }, {
             "path": "/Users/sast-core/tests_policies/src/python/EncryptionKeySize.py",
             "start": {
                "row": 14,
                "column": 73
             },
             "end": {
                "row": 15,
                "column": 86
             },
             "code_block": "key_size=size"
         }]
     },
"""

const val metadataEmptyJson = """
    "metadata": {
     },
"""

const val metadataAbsent = ""



fun createWeaknessCheckovResult(metadata: String): WeaknessCheckovResult {
    val resultJson = """
         {
         	"checkName": "Unsafe custom MessageDigest is implemented",
         	"cwe": ["CWE-327: Use of a Broken or Risky Cryptographic Algorithm"],
         	"owasp": "TBD",
         	$metadata
         	"category": "WEAKNESSES",
         	"checkType": "SAST",
         	"filePath": "/features/sast/BrokenCryptographicAlgorithm/fail.java",
         	"resource": "",
         	"name": "Unsafe custom MessageDigest is implemented (1 - 2)",
         	"id": "CKV3_SAST_13",
         	"severity": "MEDIUM",
         	"guideline": "https://docs.paloaltonetworks.com/prisma/prisma-cloud/prisma-cloud-code-security-policy-reference/sast-policies/java-policies/sast-policy-13",
         	"absoluteFilePath": "/Users/user/testing-resources/features/sast/BrokenCryptographicAlgorithm/fail.java",
         	"fileLineRange": [1, 2],
         	"codeBlock": [
         		[1.0, "class MyBadImplementation extends java.security.MessageDigest {\n"],
         		[2.0, "}\n"]
         	],
         	"codeDiffFirstLine": 1
         }
    """.trimIndent()

    val gson = Gson()
    val userObject: WeaknessCheckovResult = gson.fromJson(resultJson, WeaknessCheckovResult::class.java)

    return userObject
}
