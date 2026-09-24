package com.argomin.app.domain.usecase.dish

import com.argomin.app.data.repository.SettingsRepository
import com.argomin.app.domain.repository.ImageRepository
import kotlinx.coroutines.flow.first
import java.util.UUID

class ManageDishImageUseCase(
    private val imageRepository: ImageRepository,
    private val settingsRepository: SettingsRepository
) {
    suspend fun uploadImage(imageData: ByteArray): Result<String> {
        val fileName = "${UUID.randomUUID()}.jpg"
        
        return imageRepository.uploadProductImage(
            imageData = imageData,
            fileName = fileName
        )
    }
    
    suspend fun deleteImage(image: String) {
        imageRepository.deleteDishImage(
            image = image
        )
    }
}
