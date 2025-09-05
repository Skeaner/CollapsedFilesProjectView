package com.github.skeaner.collapsedfilesprojectview.settings

import com.github.skeaner.collapsedfilesprojectview.MyBundle.message
import com.github.skeaner.collapsedfilesprojectview.bindSelected
import com.github.skeaner.collapsedfilesprojectview.createPredicate
import com.intellij.execution.services.ServiceViewManager
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.service
import com.intellij.openapi.observable.properties.PropertyGraph
import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.options.Configurable.NoScroll
import com.intellij.openapi.project.Project
import com.intellij.ui.OnePixelSplitter
import com.intellij.ui.dsl.builder.*

class SettingsConfigurable(val project: Project) : BoundSearchableConfigurable(
    helpTopic = "CollapsedFilesProjectView",
    _id = "CollapsedFilesProjectView",
    displayName = "CollapsedFilesProjectView",
), NoScroll {

    companion object {
        const val ID = "com.github.skeaner.collapsedfilesprojectview.settings.SettingsConfigurable"
    }

    private val settings = ApplicationManager.getApplication().getService(Settings::class.java)

    private val propertyGraph = PropertyGraph()
    private val settingsProperty = propertyGraph.lazyProperty { Settings().apply { copyFrom(settings) } }
    private val foldingEnabledPredicate = settingsProperty.createPredicate(Settings::enabled)

    private val ruleProperty = propertyGraph
        .lazyProperty<Rule?> { null }
        .apply {
            afterChange {
                ApplicationManager.getApplication().invokeLater {
                    rulesTable.tableView.updateUI()
                }
                settingsProperty.setValue(null, SettingsState::rules, settingsProperty.get())
            }
        }

    private val rulesTable = RulesTable(settingsProperty)
    private val rulesEditor = RulesEditor(ruleProperty)

    private val settingsPanel = panel {
        row {
            checkBox(message("settings.enabled"))
                .bindSelected(settingsProperty, Settings::enabled)
                .comment(message("settings.enabled.comment"), MAX_LINE_LENGTH_WORD_WRAP)
                .applyToComponent { setMnemonic('e') }
        }

        rowsRange {
            row {
                checkBox(message("settings.caseSensitive"))
                    .bindSelected(settingsProperty, Settings::caseSensitive)
                    .comment(message("settings.caseSensitive.comment"), MAX_LINE_LENGTH_WORD_WRAP)
                    .applyToComponent { setMnemonic('c') }
            }

        }.enabledIf(foldingEnabledPredicate)
    }


    override fun createPanel() =
        panel {
            row {
                cell(settingsPanel)
                    .align(Align.FILL)
            }
            group(message("settings.rules")) {
                row {
                    cell(rulesTable.component)
                        .align(Align.FILL)
                        .resizableColumn()

                    cell(rulesEditor.createPanel())
                        .applyIfEnabled()

                    with(rulesTable.tableView) {
                        selectionModel.addListSelectionListener {
                            ruleProperty.set(selectedObject)
                        }
                    }
                }
            }
        }

    override fun getId() = ID

    override fun getDisplayName() = message("projectName")

    override fun isModified() = settingsProperty.get() != settings

    override fun apply() {
        settings.copyFrom(settingsProperty.get())
        invalidateTable()
        project.messageBus.syncPublisher(SettingsListener.TOPIC).settingsChanged()
    }

    override fun reset() {
        settingsProperty.set(settingsProperty.get().apply {
            copyFrom(settings)
        })
        invalidateTable()
    }

    private fun invalidateTable() = rulesTable.apply {
        setValues(settingsProperty.get().rules)
        tableView.selection.clear()
    }
}
