package com.bridgecrew.ui.buttons

import com.bridgecrew.results.BaseCheckovResult
import com.bridgecrew.ui.actions.SuppressAction
import com.intellij.openapi.project.Project
import javax.swing.JButton

class SuppressionButton(private val project: Project, private var result: BaseCheckovResult): JButton("Suppress") {

    init {
        addActionListener(SuppressAction(project, this, result))
    }
}