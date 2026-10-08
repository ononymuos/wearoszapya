package com.bastyoliva.wearable.menu.usecase

import com.bastyoliva.wearable.data.model.PinnedItem
import com.bastyoliva.wearable.data.repository.FileManagerRepository
import com.bastyoliva.wearable.data.repository.PinnedRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UnpinUseCase @Inject constructor(
    private val pinnedRepository: PinnedRepository,
    private val fileManagerRepository: FileManagerRepository
) {
    suspend operator fun invoke(paths: List<String>) = withContext(Dispatchers.IO) {

        try {
            for (path in paths){
                val file = fileManagerRepository.getFileByPath(path) ?: return@withContext

                val pinnedItem = PinnedItem(
                    path = file.absolutePath,
                    isFile = file.isFile,
                    name = file.name
                )
                pinnedRepository.unpinItem(pinnedItem)
            }


        } catch (_: Exception){

        }


    }
}