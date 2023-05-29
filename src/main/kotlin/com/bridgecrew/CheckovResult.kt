package com.bridgecrew

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken

val gson = Gson()

//data class RiskFactors(
//        @SerializedName("Has fix")
//        val has_fix: String?,
//
//        @SerializedName("Critical severity")
//        val critical_severity: String?,
//
//        @SerializedName("DoS - High")
//        val dos_: String?,
//
//        @SerializedName("Attack vector: network")
//        val has_fix: String?,
//
//        @SerializedName("Attack complexity: low")
//        val has_fix: String?,
//)

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
        val root_package_fix_version: String?
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
        val code_block: List<List<Object>>,
        var check_type: String,
        val fixed_definition: String = ""
)