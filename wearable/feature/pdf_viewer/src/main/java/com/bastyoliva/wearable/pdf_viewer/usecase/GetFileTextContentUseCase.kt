package com.bastyoliva.wearable.pdf_viewer.usecase

import javax.inject.Inject

class GetFileTextContentUseCase @Inject constructor() {
    operator fun invoke(uriString: String): String {
        return uriString
    }
}