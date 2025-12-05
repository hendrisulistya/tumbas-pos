package com.tumbaspos.app.domain.repository

import com.tumbaspos.app.domain.model.R2Config

interface ImageRepository {
    suspend fun uploadProductImage(
        imageData: ByteArray,
        fileName: String,
        r2Config: R2Config?,
        namespace: String
    ): Result<String>
    
    suspend fun deleteProductImage(
        image: String,
        r2Config: R2Config?,
        namespace: String
    ): Result<Unit>
}
