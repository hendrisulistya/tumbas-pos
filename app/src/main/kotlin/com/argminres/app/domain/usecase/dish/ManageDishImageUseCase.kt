package com.argminres.app.domain.usecase.dish

import com.argminres.app.data.repository.SettingsRepository
import com.argminres.app.domain.repository.ImageRepository
import kotlinx.coroutines.flow.first
import java.util.UUID

class ManageDishImageUseCase(
    private val imageRepository: ImageRepository,
    private val settingsRepository: SettingsRepository
) {
    suspend fun uploadImage(imageData: ByteArray): Result<String> {
        val r2Config = settingsRepository.r2Config.first()
        val namespace = settingsRepository.storeId.first() ?: settingsRepository.getAppId()
        val fileName = "${UUID.randomUUID()}.jpg"
        
        return imageRepository.uploadProductImage(
            imageData = imageData,
            fileName = fileName,
            r2Config = r2Config,
            namespace = namespace
        )
    }
    
    suspend fun deleteImage(image: String) {
        val r2Config = settingsRepository.r2Config.first()
        val namespace = settingsRepository.storeId.first() ?: settingsRepository.getAppId()
        
        imageRepository.deleteDishImage(
            image = image,
            r2Config = r2Config,
            namespace = namespace
        )
    }
}
