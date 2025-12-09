package com.tumbaspos.app.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object ThermalReceiptPdfGenerator {
    
    // 58mm thermal printer simulation
    private const val PAGE_WIDTH = 226 // 58mm in points (1mm = 3.78 points)
    private const val PAGE_HEIGHT = 842 // A4 height, will be trimmed to content
    private const val MARGIN = 20f
    private const val LINE_HEIGHT = 12f
    private const val CHAR_WIDTH = 6f // Monospace character width
    
    /**
     * Generate temporary PDF for preview (saved to cache)
     * Returns the file path if successful, null otherwise
     */
    fun generateTempPdf(
        context: Context,
        receiptText: String,
        orderNumber: String,
        logoBase64: String? = null
    ): String? {
        return generatePdfInternal(context, receiptText, orderNumber, logoBase64, isTemp = true)
    }
    
    /**
     * Generate permanent PDF (saved to Downloads)
     * Returns the file path if successful, null otherwise
     */
    fun generatePdf(
        context: Context,
        receiptText: String,
        orderNumber: String,
        logoBase64: String? = null
    ): String? {
        return generatePdfInternal(context, receiptText, orderNumber, logoBase64, isTemp = false)
    }
    
    /**
     * Internal method to generate PDF
     */
    private fun generatePdfInternal(
        context: Context,
        receiptText: String,
        orderNumber: String,
        logoBase64: String?,
        isTemp: Boolean
    ): String? {
        return try {
            // Create PDF document
            val pdfDocument = PdfDocument()
            
            // Calculate page height based on content
            val lines = receiptText.lines()
            
            // Setup paint for text
            val paint = Paint().apply {
                typeface = Typeface.MONOSPACE
                textSize = 10f
                isAntiAlias = true
                color = android.graphics.Color.BLACK
            }

            var currentContentHeight = MARGIN * 2f // Initial margin top and bottom
            var logoRenderedHeight = 0f

            if (!logoBase64.isNullOrBlank()) {
                try {
                    val imageBytes = android.util.Base64.decode(logoBase64, android.util.Base64.DEFAULT)
                    val logoBitmap = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

                    if (logoBitmap != null) {
                        val maxLogoWidth = 180f
                        val scaleFactor = if (logoBitmap.width > maxLogoWidth) {
                            maxLogoWidth / logoBitmap.width
                        } else {
                            1f
                        }
                        logoRenderedHeight = (logoBitmap.height * scaleFactor) + 10f // Logo height + spacing
                        logoBitmap.recycle() // Recycle immediately after calculating scaled height
                    }
                } catch (e: Exception) {
                    android.util.Log.e("ThermalReceiptPdf", "Error calculating logo height", e)
                }
            }
            currentContentHeight += logoRenderedHeight
            currentContentHeight += lines.size * LINE_HEIGHT
            
            // Create page with exact content height (no minimum)
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, currentContentHeight.toInt(), 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            
            var yPosition = MARGIN
            
            // Draw logo if available
            if (!logoBase64.isNullOrBlank()) {
                try {
                    val imageBytes = android.util.Base64.decode(logoBase64, android.util.Base64.DEFAULT)
                    val logoBitmap = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    
                    if (logoBitmap != null) {
                        // Scale logo to fit width (max 180 points, centered)
                        val maxLogoWidth = 180f
                        val scaleFactor = if (logoBitmap.width > maxLogoWidth) {
                            maxLogoWidth / logoBitmap.width
                        } else {
                            1f
                        }
                        
                        val scaledWidth = (logoBitmap.width * scaleFactor).toInt()
                        val scaledHeight = (logoBitmap.height * scaleFactor).toInt()
                        
                        val scaledLogo = android.graphics.Bitmap.createScaledBitmap(
                            logoBitmap,
                            scaledWidth,
                            scaledHeight,
                            true
                        )
                        
                        // Center the logo
                        val logoX = (PAGE_WIDTH - scaledWidth) / 2f
                        canvas.drawBitmap(scaledLogo, logoX, yPosition, null)
                        
                        yPosition += scaledHeight + 10f // Add spacing after logo
                        
                        scaledLogo.recycle()
                        logoBitmap.recycle()
                    }
                } catch (e: Exception) {
                    android.util.Log.e("ThermalReceiptPdf", "Error rendering logo", e)
                }
            }
            
            // Draw text lines
            lines.forEach { line ->
                canvas.drawText(line, MARGIN, yPosition, paint)
                yPosition += LINE_HEIGHT
            }
            
            pdfDocument.finishPage(page)
            
            // Save to file
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "Receipt_${orderNumber}_$timestamp.pdf"
            
            // Save to temp cache if temporary, otherwise to Downloads
            if (isTemp) {
                // Save to cache directory (temporary)
                val tempFile = File(context.cacheDir, fileName)
                FileOutputStream(tempFile).use { outputStream ->
                    pdfDocument.writeTo(outputStream)
                }
                pdfDocument.close()
                android.util.Log.d("ThermalReceiptPdf", "Temp PDF saved to: ${tempFile.absolutePath}")
                return tempFile.absolutePath
            }
            
            // Save permanently to Downloads
            // Use MediaStore for Android 10+ (API 29+)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                val contentValues = android.content.ContentValues().apply {
                    put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                
                val uri = context.contentResolver.insert(
                    android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    contentValues
                )
                
                uri?.let {
                    context.contentResolver.openOutputStream(it)?.use { outputStream ->
                        pdfDocument.writeTo(outputStream)
                    }
                    pdfDocument.close()
                    
                    // Return the file path
                    android.util.Log.d("ThermalReceiptPdf", "PDF saved to Downloads: $fileName")
                    "Downloads/$fileName"
                } ?: run {
                    pdfDocument.close()
                    android.util.Log.e("ThermalReceiptPdf", "Failed to create MediaStore entry")
                    null
                }
            } else {
                // For older Android versions, use direct file access
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                downloadsDir.mkdirs() // Ensure directory exists
                val file = File(downloadsDir, fileName)
                
                FileOutputStream(file).use { outputStream ->
                    pdfDocument.writeTo(outputStream)
                }
                
                pdfDocument.close()
                android.util.Log.d("ThermalReceiptPdf", "PDF saved to: ${file.absolutePath}")
                file.absolutePath
            }
        } catch (e: Exception) {
            android.util.Log.e("ThermalReceiptPdf", "Error generating PDF", e)
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Generate PDF and return File object for sharing
     */
    fun generatePdfFile(
        context: Context,
        receiptText: String,
        orderNumber: String
    ): File? {
        val filePath = generatePdf(context, receiptText, orderNumber)
        return filePath?.let { File(it) }
    }
}
