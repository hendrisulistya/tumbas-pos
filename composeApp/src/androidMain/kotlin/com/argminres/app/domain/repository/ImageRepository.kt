package com.argminres.app.domain.repository


interface ImageRepository {
    suspend fun uploadProductImage(
        imageData: ByteArray,
        fileName: String
    ): Result<String>
    
    suspend fun deleteDishImage(
        image: String
    ): Result<Unit>
}
