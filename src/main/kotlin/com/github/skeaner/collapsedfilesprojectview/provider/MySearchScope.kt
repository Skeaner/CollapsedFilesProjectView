package com.github.skeaner.collapsedfilesprojectview.provider

import com.github.skeaner.collapsedfilesprojectview.settings.Settings
import com.intellij.openapi.components.service
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vcs.changes.ignore.cache.PatternCache
import com.intellij.openapi.vcs.changes.ignore.lang.Syntax
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.GlobalSearchScope

class MySearchScope(
    project: Project,
    private val pattern: String,
    private val settings: Settings = project.service<Settings>(),
) : GlobalSearchScope(project) {

    private val patternCache = PatternCache.getInstance(project)
    private val patterns = pattern
        .applyCaseSensitiveSettings()
        .split(' ')
        .filter(String::isNotBlank)
        .mapNotNull {
            patternCache.createPattern(it, Syntax.GLOB)
        }

    override fun contains(file: VirtualFile): Boolean {
        if (patterns.isEmpty()) {
            return false
        }

//        val moduleDir = fileIndex.getModuleForFile(file)?.guessModuleDir() ?: project?.guessProjectDir() ?: return false
//        val base = moduleDir.toNioPath()

        val base = project?.guessProjectDir()?.toNioPath() ?: return false
        val relativePath = file.toNioPath().relativize(base) ?: return false
        val path = relativePath.toString().applyCaseSensitiveSettings()
//        val path = relativePath.first().toString().applyCaseSensitiveSettings()


        return patterns.any {
            it.matcher(path).matches()
        }
    }

    override fun isSearchInModuleContent(aModule: Module) = true

    override fun isSearchInLibraries() = false

    override fun getDisplayName() = pattern

    override fun toString() = pattern

    private fun String.applyCaseSensitiveSettings() = when (settings.caseSensitive) {
        true -> this
        false -> this.toLowerCase()
    }
}
