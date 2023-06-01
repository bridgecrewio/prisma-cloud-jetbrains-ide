package com.bridgecrew.ui.errorBubble

import com.bridgecrew.results.BaseCheckovResult
import com.bridgecrew.ui.actions.FixAction
import com.bridgecrew.ui.actions.FocusOnFileInTree
import com.bridgecrew.ui.buttons.CheckovLinkButton
import com.bridgecrew.ui.buttons.DocumentationButton
import com.bridgecrew.ui.buttons.SuppressionButton
import com.bridgecrew.utils.CheckovUtils
import com.bridgecrew.utils.FileType
import com.bridgecrew.utils.SUPPRESSION_BUTTON_ALLOWED_FILE_TYPES
import com.bridgecrew.utils.getFileType
import icons.CheckovIcons
import java.awt.Dimension
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
            add(fixButton)
        }
    }

    private fun addSuppressIfNeeded() {
        val fileType: FileType = getFileType(result.filePath)
        if (SUPPRESSION_BUTTON_ALLOWED_FILE_TYPES.contains(fileType)) {
            add(SuppressionButton(result))
        }
    }

    private fun addConsoleButton() {
        val button = CheckovLinkButton("Console")
        button.addActionListener(FocusOnFileInTree("${result.filePath}/${result.resource}/${result.name}"))
        add(button)
    }

    private fun addDocumentationIfNeeded() {
        if (result.guideline != null && !CheckovUtils.isCustomPolicy(result)) {
            add(DocumentationButton(result.guideline))
        }
    }

    private fun addLogo() {
        add(Box.createHorizontalGlue())
        add(JLabel(CheckovIcons.prismaIcon))
        add(Box.createRigidArea(Dimension(5, 0)))
        add(JLabel("Prisma Cloud"))
        add(Box.createRigidArea(Dimension(10, 0)))
    }
}