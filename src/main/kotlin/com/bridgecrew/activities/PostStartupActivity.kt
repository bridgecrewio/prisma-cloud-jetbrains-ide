package com.bridgecrew.activities

import com.bridgecrew.analytics.AnalyticsService
import com.bridgecrew.initialization.InitializationService
import com.bridgecrew.listeners.InitializationListener
import com.bridgecrew.listeners.InitializationListener.Companion.INITIALIZATION_TOPIC
import com.bridgecrew.settings.PrismaSettingsState
import com.bridgecrew.ui.CheckovToolWindowManagerPanel
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import java.util.UUID

private val LOG = logger<PostStartupActivity>()

class PostStartupActivity : StartupActivity {

    override fun runActivity(project: Project) {
        LOG.info("Startup activity starting")
        project.messageBus.connect(project).subscribe(INITIALIZATION_TOPIC, object : InitializationListener {
            override fun initializationCompleted() {
                project.service<CheckovToolWindowManagerPanel>().subscribeToInternalEvents(project)
                project.service<CheckovToolWindowManagerPanel>().subscribeToProjectEventChange()
                // project.service<ResultsCacheService>().setMockCheckovResultsFromExampleFile() // MOCK
            }

        })
        initializeProject(project)
        sendAnalyticsPluginInstalled(project)
        LOG.info("Startup activity finished")
    }

    private fun initializeProject(project: Project) {
        val initializationService = InitializationService(project)
        initializationService.initializeProject()
    }

    private fun sendAnalyticsPluginInstalled(project: Project){
        val settings = PrismaSettingsState().getInstance()
        if(settings?.installationId!!.isEmpty()){
            settings.installationId = UUID.randomUUID().toString()
            project.service<AnalyticsService>().pluginInstalled()
        }

    }
}