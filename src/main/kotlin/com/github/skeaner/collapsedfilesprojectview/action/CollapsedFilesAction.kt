package com.github.skeaner.collapsedfilesprojectview.action

import com.github.skeaner.collapsedfilesprojectview.settings.Settings
import com.intellij.ide.projectView.ProjectView
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleOptionAction
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAware
import java.util.function.Function

class CollapsedFilesAction : DumbAware, ToggleOptionAction(Function {
    object : Option {

        private val settings: Settings = ApplicationManager.getApplication().getService(Settings::class.java)

        override fun isSelected() = settings.enabled ?: false

        override fun setSelected(selected: Boolean) {
            val updated = selected != isSelected

            settings.enabled = selected

            if (updated) {
                it.project?.let { project ->
                    val view = ProjectView.getInstance(project)
                    view.currentProjectViewPane?.updateFromRoot(true)
                }
            }
        }

    }
}){
}
