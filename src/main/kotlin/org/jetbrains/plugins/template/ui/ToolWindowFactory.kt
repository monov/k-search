package org.jetbrains.plugins.template.ui

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBPanel
import com.intellij.ui.content.ContentFactory
import com.intellij.util.ui.JBUI
import org.jetbrains.plugins.template.services.SearchCoordinator
import org.jetbrains.plugins.template.ui.components.PaginationPanel
import org.jetbrains.plugins.template.ui.components.ResultsPanel
import org.jetbrains.plugins.template.ui.components.SearchPanel
import java.awt.BorderLayout

class ToolWindowFactory : ToolWindowFactory, DumbAware {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val mainPanel = createMainPanel(project)
        val content = ContentFactory.getInstance().createContent(mainPanel, "", false)
        toolWindow.contentManager.addContent(content)
    }

    private fun createMainPanel(project: Project): JBPanel<*> {
        return JBPanel<JBPanel<*>>(BorderLayout()).apply {
            border = JBUI.Borders.empty(10)
            setupComponents(project)
        }
    }

    private fun JBPanel<*>.setupComponents(project: Project) {
        val searchPanel = SearchPanel()
        val resultsPanel = ResultsPanel(project)
        val paginationPanel = PaginationPanel()

        add(searchPanel, BorderLayout.NORTH)
        add(resultsPanel, BorderLayout.CENTER)
        add(paginationPanel, BorderLayout.SOUTH)

        val searchCoordinator = SearchCoordinator(searchPanel, resultsPanel, paginationPanel)
        searchCoordinator.setupListeners()
    }
}