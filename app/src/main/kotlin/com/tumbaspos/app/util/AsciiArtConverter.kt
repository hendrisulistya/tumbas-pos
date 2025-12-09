package com.tumbaspos.app.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64

object AsciiArtConverter {
    // ASCII characters from darkest to lightest
    private const val ASCII_CHARS = "@%#*+=-:. "
    
    /**
     * Converts a Base64 encoded image to ASCII art
     * Optimized for 58mm thermal printer (32 characters width)
     */
    fun convertToAscii(
        base64Image: String?,
        width: Int = 32,
        height: Int = 8
    ): String {
        if (base64Image.isNullOrBlank()) return ""
        
        return try {
            // Decode Base64 to bitmap
            val imageBytes = Base64.decode(base64Image, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                ?: return ""
            
            // Resize bitmap to target dimensions
            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true)
            
            // Convert to ASCII
            val asciiArt = StringBuilder()
            for (y in 0 until height) {
                for (x in 0 until width) {
                    val pixel = resizedBitmap.getPixel(x, y)
                    val brightness = getBrightness(pixel)
                    val charIndex = (brightness * (ASCII_CHARS.length - 1)).toInt()
                    asciiArt.append(ASCII_CHARS[charIndex])
                }
                asciiArt.append('\n')
            }
            
            resizedBitmap.recycle()
            bitmap.recycle()
            
            asciiArt.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
    
    /**
     * Calculate brightness of a pixel (0.0 = black, 1.0 = white)
     */
    private fun getBrightness(pixel: Int): Float {
        val red = (pixel shr 16) and 0xFF
        val green = (pixel shr 8) and 0xFF
        val blue = pixel and 0xFF
        
        // Use luminosity formula for better grayscale conversion
        val brightness = (0.299 * red + 0.587 * green + 0.114 * blue) / 255.0
        return brightness.toFloat()
    }
}
