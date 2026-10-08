package com.bastyoliva.wearable.file_list.usecase

import com.bastyoliva.wearable.data.repository.FileManagerRepository
import kotlinx.coroutines.flow.Flow
import java.io.File
import javax.inject.Inject

class GetFileListUseCase @Inject constructor(
    private val fileManagerRepository: FileManagerRepository
) {
    operator fun invoke(path: String): Flow<Result<List<File>>> {
        return fileManagerRepository.getFiles(path)
    }
}