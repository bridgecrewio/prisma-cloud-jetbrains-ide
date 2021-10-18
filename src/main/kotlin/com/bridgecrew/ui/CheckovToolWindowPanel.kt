package com.bridgecrew.ui

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.ui.JBSplitter
import java.awt.*
import javax.swing.*
import javax.swing.event.*


class CheckovToolWindowPanel(val project: Project) : JPanel(), Disposable {
    val checkovDescription = CheckovToolWindowDescriptionPanel(project)
    val checkovTree = CheckovToolWindowTree(project, checkovDescription)

    /**
     * Create Splitter element which contains the tree element and description element
     * @return JBSplitter
     */
    fun dividePanel(): JPanel{
        val right = checkovDescription.createScroll()
        val left = checkovTree.createScroll()
        val split = JBSplitter()
        split.setFirstComponent(left)
        split.setSecondComponent(right)
        return split
    }

    override fun dispose() = Unit

}
