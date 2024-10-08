package com.bridgecrew.ui.errorBubble

import com.bridgecrew.listeners.ErrorBubbleFixListener
import com.bridgecrew.results.BaseCheckovResult
import com.bridgecrew.results.Category
import com.bridgecrew.services.CheckovResultsComparatorGenerator
import com.intellij.openapi.editor.markup.MarkupModel
import com.intellij.openapi.editor.markup.RangeHighlighter
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.JBPopupListener
import com.intellij.openapi.ui.popup.LightweightWindowEvent
import com.intellij.openapi.wm.ex.WindowManagerEx
import java.awt.Point

typealias navigationCallback = (Int, String) -> Unit
class CheckovErrorBubble(val project: Project, val results: List<BaseCheckovResult>, private val modalLocation: Point, private val markup: MarkupModel, private val rangeHighlighter: RangeHighlighter) {

    private var panelList: ArrayList<ErrorBubbleInnerPanel> = arrayListOf()
    private var currentPanel: ErrorBubbleInnerPanel? = null


    private val callBack: navigationCallback = { index, action ->
        val newIdx = if (action == "right") index + 1 else index - 1
        val newCurr = panelList[newIdx]
        currentPanel = newCurr
        buildAndShowErrorBubble()
    }

    init {
        val (vulnerabilityResults, otherResults) = results.partition { it.category == Category.VULNERABILITIES }

        val vulnerabilityCount = vulnerabilityResults.size
        val totalPanels = otherResults.size + vulnerabilityCount
        var runningIndex = 0
        otherResults.sortedWith(CheckovResultsComparatorGenerator.generateCheckovResultComparator()).forEachIndexed { index, baseCheckovResult ->
            panelList.add(ErrorBubbleInnerPanel(project, baseCheckovResult, 0, index, totalPanels, callBack))
            runningIndex = index + 1
        }

        if (vulnerabilityResults.isNotEmpty()) {
            vulnerabilityResults.sortedBy { it.severity }.forEach { vulnerabilityCheckovResult ->
                panelList.add(ErrorBubbleInnerPanel(project, vulnerabilityCheckovResult, 0, runningIndex, totalPanels, callBack))
                runningIndex += 1
            }
        }

        currentPanel = panelList[0]

        buildAndShowErrorBubble()
    }

    private fun buildAndShowErrorBubble() {
        val project = ProjectManager.getInstance().defaultProject

        val popup = JBPopupFactory.getInstance()
                .createComponentPopupBuilder(currentPanel!!, currentPanel)
                .setProject(project)
                .setLocateWithinScreenBounds(true)
                .setCancelOnClickOutside(true)
                .setCancelOnWindowDeactivation(false)
                .setRequestFocus(true)
                .setMayBeParent(true)
                .addListener(object : JBPopupListener {
                    override fun onClosed(event: LightweightWindowEvent) {
                        markup.removeHighlighter(rangeHighlighter)
                    }
                })
                .createPopup()

        val window = WindowManagerEx.getInstanceEx().mostRecentFocusedWindow
        popup.showInScreenCoordinates(window!!, modalLocation)

        val connection = project.messageBus.connect()
        connection.subscribe(ErrorBubbleFixListener.ERROR_BUBBLE_FIX_TOPIC, object : ErrorBubbleFixListener {
            override fun fixClicked() {
                popup.closeOk(null)
            }
        })

        currentPanel!!.requestFocus()
    }
}
