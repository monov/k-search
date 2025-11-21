package com.github.monov.ksearch.ui.components.renderers

import com.intellij.ui.SimpleColoredComponent
import com.intellij.ui.SimpleTextAttributes
import com.github.monov.ksearch.ui.models.ResultItem
import java.awt.Color
import java.awt.Component
import javax.swing.JList
import javax.swing.ListCellRenderer

class ResultCellRenderer : ListCellRenderer<ResultItem> {
    private val linkColor = Color(0xC678DE)
    private val infoColor = Color(0x6F737B)
    private val comp = SimpleColoredComponent()

    override fun getListCellRendererComponent(
        list: JList<out ResultItem>?,
        value: ResultItem?,
        index: Int,
        isSelected: Boolean,
        cellHasFocus: Boolean
    ): Component {
        comp.clear()

        when (value) {
            is ResultItem.FileHeader ->
                comp.append(value.file.path, SimpleTextAttributes(SimpleTextAttributes.STYLE_BOLD, linkColor))

            is ResultItem.Occurrence ->
                comp.append("${value.line}:${value.offset}")

            is ResultItem.Message ->
                comp.append(value.text, SimpleTextAttributes(SimpleTextAttributes.STYLE_NO_BORDER, infoColor))

            null -> comp.append("")
        }

        comp.isOpaque = true
        comp.background = if (isSelected) list?.selectionBackground else list?.background
        return comp
    }
}