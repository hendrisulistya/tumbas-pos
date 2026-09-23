package com.argminres.app.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.argminres.app.domain.model.AggregatedDishUsage
import com.argminres.app.domain.model.AggregatedIngredientUsage
import com.argminres.app.domain.model.TopProduct
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.NumberFormat
import java.util.Locale
import android.media.MediaScannerConnection

data class PdfReportData(
    val storeName: String,
    val storeAddress: String,
    val reportTitle: String,
    val periodLabel: String,
    val totalRevenue: Double,
    val totalCost: Double,
    val totalWaste: Double,
    val topProducts: List<TopProduct>,
    val ingredientUsage: List<AggregatedIngredientUsage> = emptyList(),
    val dishUsage: List<AggregatedDishUsage> = emptyList(),
    val isDetailed: Boolean = false
)

class PdfReportExporter(private val context: Context) {

    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

    fun generateAndSaveReport(data: PdfReportData): Uri? {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas
        var y = 40f

        val paint = Paint().apply {
            color = Color.BLACK
        }
        val titlePaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 18f
            color = Color.BLACK
        }
        val headerPaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 12f
            color = Color.BLACK
        }
        val normalPaint = Paint().apply {
            textSize = 10f
            color = Color.BLACK
        }
        val boldPaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 10f
            color = Color.BLACK
        }

        // Draw Header
        canvas.drawText(data.storeName, 40f, y, titlePaint)
        y += 20f
        canvas.drawText(data.storeAddress, 40f, y, normalPaint)
        y += 30f
        
        canvas.drawText(data.reportTitle, 40f, y, headerPaint)
        y += 15f
        canvas.drawText(data.periodLabel, 40f, y, normalPaint)
        y += 30f

        // Draw Summary
        canvas.drawText("RINGKASAN OPERASIONAL", 40f, y, boldPaint)
        y += 20f
        drawSummaryRow(canvas, "Total Pendapatan", currencyFormatter.format(data.totalRevenue), 40f, y, normalPaint)
        y += 15f
        drawSummaryRow(canvas, "Biaya Bahan", "- ${currencyFormatter.format(data.totalCost)}", 40f, y, normalPaint)
        y += 15f
        drawSummaryRow(canvas, "Nilai Buangan", "- ${currencyFormatter.format(data.totalWaste)}", 40f, y, normalPaint)
        y += 20f
        val netProfit = data.totalRevenue - data.totalCost - data.totalWaste
        drawSummaryRow(canvas, "LABA BERSIH", currencyFormatter.format(netProfit), 40f, y, boldPaint)
        y += 40f

        // Draw Products Table
        canvas.drawText(if (data.isDetailed) "DETAIL PENJUALAN" else "PRODUK TERLARIS (TOP 5)", 40f, y, boldPaint)
        y += 15f
        drawTableRow(canvas, listOf("Produk", "Terjual", "Pendapatan"), listOf(250f, 80f, 150f), 40f, y, boldPaint)
        y += 5f
        canvas.drawLine(40f, y, 550f, y, paint)
        y += 15f
        
        val productList = if (data.isDetailed) data.topProducts else data.topProducts.take(5)
        productList.forEach { product ->
            if (y > 780f) {
                document.finishPage(page)
                page = document.startPage(pageInfo)
                canvas = page.canvas
                y = 40f
            }
            drawTableRow(canvas, listOf(product.productName, product.quantitySold.toString(), currencyFormatter.format(product.totalRevenue)), listOf(250f, 80f, 150f), 40f, y, normalPaint)
            y += 15f
        }
        
        y += 20f
        
        // Detailed Sections (Only for isDetailed reports)
        if (data.isDetailed) {
            // Ingredient Usage
            if (data.ingredientUsage.isNotEmpty()) {
                if (y > 700f) {
                    document.finishPage(page)
                    page = document.startPage(pageInfo)
                    canvas = page.canvas
                    y = 40f
                }
                canvas.drawText("PENGGUNAAN BAHAN", 40f, y, boldPaint)
                y += 15f
                drawTableRow(canvas, listOf("Bahan", "Pakai", "Biaya"), listOf(250f, 80f, 150f), 40f, y, boldPaint)
                y += 5f
                canvas.drawLine(40f, y, 550f, y, paint)
                y += 15f
                
                data.ingredientUsage.forEach { usage ->
                    if (y > 780f) {
                        document.finishPage(page)
                        page = document.startPage(pageInfo)
                        canvas = page.canvas
                        y = 40f
                    }
                    drawTableRow(canvas, listOf(usage.ingredientName, "${usage.quantityUsed} ${usage.unit}", currencyFormatter.format(usage.totalCost)), listOf(250f, 80f, 150f), 40f, y, normalPaint)
                    y += 15f
                }
            }

            y += 20f

            // Waste Records
            if (data.dishUsage.isNotEmpty()) {
                if (y > 700f) {
                    document.finishPage(page)
                    page = document.startPage(pageInfo)
                    canvas = page.canvas
                    y = 40f
                }
                canvas.drawText("CATATAN BUANGAN (UNSOLD)", 40f, y, boldPaint)
                y += 15f
                drawTableRow(canvas, listOf("Produk", "Produksi", "Sisa", "Buang"), listOf(250f, 70f, 70f, 70f), 40f, y, boldPaint)
                y += 5f
                canvas.drawLine(40f, y, 550f, y, paint)
                y += 15f

                data.dishUsage.forEach { dish ->
                    if (y > 780f) {
                        document.finishPage(page)
                        page = document.startPage(pageInfo)
                        canvas = page.canvas
                        y = 40f
                    }
                    drawTableRow(canvas, listOf(dish.dishName, dish.producedQuantity.toString(), dish.remainingQuantity.toString(), dish.wasteQuantity.toString()), listOf(250f, 70f, 70f, 70f), 40f, y, normalPaint)
                    y += 15f
                }
            }
        }

        document.finishPage(page)

        // Save to MediaStore
        val fileName = "${data.reportTitle.replace(" ", "_")}_${System.currentTimeMillis()}.pdf"
        return savePdfToDownloads(document, fileName)
    }

    private fun drawSummaryRow(canvas: Canvas, label: String, value: String, x: Float, y: Float, paint: Paint) {
        canvas.drawText(label, x, y, paint)
        val valueWidth = paint.measureText(value)
        canvas.drawText(value, 550f - valueWidth, y, paint)
    }

    private fun drawTableRow(canvas: Canvas, cells: List<String>, widths: List<Float>, x: Float, y: Float, paint: Paint) {
        var currentX = x
        cells.forEachIndexed { index, cell ->
            canvas.drawText(cell, currentX, y, paint)
            currentX += widths[index]
        }
    }

    private fun savePdfToDownloads(document: PdfDocument, fileName: String): Uri? {
        val subfolderName = "PadangPOS"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/$subfolderName")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }

            val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            val uri = resolver.insert(collection, contentValues)
            
            if (uri != null) {
                try {
                    resolver.openOutputStream(uri)?.use { outputStream ->
                        document.writeTo(outputStream)
                    }
                    
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(uri, contentValues, null, null)
                    
                    document.close()
                    return uri
                } catch (e: Exception) {
                    resolver.delete(uri, null, null)
                    document.close()
                    return null
                }
            }
        } else {
            // Legacy Storage for API < 29
            try {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val tumbasDir = File(downloadsDir, subfolderName)
                if (!tumbasDir.exists()) {
                    tumbasDir.mkdirs()
                }
                
                val file = File(tumbasDir, fileName)
                FileOutputStream(file).use { outputStream ->
                    document.writeTo(outputStream)
                }
                
                // Explicitly scan the file to make it visible in File Managers
                MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), arrayOf("application/pdf"), null)
                
                document.close()
                return Uri.fromFile(file)
            } catch (e: Exception) {
                document.close()
                return null
            }
        }
        document.close()
        return null
    }
}
