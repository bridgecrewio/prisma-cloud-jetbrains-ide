package com.bridgecrew.activities

import com.bridgecrew.analytics.AnalyticsService
import com.bridgecrew.initialization.InitializationService
import com.bridgecrew.listeners.InitializationListener
import com.bridgecrew.listeners.InitializationListener.Companion.INITIALIZATION_TOPIC
import com.bridgecrew.settings.PrismaSettingsState
import com.bridgecrew.ui.CheckovToolWindowManagerPanel
import com.intellij.ide.plugins.IdeaPluginDescriptor
import com.intellij.ide.plugins.PluginInstaller
import com.intellij.ide.plugins.PluginStateListener
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import java.util.*


private val LOG = logger<PostStartupActivity>()

class PostStartupActivity : StartupActivity {

    override fun runActivity(project: Project) {
        LOG.info("Startup activity starting")
        project.messageBus.connect(project).subscribe(INITIALIZATION_TOPIC, object : InitializationListener {
            override fun initializationCompleted() {
                project.service<CheckovToolWindowManagerPanel>().subscribeToInternalEvents(project)
                project.service<CheckovToolWindowManagerPanel>().subscribeToProjectEventChange()
                project.service<AnalyticsService>().startSchedulerReleasingAnalytics()
                // project.service<ResultsCacheService>().setMockCheckovResultsFromExampleFile() // MOCK
            }

        })

        PluginInstaller.addStateListener(object : PluginStateListener {
            override fun install(ideaPluginDescriptor: IdeaPluginDescriptor) {
                //todo this event wasn't trigger, need review
                LOG.info("Plugin was installed")
            }
            override fun uninstall(ideaPluginDescriptor: IdeaPluginDescriptor) {
                //todo uncomment after backend added "onPluginUninstall" event support
                //sendAnalyticsPluginUninstalled(project)
                LOG.info("Plugin was uninstalled")
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

    private fun sendAnalyticsPluginUninstalled(project: Project){
        project.service<AnalyticsService>().pluginUninstalled()
    }
}