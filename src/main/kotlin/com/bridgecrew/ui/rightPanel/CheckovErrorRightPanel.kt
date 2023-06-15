package com.bridgecrew.ui.rightPanel

import com.bridgecrew.results.*
import com.bridgecrew.ui.rightPanel.extraInfoPanel.*
import com.bridgecrew.ui.rightPanel.topPanel.*
import com.intellij.ui.ScrollPaneFactory
import com.intellij.util.ui.UIUtil
import java.awt.GridBagLayout
import javax.swing.*

class CheckovErrorRightPanel(var result: BaseCheckovResult): JPanel() {

    init {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        background = UIUtil.getEditorPaneBackground() ?: background
        add(createTitlePanel())
        add(createExtraInfoPanel())
        addVerticalGlue()
        border = BorderFactory.createEmptyBorder(0,10,16,10)
    }

    private fun createTitlePanel(): JPanel {
        val titlePanel = when(result.category) {
            Category.IAC -> IacPanelTop(result as IacCheckovResult)
            Category.VULNERABILITIES -> VulnerabilitiesPanelTop(result as VulnerabilityCheckovResult)
            Category.SECRETS -> SecretsPanelTop(result as SecretsCheckovResult)
            Category.LICENSES -> LicensePanelTop(result as LicenseCheckovResult)
        }
        return titlePanel
    }

    private fun createExtraInfoPanel(): JScrollPane {
        val extraInfoPanel = when(result.category) {
            Category.IAC -> IacExtraInfoPanel(result)
            Category.VULNERABILITIES -> VulnerabilitiesExtraInfoPanel(result as VulnerabilityCheckovResult)
            Category.SECRETS -> SecretsExtraInfoPanel(result as SecretsCheckovResult)
            Category.LICENSES -> LicenseExtraInfoPanel(result as LicenseCheckovResult)
        }
        return ScrollPaneFactory.createScrollPane(
                extraInfoPanel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        )
    }

    private fun addVerticalGlue() {
        val p = JPanel()
        p.layout = GridBagLayout()
        add(p)
    }
}