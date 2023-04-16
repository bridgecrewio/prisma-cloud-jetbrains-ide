package com.bridgecrew.ui

import com.bridgecrew.analytics.AnalyticsService
import com.bridgecrew.listeners.CheckovScanListener
import com.bridgecrew.listeners.CheckovSettingsListener
import com.bridgecrew.services.ResultsCacheService
import com.bridgecrew.services.scan.CheckovScanService
import com.bridgecrew.services.scan.FullScanStateService
import com.bridgecrew.settings.CheckovSettingsState
import com.bridgecrew.ui.actions.CheckovScanAction
import com.bridgecrew.ui.topPanel.CheckovTopPanel
import com.bridgecrew.ui.vulnerabilitiesTree.CheckovToolWindowTree
import com.bridgecrew.utils.FULL_SCAN_EXCLUDED_PATHS
import com.bridgecrew.utils.PANELTYPE
import com.bridgecrew.utils.getGitIgnoreValues
import com.bridgecrew.utils.toVirtualFilePath
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.openapi.wm.ex.ToolWindowManagerListener
import com.intellij.ui.OnePixelSplitter
import java.awt.BorderLayout
import java.io.File
import javax.swing.SwingUtilities

@Service
class CheckovToolWindowManagerPanel(val project: Project) : SimpleToolWindowPanel(true, true), Disposable {

    private val checkovDescription = CheckovToolWindowDescriptionPanel(project)
    private val mainPanelSplitter = OnePixelSplitter(PANEL_SPLITTER_KEY, 0.5f)
    private val LOG = logger<CheckovToolWindowManagerPanel>()

    /**
     * Create Splitter element which contains the tree element and description element
     * @return JBSplitter
     */
    init {
        loadMainPanel(PANELTYPE.CHECKOV_INITIALIZATION_PROGRESS)
    }

    companion object {
        const val PANEL_SPLITTER_KEY = "CHECKOV_PANEL_SPLITTER_KEY"
    }

    fun loadMainPanel(panelType: Int = PANELTYPE.AUTO_CHOOSE_PANEL, scanSourceType: CheckovScanService.ScanSourceType? = null) {
        removeAll()
        add(CheckovTopPanel(project), BorderLayout.NORTH)
        when (panelType) {
            PANELTYPE.CHECKOV_REPOSITORY_SCAN_STARTED -> {
                reloadTabCounts()
                add(checkovDescription.duringScanDescription("Scanning your repository..."))
            }

            PANELTYPE.CHECKOV_INITIALIZATION_PROGRESS -> {
                add(checkovDescription.initializationDescription())
            }

            PANELTYPE.CHECKOV_FILE_SCAN_FINISHED -> {
                loadScanResultsPanel(panelType)
            }

            PANELTYPE.CHECKOV_FRAMEWORK_SCAN_FINISHED -> {
                if (project.service<FullScanStateService>().wereAllFrameworksFinished()) {
                    CheckovScanAction.resetActionDynamically(true)
                    if (project.service<FullScanStateService>().onCancel) {
                        loadPreviousStatePanel(panelType)
                        return
                    }
                }
                if (!project.service<FullScanStateService>().onCancel) {
                    loadScanResultsPanel(panelType)
                }
            }

            PANELTYPE.CHECKOV_REPOSITORY_SCAN_FAILED -> {
                loadErrorsPanel()
            }

            PANELTYPE.AUTO_CHOOSE_PANEL -> {
                loadAutoChoosePanel()
            }
        }
        revalidate()
        if (panelType == PANELTYPE.CHECKOV_FRAMEWORK_SCAN_FINISHED && project.service<FullScanStateService>().wereAllFrameworksFinished()) {
            project.service<AnalyticsService>().fullScanResultsWereFullyDisplayed()
        }
    }

    private fun loadScanResultsPanel(panelType: Int) {
        removeAll()
        reloadTabCounts()
        add(CheckovTopPanel(project), BorderLayout.NORTH)
        val checkovTree = CheckovToolWindowTree(project, mainPanelSplitter, checkovDescription)
        val filesTreePanel = checkovTree.createScroll()
        val fullScanAnalyticsData: AnalyticsService.FullScanAnalyticsData? = project.service<AnalyticsService>().fullScanData
        if (fullScanAnalyticsData != null) {
            if (shouldDisplayNoErrorPanel(panelType)) {
                add(checkovDescription.noErrorsPanel())
            } else {
                val descriptionPanel = checkovDescription.emptyDescription()
                mainPanelSplitter.firstComponent = filesTreePanel
                mainPanelSplitter.secondComponent = descriptionPanel
                add(mainPanelSplitter)
            }
        }
    }

    private fun shouldDisplayNoErrorPanel(panelType: Int): Boolean {
        return project.service<ResultsCacheService>().getAllCheckovResults().isEmpty() &&
                (panelType == PANELTYPE.CHECKOV_FILE_SCAN_FINISHED ||
                        (panelType == PANELTYPE.CHECKOV_FRAMEWORK_SCAN_FINISHED && project.service<FullScanStateService>().wereAllFrameworksFinished()))
    }

    private fun loadAutoChoosePanel() {
        val setting = CheckovSettingsState().getInstance()
        when {
            setting?.apiToken.isNullOrEmpty() -> add(checkovDescription.configurationDescription())
            else -> add(checkovDescription.preScanDescription())
        }
    }

    private fun loadErrorsPanel() {
        add(checkovDescription.failedScanDescription())
    }

    private fun loadPreviousStatePanel(panelType: Int) {
        when (project.service<FullScanStateService>().previousState) {
            FullScanStateService.State.FIRST_TIME_SCAN -> {
                loadAutoChoosePanel()
            }

            FullScanStateService.State.SUCCESSFUL_SCAN -> {
                loadScanResultsPanel(panelType)
            }

            FullScanStateService.State.FAILED_SCAN -> {
                loadErrorsPanel()
            }
        }
    }

    private fun reloadTabCounts() {
        val toolWindowManager = ToolWindowManager.getInstance(project)
        CheckovToolWindowFactory.internalExecution = true
        project.messageBus.syncPublisher(ToolWindowManagerListener.TOPIC).stateChanged(toolWindowManager)
    }

    fun subscribeToProjectEventChange() {
        if (SwingUtilities.isEventDispatchThread()) {
            project.service<CheckovToolWindowManagerPanel>().loadMainPanel()
        } else {
            ApplicationManager.getApplication().invokeLater {
                project.service<CheckovToolWindowManagerPanel>().loadMainPanel()
            }
        }

        // subscribe to open file events
        project.messageBus.connect().subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, object :
                FileEditorManagerListener {
            override fun fileOpened(source: FileEditorManager, file: VirtualFile) {
                super.fileOpened(source, file)
                if (shouldScanFile(file)) {
                    val hasResources = project.service<ResultsCacheService>().getCheckovResultsByPath(toVirtualFilePath(project, file)).isNotEmpty()
                    if(!hasResources){
                        project.service<CheckovScanService>().scanFile(file.path, project)
                    }
                }
            }
        })

        // subscribe to update file events
        project.messageBus.connect().subscribe(VirtualFileManager.VFS_CHANGES, object : BulkFileListener {
            override fun after(events: MutableList<out VFileEvent>) {
                if (events.isEmpty()) {
                    return
                }

                LOG.debug("file event for file: ${events[0].file!!.path}. isValid: ${events[0].isValid}, isFromRefresh: ${events[0].isFromRefresh}, isFromSave: ${events[0].isFromSave}, requestor: ${events[0].requestor}")

                if (events.isEmpty() || !events[0].isFromSave || events[0].file == null) {
                    return
                }

                if (shouldScanFile(events[0].file!!)) {
                    project.service<CheckovScanService>().scanFile(events[0].file!!.path, project);
                }
            }
        })
    }

    fun shouldScanFile(virtualFile: VirtualFile): Boolean {
        if (!virtualFile.isValid) {
            return false
        }

        val virtualFilePath: String = toVirtualFilePath(project, virtualFile)

        val excludedPaths = (getGitIgnoreValues(project) + FULL_SCAN_EXCLUDED_PATHS).distinct()

        return ProjectRootManager.getInstance(project).fileIndex.isInContent(virtualFile) &&
                excludedPaths.find { excludedPath -> virtualFilePath.startsWith(excludedPath) }.isNullOrEmpty()
    }

    fun subscribeToInternalEvents(project: Project) {
        // Subscribe to Scanning Topic
        project.messageBus.connect(this)
                .subscribe(CheckovScanListener.SCAN_TOPIC, object : CheckovScanListener {

                    override fun projectScanningStarted() {
                        project.service<CheckovToolWindowManagerPanel>().loadMainPanel(PANELTYPE.CHECKOV_REPOSITORY_SCAN_STARTED)
                    }

                    override fun scanningFinished(scanSourceType: CheckovScanService.ScanSourceType) {
                        ApplicationManager.getApplication().invokeLater {
                            if (scanSourceType == CheckovScanService.ScanSourceType.FILE) {
                                project.service<CheckovToolWindowManagerPanel>().loadMainPanel(PANELTYPE.CHECKOV_FILE_SCAN_FINISHED)
                            } else {
                                project.service<CheckovToolWindowManagerPanel>().loadMainPanel(PANELTYPE.CHECKOV_FRAMEWORK_SCAN_FINISHED)
                            }
                        }
                    }

                    override fun fullScanFailed() {
                        ApplicationManager.getApplication().invokeLater {
                            project.service<CheckovToolWindowManagerPanel>().loadMainPanel(PANELTYPE.CHECKOV_REPOSITORY_SCAN_FAILED)
                        }
                    }
                })

        // Subscribe to Settings Topic
        project.messageBus.connect(this)
                .subscribe(CheckovSettingsListener.SETTINGS_TOPIC, object : CheckovSettingsListener {
                    override fun settingsUpdated() {
                        project.service<CheckovToolWindowManagerPanel>().loadMainPanel()
                    }
                })
    }

    override fun dispose() = Unit
}
