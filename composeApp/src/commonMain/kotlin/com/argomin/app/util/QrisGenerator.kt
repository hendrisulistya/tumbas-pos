package com.argomin.app.util

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.argomin.app.generated.resources.Res
import com.argomin.app.generated.resources.logo_gpn
import com.argomin.app.generated.resources.logo_qris
import com.argomin.app.generated.resources.logo_qris_header
import com.argomin.app.presentation.theme.*
import org.jetbrains.compose.resources.painterResource

/**
 * QRIS Generator & Parser for tambooPOS. Standards: EMVCo Merchant-Presented Mode (MPM) & ASPI
 * (Bank Indonesia) QRIS.
 */
object QrisConfig {
    const val STATIC_PAYLOAD =
            "00020101021126610014COM.GO-JEK.WWW01189360091433961813320210G3961813320303UMI51440014ID.CO.QRIS.WWW0215ID10264699901090303UMI5204581253033605802ID5925RM ARGO MINANG 3 KEPEK, P6011KULON PROGO61055565262070703A0263041DE8"
    const val MERCHANT_NAME = "RM ARGO MINANG 3 KEPEK, P"
    const val NMID = "ID1026469990109"
    const val TERMINAL_ID = "A02"
    const val DEFAULT_FEE = 1000L
}

data class DynamicQrisData(
        val payload: String,
        val baseAmount: Long,
        val fee: Long,
        val totalAmount: Long,
        val merchantName: String = QrisConfig.MERCHANT_NAME,
        val nmid: String = QrisConfig.NMID,
        val terminalId: String = QrisConfig.TERMINAL_ID
)

/**
 * Calculates standard CRC-16/CCITT-FALSE checksum for EMVCo QRIS payload. Polynomial: 0x1021,
 * Initial value: 0xFFFF.
 */
fun crc16Ccitt(data: String): String {
    var crc = 0xFFFF
    val bytes = data.encodeToByteArray()
    for (b in bytes) {
        crc = crc xor ((b.toInt() and 0xFF) shl 8)
        for (i in 0 until 8) {
            crc =
                    if ((crc and 0x8000) != 0) {
                        ((crc shl 1) xor 0x1021) and 0xFFFF
                    } else {
                        (crc shl 1) and 0xFFFF
                    }
        }
    }
    return crc.toString(16).uppercase().padStart(4, '0')
}

/**
 * Converts static QRIS template into Dynamic QRIS with exact order amount + fee.
 * - Point of Initiation Method: 11 (Static) -> 12 (Dynamic)
 * - Injects Tag 54 (Transaction Amount)
 * - Recalculates CRC-16/CCITT-FALSE on Tag 63
 */
fun generateDynamicQris(
    orderAmount: Long,
    fee: Long = QrisConfig.DEFAULT_FEE,
    merchantName: String = QrisConfig.MERCHANT_NAME,
    nmid: String = QrisConfig.NMID,
    terminalId: String = QrisConfig.TERMINAL_ID
): DynamicQrisData {
    val total = orderAmount + fee

    // 1. Strip CRC (last 8 characters: 6304 + 4 hex characters)
    val crcTagIdx = QrisConfig.STATIC_PAYLOAD.lastIndexOf("6304")
    var base =
            if (crcTagIdx != -1) {
                QrisConfig.STATIC_PAYLOAD.substring(0, crcTagIdx)
            } else {
                QrisConfig.STATIC_PAYLOAD
            }

    // 2. Change Point of Initiation Method from 11 (Static) to 12 (Dynamic)
    base = base.replaceFirst("010211", "010212")

    // 3. Inject Tag 54 (Transaction Amount) right before Tag 58 (Country Code '5802ID')
    val strAmount = total.toString()
    val lenStr = strAmount.length.toString().padStart(2, '0')
    val tag54 = "54$lenStr$strAmount"

    val tag58Idx = base.indexOf("5802ID")
    base =
            if (tag58Idx != -1) {
                base.substring(0, tag58Idx) + tag54 + base.substring(tag58Idx)
            } else {
                val tag53Idx = base.indexOf("5303360") + "5303360".length
                base.substring(0, tag53Idx) + tag54 + base.substring(tag53Idx)
            }

    // 4. Calculate new CRC16
    val dataForCrc = base + "6304"
    val crc = crc16Ccitt(dataForCrc)
    val finalPayload = dataForCrc + crc

    return DynamicQrisData(
            payload = finalPayload,
            baseAmount = orderAmount,
            fee = fee,
            totalAmount = total,
            merchantName = merchantName,
            nmid = nmid,
            terminalId = terminalId
    )
}

/** Beautiful, vector-sharp QR Code view rendered directly on Compose Canvas. */
@Composable
fun DynamicQrisCard(qrisData: DynamicQrisData, modifier: Modifier = Modifier) {
    val qrCode =
            remember(qrisData.payload) { QrCode.encodeText(qrisData.payload, QrCode.Ecc.MEDIUM) }

    Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Gray300)
    ) {
        Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // MPM QRIS Dasar Layout Container
            Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Gray300),
                    shadowElevation = 2.dp,
                    modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Column(
                        modifier = Modifier.padding(vertical = 16.dp, horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Official QRIS Logo (with scanner corner brackets)
                    Image(
                            painter = painterResource(Res.drawable.logo_qris),
                            contentDescription = "Logo QRIS",
                            modifier = Modifier.height(42.dp).wrapContentWidth()
                    )

                    Spacer(Modifier.height(12.dp))

                    // Canvas Vector QR Code
                    Box(
                            modifier = Modifier.size(210.dp).background(White).padding(4.dp),
                            contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val matrixSize = qrCode.size
                            val quietZone = 4
                            val totalCells = matrixSize + (quietZone * 2)
                            val cellSize = size.width / totalCells

                            drawRect(Color.White)

                            for (y in 0 until matrixSize) {
                                for (x in 0 until matrixSize) {
                                    if (qrCode.getModule(x, y)) {
                                        drawRect(
                                                color = Color.Black,
                                                topLeft =
                                                        Offset(
                                                                (x + quietZone) * cellSize,
                                                                (y + quietZone) * cellSize
                                                        ),
                                                size = Size(cellSize + 0.3f, cellSize + 0.3f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    // NMID according to MPM QRIS Dasar specification
                    Text(
                            "NMID: ${qrisData.nmid}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Gray800,
                            letterSpacing = 0.5.sp,
                            textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(2.dp))

                    Text(
                            "${qrisData.merchantName} • A02",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Normal,
                            color = Gray600,
                            textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Price Breakdown Box
            Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Cyan50,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Cyan200),
                    modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Tagihan Pesanan", fontSize = 12.sp, color = Gray600)
                        Text(
                                formatRupiah(qrisData.baseAmount),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Biaya Layanan (Fee)", fontSize = 12.sp, color = Gray600)
                        Text(
                                "+${formatRupiah(qrisData.fee)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Cyan700
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = Cyan200)
                    Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                                "Total Bayar QRIS",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Cyan900
                        )
                        Text(
                                formatRupiah(qrisData.totalAmount),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Cyan700
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                    "Pelanggan tinggal scan QR di atas. Nominal otomatis terkunci di aplikasi perbankan/e-wallet pelanggan.",
                    fontSize = 11.sp,
                    color = Gray600,
                    textAlign = TextAlign.Center,
                    lineHeight = 15.sp,
                    modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

/**
 * Tampilan QRIS MPM Ringkas sesuai Juklak / Buletin ASPI No: 3/III/BASPI/2021.
 * Elemen standar:
 * 1. Header: Logo QRIS (dengan teks "QR Code Standar Pembayaran Nasional") di kiri sejajar batas bawah dengan Logo GPN di kanan.
 * 2. Merchant Info: Nama Merchant (bold uppercase) & NMID di tengah.
 * 3. QR Code: Standar MPM dengan modul presisi dan 4-cell quiet zone.
 * 4. Ornamen Geometris: Aksen merah (Pantone Red 032C / #E11931) di sisi kiri dan sudut kanan bawah.
 * 5. Footer: "Dicetak oleh: [Kode NNS]" di kiri bawah.
 */
@Composable
fun QrisCodeDisplay(qrisData: DynamicQrisData, modifier: Modifier = Modifier) {
    val qrCode =
            remember(qrisData.payload) { QrCode.encodeText(qrisData.payload, QrCode.Ecc.MEDIUM) }

    // Ekstrak kode NNS dari payload jika ada (8 digit, misal 93600914)
    val nnsCode = remember(qrisData.payload) {
        val regex = Regex("9360[0-9]{4}")
        regex.find(qrisData.payload)?.value ?: "93600914"
    }

    Surface(
            shape = RoundedCornerShape(12.dp),
            color = White,
            border = androidx.compose.foundation.BorderStroke(1.dp, Gray300),
            shadowElevation = 3.dp,
            modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
        ) {
            // Ornamen Geometris Merah Sesuai Juklak MPM Ringkas (Pantone Red 032C: #E11931)
            Canvas(modifier = Modifier.matchParentSize()) {
                val w = size.width
                val h = size.height
                val qrisRed = Color(0xFFE11931)

                // 1. Aksen bendera/pita merah sisi kiri
                val leftPath = Path().apply {
                    moveTo(0f, h * 0.22f)
                    lineTo(w * 0.11f, h * 0.22f + w * 0.10f)
                    lineTo(w * 0.055f, h * 0.22f + w * 0.10f)
                    lineTo(w * 0.055f, h * 0.555f)
                    lineTo(0f, h * 0.555f + w * 0.045f)
                    close()
                }
                drawPath(leftPath, color = qrisRed)

                // 2. Aksen pita merah sudut kanan bawah
                val brPath = Path().apply {
                    moveTo(w, h * 0.803f)
                    lineTo(w - w * 0.046f, h * 0.803f + w * 0.046f)
                    lineTo(w - w * 0.046f, h * 0.901f)
                    lineTo(w - w * 0.194f, h * 0.901f)
                    lineTo(w - w * 0.315f, h)
                    lineTo(w, h)
                    close()
                }
                drawPath(brPath, color = qrisRed)
            }

            // Konten Tampilan QRIS MPM Ringkas
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. Header: Logo QRIS + Teks di kiri, Logo GPN di kanan
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Image(
                        painter = painterResource(Res.drawable.logo_qris_header),
                        contentDescription = "QRIS - QR Code Standar Pembayaran Nasional",
                        modifier = Modifier.height(26.dp).wrapContentWidth()
                    )
                    Image(
                        painter = painterResource(Res.drawable.logo_gpn),
                        contentDescription = "Logo GPN",
                        modifier = Modifier.height(30.dp).wrapContentWidth()
                    )
                }

                Spacer(Modifier.height(10.dp))

                // 2. Data Merchant (Nama & NMID)
                Text(
                    text = qrisData.merchantName.uppercase(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Black,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(Modifier.height(2.dp))

                Text(
                    text = "NMID: ${qrisData.nmid}",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = Neutral800,
                    letterSpacing = 0.5.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(10.dp))

                // 3. QR Code Canvas (High contrast, quiet zone)
                Box(
                    modifier = Modifier
                        .size(205.dp)
                        .background(White)
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val matrixSize = qrCode.size
                        val quietZone = 4
                        val totalCells = matrixSize + (quietZone * 2)
                        val cellSize = size.width / totalCells

                        drawRect(Color.White)

                        for (y in 0 until matrixSize) {
                            for (x in 0 until matrixSize) {
                                if (qrCode.getModule(x, y)) {
                                    drawRect(
                                        color = Color.Black,
                                        topLeft = Offset(
                                            (x + quietZone) * cellSize,
                                            (y + quietZone) * cellSize
                                        ),
                                        size = Size(cellSize + 0.3f, cellSize + 0.3f)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                // 4. Footer: Dicetak oleh: [Kode NNS]
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Dicetak oleh: $nnsCode",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Normal,
                        color = Neutral700
                    )
                }
            }
        }
    }
}
