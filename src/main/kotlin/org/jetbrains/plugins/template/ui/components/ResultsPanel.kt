package org.jetbrains.plugins.template.ui.components

import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBPanel
import org.jetbrains.plugins.template.ui.components.renderers.ResultCellRenderer
import org.jetbrains.plugins.template.ui.models.ResultItem
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.DefaultListModel

class ResultsPanel(private val project: Project) : JBPanel<ResultsPanel>(BorderLayout()) {
    val listModel = DefaultListModel<ResultItem>()
    val resultsList = JBList(listModel)
    private val scrollPane = JBScrollPane(resultsList).apply {
        preferredSize = Dimension(400, 300)
        border = null
    }

    init {
        setupComponents()
        setupListeners()
    }

    private fun setupComponents() {
        resultsList.border = null
        resultsList.cellRenderer = ResultCellRenderer()
        add(scrollPane, BorderLayout.CENTER)
    }

    private fun setupListeners() {
        resultsList.addMouseListener(object : java.awt.event.MouseAdapter() {
            override fun mouseClicked(e: java.awt.event.MouseEvent) {
                val index = resultsList.locationToIndex(e.point)
                if (index != -1) {
                    handleItemClick(listModel[index])
                }
            }
        })
    }

    private fun handleItemClick(item: ResultItem) {
        when (item) {
            is ResultItem.Occurrence -> openFileAtPosition(item)
            else -> {}
        }
    }

    private fun openFileAtPosition(item: ResultItem.Occurrence) {
        val vFile = com.intellij.openapi.vfs.LocalFileSystem.getInstance()
            .findFileByIoFile(item.file)
        if (vFile != null) {
            val editorManager = com.intellij.openapi.fileEditor.FileEditorManager.getInstance(project)
            editorManager.openFile(vFile, true)

            val editor = editorManager.selectedTextEditor
            editor?.caretModel?.moveToLogicalPosition(
                com.intellij.openapi.editor.LogicalPosition(item.line - 1, item.offset)
            )
            editor?.scrollingModel?.scrollToCaret(
                com.intellij.openapi.editor.ScrollType.CENTER
            )
        }
    }
    fun scrollToTop() {
        if (listModel.size > 0) {
            resultsList.selectedIndex = 0
            resultsList.ensureIndexIsVisible(0)
        }
    }

    fun clear() = listModel.clear()
    fun addItem(item: ResultItem) = listModel.addElement(item)
    fun addItems(items: List<ResultItem>) = items.forEach { listModel.addElement(it) }
}