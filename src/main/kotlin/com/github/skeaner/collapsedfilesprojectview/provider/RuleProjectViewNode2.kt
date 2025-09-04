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
    private val settings: Settings,
    private val rule: Rule,
    private val nodes: List<AbstractTreeNode<*>>,
) : ProjectViewNode<String>(project, rule.name, viewSettings),
    PsiElementProcessor<PsiFileSystemItem> {

    val containsMatchedChildKey: Key<Boolean> = Key.create("FOLDABLE_PROJECT_VIEW_CONTAINS_MATCHED_CHILD")
    val ruleScope = MySearchScope(project, rule.pattern, settings)

    override fun update(presentation: PresentationData) {
        presentation.apply {
            val textAttributes = SimpleTextAttributes(STYLE_PLAIN, rule.foreground)
            addText(ColoredFragment(rule.name, rule.pattern, textAttributes))
            setIcon(CollapseComponent)
        }
    }

    override fun getName() = rule.name

    override fun toString() = name

    override fun execute(item: PsiFileSystemItem): Boolean {
        return true
        val matched = matchesPattern(item)

        if (matched) {
//            putUserDataUntilRoot(item.parent, matched)
            item.parent?.putUserData(containsMatchedChildKey, true)
        }

        return !matched // stop processing other children if this matches
    }
//    private fun putUserDataUntilRoot(item: PsiFileSystemItem?, matched: Boolean) { // replace ItemType with the actual type of item
//        if (item == null || item == project) {
//            return
//        }
//
//        item.putUserData(containsMatchedChildKey, matched)
//        putUserDataUntilRoot(item.parent, matched)
//    }


    override fun computeBackgroundColor() = rule.background


    override fun getChildren() = nodes.map { if (it is PsiDirectoryNode) NoChildProjectViewNode(project, it.value, viewSettings) else it }

    override fun contains(file: VirtualFile) = true

//    override fun contains(file: VirtualFile) = children.firstOrNull {
//        it is ProjectViewNode && it.virtualFile == file
//    } != null

    private fun matchesPattern(item: PsiFileSystemItem): Boolean {
        // check userdata
        if (item.isDirectory) {
            return !item.processChildren(this@RuleProjectViewNode2)
        } else
            return ruleScope.contains(item.virtualFile) || !item.processChildren(this@RuleProjectViewNode2) // processChildren returns false if {#execute} found matched child
    }
}
