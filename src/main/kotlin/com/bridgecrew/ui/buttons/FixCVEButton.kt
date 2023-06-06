package com.bridgecrew.ui.buttons

import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.JButton
import javax.swing.JOptionPane

class FixCVEButton(private var id: String): JButton("Fix CVE"), ActionListener {

    init {
        addActionListener(this)
    }

    override fun actionPerformed(e: ActionEvent?) {
        JOptionPane.showMessageDialog(null, "fix CVE clicked id is $id")
    }
}