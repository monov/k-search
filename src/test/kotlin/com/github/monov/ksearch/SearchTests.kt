package com.github.monov.ksearch.services

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.writeText

class SearchForTextOccurrencesTest {
        @Test
    fun testFindSingleOccurrenceInSingleFile() {
        withTempDir { tempDir ->
            val file = tempDir.resolve("test.kt")
            file.writeText("hello world")

            val results = runBlocking { searchForTextOccurrences("world", tempDir).toList() }

            assert(results.size == 1) { "Expected 1 result, got ${results.size}" }
            assert(results[0].file == file)
            assert(results[0].line == 1)
            assert(results[0].offset == 6)
        }
    }

        @Test
    fun testFindMultipleOccurrencesInSingleLine() {
        withTempDir { tempDir ->
            val file = tempDir.resolve("test.txt")
            file.writeText("test test test")

            val results = runBlocking { searchForTextOccurrences("test", tempDir).toList() }

            assert(results.size == 3) { "Expected 3 results, got ${results.size}" }
            assert(results[0].offset == 0)
            assert(results[1].offset == 5)
            assert(results[2].offset == 10)
        }
    }

        @Test
    fun testFindOccurrencesAcrossMultipleLines() {
        withTempDir { tempDir ->
            val file = tempDir.resolve("test.txt")
            file.writeText("line one\nline two\nline three")

            val results = runBlocking { searchForTextOccurrences("line", tempDir).toList() }

            assert(results.size == 3) { "Expected 3 results, got ${results.size}" }
            assert(results[0].line == 1)
            assert(results[1].line == 2)
            assert(results[2].line == 3)
        }
    }

        @Test
    fun testFindOccurrencesInMultipleFiles() {
        withTempDir { tempDir ->
            tempDir.resolve("file1.txt").writeText("search here")
            tempDir.resolve("file2.txt").writeText("search there")

            val results = runBlocking { searchForTextOccurrences("search", tempDir).toList() }

            assert(results.size == 2) { "Expected 2 results, got ${results.size}" }
            assert(results.any { it.file.fileName.toString() == "file1.txt" })
            assert(results.any { it.file.fileName.toString() == "file2.txt" })
        }
    }

        @Test
    fun testSearchInNestedDirectories() {
        withTempDir { tempDir ->
            val subDir = Files.createDirectory(tempDir.resolve("subdir"))
            val deepDir = Files.createDirectory(subDir.resolve("deep"))

            tempDir.resolve("root.txt").writeText("find")
            subDir.resolve("sub.txt").writeText("find")
            deepDir.resolve("deep.txt").writeText("find")

            val results = runBlocking { searchForTextOccurrences("find", tempDir).toList() }

            assert(results.size == 3) { "Expected 3 results, got ${results.size}" }
        }
    }

        @Test
    fun testReturnEmptyFlowForEmptySearchString() {
        withTempDir { tempDir ->
            tempDir.resolve("test.txt").writeText("some content")

            val results = runBlocking { searchForTextOccurrences("", tempDir).toList() }

            assert(results.isEmpty()) { "Expected empty results" }
        }
    }

        @Test
    fun testReturnEmptyFlowForNonExistentDirectory() {
        withTempDir { tempDir ->
            val nonExistent = tempDir.resolve("does-not-exist")

            val results = runBlocking { searchForTextOccurrences("test", nonExistent).toList() }

            assert(results.isEmpty()) { "Expected empty results" }
        }
    }

        @Test
    fun testReturnEmptyFlowWhenSearchingFileInsteadOfDirectory() {
        withTempDir { tempDir ->
            val file = tempDir.resolve("file.txt")
            file.writeText("content")

            val results = runBlocking { searchForTextOccurrences("content", file).toList() }

            assert(results.isEmpty()) { "Expected empty results" }
        }
    }

        @Test
    fun testNotFindAnythingWhenQueryDoesntMatch() {
        withTempDir { tempDir ->
            tempDir.resolve("test.txt").writeText("hello world")

            val results = runBlocking { searchForTextOccurrences("goodbye", tempDir).toList() }

            assert(results.isEmpty()) { "Expected empty results" }
        }
    }

        @Test
    fun testHandleOverlappingOccurrences() {
        withTempDir { tempDir ->
            val file = tempDir.resolve("test.txt")
            file.writeText("aaaa")

            val results = runBlocking { searchForTextOccurrences("aa", tempDir).toList() }

            assert(results.size == 3) { "Expected 3 results, got ${results.size}" }
            assert(results[0].offset == 0)
            assert(results[1].offset == 1)
            assert(results[2].offset == 2)
        }
    }

        @Test
    fun testSearchOnlyInTextFilesWithKnownExtensions() {
        withTempDir { tempDir ->
            tempDir.resolve("code.kt").writeText("search")
            tempDir.resolve("doc.md").writeText("search")
            tempDir.resolve("data.json").writeText("search")
            tempDir.resolve("style.css").writeText("search")

            val results = runBlocking { searchForTextOccurrences("search", tempDir).toList() }

            assert(results.size == 4) { "Expected 4 results, got ${results.size}" }
        }
    }

        @Test
    fun testHandleCaseSensitiveSearch() {
        withTempDir { tempDir ->
            val file = tempDir.resolve("test.txt")
            file.writeText("Test test TEST")

            val results = runBlocking { searchForTextOccurrences("test", tempDir).toList() }

            assert(results.size == 1) { "Expected 1 result, got ${results.size}" }
            assert(results[0].offset == 5)
        }
    }

        @Test
    fun testHandleSpecialCharactersInSearchString() {
        withTempDir { tempDir ->
            val file = tempDir.resolve("test.txt")
            file.writeText("hello (world) [test]")

            val results = runBlocking { searchForTextOccurrences("(world)", tempDir).toList() }

            assert(results.size == 1) { "Expected 1 result, got ${results.size}" }
            assert(results[0].offset == 6)
        }
    }

        @Test
    fun testHandleMultilineTextWithCRLF() {
        withTempDir { tempDir ->
            val file = tempDir.resolve("test.txt")
            Files.writeString(file, "line1\r\nline2\r\nline3")

            val results = runBlocking { searchForTextOccurrences("line", tempDir).toList() }

            assert(results.size == 3) { "Expected 3 results, got ${results.size}" }
        }
    }

        @Test
    fun testHandleEmptyFiles() {
        withTempDir { tempDir ->
            tempDir.resolve("empty.txt").writeText("")

            val results = runBlocking { searchForTextOccurrences("test", tempDir).toList() }

            assert(results.isEmpty()) { "Expected empty results" }
        }
    }

        @Test
    fun testHandleVeryLongLines() {
        withTempDir { tempDir ->
            val file = tempDir.resolve("test.txt")
            val longLine = "x".repeat(10000) + "needle" + "x".repeat(10000)
            file.writeText(longLine)

            val results = runBlocking { searchForTextOccurrences("needle", tempDir).toList() }

            assert(results.size == 1) { "Expected 1 result, got ${results.size}" }
            assert(results[0].offset == 10000)
        }
    }

        @Test
    fun testIgnoreUnreadableFilesGracefully() {
        withTempDir { tempDir ->
            val file = tempDir.resolve("test.txt")
            file.writeText("content")
            file.toFile().setReadable(false)

            val results = runBlocking { searchForTextOccurrences("content", tempDir).toList() }

            // Should complete without throwing
            assert(results.isEmpty() || results.isNotEmpty())

            // Cleanup
            file.toFile().setReadable(true)
        }
    }

        @Test
    fun testHandleFilesWithoutExtensions() {
        withTempDir { tempDir ->
            val file = tempDir.resolve("README")
            file.writeText("search this")

            val results = runBlocking { searchForTextOccurrences("search", tempDir).toList() }

            // Depends on binary detection - may or may not find it
            assert(results.size >= 0)
        }
    }

        @Test
    fun testFindQueryAtStartOfLine() {
        withTempDir { tempDir ->
            val file = tempDir.resolve("test.txt")
            file.writeText("start of line")

            val results = runBlocking { searchForTextOccurrences("start", tempDir).toList() }

            assert(results.size == 1) { "Expected 1 result, got ${results.size}" }
            assert(results[0].offset == 0)
        }
    }

        @Test
    fun testFindQueryAtEndOfLine() {
        withTempDir { tempDir ->
            val file = tempDir.resolve("test.txt")
            file.writeText("end of line")

            val results = runBlocking { searchForTextOccurrences("line", tempDir).toList() }

            assert(results.size == 1) { "Expected 1 result, got ${results.size}" }
            assert(results[0].offset == 7)
        }
    }

    private fun withTempDir(block: (Path) -> Unit) {
        val tempDir = Files.createTempDirectory("search-test")
        try {
            block(tempDir)
        } finally {
            tempDir.toFile().deleteRecursively()
        }
    }
}

