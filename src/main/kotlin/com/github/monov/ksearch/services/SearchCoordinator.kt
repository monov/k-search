package com.github.monov.ksearch.services

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flowOn
import com.github.monov.ksearch.ui.components.PaginationPanel
import com.github.monov.ksearch.ui.components.ResultsPanel
import com.github.monov.ksearch.ui.components.SearchPanel
import com.github.monov.ksearch.ui.models.ResultItem
import java.io.File
import javax.swing.SwingUtilities


class SearchCoordinator(
    private val searchPanel: SearchPanel,
    private val resultsPanel: ResultsPanel,
    private val paginationPanel: PaginationPanel
) {
    private val uiScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var searchJob: Job? = null

    private companion object {
        const val PAGE_SIZE = 200
    }

    private val allResults = mutableListOf<ResultItem>()
    private var currentPage = 0

    fun setupListeners() {
        setupSearchListeners()
        setupPaginationListeners()
        setupDirectoryListener()
    }

    private fun setupSearchListeners() {
        searchPanel.searchButton.addActionListener { startSearch() }
        searchPanel.cancelButton.addActionListener { cancelSearch() }
    }

    private fun setupPaginationListeners() {
        paginationPanel.previousButton.addActionListener { showPreviousPage() }
        paginationPanel.nextButton.addActionListener { showNextPage() }
    }

    private fun setupDirectoryListener() {
        searchPanel.directoryButton.addActionListener { chooseDirectory() }
    }

    private fun startSearch() {
        val query = searchPanel.searchField.text
        val dir = File(searchPanel.directoryField.text)

        if (!validateInput(query, dir)) {
            return
        }

        resetSearchState()
        executeSearch(query, dir)
    }

    private fun validateInput(query: String, dir: File): Boolean {
        if (query.isEmpty() || !dir.exists() || !dir.isDirectory) {
            javax.swing.JOptionPane.showMessageDialog(
                searchPanel.searchButton,
                "Please enter text and select a valid directory"
            )
            return false
        }
        return true
    }

    private fun resetSearchState() {
        allResults.clear()
        resultsPanel.clear()
        resultsPanel.addItem(ResultItem.textOnly("Indexing and Searching..."))
        updateResultCount(0)
        searchPanel.cancelButton.isEnabled = true
        searchPanel.searchButton.isEnabled = false
        searchJob?.cancel()
    }

    private fun executeSearch(query: String, dir: File) {
        searchJob = uiScope.launch {
            try {
                performSearch(query, dir)
            } catch (e: CancellationException) {
                handleSearchCancelled()
            } catch (e: Exception) {
                handleSearchError(e)
            } finally {
                searchPanel.searchButton.isEnabled = true
                searchPanel.cancelButton.isEnabled = false
            }
        }
    }

    private suspend fun performSearch(query: String, dir: File) {
        val seenFiles = LinkedHashSet<File>()
        var occurrenceCount = 0

        searchForTextOccurrences(query, dir.toPath())
            .flowOn(Dispatchers.IO)
            .collect { occurrence ->
                val currentFile = occurrence.file.toFile()
                occurrenceCount++

                if (seenFiles.add(currentFile)) {
                    allResults.add(ResultItem.FileHeader(currentFile))
                }

                allResults.add(ResultItem.Occurrence(
                    file = currentFile,
                    line = occurrence.line,
                    offset = occurrence.offset
                ))

                updateResultCount(occurrenceCount)

                if (allResults.size <= PAGE_SIZE) {
                    updateUIForFirstPage()
                }
            }

        handleSearchCompletion()
    }

    private fun handleSearchCancelled() {
        SwingUtilities.invokeLater {
            resultsPanel.addItem(ResultItem.textOnly("Search cancelled"))
        }
    }

    private fun handleSearchError(e: Exception) {
        SwingUtilities.invokeLater {
            resultsPanel.addItem(ResultItem.textOnly("Error: ${e.message}"))
        }
    }

    private fun handleSearchCompletion() {
        SwingUtilities.invokeLater {
            if (allResults.isEmpty()) {
                resultsPanel.clear()
                resultsPanel.addItem(ResultItem.textOnly("No results found"))
            } else {
                currentPage = 0
                showPage(currentPage)
                updatePagination()
            }
        }
    }

    private fun updateUIForFirstPage() {
        SwingUtilities.invokeLater {
            currentPage = 0
            showPage(currentPage)
            updatePagination()
        }
    }

    private fun cancelSearch() {
        searchJob?.cancel()
        searchPanel.cancelButton.isEnabled = false
    }

    private fun chooseDirectory() {
        val chooser = javax.swing.JFileChooser().apply {
            fileSelectionMode = javax.swing.JFileChooser.DIRECTORIES_ONLY
        }

        if (chooser.showOpenDialog(searchPanel.searchButton) == javax.swing.JFileChooser.APPROVE_OPTION) {
            searchPanel.directoryField.text = chooser.selectedFile.path
        }
    }

    private fun showPreviousPage() {
        if (currentPage > 0) {
            currentPage--
            showPage(currentPage)
            updatePagination()
            resultsPanel.scrollToTop()
        }
    }

    private fun showNextPage() {
        if ((currentPage + 1) * PAGE_SIZE < allResults.size) {
            currentPage++
            showPage(currentPage)
            updatePagination()
            resultsPanel.scrollToTop()
        }
    }

    private fun showPage(page: Int) {
        val start = page * PAGE_SIZE
        val end = (start + PAGE_SIZE).coerceAtMost(allResults.size)
        resultsPanel.clear()

        if (start < end) {
            resultsPanel.addItems(allResults.subList(start, end))
        }
    }

    private fun updatePagination() {
        val totalPages = ((allResults.size + PAGE_SIZE - 1) / PAGE_SIZE).coerceAtLeast(1)
        paginationPanel.updateState(currentPage, totalPages, allResults.size)
    }

    private fun updateResultCount(count: Int) {
        SwingUtilities.invokeLater {
            searchPanel.resultCountLabel.text = "$count results"
        }
    }

    fun dispose() {
        searchJob?.cancel()
        uiScope.cancel()
    }
}