package com.bridgecrew.ui.rightPanel.dictionaryDetails

import com.bridgecrew.results.WeaknessCheckovResult

class WeaknessDictionaryPanel(result: WeaknessCheckovResult): DictionaryExtraInfoPanel() {
    override var fieldsMap: MutableMap<String, Any?> = mutableMapOf(
            "Description" to result.description,
            "Code" to extractCode(result)
    )
    init {
        addCustomPolicyGuidelinesIfNeeded(result)
        createDictionaryLayout()
    }

    private fun extractCode(result: WeaknessCheckovResult): Any {
        return try {
            result.codeBlock[0][1].toString().trim()
        } catch (e: Exception) {
            ""
        }
    }
}