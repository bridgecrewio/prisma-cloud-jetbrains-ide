package com.bridgecrew.ui.buttons

import java.awt.Cursor
import javax.swing.JButton

open class CheckovLinkButton(userText: String): JButton() {

    init {
        this.text = userText
        this.isBorderPainted = false
        this.isOpaque = false
        this.isContentAreaFilled = false
        setEnabledLook()
    }

    fun setDisabledLook() {
        this.cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)
    }

    fun setEnabledLook() {
        this.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
    }
}