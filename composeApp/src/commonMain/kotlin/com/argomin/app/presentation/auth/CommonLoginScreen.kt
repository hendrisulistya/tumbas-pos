package com.argomin.app.presentation.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.argomin.app.core.PlatformConfig
import com.argomin.app.domain.model.Employee
import com.argomin.app.generated.resources.Res
import com.argomin.app.generated.resources.logo
import com.argomin.app.presentation.theme.*
import org.jetbrains.compose.resources.painterResource

enum class LoginStep {
        WELCOME,
        SELECT_USER,
        INPUT_PIN
}

@Composable
fun CommonLoginScreen(employees: List<Employee>, onLoginSuccess: (Employee) -> Unit) {
        var currentStep by remember { mutableStateOf(LoginStep.WELCOME) }
        var selectedEmployee by remember { mutableStateOf<Employee?>(null) }
        var pin by remember { mutableStateOf("") }
        var errorMsg by remember { mutableStateOf<String?>(null) }

        Row(modifier = Modifier.fillMaxSize().background(White)) {
                // ── Panel Kiri: Branding tambooPOS (38% lebar) ─────────────────────
                Box(
                        modifier =
                                Modifier.weight(0.38f)
                                        .fillMaxHeight()
                                        .background(
                                                Brush.verticalGradient(
                                                        listOf(Cyan800, Cyan700, Cyan900)
                                                )
                                        ),
                        contentAlignment = Alignment.Center
                ) {
                        Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.padding(32.dp)
                        ) {
                                Surface(
                                        shape = CircleShape,
                                        color = White,
                                        modifier = Modifier.size(92.dp),
                                        shadowElevation = 6.dp
                                ) {
                                        Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier.padding(14.dp)
                                        ) {
                                                Image(
                                                        painter =
                                                                painterResource(Res.drawable.logo),
                                                        contentDescription = "tambooPOS Logo",
                                                        modifier = Modifier.fillMaxSize()
                                                )
                                        }
                                }

                                Text(
                                        text = "tambooPOS",
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = White,
                                        letterSpacing = 0.5.sp
                                )
                                Text(
                                        text = "Sistem Kasir Rumah Makan Padang",
                                        fontSize = 15.sp,
                                        color = Cyan100,
                                        textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Feature Highlights
                                listOf(
                                                "Pembayaran Cepat (Cash & QRIS)",
                                                "Etalase Saji & Stok Real-Time",
                                                "Multi-Karyawan & Otoritas PIN",
                                                "Laporan Omset & Tutup Hari"
                                        )
                                        .forEach { feature ->
                                                Row(
                                                        verticalAlignment =
                                                                Alignment.CenterVertically,
                                                        horizontalArrangement =
                                                                Arrangement.spacedBy(10.dp),
                                                        modifier =
                                                                Modifier.fillMaxWidth()
                                                                        .background(
                                                                                Color(0x1FFFFFFF),
                                                                                RoundedCornerShape(
                                                                                        20.dp
                                                                                )
                                                                        )
                                                                        .padding(
                                                                                horizontal = 14.dp,
                                                                                vertical = 10.dp
                                                                        )
                                                ) {
                                                        Box(
                                                                modifier =
                                                                        Modifier.size(7.dp)
                                                                                .background(
                                                                                        White,
                                                                                        CircleShape
                                                                                )
                                                        )
                                                        Text(
                                                                feature,
                                                                fontSize = 13.sp,
                                                                color = White,
                                                                fontWeight = FontWeight.Medium
                                                        )
                                                }
                                        }

                                Spacer(modifier = Modifier.height(8.dp))

                                Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0x33FFFFFF)
                                ) {
                                        Text(
                                                text = "Platform: ${PlatformConfig.platformName}",
                                                fontSize = 11.sp,
                                                color = White,
                                                modifier =
                                                        Modifier.padding(
                                                                horizontal = 12.dp,
                                                                vertical = 4.dp
                                                        )
                                        )
                                }
                        }
                }

                // ── Panel Kanan: Alur Elegan (Tombol Masuk -> Pilih User -> Input PIN) ────────
                Box(
                        modifier =
                                Modifier.weight(0.62f)
                                        .fillMaxHeight()
                                        .background(Neutral50)
                                        .padding(28.dp),
                        contentAlignment = Alignment.Center
                ) {
                        Card(
                                modifier = Modifier.widthIn(max = 480.dp).fillMaxWidth(),
                                shape = RoundedCornerShape(24.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                border = BorderStroke(1.dp, Neutral200),
                                colors = CardDefaults.cardColors(containerColor = White)
                        ) {
                                AnimatedContent(
                                        targetState = currentStep,
                                        transitionSpec = {
                                                if (targetState.ordinal > initialState.ordinal) {
                                                        (slideInHorizontally { width ->
                                                                width / 3
                                                        } + fadeIn()) togetherWith
                                                                (slideOutHorizontally { width ->
                                                                        -width / 3
                                                                } + fadeOut())
                                                } else {
                                                        (slideInHorizontally { width ->
                                                                -width / 3
                                                        } + fadeIn()) togetherWith
                                                                (slideOutHorizontally { width ->
                                                                        width / 3
                                                                } + fadeOut())
                                                }
                                        },
                                        label = "LoginStepTransition"
                                ) { step ->
                                        Column(
                                                modifier = Modifier.padding(32.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                                when (step) {
                                                        // ── TAHAP 1: TOMBOL MASUK AWAL
                                                        // ───────────────────────
                                                        LoginStep.WELCOME -> {
                                                                Spacer(Modifier.height(12.dp))

                                                                Surface(
                                                                        shape = CircleShape,
                                                                        color = Cyan50,
                                                                        border =
                                                                                BorderStroke(
                                                                                        1.dp,
                                                                                        Cyan200
                                                                                ),
                                                                        modifier =
                                                                                Modifier.size(72.dp)
                                                                ) {
                                                                        Box(
                                                                                contentAlignment =
                                                                                        Alignment
                                                                                                .Center
                                                                        ) {
                                                                                Icon(
                                                                                        imageVector =
                                                                                                Icons.Default
                                                                                                        .Storefront,
                                                                                        contentDescription =
                                                                                                null,
                                                                                        tint =
                                                                                                Cyan700,
                                                                                        modifier =
                                                                                                Modifier.size(
                                                                                                        34.dp
                                                                                                )
                                                                                )
                                                                        }
                                                                }

                                                                Spacer(Modifier.height(20.dp))

                                                                Text(
                                                                        text = "Selamat Datang",
                                                                        fontSize = 24.sp,
                                                                        fontWeight =
                                                                                FontWeight.Bold,
                                                                        color = Neutral900
                                                                )

                                                                Spacer(Modifier.height(6.dp))

                                                                Text(
                                                                        text =
                                                                                "Sistem Kasir & Operasional Resto",
                                                                        fontSize = 14.sp,
                                                                        color = Neutral500,
                                                                        textAlign = TextAlign.Center
                                                                )

                                                                Spacer(Modifier.height(16.dp))

                                                                Button(
                                                                        onClick = {
                                                                                currentStep =
                                                                                        LoginStep
                                                                                                .SELECT_USER
                                                                        },
                                                                        modifier =
                                                                                Modifier.fillMaxWidth()
                                                                                        .height(
                                                                                                52.dp
                                                                                        ),
                                                                        colors =
                                                                                ButtonDefaults
                                                                                        .buttonColors(
                                                                                                containerColor =
                                                                                                        Cyan700,
                                                                                                contentColor =
                                                                                                        White
                                                                                        ),
                                                                        shape =
                                                                                RoundedCornerShape(
                                                                                        14.dp
                                                                                ),
                                                                        elevation =
                                                                                ButtonDefaults
                                                                                        .buttonElevation(
                                                                                                defaultElevation =
                                                                                                        2.dp,
                                                                                                pressedElevation =
                                                                                                        0.dp
                                                                                        )
                                                                ) {
                                                                        Row(
                                                                                verticalAlignment =
                                                                                        Alignment
                                                                                                .CenterVertically,
                                                                                horizontalArrangement =
                                                                                        Arrangement
                                                                                                .Center
                                                                        ) {
                                                                                Icon(
                                                                                        imageVector =
                                                                                                Icons.AutoMirrored
                                                                                                        .Filled
                                                                                                        .Login,
                                                                                        contentDescription =
                                                                                                null,
                                                                                        modifier =
                                                                                                Modifier.size(
                                                                                                        20.dp
                                                                                                )
                                                                                )
                                                                                Spacer(
                                                                                        Modifier.width(
                                                                                                10.dp
                                                                                        )
                                                                                )
                                                                                Text(
                                                                                        text =
                                                                                                "Masuk",
                                                                                        fontSize =
                                                                                                16.sp,
                                                                                        fontWeight =
                                                                                                FontWeight
                                                                                                        .Bold,
                                                                                        letterSpacing =
                                                                                                0.5.sp
                                                                                )
                                                                        }
                                                                }

                                                                Spacer(Modifier.height(12.dp))

                                                                Text(
                                                                        text =
                                                                                "Sentuh tombol masuk untuk memilih profil kasir",
                                                                        fontSize = 12.sp,
                                                                        color = Neutral400,
                                                                        textAlign = TextAlign.Center
                                                                )

                                                                Spacer(Modifier.height(8.dp))
                                                        }

                                                        // ── TAHAP 2: PILIH USER
                                                        // ──────────────────────────────
                                                        LoginStep.SELECT_USER -> {
                                                                Row(
                                                                        modifier =
                                                                                Modifier.fillMaxWidth(),
                                                                        verticalAlignment =
                                                                                Alignment
                                                                                        .CenterVertically
                                                                ) {
                                                                        IconButton(
                                                                                onClick = {
                                                                                        currentStep =
                                                                                                LoginStep
                                                                                                        .WELCOME
                                                                                }
                                                                        ) {
                                                                                Icon(
                                                                                        imageVector =
                                                                                                Icons.AutoMirrored
                                                                                                        .Filled
                                                                                                        .ArrowBack,
                                                                                        contentDescription =
                                                                                                "Kembali ke Beranda",
                                                                                        tint =
                                                                                                Neutral600
                                                                                )
                                                                        }
                                                                        Spacer(Modifier.width(6.dp))
                                                                        Column {
                                                                                Text(
                                                                                        text =
                                                                                                "Pilih Pengguna",
                                                                                        fontSize =
                                                                                                20.sp,
                                                                                        fontWeight =
                                                                                                FontWeight
                                                                                                        .Bold,
                                                                                        color =
                                                                                                Neutral800
                                                                                )
                                                                                Text(
                                                                                        text =
                                                                                                "Pilih profil karyawan untuk masuk",
                                                                                        fontSize =
                                                                                                13.sp,
                                                                                        color =
                                                                                                Neutral500
                                                                                )
                                                                        }
                                                                }

                                                                Spacer(Modifier.height(20.dp))

                                                                Column(
                                                                        verticalArrangement =
                                                                                Arrangement
                                                                                        .spacedBy(
                                                                                                10.dp
                                                                                        ),
                                                                        modifier =
                                                                                Modifier.fillMaxWidth()
                                                                ) {
                                                                        employees.forEach { emp ->
                                                                                Surface(
                                                                                        modifier =
                                                                                                Modifier.fillMaxWidth()
                                                                                                        .clip(
                                                                                                                RoundedCornerShape(
                                                                                                                        14.dp
                                                                                                                )
                                                                                                        )
                                                                                                        .clickable {
                                                                                                                selectedEmployee =
                                                                                                                        emp
                                                                                                                pin =
                                                                                                                        ""
                                                                                                                errorMsg =
                                                                                                                        null
                                                                                                                currentStep =
                                                                                                                        LoginStep
                                                                                                                                .INPUT_PIN
                                                                                                        },
                                                                                        color =
                                                                                                Cyan50,
                                                                                        border =
                                                                                                BorderStroke(
                                                                                                        1.dp,
                                                                                                        Cyan200
                                                                                                )
                                                                                ) {
                                                                                        Row(
                                                                                                modifier =
                                                                                                        Modifier.padding(
                                                                                                                horizontal =
                                                                                                                        16.dp,
                                                                                                                vertical =
                                                                                                                        12.dp
                                                                                                        ),
                                                                                                verticalAlignment =
                                                                                                        Alignment
                                                                                                                .CenterVertically,
                                                                                                horizontalArrangement =
                                                                                                        Arrangement
                                                                                                                .SpaceBetween
                                                                                        ) {
                                                                                                Row(
                                                                                                        verticalAlignment =
                                                                                                                Alignment
                                                                                                                        .CenterVertically
                                                                                                ) {
                                                                                                        Surface(
                                                                                                                shape =
                                                                                                                        CircleShape,
                                                                                                                color =
                                                                                                                        Cyan600,
                                                                                                                modifier =
                                                                                                                        Modifier.size(
                                                                                                                                42.dp
                                                                                                                        )
                                                                                                        ) {
                                                                                                                Box(
                                                                                                                        contentAlignment =
                                                                                                                                Alignment
                                                                                                                                        .Center
                                                                                                                ) {
                                                                                                                        Text(
                                                                                                                                emp.name
                                                                                                                                        .take(
                                                                                                                                                1
                                                                                                                                        )
                                                                                                                                        .uppercase(),
                                                                                                                                color =
                                                                                                                                        White,
                                                                                                                                fontWeight =
                                                                                                                                        FontWeight
                                                                                                                                                .Bold,
                                                                                                                                fontSize =
                                                                                                                                        16.sp
                                                                                                                        )
                                                                                                                }
                                                                                                        }
                                                                                                        Spacer(
                                                                                                                Modifier.width(
                                                                                                                        14.dp
                                                                                                                )
                                                                                                        )
                                                                                                        Column {
                                                                                                                Text(
                                                                                                                        emp.name,
                                                                                                                        fontSize =
                                                                                                                                15.sp,
                                                                                                                        fontWeight =
                                                                                                                                FontWeight
                                                                                                                                        .Bold,
                                                                                                                        color =
                                                                                                                                Neutral800
                                                                                                                )
                                                                                                                Surface(
                                                                                                                        shape =
                                                                                                                                RoundedCornerShape(
                                                                                                                                        4.dp
                                                                                                                                ),
                                                                                                                        color =
                                                                                                                                if (emp.role ==
                                                                                                                                                "MANAGER"
                                                                                                                                )
                                                                                                                                        SuccessContainer
                                                                                                                                else
                                                                                                                                        Cyan100
                                                                                                                ) {
                                                                                                                        Text(
                                                                                                                                text =
                                                                                                                                        emp.role,
                                                                                                                                fontSize =
                                                                                                                                        11.sp,
                                                                                                                                fontWeight =
                                                                                                                                        FontWeight
                                                                                                                                                .Bold,
                                                                                                                                color =
                                                                                                                                        if (emp.role ==
                                                                                                                                                        "MANAGER"
                                                                                                                                        )
                                                                                                                                                SuccessBase
                                                                                                                                        else
                                                                                                                                                Cyan800,
                                                                                                                                modifier =
                                                                                                                                        Modifier.padding(
                                                                                                                                                horizontal =
                                                                                                                                                        6.dp,
                                                                                                                                                vertical =
                                                                                                                                                        2.dp
                                                                                                                                        )
                                                                                                                        )
                                                                                                                }
                                                                                                        }
                                                                                                }
                                                                                                Icon(
                                                                                                        Icons.Default
                                                                                                                .ChevronRight,
                                                                                                        contentDescription =
                                                                                                                null,
                                                                                                        tint =
                                                                                                                Neutral500
                                                                                                )
                                                                                        }
                                                                                }
                                                                        }
                                                                }
                                                        }

                                                        // ── TAHAP 3: PIN INPUT
                                                        // ───────────────────────────────
                                                        LoginStep.INPUT_PIN -> {
                                                                val currentEmp =
                                                                        selectedEmployee
                                                                                ?: employees.first()

                                                                Row(
                                                                        modifier =
                                                                                Modifier.fillMaxWidth(),
                                                                        verticalAlignment =
                                                                                Alignment
                                                                                        .CenterVertically
                                                                ) {
                                                                        IconButton(
                                                                                onClick = {
                                                                                        currentStep =
                                                                                                LoginStep
                                                                                                        .SELECT_USER
                                                                                        pin = ""
                                                                                        errorMsg =
                                                                                                null
                                                                                }
                                                                        ) {
                                                                                Icon(
                                                                                        imageVector =
                                                                                                Icons.AutoMirrored
                                                                                                        .Filled
                                                                                                        .ArrowBack,
                                                                                        contentDescription =
                                                                                                "Kembali ke Pilih Pengguna",
                                                                                        tint =
                                                                                                Neutral600
                                                                                )
                                                                        }
                                                                        Spacer(Modifier.width(6.dp))
                                                                        Column {
                                                                                Text(
                                                                                        text =
                                                                                                "Masukkan PIN",
                                                                                        fontSize =
                                                                                                20.sp,
                                                                                        fontWeight =
                                                                                                FontWeight
                                                                                                        .Bold,
                                                                                        color =
                                                                                                Neutral800
                                                                                )
                                                                                Text(
                                                                                        text =
                                                                                                "${currentEmp.name} (${currentEmp.role})",
                                                                                        fontSize =
                                                                                                13.sp,
                                                                                        color =
                                                                                                Cyan700,
                                                                                        fontWeight =
                                                                                                FontWeight
                                                                                                        .SemiBold
                                                                                )
                                                                        }
                                                                }

                                                                Spacer(Modifier.height(14.dp))

                                                                // 4 Pin Dots Indicator
                                                                Row(
                                                                        horizontalArrangement =
                                                                                Arrangement
                                                                                        .spacedBy(
                                                                                                16.dp
                                                                                        ),
                                                                        modifier =
                                                                                Modifier.padding(
                                                                                        vertical =
                                                                                                10.dp
                                                                                )
                                                                ) {
                                                                        repeat(4) { idx ->
                                                                                val isFilled =
                                                                                        idx <
                                                                                                pin.length
                                                                                Box(
                                                                                        modifier =
                                                                                                Modifier.size(
                                                                                                                18.dp
                                                                                                        )
                                                                                                        .background(
                                                                                                                if (isFilled
                                                                                                                )
                                                                                                                        Cyan600
                                                                                                                else
                                                                                                                        Neutral100,
                                                                                                                CircleShape
                                                                                                        )
                                                                                                        .border(
                                                                                                                1.5.dp,
                                                                                                                if (isFilled
                                                                                                                )
                                                                                                                        Cyan700
                                                                                                                else
                                                                                                                        Neutral300,
                                                                                                                CircleShape
                                                                                                        )
                                                                                )
                                                                        }
                                                                }

                                                                if (errorMsg != null) {
                                                                        Text(
                                                                                text = errorMsg!!,
                                                                                color = ErrorBase,
                                                                                fontSize = 12.sp,
                                                                                fontWeight =
                                                                                        FontWeight
                                                                                                .Bold,
                                                                                textAlign =
                                                                                        TextAlign
                                                                                                .Center,
                                                                                modifier =
                                                                                        Modifier.padding(
                                                                                                vertical =
                                                                                                        4.dp
                                                                                        )
                                                                        )
                                                                } else {
                                                                        Spacer(
                                                                                Modifier.height(
                                                                                        18.dp
                                                                                )
                                                                        )
                                                                }

                                                                // Keypad Numerik (1-9, 0,
                                                                // Backspace)
                                                                val keypadButtons =
                                                                        listOf(
                                                                                "1",
                                                                                "2",
                                                                                "3",
                                                                                "4",
                                                                                "5",
                                                                                "6",
                                                                                "7",
                                                                                "8",
                                                                                "9",
                                                                                "",
                                                                                "0",
                                                                                "delete"
                                                                        )
                                                                Column(
                                                                        verticalArrangement =
                                                                                Arrangement
                                                                                        .spacedBy(
                                                                                                8.dp
                                                                                        ),
                                                                        horizontalAlignment =
                                                                                Alignment
                                                                                        .CenterHorizontally
                                                                ) {
                                                                        for (row in 0 until 4) {
                                                                                Row(
                                                                                        horizontalArrangement =
                                                                                                Arrangement
                                                                                                        .spacedBy(
                                                                                                                10.dp
                                                                                                        )
                                                                                ) {
                                                                                        for (col in
                                                                                                0 until
                                                                                                        3) {
                                                                                                val idx =
                                                                                                        row *
                                                                                                                3 +
                                                                                                                col
                                                                                                val key =
                                                                                                        keypadButtons[
                                                                                                                idx]
                                                                                                if (key.isNotEmpty()
                                                                                                ) {
                                                                                                        val isDel =
                                                                                                                key ==
                                                                                                                        "delete"
                                                                                                        Surface(
                                                                                                                modifier =
                                                                                                                        Modifier.size(
                                                                                                                                        width =
                                                                                                                                                68.dp,
                                                                                                                                        height =
                                                                                                                                                48.dp
                                                                                                                                )
                                                                                                                                .clip(
                                                                                                                                        RoundedCornerShape(
                                                                                                                                                10.dp
                                                                                                                                        )
                                                                                                                                )
                                                                                                                                .clickable {
                                                                                                                                        if (isDel
                                                                                                                                        ) {
                                                                                                                                                if (pin.isNotEmpty()
                                                                                                                                                ) {
                                                                                                                                                        pin =
                                                                                                                                                                pin.dropLast(
                                                                                                                                                                        1
                                                                                                                                                                )
                                                                                                                                                        errorMsg =
                                                                                                                                                                null
                                                                                                                                                }
                                                                                                                                        } else {
                                                                                                                                                if (pin.length <
                                                                                                                                                                4
                                                                                                                                                ) {
                                                                                                                                                        val nextPin =
                                                                                                                                                                pin +
                                                                                                                                                                        key
                                                                                                                                                        pin =
                                                                                                                                                                nextPin
                                                                                                                                                        errorMsg =
                                                                                                                                                                null
                                                                                                                                                        if (nextPin.length ==
                                                                                                                                                                        4
                                                                                                                                                        ) {
                                                                                                                                                                if (nextPin ==
                                                                                                                                                                                currentEmp
                                                                                                                                                                                        .pin
                                                                                                                                                                ) {
                                                                                                                                                                        onLoginSuccess(
                                                                                                                                                                                currentEmp
                                                                                                                                                                        )
                                                                                                                                                                } else {
                                                                                                                                                                        errorMsg =
                                                                                                                                                                                "PIN salah! Silakan coba lagi."
                                                                                                                                                                        pin =
                                                                                                                                                                                ""
                                                                                                                                                                }
                                                                                                                                                        }
                                                                                                                                                }
                                                                                                                                        }
                                                                                                                                },
                                                                                                                color =
                                                                                                                        if (isDel
                                                                                                                        )
                                                                                                                                ErrorContainer
                                                                                                                        else
                                                                                                                                White,
                                                                                                                border =
                                                                                                                        BorderStroke(
                                                                                                                                1.dp,
                                                                                                                                if (isDel
                                                                                                                                )
                                                                                                                                        ErrorBorder
                                                                                                                                else
                                                                                                                                        Neutral300
                                                                                                                        )
                                                                                                        ) {
                                                                                                                Box(
                                                                                                                        contentAlignment =
                                                                                                                                Alignment
                                                                                                                                        .Center
                                                                                                                ) {
                                                                                                                        if (isDel
                                                                                                                        ) {
                                                                                                                                Icon(
                                                                                                                                        imageVector =
                                                                                                                                                Icons.AutoMirrored
                                                                                                                                                        .Filled
                                                                                                                                                        .Backspace,
                                                                                                                                        contentDescription =
                                                                                                                                                "Hapus",
                                                                                                                                        tint =
                                                                                                                                                ErrorBase,
                                                                                                                                        modifier =
                                                                                                                                                Modifier.size(
                                                                                                                                                        20.dp
                                                                                                                                                )
                                                                                                                                )
                                                                                                                        } else {
                                                                                                                                Text(
                                                                                                                                        text =
                                                                                                                                                key,
                                                                                                                                        fontSize =
                                                                                                                                                20.sp,
                                                                                                                                        fontWeight =
                                                                                                                                                FontWeight
                                                                                                                                                        .Bold,
                                                                                                                                        color =
                                                                                                                                                Neutral800
                                                                                                                                )
                                                                                                                        }
                                                                                                                }
                                                                                                        }
                                                                                                } else {
                                                                                                        Spacer(
                                                                                                                Modifier.size(
                                                                                                                        width =
                                                                                                                                68.dp,
                                                                                                                        height =
                                                                                                                                48.dp
                                                                                                                )
                                                                                                        )
                                                                                                }
                                                                                        }
                                                                                }
                                                                        }
                                                                }

                                                                Spacer(Modifier.height(14.dp))

                                                                // Petunjuk PIN
                                                                Text(
                                                                        text =
                                                                                "Petunjuk PIN: Manager (Budi) = 0000 | Kasir / Koki = 1234",
                                                                        fontSize = 11.sp,
                                                                        color = Gray600,
                                                                        textAlign = TextAlign.Center
                                                                )

                                                                Spacer(Modifier.height(4.dp))

                                                                TextButton(
                                                                        onClick = {
                                                                                currentStep =
                                                                                        LoginStep
                                                                                                .SELECT_USER
                                                                                pin = ""
                                                                                errorMsg = null
                                                                        }
                                                                ) {
                                                                        Text(
                                                                                "Bukan ${currentEmp.name}? Ganti Pengguna",
                                                                                color = Cyan700,
                                                                                fontSize = 12.sp
                                                                        )
                                                                }
                                                        }
                                                }
                                        }
                                }
                        }
                }
        }
}
