package com.github.skeaner.collapsedfilesprojectview.provider

import com.github.skeaner.collapsedfilesprojectview.settings.Rule
import com.github.skeaner.collapsedfilesprojectview.settings.Settings
import com.intellij.icons.AllIcons.General.CollapseComponent
import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.projectView.ProjectViewNode
import com.intellij.ide.projectView.ViewSettings
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode
import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFileSystemItem
import com.intellij.psi.search.PsiElementProcessor
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.SimpleTextAttributes.STYLE_PLAIN

class RuleProjectViewNode2(
    project: Project,
    private val viewSettings: ViewSettings?,
    private val rule: Rule,
    private val nodes: List<AbstractTreeNode<*>>,
) : ProjectViewNode<String>(project, rule.toString(), viewSettings) {


    override fun update(presentation: PresentationData) {
        presentation.apply {
            val textAttributes = SimpleTextAttributes(STYLE_PLAIN, rule.foreground)
            addText(ColoredFragment(rule.name, rule.pattern, textAttributes))
            setIcon(CollapseComponent)
        }
    }

    override fun getName() = rule.name

    override fun toString() = name


    override fun computeBackgroundColor() = rule.background


    override fun getChildren() =
        nodes.map { if (!rule.showChildren && it is PsiDirectoryNode) NoChildProjectViewNode(project, it, viewSettings) else it }

    override fun contains(file: VirtualFile) = children.firstOrNull {
        it is ProjectViewNode && it.virtualFile == file
    } != null

    override fun isAutoExpandAllowed(): Boolean {
        return false
    }

    override fun isAlwaysExpand(): Boolean {
        return false
    }


}
