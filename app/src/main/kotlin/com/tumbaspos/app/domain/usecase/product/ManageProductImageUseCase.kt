package com.tumbaspos.app.domain.usecase.product

import com.tumbaspos.app.data.repository.SettingsRepository
import com.tumbaspos.app.domain.repository.ImageRepository
import kotlinx.coroutines.flow.first
import java.util.UUID

class ManageProductImageUseCase(
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
        
        imageRepository.deleteProductImage(
            image = image,
            r2Config = r2Config,
            namespace = namespace
        )
    }
}
