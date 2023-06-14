package com.bridgecrew.ui.rightPanel

import com.bridgecrew.results.*
import com.bridgecrew.ui.rightPanel.extraInfoPanel.*
import com.bridgecrew.ui.rightPanel.topPanel.*
import com.bridgecrew.utils.*
import com.intellij.ui.ScrollPaneFactory
import com.intellij.util.ui.UIUtil
import java.awt.Graphics
import java.awt.Graphics2D
import javax.swing.*
import javax.swing.plaf.basic.BasicSeparatorUI

class CheckovErrorRightPanel(var result: BaseCheckovResult): JPanel() {

    init {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        background = UIUtil.getEditorPaneBackground() ?: background
        add(createTitlePanel())
        add(createSeparator())
        add(createExtraInfoPanel())
        border = BorderFactory.createEmptyBorder(0,10,0,10)
    }

    private fun createSeparator(): JSeparator {
        val separator = JSeparator(JSeparator.HORIZONTAL)
        separator.setUI(object : BasicSeparatorUI() {
            override fun paint(g: Graphics, c: JComponent) {
                val g2d = g.create() as Graphics2D
                g2d.paint = if(isDarkMode()) separatorColorDark else separatorColorLight
                g2d.fillRect(0, 0, c.width, 1)
                g2d.dispose()
            }
        })
        return separator
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
}