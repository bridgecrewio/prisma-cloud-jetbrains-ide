package com.bridgecrew.ui.rightPanel.extraInfoPanel

import com.bridgecrew.results.WeaknessCheckovResult
import com.bridgecrew.ui.rightPanel.dictionaryDetails.WeaknessDictionaryPanel
import com.intellij.openapi.project.Project

class WeaknessExtraInfoPanel(result: WeaknessCheckovResult, project: Project) : CheckovExtraInfoPanel(result) {

    init {
        initLayout()
        add(WeaknessDictionaryPanel(result, project))
        addCodeDiffPanel()
    }
}