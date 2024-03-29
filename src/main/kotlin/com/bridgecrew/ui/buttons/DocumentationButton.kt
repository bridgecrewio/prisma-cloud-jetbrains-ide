package com.bridgecrew.ui.buttons

import java.awt.Desktop
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.net.URI
import javax.swing.JButton

class DocumentationButton(private var link: String) : JButton("Documentation"), ActionListener {

    init {
        addActionListener(this)
    }

    override fun actionPerformed(e: ActionEvent?) {
        Desktop.getDesktop().browse(URI(link))
    }
}