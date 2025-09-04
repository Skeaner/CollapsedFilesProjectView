package com.github.skeaner.collapsedfilesprojectview.settings

import com.github.skeaner.collapsedfilesprojectview.MyBundle.message
import com.github.skeaner.collapsedfilesprojectview.bindColor
import com.github.skeaner.collapsedfilesprojectview.bindColorControl
import com.github.skeaner.collapsedfilesprojectview.bindText
import com.intellij.openapi.observable.properties.ObservableMutableProperty
import com.intellij.openapi.observable.util.isNotNull
import com.intellij.openapi.options.UiDslUnnamedConfigurable
import com.intellij.ui.ColorPanel
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import com.intellij.ui.components.fields.ExpandableTextField
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.selected
import com.intellij.ui.layout.ComponentPredicate

class RulesEditor(val ruleProperty: ObservableMutableProperty<Rule?>) : UiDslUnnamedConfigurable.Simple() {

    private lateinit var backgroundCheckBox: Cell<JBCheckBox>
    private lateinit var backgroundColorPanel: Cell<ColorPanel>
    private lateinit var foregroundCheckBox: Cell<JBCheckBox>
    private lateinit var foregroundColorPanel: Cell<ColorPanel>
    private lateinit var nameTextField: Cell<JBTextField>
    private lateinit var patternTextField: Cell<ExpandableTextField>

    private val selectedRowPredicate = object : ComponentPredicate() {

        override fun invoke() = ruleProperty.isNotNull().get()

        override fun addListener(listener: (Boolean) -> Unit) =
            ruleProperty.afterChange {
                listener(it != null)
            }
    }

    override fun Panel.createContent() {
        rowsRange {
            row(message("settings.name")) {
                nameTextField = textField()
                    .align(Align.FILL)
                    .bindText(ruleProperty, Rule::name)

            }
            row(message("settings.rule")) {
                patternTextField = expandableTextField()
                    .align(Align.FILL)
                    .comment(message("settings.rule.comment"), 40)
                    .bindText(ruleProperty, Rule::pattern)
            }
            row {
                foregroundCheckBox = checkBox(message("settings.foreground"))
                    .bindColorControl(ruleProperty, Rule::foreground, JBColor.foreground().brighter())

                foregroundColorPanel = cell(ColorPanel())
                    .align(Align.FILL)
                    .enabledIf(foregroundCheckBox.selected)
                    .bindColor(ruleProperty, Rule::foreground)
            }
            row {
                backgroundCheckBox = checkBox(message("settings.background"))
                    .bindColorControl(ruleProperty, Rule::background, JBColor.background().darker())

                backgroundColorPanel = cell(ColorPanel())
                    .align(Align.FILL)
                    .enabledIf(backgroundCheckBox.selected)
                    .bindColor(ruleProperty, Rule::background)
            }
        }.enabledIf(selectedRowPredicate)
    }
}
