package com.bridgecrew.ui.buttons

import com.bridgecrew.results.BaseCheckovResult
import com.bridgecrew.ui.actions.SuppressAction
import javax.swing.JButton

class SuppressionButton(private var result: BaseCheckovResult): JButton("Suppress") {

    init {
        addActionListener(SuppressAction(this, result))
    }
}