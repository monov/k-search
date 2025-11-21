package com.github.monov.ksearch.ui.components

import com.intellij.openapi.util.IconLoader
import com.intellij.ui.components.JBTextField
import com.intellij.ui.components.JBPanel
import com.intellij.util.ui.JBUI
import com.github.monov.ksearch.ui.ToolWindowFactory
import java.awt.*
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel

class SearchPanel : JBPanel<SearchPanel>(GridBagLayout()) {
    val searchField = JBTextField().apply {
        emptyText.text = "Search word"
        preferredSize = Dimension(300, 30)
    }

    val directoryField = JBTextField().apply {
        emptyText.text = "Directory path"
        preferredSize = Dimension(300, 30)
    }

    val directoryButton = JButton().apply {
        icon = IconLoader.getIcon("/assets/menu-open_dark.svg", ToolWindowFactory::class.java)
        preferredSize = Dimension(30, 30)
        isFocusPainted = false
        isBorderPainted = true
    }

    val searchButton = JButton("Search").apply {
        preferredSize = Dimension(80, 30)
    }

    val cancelButton = JButton("Cancel").apply {
        preferredSize = Dimension(80, 30)
        isEnabled = false
    }

    val resultCountLabel = JLabel("0 results")

    init {
        setupLayout()
    }

    private fun setupLayout() {
        val gbc = GridBagConstraints().apply {
            fill = GridBagConstraints.HORIZONTAL
            weightx = 1.0
            insets = JBUI.insets(5)
        }

        var row = 0

        // Search
        gbc.gridx = 0
        gbc.gridy = row++
        gbc.gridwidth = 3
        add(searchField, gbc)

        // Directory
        val dirPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
        }
        dirPanel.add(directoryField)
        dirPanel.add(Box.createRigidArea(Dimension(5, 0)))
        dirPanel.add(directoryButton)

        gbc.gridx = 0
        gbc.gridy = row++
        gbc.gridwidth = 3
        add(dirPanel, gbc)

        // Control button
        val controlPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            alignmentX = Component.LEFT_ALIGNMENT
        }
        controlPanel.add(searchButton)
        controlPanel.add(Box.createRigidArea(Dimension(5, 0)))
        controlPanel.add(cancelButton)
        controlPanel.add(Box.createRigidArea(Dimension(10, 0)))
        controlPanel.add(resultCountLabel)
        controlPanel.add(Box.createHorizontalGlue())

        gbc.gridx = 0
        gbc.gridy = row++
        gbc.gridwidth = 3
        gbc.fill = GridBagConstraints.HORIZONTAL
        add(controlPanel, gbc)
    }
}