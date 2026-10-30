package com.example.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.MainActivity
import com.example.R
import com.example.data.db.MetricType
import com.example.data.repository.WellbeingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DigitalWellbeingWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = WellbeingRepository(context)
        val summary = repository.getSummaryForWidget()

        provideContent {
            GlanceTheme {
                WidgetContent(context = context, totalMinutes = summary.totalScreenTimeMinutes, topApps = summary.appUsages.take(3))
            }
        }
    }

    @Composable
    private fun WidgetContent(
        context: Context,
        totalMinutes: Int,
        topApps: List<com.example.data.model.AppUsageModel>
    ) {
        val totalFormatted = MetricType.SCREEN_TIME.formatShortValue(totalMinutes)

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(Color(0xFF1C1B1F))
                .padding(14.dp)
                .clickable(actionStartActivity<MainActivity>())
        ) {
            // Header Row
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Screen time",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFFC4C6D0)),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    modifier = GlanceModifier.defaultWeight()
                )
                Image(
                    provider = ImageProvider(R.drawable.ic_launcher_foreground),
                    contentDescription = "Refresh",
                    modifier = GlanceModifier.size(16.dp)
                )
            }

            Spacer(modifier = GlanceModifier.height(4.dp))

            // Total Hero Time
            Text(
                text = totalFormatted,
                style = TextStyle(
                    color = ColorProvider(Color.White),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = GlanceModifier.height(8.dp))

            // Top 3 Apps Stack
            Column(
                modifier = GlanceModifier.fillMaxWidth()
            ) {
                topApps.forEach { app ->
                    Row(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Time Bubble
                        Box(
                            modifier = GlanceModifier
                                .background(Color(0xFFD3E3FD))
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = MetricType.SCREEN_TIME.formatShortValue(app.screenTimeMinutes),
                                style = TextStyle(
                                    color = ColorProvider(Color(0xFF003062)),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }

                        Spacer(modifier = GlanceModifier.width(8.dp))

                        // App Name
                        Text(
                            text = app.appName,
                            style = TextStyle(
                                color = ColorProvider(Color(0xFFE2E2E6)),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }

    companion object {
        suspend fun updateWidget(context: Context) {
            withContext(Dispatchers.IO) {
                try {
                    DigitalWellbeingWidget().updateAll(context)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}

class DigitalWellbeingWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DigitalWellbeingWidget()
}
