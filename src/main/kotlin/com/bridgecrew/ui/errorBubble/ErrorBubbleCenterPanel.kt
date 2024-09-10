package com.bridgecrew.ui.errorBubble

import com.intellij.ui.ScrollPaneFactory
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.BorderFactory
import javax.swing.JPanel
import javax.swing.JTextArea
import javax.swing.ScrollPaneConstants

class ErrorBubbleCenterPanel(text: String) : JPanel() {

    init {
        layout = BorderLayout()
        val textArea = JTextArea(text)
        border = BorderFactory.createEmptyBorder()
        textArea.lineWrap = true
        textArea.wrapStyleWord = true
        textArea.isOpaque = false
        textArea.isEditable = false

        val scroll = ScrollPaneFactory.createScrollPane(textArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED)
        scroll.minimumSize = Dimension(scroll.preferredSize.width, ErrorBubbleInnerPanel.MIN_INNER_PANEL_HEIGHT)
        scroll.isOpaque = false
        scroll.viewport.isOpaque = false
        scroll.border = null
        scroll.viewportBorder = null
        add(scroll, BorderLayout.CENTER)
    }
}