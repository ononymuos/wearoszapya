package com.bastyoliva.wearable.data.repository

import kotlinx.coroutines.flow.Flow
import java.io.File

interface FileManagerRepository {
    fun getFiles(path: String): Flow<Result<List<File>>>

    suspend fun rename(path: String, newName: String): Result<Boolean>

    suspend fun getParentFilePath(path: String): String?
    fun hasFileAccess(): Boolean

    fun hasImagesAccess(): Boolean

    fun hasVideosAccess(): Boolean

    fun hasAudioAccess(): Boolean

    fun getFileByPath(path: String): File?


    suspend fun delete(path: String): Result<Boolean>

    suspend fun delete(paths: List<String>): Result<Boolean>

    suspend fun createDirectory(path: String, name: String): Result<Boolean>

    fun getBasePath(): String

    fun getFileName(path: String): Result<String>

    fun canNavigateUpFrom(path: String): Boolean

    suspend fun canBeRenamed(path: String): Boolean

    suspend fun canBeRenamed(paths: List<String>): Boolean

    suspend fun canBeDeleted(path: String): Boolean

    suspend fun canBeDeleted(paths: List<String>): Boolean

    suspend fun canCreateDirHere(path: String): Boolean

    suspend fun isDirectory(path: String): Boolean


}