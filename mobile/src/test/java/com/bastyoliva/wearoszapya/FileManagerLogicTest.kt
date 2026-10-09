package com.bastyoliva.wearoszapya

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.util.Locale

class FileManagerLogicTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
        val index = digitGroups.coerceIn(0, units.size - 1)
        return String.format(Locale.US, "%.1f %s", bytes / Math.pow(1024.0, index.toDouble()), units[index])
    }

    private fun getFileCategory(extension: String): String {
        return when (extension.lowercase(Locale.ROOT)) {
            "jpg", "jpeg", "png", "webp", "gif", "svg" -> "IMAGE"
            "mp3", "wav", "flac", "ogg", "m4a", "aac" -> "AUDIO"
            "mp4", "mkv", "webm", "avi", "mov" -> "VIDEO"
            "pdf" -> "PDF"
            "txt", "md", "json", "xml", "csv" -> "DOCUMENT"
            "apk" -> "APPLICATION"
            else -> "OTHER"
        }
    }

    @Test
    fun testDirectoryCreationAndNesting() {
        val rootDir = tempFolder.root
        val zapyaDir = File(rootDir, "ZapyaTransfers")
        assertFalse(zapyaDir.exists())

        val created = zapyaDir.mkdirs()
        assertTrue(created)
        assertTrue(zapyaDir.isDirectory)

        // Nested subdirectories
        val nested = File(zapyaDir, "Music/Albums/2026")
        assertTrue(nested.mkdirs())
        assertTrue(nested.exists())
        assertEquals(zapyaDir.absolutePath, nested.parentFile?.parentFile?.parentFile?.absolutePath)
    }

    @Test
    fun testFileRenamingLogic() {
        val rootDir = tempFolder.root
        val originalFile = File(rootDir, "recording.mp3")
        originalFile.writeText("audio sample data")
        assertTrue(originalFile.exists())

        val newFile = File(rootDir, "voice_memo_01.mp3")
        val renamed = originalFile.renameTo(newFile)
        assertTrue(renamed)
        assertFalse(originalFile.exists())
        assertTrue(newFile.exists())
        assertEquals("audio sample data", newFile.readText())
    }

    @Test
    fun testFileAndRecursiveDirectoryDeletion() {
        val rootDir = tempFolder.root
        val folderToDelete = File(rootDir, "TempTransferFolder")
        folderToDelete.mkdirs()

        val subFile1 = File(folderToDelete, "chunk_0.part").also { it.writeBytes(ByteArray(1024)) }
        val subFile2 = File(folderToDelete, "chunk_1.part").also { it.writeBytes(ByteArray(2048)) }
        val nestedDir = File(folderToDelete, "nested").also { it.mkdirs() }
        val subFile3 = File(nestedDir, "chunk_2.part").also { it.writeBytes(ByteArray(512)) }

        assertTrue(subFile1.exists() && subFile2.exists() && subFile3.exists())

        // Recursive deletion as implemented in Wearable FileManagerRepositoryImpl
        val deleted = folderToDelete.deleteRecursively()
        assertTrue(deleted)
        assertFalse(folderToDelete.exists())
    }

    @Test
    fun testFileSizeFormatting() {
        assertEquals("0 B", formatFileSize(0L))
        assertEquals("500.0 B", formatFileSize(500L))
        assertEquals("1.0 KB", formatFileSize(1024L))
        assertEquals("512.0 KB", formatFileSize(512L * 1024L))
        assertEquals("1.0 MB", formatFileSize(1024L * 1024L))
        assertEquals("10.5 MB", formatFileSize((10.5 * 1024 * 1024).toLong()))
        assertEquals("1.5 GB", formatFileSize((1.5 * 1024 * 1024 * 1024).toLong()))
    }

    @Test
    fun testFileCategoryClassification() {
        assertEquals("IMAGE", getFileCategory("jpg"))
        assertEquals("IMAGE", getFileCategory("PNG"))
        assertEquals("AUDIO", getFileCategory("mp3"))
        assertEquals("AUDIO", getFileCategory("flac"))
        assertEquals("VIDEO", getFileCategory("mp4"))
        assertEquals("VIDEO", getFileCategory("MKV"))
        assertEquals("PDF", getFileCategory("pdf"))
        assertEquals("DOCUMENT", getFileCategory("txt"))
        assertEquals("APPLICATION", getFileCategory("apk"))
        assertEquals("OTHER", getFileCategory("unknown_bin"))
    }

    @Test
    fun testFileSortingFoldersFirst() {
        val rootDir = tempFolder.root
        val fileZ = File(rootDir, "zebra.txt").also { it.writeText("z") }
        val dirA = File(rootDir, "alpha_folder").also { it.mkdir() }
        val fileA = File(rootDir, "apple.txt").also { it.writeText("a") }
        val dirZ = File(rootDir, "zulu_folder").also { it.mkdir() }

        val files = listOf(fileZ, dirA, fileA, dirZ)
        val sorted = files.sortedWith(
            compareBy<File> { !it.isDirectory }
                .thenBy { it.name.lowercase(Locale.ROOT) }
        )

        assertEquals("alpha_folder", sorted[0].name)
        assertEquals("zulu_folder", sorted[1].name)
        assertEquals("apple.txt", sorted[2].name)
        assertEquals("zebra.txt", sorted[3].name)
    }
}
