package com.bridgecrew.results

import com.bridgecrew.Metadata

class WeaknessCheckovResult(
    checkType: CheckType,
    filePath: String,
    resource: String,
    name: String,
    id: String,
    severity: Severity,
    description: String?,
    guideline: String?,
    absoluteFilePath: String,
    fileLineRange: List<Int>,
    fixDefinition: String?,
    codeBlock: List<List<Any>>,
    val checkName: String,
    val cwe: List<String>?,
    val owasp: List<String>?,
    val metadata: Metadata?
) : BaseCheckovResult(
    category = Category.WEAKNESSES,
    checkType,
    filePath,
    resource,
    name,
    id,
    severity,
    description,
    guideline,
    absoluteFilePath,
    fileLineRange,
    fixDefinition,
    codeBlock
)