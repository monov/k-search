package org.jetbrains.plugins.template.ui.components

import com.intellij.ui.components.JBPanel
import java.awt.BorderLayout
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.SwingConstants

class PaginationPanel : JBPanel<PaginationPanel>(BorderLayout()) {
    val previousButton = JButton("Previous").apply { isEnabled = false }
    val nextButton = JButton("Next").apply { isEnabled = false }
    val pageLabel = JLabel("0/0", SwingConstants.CENTER)

    init {
        add(previousButton, BorderLayout.WEST)
        add(pageLabel, BorderLayout.CENTER)
        add(nextButton, BorderLayout.EAST)
    }

    fun updateState(currentPage: Int, totalPages: Int, totalResults: Int) {
        previousButton.isEnabled = currentPage > 0
        nextButton.isEnabled = currentPage < totalPages - 1
        pageLabel.text = "${currentPage + 1}/$totalPages"
    }
}