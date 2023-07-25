package com.bridgecrew.ui.rightPanel.topPanel

import com.bridgecrew.results.*
import com.bridgecrew.ui.buttons.SuppressionButton
import com.bridgecrew.utils.*
import com.intellij.ide.DataManager
import com.intellij.openapi.project.Project
import com.intellij.util.ui.UIUtil
import java.awt.Dimension
import java.awt.Font
import java.awt.GridBagLayout
import javax.swing.*

open class CheckovDescriptionPanelTop(val result: BaseCheckovResult) : JPanel() {

    init {
        layout = GridBagLayout()
        maximumSize = Dimension(Int.MAX_VALUE, 30)
        border = BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0,0,1,0, getSeparatorColor()),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        )

        background = UIUtil.getEditorPaneBackground()
    }

    fun createTitleAndIcon(title: String, severity: Severity): JLabel {
        val font = Font("SF Pro Text", Font.BOLD, 13)
        val titleLabel = JLabel(title, getSeverityIconBySeverity(severity), SwingConstants.LEFT)
        titleLabel.toolTipText = title
        titleLabel.preferredSize = Dimension(600, 30)
        titleLabel.font = font.deriveFont(Font.BOLD)
        titleLabel.text = "<html><div style='line-height: 16px;'>" + titleLabel.text + "</div></html>"
        return titleLabel
    }

    fun getTitle(result: BaseCheckovResult): String {
        if (CheckovUtils.isCustomPolicy(result)) {
            return result.name
        }

        if (result.category == Category.VULNERABILITIES &&
                (result.checkType == CheckType.SCA_PACKAGE || result.checkType == CheckType.SCA_IMAGE)) {
            return result.name
        }

        if (result.category == Category.IAC) {
            return (result as IacCheckovResult).checkName
        }

        return result.id
    }

    fun createActionsPanel(): JPanel {
        val actionsPanel = JPanel().apply { layout = BoxLayout(this, BoxLayout.X_AXIS) }
        actionsPanel.add(Box.createHorizontalGlue())
        return actionsPanel
    }

    fun isShowDocumentationButton(result: BaseCheckovResult): Boolean {
        return !CheckovUtils.isCustomPolicy(result) && result.guideline != null && isUrl(result.guideline)
    }

    fun createSuppressionButton(panel: JPanel) {
        if (isShowSuppressionButton(result)) {
            val dataContext = DataManager.getInstance().dataContext
            val project = dataContext.getData("project") as Project
            panel.add(SuppressionButton(project, result))
        }
    }
}