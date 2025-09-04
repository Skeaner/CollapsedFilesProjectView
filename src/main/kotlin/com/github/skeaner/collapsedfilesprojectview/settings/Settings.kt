package com.github.skeaner.collapsedfilesprojectview.settings

import com.github.skeaner.collapsedfilesprojectview.Constants
import com.intellij.openapi.components.*
import com.intellij.util.xmlb.annotations.OptionTag

@Service(Service.Level.PROJECT)
@State(name = "Settings", storages = [Storage(Constants.STORAGE_FILE)])
class Settings : SettingsState, BaseState(), PersistentStateComponent<Settings> {
    @get:OptionTag("ENABLED")
    override var enabled by property(true)


    @get:OptionTag("CASE_SENSITIVE")
    override var caseSensitive by property(true)


    @get:OptionTag("RULES")
    override var rules by list<Rule>()

    override fun getState() = this

    override fun loadState(state: Settings) = copyFrom(state)
}
