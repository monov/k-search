package com.github.monov.ksearch.ui.models

import java.io.File

sealed class ResultItem {
    data class FileHeader(val file: File) : ResultItem()
    data class Occurrence(
        val file: File,
        val line: Int,
        val offset: Int,
        val preview: String? = null
    ) : ResultItem()

    data class Message(val text: String) : ResultItem()

    companion object {
        fun textOnly(text: String) = Message(text)
    }
}