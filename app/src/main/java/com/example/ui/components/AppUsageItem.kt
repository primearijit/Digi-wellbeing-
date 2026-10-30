package com.example.ui.components

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.data.db.MetricType
import com.example.data.model.AppUsageModel

@Composable
fun AppUsageItem(
    app: AppUsageModel,
    metricType: MetricType,
    onClickTimer: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .testTag("app_item_${app.packageName}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App Icon
        AppIconBadge(
            packageName = app.packageName,
            iconCategory = app.iconCategory,
            appName = app.appName
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Title and Usage text
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = app.appName,
                color = Color(0xFFE2E2E6),
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal
            )
            val metricValue = when (metricType) {
                MetricType.SCREEN_TIME -> app.screenTimeMinutes
                MetricType.NOTIFICATIONS -> app.notifications
                MetricType.TIMES_OPENED -> app.opens
            }
            Text(
                text = metricType.formatValue(metricValue),
                color = Color(0xFFC4C6D0),
                fontSize = 14.sp
            )
        }

        // Vertical divider and Hourglass/Timer icon
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .height(28.dp)
                    .width(1.dp)
                    .background(Color(0xFF383B46))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable { onClickTimer() }
                    .testTag("timer_button_${app.packageName}"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.HourglassEmpty,
                    contentDescription = "Set timer limit",
                    tint = Color(0xFFC4C6D0),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
fun AppIconBadge(
    packageName: String,
    iconCategory: String,
    appName: String
) {
    val context = LocalContext.current
    val realIconDrawable: Drawable? = remember(packageName) {
        try {
            if (packageName.isNotEmpty() && !packageName.startsWith("custom.pkg.")) {
                context.packageManager.getApplicationIcon(packageName)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    if (realIconDrawable != null) {
        Image(
            painter = rememberAsyncImagePainter(model = realIconDrawable),
            contentDescription = appName,
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
        )
    } else {
        val (bgColor, iconVector) = when (iconCategory.uppercase()) {
            "BRAVE" -> Pair(Color(0xFFFF5722), Icons.Default.Language)
            "GMAIL" -> Pair(Color(0xFFEA4335), Icons.Default.Email)
            "CHROME" -> Pair(Color(0xFF4285F4), Icons.Default.Public)
            "YOUTUBE" -> Pair(Color(0xFFFF0000), Icons.Default.PlayArrow)
            "INSTAGRAM" -> Pair(Color(0xFFE1306C), Icons.Default.CameraAlt)
            "WHATSAPP" -> Pair(Color(0xFF25D366), Icons.Default.Chat)
            "TELEGRAM" -> Pair(Color(0xFF0088CC), Icons.Default.Send)
            else -> Pair(Color(0xFF383B46), Icons.Default.Android)
        }

        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = iconVector,
                contentDescription = appName,
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}
