package com.bridgecrew.ui.buttons

import com.bridgecrew.results.BaseCheckovResult
import com.bridgecrew.ui.actions.SuppressAction
import com.intellij.openapi.project.Project

const val suppressionButtonText = "Suppress"

class SuppressionLinkButton(private val project: Project, private var result: BaseCheckovResult) : CheckovLinkButton(suppressionButtonText) {

    init {
        addActionListener(SuppressAction(project, this, result))
    }
}