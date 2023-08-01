package com.bridgecrew.listeners

import com.bridgecrew.services.ResultsCacheService
import com.bridgecrew.ui.CheckovToolWindowManagerPanel
import com.bridgecrew.utils.PANELTYPE
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileEvent
import com.intellij.openapi.vfs.VirtualFileListener


class PrismaVirtualFileListener(val project: Project) : VirtualFileListener {
    override fun fileDeleted(event: VirtualFileEvent) {
        val deletedFile = event.file
        project.service<ResultsCacheService>().removeCheckovResultByFilePath(deletedFile.path)
        project.service<CheckovToolWindowManagerPanel>().loadMainPanel(PANELTYPE.CHECKOV_FILE_SCAN_FINISHED)
    }
}


