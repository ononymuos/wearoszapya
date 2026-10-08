package com.bastyoliva.wearable.navigation

import kotlinx.serialization.Serializable

sealed interface Routes {
    @Serializable
    data object Home : Routes

    @Serializable
    data object Gallery : Routes

    @Serializable
    data object Settings : Routes

    @Serializable
    data object Theming : Routes

    @Serializable
    data object Video : Routes

    @Serializable
    data object Music : Routes

    @Serializable
    data class FilesList(
        val path: String? = null
    ) : Routes

    @Serializable
    data class Rename(
        val path: String
    ) : Routes

    @Serializable
    data class NewDirectory(
        val path: String
    ) : Routes

    @Serializable
    data class Delete(
        val paths: List<String>
    ) : Routes

    @Serializable
    data class TextViewer(val uriString: String) : Routes

    @Serializable
    data class PdfViewer(val uriString: String) : Routes
}