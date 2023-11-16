package com.bridgecrew.ui.rightPanel.extraInfoPanel

import com.bridgecrew.results.WeaknessCheckovResult
import com.bridgecrew.ui.rightPanel.dictionaryDetails.WeaknessDictionaryPanel

class WeaknessExtraInfoPanel(result: WeaknessCheckovResult) : CheckovExtraInfoPanel(result) {

    init {
        initLayout()
        add(WeaknessDictionaryPanel(result))
        addCodeDiffPanel()
    }
}