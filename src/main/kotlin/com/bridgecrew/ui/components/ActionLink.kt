package com.bridgecrew.ui.components

import com.intellij.ui.AnimatedIcon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import java.awt.Cursor
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.util.function.Supplier
import javax.swing.JLabel
import javax.swing.SwingConstants

const val LINK_WRAPPER = "<html><a href=''>%s</a></html>"

class ActionLink(
    private val label: String,
    private val onClick: Runnable,
    private val preClickValidate: Supplier<Boolean>? = null
) : JLabel() {

    init {
        setDefaultState()
        this.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                setLoadingState()
                val isValid: Boolean = preClickValidate?.get() == true
                if (!isValid) {
                    setDefaultState()
                    return
                }
                CoroutineScope(Dispatchers.IO).launch {
                    onClick.run()
                    setDefaultState()
                }
            }
        })
    }

    private fun setDefaultState() {
        this.text = LINK_WRAPPER.format(label)
        this.icon = null
        this.horizontalAlignment = SwingConstants.LEFT
        this.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
    }

    private fun setLoadingState() {
        this.text = ""
        this.icon = AnimatedIcon.Default()
        this.horizontalAlignment = SwingConstants.CENTER
        this.cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)
    }
}