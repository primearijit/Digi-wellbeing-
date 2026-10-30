package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.MetricType
import com.example.data.model.DayChartBarModel

@Composable
fun UsageBarChart(
    bars: List<DayChartBarModel>,
    metricType: MetricType,
    onBarSelected: (String) -> Unit
) {
    if (bars.isEmpty()) return

    val textMeasurer = rememberTextMeasurer()

    // Fixed guidelines according to reference screenshot: 8h, 6h, 4h, 2h, 0h
    val maxChartUnits = if (metricType == MetricType.SCREEN_TIME) {
        480 // 8 hours in minutes
    } else {
        val maxVal = (bars.maxOfOrNull { it.totalScreenTimeMinutes } ?: 1).coerceAtLeast(100)
        maxVal + (maxVal / 4).coerceAtLeast(10)
    }

    val guideLines = if (metricType == MetricType.SCREEN_TIME) {
        listOf(
            480 to "8h",
            360 to "6h",
            240 to "4h",
            120 to "2h",
            0 to "0h"
        )
    } else {
        val step = maxChartUnits / 4
        listOf(
            maxChartUnits to "$maxChartUnits",
            step * 3 to "${step * 3}",
            step * 2 to "${step * 2}",
            step to "$step",
            0 to "0"
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .testTag("usage_bar_chart")
    ) {
        // Chart Canvas Area (Height = 160.dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(bars) {
                        detectTapGestures { offset ->
                            val labelWidth = 32.dp.toPx()
                            val chartWidth = size.width - labelWidth
                            if (offset.x in 0f..chartWidth && bars.isNotEmpty()) {
                                val slotWidth = chartWidth / bars.size
                                val index = (offset.x / slotWidth).toInt().coerceIn(0, bars.size - 1)
                                onBarSelected(bars[index].dateString)
                            }
                        }
                    }
            ) {
                val labelAreaWidth = 30.dp.toPx()
                val chartWidth = size.width - labelAreaWidth
                val chartHeight = size.height

                // 1. Draw horizontal guideline marks across chart
                guideLines.forEach { (valMins, label) ->
                    val ratio = valMins.toFloat() / maxChartUnits
                    val y = chartHeight - (ratio * chartHeight)

                    // Draw thin baseline guideline
                    drawLine(
                        color = Color(0xFF444752),
                        start = Offset(0f, y),
                        end = Offset(chartWidth, y),
                        strokeWidth = 1.dp.toPx()
                    )

                    // Draw right-aligned Y-axis label right on line level
                    val textLayoutResult = textMeasurer.measure(
                        text = label,
                        style = TextStyle(
                            color = Color(0xFFC4C6D0),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Normal
                        )
                    )
                    drawText(
                        textLayoutResult = textLayoutResult,
                        topLeft = Offset(
                            x = chartWidth + 6.dp.toPx(),
                            y = y - (textLayoutResult.size.height / 2f)
                        )
                    )
                }

                // 2. Draw 7 vertical bars
                if (bars.isNotEmpty()) {
                    val slotWidth = chartWidth / bars.size
                    val barWidth = (slotWidth * 0.65f).coerceAtMost(36.dp.toPx())

                    bars.forEachIndexed { index, bar ->
                        val isSelected = bar.isSelected
                        val isToday = bar.isToday

                        // 1:1 color matching from reference screenshot
                        val barColor = when {
                            isSelected -> Color(0xFF5383EC) // Active blue highlight
                            isToday -> Color(0xFFA8C7FA)
                            else -> Color(0xFFC4D8F9) // Pastel light blue for standard days
                        }

                        val mins = bar.totalScreenTimeMinutes
                        if (mins > 0) {
                            val ratio = (mins.toFloat() / maxChartUnits).coerceIn(0.015f, 1f)
                            val barHeight = ratio * chartHeight

                            val xCenter = (index * slotWidth) + (slotWidth / 2f)
                            val left = xCenter - (barWidth / 2f)
                            val top = chartHeight - barHeight

                            // Draw vertical bar with subtle top corner radius
                            drawRoundRect(
                                color = barColor,
                                topLeft = Offset(left, top),
                                size = Size(barWidth, barHeight),
                                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // X-Axis Day Labels (Sun, Mon, Tue, Wed, Thu, Fri, Sat)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 30.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            bars.forEach { bar ->
                Text(
                    text = bar.dayOfWeekLabel,
                    color = if (bar.isSelected) Color.White else Color(0xFFC4C6D0),
                    fontWeight = if (bar.isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 12.sp,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
