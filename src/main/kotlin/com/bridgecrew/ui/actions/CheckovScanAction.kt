package com.bridgecrew.ui.actions

import com.bridgecrew.analytics.AnalyticsService
import com.bridgecrew.services.scan.CheckovScanService
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAware

object CheckovScanAction : AnAction(), DumbAware {

    private val presentation = Presentation()
    private var isExecuteState = true

    init {
        updateIcon()
        presentation.isEnabled = false
    }

//    TODO: Uncomment when moving to Kotlin 2 + Java 21
//    override fun getActionUpdateThread(): ActionUpdateThread {
//        return ActionUpdateThread.EDT
//    }

    override fun actionPerformed(actionEvent: AnActionEvent) {
        val project = actionEvent.project
        if (actionEvent.presentation.icon == AllIcons.Actions.Execute) {
            isExecuteState = false
            project!!.service<AnalyticsService>().fullScanButtonWasPressed()
            project.service<CheckovScanService>().scanProject(project)
        } else {
            isExecuteState = true
            presentation.isEnabled = false
            project?.service<CheckovScanService>()?.cancelFullScan(project)
        }
        updateIcon()
    }

    override fun update(e: AnActionEvent) {
        super.update(e)
        e.presentation.copyFrom(presentation)
        updateIcon()
    }

    private fun updateIcon() {
        if (isExecuteState) {
            presentation.icon = AllIcons.Actions.Execute
            presentation.text = "Run Prisma Cloud Scan"
        } else {
            presentation.icon = AllIcons.Actions.Suspend
            presentation.text = "Cancel Prisma Cloud Scan"
        }
    }

    fun resetActionDynamically(isExecuteState: Boolean) {
        presentation.isEnabled = true
        CheckovScanAction.isExecuteState = isExecuteState
        updateIcon()
    }
}