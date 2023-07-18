package com.bridgecrew.settings

import com.bridgecrew.utils.PANELTYPE
import javax.swing.tree.TreePath

object CheckovGlobalState {
    var expandedDescendants = emptyList<TreePath>()
    var lastLoadedPanel = PANELTYPE.AUTO_CHOOSE_PANEL
    var scanInProgress: Boolean = false
    var filesNotToScanAfterFix: MutableList<String> = mutableListOf()
    var suppressedFileToIgnore: String = ""
}