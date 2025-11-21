package com.github.monov.ksearch.services

import com.intellij.openapi.progress.Cancellation.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.isReadable
import kotlin.io.path.isRegularFile
import kotlin.io.path.extension


interface Occurrence {
    val file: Path
    val line: Int
    val offset: Int
}

data class OccurrenceImpl(
    override val file: Path,
    override val line: Int,
    override val offset: Int
) : Occurrence

fun searchForTextOccurrences(stringToSearch: String, directory: Path): Flow<Occurrence> = flow {
    if (stringToSearch.isEmpty()) return@flow
    if (!Files.exists(directory)) return@flow
    if (!Files.isDirectory(directory)) return@flow
    if (!Files.isReadable(directory)) return@flow


    findSearchableFilesAsFlow(directory)
        .flatMapMerge(concurrency = 10) { file -> searchFileFor(stringToSearch, file) }
        .collect { occurrence -> emit(occurrence) }
}.flowOn(Dispatchers.IO)


private fun findSearchableFilesAsFlow(root: Path): Flow<Path> = flow {
    Files.walk(root).use { stream ->
        val iterator = stream.iterator()
        while (iterator.hasNext()) {
            ensureActive()
            val path = iterator.next()
            if (path.isRegularFile() && path.isReadable() && isTextFile(path)) {
                emit(path)
            }
        }
    }
}

private fun searchFileFor(query: String, file: Path): Flow<Occurrence> = flow {
    try {
        val lines = Files.readAllLines(file, StandardCharsets.UTF_8)
        lines.forEachIndexed { lineIndex, line ->
            var start = 0
            while (true) {
                val idx = line.indexOf(query, start)
                if (idx == -1) break
                emit(OccurrenceImpl(file = file, line = lineIndex + 1, offset = idx))
                start = idx + 1
            }
        }
    } catch (_: Exception) {
        //ignore files that cant be read
    }
}

private fun isTextFile(path: Path): Boolean {
    val knownTextExts = setOf(
        "txt","md","kt","java","py","js","ts","html","css","xml","json","yml","yaml",
        "properties","gradle","sql","sh","bat","ps1","c","cpp","h","hpp","cs","php","rb",
        "go","rs","swift","scala","clj","hs","ml","r","m","pl","lua","dart","elm"
    )
    val ext = path.extension.lowercase()
    if (ext.isEmpty()) {
        return try {
            val head = Files.readAllBytes(path).take(1024).toByteArray()
            !head.contains(0.toByte())
        } catch (_: Exception) { false }
    }
    return ext in knownTextExts
}