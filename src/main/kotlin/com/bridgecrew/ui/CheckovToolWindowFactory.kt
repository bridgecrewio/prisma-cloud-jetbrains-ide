package com.bridgecrew.ui

import com.bridgecrew.listeners.InitializationListener
import com.bridgecrew.results.Category
import com.bridgecrew.services.CheckovResultsListUtils
import com.bridgecrew.services.ResultsCacheService
import com.bridgecrew.ui.actions.SeverityFilterActions
import com.bridgecrew.ui.topPanel.CheckovActionToolbar
import com.bridgecrew.utils.PANELTYPE
import com.bridgecrew.utils.formatNumberWithCommas
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.openapi.wm.ex.ToolWindowManagerListener
import com.intellij.util.messages.MessageBusConnection
import org.slf4j.LoggerFactory

const val PRISMA_CLOUD_TOOL_WINDOW_ID = "Prisma Cloud"
const val OVERVIEW_TAB_NAME = "Overview"
const val IAC_TAB_NAME = "IaC"
const val VULNERABILITIES_TAB_NAME = "Vulnerabilities"
const val LICENSES_TAB_NAME = "Licenses"
const val SECRETS_TAB_NAME = "Secrets"
const val WEAKNESSES_TAB_NAME = "Weaknesses"

private val tabNameToCategory: Map<String, Category?> = mapOf(
        OVERVIEW_TAB_NAME to null,
        IAC_TAB_NAME to Category.IAC,
        VULNERABILITIES_TAB_NAME to Category.VULNERABILITIES,
        LICENSES_TAB_NAME to Category.LICENSES,
        SECRETS_TAB_NAME to Category.SECRETS,
        WEAKNESSES_TAB_NAME to Category.WEAKNESSES
)

class CheckovToolWindowFactory : ToolWindowFactory {

    private val logger = LoggerFactory.getLogger(javaClass)

    companion object {
        var internalExecution = false
        var isInitializationCompleted = false
        var currentlyRunning = false
        private var lastSelectedTab = ""
        var lastSelectedCategory = if (tabNameToCategory.contains(lastSelectedTab)) tabNameToCategory[lastSelectedTab] else null
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val checkovToolWindowPanel = CheckovToolWindowPanel(project)
        CheckovActionToolbar.setComponent(checkovToolWindowPanel)
        buildTabs(project, toolWindow, checkovToolWindowPanel)

        Disposer.register(toolWindow.disposable, checkovToolWindowPanel)

        val connection: MessageBusConnection = project.messageBus.connect()
        connection.subscribe(InitializationListener.INITIALIZATION_TOPIC, object : InitializationListener {
            override fun initializationCompleted() {
                isInitializationCompleted = true
            }
        })
        subscribeToTollWindowManagerEvents(connection, project)
    }

    private fun subscribeToTollWindowManagerEvents(connection: MessageBusConnection, project: Project) {
        connection.subscribe(ToolWindowManagerListener.TOPIC, object : ToolWindowManagerListener {
            override fun stateChanged(toolWindowManager: ToolWindowManager) {
                try {
                    if (isInitializationCompleted && !currentlyRunning && (internalExecution || toolWindowManager.activeToolWindowId == PRISMA_CLOUD_TOOL_WINDOW_ID)) {
                        internalExecution = false
                        currentlyRunning = true
                        val selectedContent = toolWindowManager.getToolWindow(PRISMA_CLOUD_TOOL_WINDOW_ID)?.contentManager?.selectedContent
                                ?: return

                        refreshCounts(toolWindowManager, project)

                        val checkovTabContent = selectedContent as CheckovTabContent
                        reloadContents(project, checkovTabContent.id)
                    }
                } catch (e: Exception) {
                    logger.error("Error while creating tool window: $e.message")
                } finally {
                    currentlyRunning = false
                }
            }
        })
    }

    private fun reloadContents(project: Project, tabId: String) {
        if (tabNameToCategory.keys.contains(tabId)) {
            if (lastSelectedTab != tabId) {
                val category = tabNameToCategory[tabId]
                lastSelectedCategory = category
                SeverityFilterActions.onChangeCategory(category, project)
                project.service<CheckovToolWindowManagerPanel>().loadMainPanel(PANELTYPE.CHECKOV_LOAD_TABS_CONTENT)
            }
            lastSelectedTab = tabId
        }
    }

    private fun buildTabs(project: Project, toolWindow: ToolWindow, checkovToolWindowPanel: CheckovToolWindowPanel) {
        val contentManager = toolWindow.contentManager
        tabNameToCategory.forEach { (name, category) ->
            val tabName = getTabName(project, name, category)
            val tabContent = CheckovTabContent(checkovToolWindowPanel, tabName, name, category)
            contentManager.addContent(tabContent)
        }
    }

    private fun getTabName(project: Project, name: String, category: Category?): String {
        val categories = if(category != null) listOf(category) else Category.values().toList()
        val checkovResults = project.service<ResultsCacheService>().getAdjustedCheckovResults()
        val resultsCount = CheckovResultsListUtils.filterResultsByCategoriesAndSeverities(checkovResults, categories).size
        val formattedCount = formatNumberWithCommas(resultsCount)
        return "$name ($formattedCount)"
    }

    private fun refreshCounts(toolWindowManager: ToolWindowManager, project: Project) {
        toolWindowManager.getToolWindow(PRISMA_CLOUD_TOOL_WINDOW_ID)?.contentManager?.contents?.forEach { content ->
            val checkovTabContent = content as CheckovTabContent
            checkovTabContent.displayName = getTabName(project, checkovTabContent.id, checkovTabContent.category)
        }
    }
}