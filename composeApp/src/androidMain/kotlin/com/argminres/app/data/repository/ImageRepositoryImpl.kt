package com.argminres.app.data.repository

import android.util.Base64
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import java.io.ByteArrayOutputStream
import com.argminres.app.domain.repository.ImageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ImageRepositoryImpl : ImageRepository {
    
    override suspend fun uploadProductImage(
        imageData: ByteArray,
        fileName: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Convert to grayscale and compress
            val processedImageData = processImageToGrayscale(imageData)
            
            // Store as base64
            val base64 = "data:image/jpeg;base64," + Base64.encodeToString(processedImageData, Base64.NO_WRAP)
            Result.success(base64)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteDishImage(
        image: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        // Base64 images are stored in the database, no file deletion needed
        Result.success(Unit)
    }
    
    private fun processImageToGrayscale(imageData: ByteArray): ByteArray {
        // Decode the image
        val originalBitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
            ?: throw Exception("Failed to decode image")

        // Convert to grayscale (black and white)
        val grayscaleBitmap = toGrayscale(originalBitmap)
        
        // Compress if needed (target ~500KB)
        val targetSizeKB = 500
        var quality = 90
        var compressedData: ByteArray
        
        do {
            val outputStream = ByteArrayOutputStream()
            grayscaleBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            compressedData = outputStream.toByteArray()
            quality -= 10
        } while (compressedData.size > targetSizeKB * 1024 && quality > 10)
        
        // Clean up
        if (grayscaleBitmap != originalBitmap) {
            grayscaleBitmap.recycle()
        }
        originalBitmap.recycle()
        
        return compressedData
    }
    
    private fun toGrayscale(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val grayscaleBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(grayscaleBitmap)
        val paint = Paint()
        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(0f) // 0 = grayscale, 1 = original colors
        val colorFilter = ColorMatrixColorFilter(colorMatrix)
        paint.colorFilter = colorFilter
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return grayscaleBitmap
    }
}
