package com.github.skeaner.collapsedfilesprojectview.settings

import com.github.skeaner.collapsedfilesprojectview.Constants.DEFAULT_RULE_NAME
import com.github.skeaner.collapsedfilesprojectview.Constants.DEFAULT_RULE_PATTERN
import com.intellij.ui.JBColor
import com.intellij.util.xmlb.Converter
import com.intellij.util.xmlb.annotations.OptionTag
import java.awt.Color

interface SettingsState {
    val enabled: Boolean
    val caseSensitive: Boolean
    val hideEmptyRules: Boolean
    val hideAllRules: Boolean
    val rules: MutableList<Rule>
}

data class Rule(
    var name: String = DEFAULT_RULE_NAME,

    var pattern: String = DEFAULT_RULE_PATTERN,

    var showChildren: Boolean = true, // New property to control whether to show children or not

    @get:OptionTag(converter = ColorConverter::class)
    var background: Color? = null,

    @get:OptionTag(converter = ColorConverter::class)
    var foreground: Color? = null,
)

private class ColorConverter : Converter<Color>() {
    override fun toString(value: Color) = value.rgb.toString()
    override fun fromString(value: String) = runCatching { JBColor.decode(value) }.getOrNull()
}
