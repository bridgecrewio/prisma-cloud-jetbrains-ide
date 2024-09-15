package com.bridgecrew.listeners

import com.bridgecrew.analytics.AnalyticsService
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener

class ProjectListener : ProjectManagerListener {

    override fun projectClosing(project: Project) {
        project.service<AnalyticsService>().stopAnalyticsService()
        super.projectClosing(project)
    }
}