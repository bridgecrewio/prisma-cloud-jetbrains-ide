package com.bridgecrew.activities

import com.bridgecrew.analytics.AnalyticsService
import com.bridgecrew.initialization.InitializationService
import com.bridgecrew.listeners.InitializationListener
import com.bridgecrew.listeners.InitializationListener.Companion.INITIALIZATION_TOPIC
import com.bridgecrew.listeners.PrismaVirtualFileListener
import com.bridgecrew.settings.PrismaSettingsState
import com.bridgecrew.ui.CheckovToolWindowManagerPanel
import com.intellij.ide.plugins.IdeaPluginDescriptor
import com.intellij.ide.plugins.PluginInstaller
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.ide.plugins.PluginStateListener
import com.intellij.openapi.components.service
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.vfs.LocalFileSystem
import org.slf4j.LoggerFactory
import java.util.*

class PostStartupActivity : ProjectActivity {

    private val logger = LoggerFactory.getLogger(javaClass)

    override suspend fun execute(project: Project) {
        val version = PluginManagerCore.getPlugin(PluginId.getId("com.github.bridgecrewio.prismacloud"))?.version
        logger.info("Starting Prisma Cloud JetBrains plugin version $version")
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
                logger.info("Plugin was installed")
            }
            override fun uninstall(ideaPluginDescriptor: IdeaPluginDescriptor) {
                //todo uncomment after backend added "onPluginUninstall" event support
                //sendAnalyticsPluginUninstalled(project)
                logger.info("Plugin was uninstalled")
            }
        })

        LocalFileSystem.getInstance().addVirtualFileListener(PrismaVirtualFileListener(project))

        initializeProject(project)
        sendAnalyticsPluginInstalled(project)
        logger.info("Startup activity finished")
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
