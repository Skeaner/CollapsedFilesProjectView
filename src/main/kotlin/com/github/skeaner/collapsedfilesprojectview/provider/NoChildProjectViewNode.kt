package com.github.skeaner.collapsedfilesprojectview.provider

import com.intellij.icons.AllIcons
import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.projectView.ProjectViewNode
import com.intellij.ide.projectView.ViewSettings
import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.SimpleTextAttributes.STYLE_PLAIN

class NoChildProjectViewNode(
    project: Project,
    private val dir: PsiDirectory,
    viewSettings: ViewSettings?
) : ProjectViewNode<PsiDirectory>(project, dir, viewSettings) {


    override fun update(presentation: PresentationData) {
        presentation.apply {
            val textAttributes = SimpleTextAttributes(STYLE_PLAIN, null)
            addText(dir.name, textAttributes)
            setIcon(AllIcons.Nodes.Folder)
        }
    }

    override fun getChildren(): MutableCollection<out AbstractTreeNode<*>> = mutableSetOf()

    override fun contains(file: VirtualFile) = false


}
