package com.bridgecrew.ui.rightPanel.extraInfoPanel

import com.bridgecrew.results.BaseCheckovResult
import com.bridgecrew.ui.CodeDiffPanel
import com.intellij.util.ui.UIUtil
import java.awt.Point
import java.awt.Rectangle
import javax.swing.*

open class CheckovExtraInfoPanel(val result: BaseCheckovResult): JPanel() {

    fun initLayout() {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        background = UIUtil.getEditorPaneBackground()
    }

    fun addCodeDiffPanel(){
        if(result.fixDefinition != null){
            add(CodeDiffPanel(result, true))
        }
    }

    override fun scrollRectToVisible(aRect: Rectangle?) {
        val viewPosition = Point(0,0)
        super.scrollRectToVisible(Rectangle(viewPosition))
    }
}