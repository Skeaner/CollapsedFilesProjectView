package com.github.skeaner.collapsedfilesprojectview.settings

import com.intellij.util.messages.Topic
import java.util.*

@FunctionalInterface
fun interface SettingsListener : EventListener {

    companion object {
        @Topic.ProjectLevel
        val TOPIC = Topic(SettingsListener::class.java)
    }

    fun settingsChanged(settings: Settings)
}
