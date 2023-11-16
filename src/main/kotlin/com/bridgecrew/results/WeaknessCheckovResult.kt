package com.bridgecrew.results

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
        val checkName: String) :
        BaseCheckovResult(
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
                codeBlock)