package com.tvbc.tvbcapps.ui.theme.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.min

@Composable
fun AbsensiPieChart(
    hadir: Int,
    totalKehadiran: Int,
    modifier: Modifier = Modifier
) {
    val tidakHadir = totalKehadiran - hadir
    val persentaseHadir = (hadir.toFloat() / totalKehadiran.toFloat() * 100).toInt()

    val data = listOf(
        PieChartData("Hadir", hadir.toFloat(), Color(0xFF006600)), // Warna hijau
        PieChartData("Tidak Hadir", tidakHadir.toFloat(), Color(0xFF660000)) // Warna merah maroon
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(250.dp)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.Center)
        ) {
            val total = data.sumOf { it.value.toDouble() }.toFloat()
            var startAngle = -90f

            data.forEach { pieData ->
                val sweepAngle = pieData.value / total * 360f

                // Gambar slice pie
                drawArc(
                    color = pieData.color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    size = Size(size.width, size.height)
                )

                // Gambar outline
                drawArc(
                    color = Color.White,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = 2f, cap = StrokeCap.Round),
                    size = Size(size.width, size.height)
                )

                startAngle += sweepAngle
            }

            // Jika ingin menggambar lingkaran putih di tengah
            val center = Offset(size.width / 2, size.height / 2)
            val radius = min(size.width, size.height) / 4
            drawCircle(
                color = Color.White,
                radius = radius,
                center = center
            )
        }

        // Tampilkan persentase di tengah
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$persentaseHadir%",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Kehadiran",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    // Legenda
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        data.forEach { pieData ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .padding(end = 4.dp)
                        .background(pieData.color, shape = CircleShape)
                )
                Text(
                    text = "${pieData.label} (${pieData.value.toInt()})",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

data class PieChartData(
    val label: String,
    val value: Float,
    val color: Color
)