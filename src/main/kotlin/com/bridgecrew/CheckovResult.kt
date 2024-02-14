package com.bridgecrew

import com.google.gson.Gson

val gson = Gson()

data class VulnerabilityDetails(
        val id: String?,
        val package_name: String?,
        val package_version: String?,
        val link: String?,
        val description: String?,
        val license: String?,
        val cvss: Double?,
        val lowest_fixed_version: String?,
        val published_date: String?,
        val vector: String?,
        val risk_factors: Map<String, Any>,
        val root_package_name: String?,
        val root_package_version: String?,
        val root_package_fix_version: String?,
        val fix_command: FixCommand?
)

data class FixCommand(
        val msg: String?,
        val cmds: ArrayList<String>,
        val manualCodeFix: Boolean
)

data class CheckovResult(
        val check_id: String,
        val bc_check_id: String = "",
        val check_name: String,
        val file_path: String,
        val repo_file_path: String,
        var file_abs_path: String,
        val file_line_range: ArrayList<Int>,
        val resource: String,
        val severity: String,
        val description: String,
        val short_description: String,
        val vulnerability_details: VulnerabilityDetails?,
        val guideline: String = "\"No Guide\")",
        val code_block: List<List<Any>>,
        var check_type: String,
        val fixed_definition: String = "",
        val cwe: ArrayList<String>? = ArrayList(),
        val owasp: ArrayList<String>? = ArrayList(),
        val metadata: Metadata? = null
)

data class Metadata(
        val code_locations: List<DataFlow>?,
        val taint_mode: TaintMode?
)

data class TaintMode(
        val data_flow: List<DataFlow>?
)

data class DataFlow(
        val path: String,
        val start: CodePosition,
        val end: CodePosition,
        val code_block: String
)

data class CodePosition(
        val row: Int,
        val column: Int
)