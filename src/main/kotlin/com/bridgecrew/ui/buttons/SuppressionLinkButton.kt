package com.bridgecrew.ui.buttons

import com.bridgecrew.results.BaseCheckovResult
import com.bridgecrew.ui.actions.SuppressAction

const val suppressionButtonText = "Suppress"

class SuppressionLinkButton(private var result: BaseCheckovResult) : CheckovLinkButton(suppressionButtonText) {

    init {
        addActionListener(SuppressAction(this, result))
    }
}