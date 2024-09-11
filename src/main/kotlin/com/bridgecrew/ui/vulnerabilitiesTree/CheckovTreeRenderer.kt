package com.bridgecrew.ui.vulnerabilitiesTree

import com.intellij.util.ui.UIUtil
import java.awt.Component
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeCellRenderer

class CheckovTreeRenderer : DefaultTreeCellRenderer() {
    override fun getTreeCellRendererComponent(tree: JTree?,
                                              value: Any?,
                                              selected: Boolean,
                                              expanded: Boolean,
                                              leaf: Boolean,
                                              row: Int,
                                              hasFocus: Boolean): Component
    {
        super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus)

        if(value is DefaultMutableTreeNode){
            val userObject = value.userObject
            if(userObject is CheckovTreeNode){
                icon = userObject.getNodeIcon()
            }
        }
        setBackgroundSelectionColor(UIUtil.TRANSPARENT_COLOR)
        setBorderSelectionColor(backgroundSelectionColor)
        setBackgroundNonSelectionColor(UIUtil.getEditorPaneBackground() ?: background)
        foreground = UIUtil.getListForeground(true, hasFocus)

        return this
    }
}