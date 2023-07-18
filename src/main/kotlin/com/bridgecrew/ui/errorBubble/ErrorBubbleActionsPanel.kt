package com.bridgecrew.ui.errorBubble

import com.bridgecrew.results.BaseCheckovResult
import com.bridgecrew.ui.actions.FixAction
import com.bridgecrew.ui.actions.FocusOnFileInTree
import com.bridgecrew.ui.buttons.CheckovLinkButton
import com.bridgecrew.ui.buttons.DocumentationLinkButton
import com.bridgecrew.ui.buttons.SuppressionButton
import com.bridgecrew.ui.buttons.SuppressionLinkButton
import com.bridgecrew.utils.*
import icons.CheckovIcons
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import javax.swing.*

class ErrorBubbleActionsPanel(val result: BaseCheckovResult) : JPanel() {

    init {
        layout = BoxLayout(this, BoxLayout.X_AXIS)
        addFixIfNeeded()
        addSuppressIfNeeded()
        addConsoleButton()
        addDocumentationIfNeeded()
        addLogo()
        maximumSize = Dimension(ErrorBubbleInnerPanel.PANEL_WIDTH, this.preferredSize.height)
    }

    private fun addFixIfNeeded() {
        if (result.fixDefinition != null) {
            val fixButton = CheckovLinkButton("Fix")
            fixButton.addActionListener(FixAction(fixButton, result))
            addStyledButton(fixButton)
        }
    }

    private fun addSuppressIfNeeded() {
        if (isShowSuppressionButton(result)) {
            addStyledButton(SuppressionLinkButton(result))
        }
    }

    private fun addConsoleButton() {
        val button = CheckovLinkButton("Console")
        button.addActionListener(FocusOnFileInTree("${result.filePath}/${result.resource}/${result.name}"))
        addStyledButton(button)
    }

    private fun addDocumentationIfNeeded() {
        if (result.guideline != null && !CheckovUtils.isCustomPolicy(result)) {
            addStyledButton(DocumentationLinkButton(result.guideline))
        }
    }

    private fun addLogo() {
        add(Box.createHorizontalGlue())
        add(JLabel(CheckovIcons.prismaIcon))
        add(Box.createRigidArea(Dimension(5, 0)))
        val prismaLabel = JLabel("Prisma Cloud")
        prismaLabel.font = Font("SF Pro Text", Font.BOLD, 10)
        prismaLabel.foreground = Color.decode("#7F8B91")
        add(prismaLabel)
        add(Box.createRigidArea(Dimension(10, 0)))
    }

    private fun addStyledButton(button: JButton) {
        button.font = ERROR_BUBBLE_ACTIONS_FONT
        button.horizontalAlignment = JButton.LEFT
        button.margin = ERROR_BUBBLE_ACTIONS_MARGIN
        button.foreground = if (isDarkMode()) ERROR_BUBBLE_ACTIONS_COLOR_DARK else ERROR_BUBBLE_ACTIONS_COLOR_LIGHT

        val textMetrics = button.getFontMetrics(button.font)
        val textWidth = textMetrics.stringWidth(button.text)
        val textHeight = textMetrics.height
        button.preferredSize = Dimension(
            textWidth + button.insets.left  + button.insets.right + button.margin.left + button.margin.right,
            textHeight + button.margin.top + button.margin.bottom
        )
        add(button)
    }
}