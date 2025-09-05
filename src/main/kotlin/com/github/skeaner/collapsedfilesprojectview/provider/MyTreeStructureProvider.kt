package com.github.skeaner.collapsedfilesprojectview.provider

import com.github.skeaner.collapsedfilesprojectview.or
import com.github.skeaner.collapsedfilesprojectview.settings.Settings
import com.github.skeaner.collapsedfilesprojectview.settings.SettingsListener
import com.intellij.ide.projectView.ProjectView
import com.intellij.ide.projectView.ProjectViewNode
import com.intellij.ide.projectView.TreeStructureProvider
import com.intellij.ide.projectView.ViewSettings
import com.intellij.ide.projectView.impl.ProjectViewPane
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode
import com.intellij.ide.projectView.impl.nodes.PsiFileNode
import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.observable.properties.ObservableMutableProperty
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessModuleDir
import com.intellij.openapi.vcs.FileStatusListener
import com.intellij.openapi.vcs.FileStatusManager
import com.intellij.openapi.vcs.changes.ignore.cache.PatternCache
import com.intellij.openapi.vcs.changes.ignore.lang.Syntax
import com.intellij.profile.codeInspection.ProjectInspectionProfileManager
import java.beans.PropertyChangeEvent

class MyTreeStructureProvider(private val project: Project) : TreeStructureProvider {

    private val settings by lazy { ApplicationManager.getApplication().getService(Settings::class.java) }
    private val patternCache = PatternCache.getInstance(project)
    private var previewProjectViewPane: ProjectViewPane? = null
    private var previewGraphProperty: ObservableMutableProperty<Settings>? = null
    private val state get() = previewGraphProperty?.get() ?: settings

    // TODO: Move to project service?
    init {
        //订阅通知, 档设置变更时候刷新
        project.messageBus.connect()
            .subscribe(SettingsListener.TOPIC, SettingsListener {
                refreshProjectView()
            })

        FileStatusManager.getInstance(project).addFileStatusListener(object : FileStatusListener {
            override fun fileStatusesChanged() {
//                refreshProjectView()
//                if (state.foldIgnoredFiles) {
//                    refreshProjectView()
//                }
            }
        }, project)
    }

    override fun modify(
        parent: AbstractTreeNode<*>,
        children: MutableCollection<AbstractTreeNode<*>>,
        viewSettings: ViewSettings?,
    ): Collection<AbstractTreeNode<*>> {
        val project = parent.project ?: return children
        val foldingGroup = parent.foldingFolder

        return when {
            // Folding is disabled
            !state.enabled -> children

//            foldingGroup != null -> {
//                val parentPath = (foldingGroup.parent as PsiDirectoryNode).virtualFile?.toNioPath()
//
//                children.filter {
//                    true
//                }
//            }

            // Parent is not a directory node
            parent !is PsiDirectoryNode -> children

            // Parent is a directory node, not a module, and matching nested is disabled
            !isModule(parent, project) -> children

            else -> {
                val matched = mutableSetOf<AbstractTreeNode<*>>()
                val folders = mutableListOf<AbstractTreeNode<*>>()
                state.rules.forEach {
                    //  (children - matched)
                    children
                        .match(it.pattern)
                        .run {
                            matched.addAll(this)
                            if (!settings.hideAllRules && !(settings.hideEmptyRules && this.isEmpty())) {
                                folders.add(RuleProjectViewNode2(project, viewSettings, it, this))
                            }
                        }

                }

                children - matched + folders
            }
        }
    }

    fun withProjectViewPane(projectViewPane: ProjectViewPane) = apply {
        previewProjectViewPane = projectViewPane
    }

    fun withState(property: ObservableMutableProperty<Settings>) = apply {
        previewGraphProperty = property.also {
            it.afterChange {
                refreshProjectView()
            }
        }
    }

    private fun isModule(node: PsiDirectoryNode, project: Project) =
        node.virtualFile
            ?.let { ModuleUtil.findModuleForFile(it, project)?.guessModuleDir() == it }
            ?: false

    private fun Collection<AbstractTreeNode<*>>.match(patterns: String) = this
        .filter {
            when (it) {
                is PsiDirectoryNode -> true
                is PsiFileNode -> true
                else -> false
            }
        }
        .filter {
            when (it) {
                is ProjectViewNode -> it.virtualFile?.name ?: it.name
                else -> it.name
            }.let { name ->
                patterns
                    .split(' ')
                    .any { pattern ->
                        patternCache
                            .createPattern(pattern, Syntax.GLOB)
                            ?.matcher(name)
                            ?.matches()
                            ?: false
                    }
            }
        }

    private fun refreshProjectView() = previewProjectViewPane
        .or { ProjectView.getInstance(project).currentProjectViewPane }
        ?.updateFromRoot(true)

    private val <T> AbstractTreeNode<T>.isFolded: Boolean
        get() = parent?.run { this is RuleProjectViewNode || isFolded } ?: false

    private val <T> AbstractTreeNode<T>.foldingFolder: AbstractTreeNode<*>?
        get() = parent.takeIf { it is RuleProjectViewNode } ?: parent?.foldingFolder
}
