package com.github.skeaner.collapsedfilesprojectview.settings

import com.github.skeaner.collapsedfilesprojectview.MyBundle.message
import com.github.skeaner.collapsedfilesprojectview.Constants.COLOR_COLUMN_TEXT
import com.intellij.execution.util.ListTableWithButtons
import com.intellij.openapi.actionSystem.ActionToolbarPosition
import com.intellij.openapi.observable.properties.ObservableMutableProperty
import com.intellij.openapi.observable.util.transform
import com.intellij.ui.ToolbarDecorator
import com.intellij.util.ui.ColumnInfo
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.ListTableModel
import java.awt.Component
import javax.swing.JTable
import javax.swing.ListSelectionModel
import javax.swing.SwingUtilities
import javax.swing.table.DefaultTableCellRenderer

class RulesTable(private val settingsProperty: ObservableMutableProperty<Settings>) :
    ListTableWithButtons<Rule>() {

    private val rulesProperty = settingsProperty.transform(
        { it.rules },
        {
            settingsProperty.get().apply {
                with(rules) {
                    clear()
                    addAll(it)
                }
            }
        },
    )

    init {
        tableView.apply {
            columnSelectionAllowed = false
            tableHeader.reorderingAllowed = false
            columnModel.getColumn(0).apply {
                maxWidth = JBUI.scale(24)
                minWidth = JBUI.scale(24)
            }
            model.addTableModelListener {
                rulesProperty.set(elements)
            }
            setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
            setShowGrid(false)
            setValues(rulesProperty.get())
        }
        rulesProperty.afterChange {
            refreshValues()
        }
    }

    override fun createToolbarDecorator() = ToolbarDecorator
        .createDecorator(tableView, null)
        .setToolbarPosition(ActionToolbarPosition.RIGHT)
        .setPanelBorder(JBUI.Borders.empty())

    override fun isUpDownSupported() = true

    override fun shouldEditRowOnCreation() = false

    override fun createListModel() = ListTableModel<Rule>(ColorsColumn(), NameColumn(), RulesColumn(), ShowChildrenColumn())

    override fun createElement() = Rule()

    override fun addNewElement(newElement: Rule) {
        super.addNewElement(newElement)
        SwingUtilities.invokeLater {
            tableView.selection = listOf(newElement)
        }
    }

    override fun canDeleteElement(selection: Rule) = true

    override fun cloneElement(variable: Rule) = variable.copy()

    override fun isEmpty(element: Rule) = element.pattern.isBlank()

    private class ColorsColumn : ColumnInfo<Rule, String>(message("settings.color")) {

        override fun getName() = ""

        override fun valueOf(item: Rule?) = COLOR_COLUMN_TEXT

        override fun getRenderer(item: Rule?) = object : DefaultTableCellRenderer() {

            override fun getTableCellRendererComponent(
                table: JTable?,
                value: Any?,
                isSelected: Boolean,
                hasFocus: Boolean,
                row: Int,
                column: Int
            ): Component {
                val hasNoColor = item?.foreground == null && item?.background == null
                return super.getTableCellRendererComponent(
                    table,
                    value,
                    isSelected && hasNoColor,
                    hasFocus,
                    row,
                    column
                )
                    .apply {
                        foreground = item?.foreground
                        background = item?.background
                    }
            }
        }
    }

    private class NameColumn : ColumnInfo<Rule, String>(message("settings.ruleName")) {

        override fun valueOf(item: Rule?) = item?.name
    }

    private class RulesColumn : ColumnInfo<Rule, String>(message("settings.rule")) {

        override fun valueOf(item: Rule?) = item?.pattern
    }

    private class ShowChildrenColumn : ColumnInfo<Rule, String>(message("settings.children")) {

        override fun valueOf(item: Rule?) = if (item?.showChildren == true) "show" else "hide"
    }
}
