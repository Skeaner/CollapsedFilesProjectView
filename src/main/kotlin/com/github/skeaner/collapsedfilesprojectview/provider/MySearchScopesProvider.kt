package com.github.skeaner.collapsedfilesprojectview.provider

import com.github.skeaner.collapsedfilesprojectview.MyBundle
import com.github.skeaner.collapsedfilesprojectview.settings.Settings
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.psi.search.SearchScope
import com.intellij.psi.search.SearchScopeProvider

class MySearchScopesProvider : SearchScopeProvider {

    override fun getDisplayName() = MyBundle.getMessage("projectName")

    override fun getSearchScopes(project: Project, dataContext: DataContext): MutableList<SearchScope> {
        val settings = project.service<Settings>()
        return settings.rules.map { MySearchScope(project, it.pattern) }.toMutableList()
    }
}
