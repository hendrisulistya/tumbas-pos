package com.argminres.app.domain.repository

import com.argminres.app.domain.model.R2Config

interface ImageRepository {
    suspend fun uploadProductImage(
        imageData: ByteArray,
        fileName: String,
        r2Config: R2Config?,
        namespace: String
    ): Result<String>
    
    suspend fun deleteDishImage(
        image: String,
        r2Config: R2Config?,
        namespace: String
    ): Result<Unit>
}
