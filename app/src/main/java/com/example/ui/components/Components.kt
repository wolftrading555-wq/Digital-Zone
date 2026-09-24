package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberGold
import com.example.ui.theme.AmberGoldDark
import com.example.ui.theme.CrimsonDanger
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.SkyBlueAccent
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateSurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MonthlyChartPoint

@Composable
fun DashboardMetricCard(
    title: String,
    value: String,
    subtitle: String? = null,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    testTag: String = "",
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .testTag(testTag)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SlateSurfaceCard),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(SlateBorder, accentColor.copy(alpha = 0.3f))))
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    letterSpacing = (-0.5).sp
                ),
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (subtitle != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = accentColor,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun SalesRecoveryMultiLineChart(
    data: List<MonthlyChartPoint>,
    modifier: Modifier = Modifier,
    selectedIndex: Int? = null,
    onSelectIndex: (Int?) -> Unit = {}
) {
    val maxVal = 1600000.0 // 1.6M PKR scale

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        // Cartesian Background Grid & Y-Axis Labels (Recharts CartesianGrid)
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("1.6M", "1.2M", "800K", "400K", "0").forEach { label ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        modifier = Modifier.width(38.dp),
                        textAlign = TextAlign.End
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(1.dp)
                            .background(SlateBorder.copy(alpha = 0.45f))
                    )
                }
            }
        }

        // Multi-Line Canvas (Spline curves + Area gradient + Node markers + Scrub indicator)
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 46.dp, end = 12.dp, bottom = 10.dp, top = 10.dp)
                .pointerInput(data) {
                    detectTapGestures { offset ->
                        val count = data.size
                        if (count > 1) {
                            val step = size.width / (count - 1)
                            val idx = ((offset.x + (step / 2f)) / step).toInt().coerceIn(0, data.lastIndex)
                            onSelectIndex(if (selectedIndex == idx) null else idx)
                        }
                    }
                }
                .pointerInput(data) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val count = data.size
                            if (count > 1) {
                                val step = size.width / (count - 1)
                                val idx = ((offset.x + (step / 2f)) / step).toInt().coerceIn(0, data.lastIndex)
                                onSelectIndex(idx)
                            }
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            val count = data.size
                            if (count > 1) {
                                val step = size.width / (count - 1)
                                val idx = ((change.position.x + (step / 2f)) / step).toInt().coerceIn(0, data.lastIndex)
                                onSelectIndex(idx)
                            }
                        }
                    )
                }
        ) {
            val count = data.size
            if (count < 2) return@Canvas

            val stepX = size.width / (count - 1)
            val chartHeight = size.height

            val salesPoints = mutableListOf<Offset>()
            val recoveryPoints = mutableListOf<Offset>()

            data.forEachIndexed { i, point ->
                val x = i * stepX
                val ySales = (chartHeight - ((point.sales / maxVal) * chartHeight).toFloat()).coerceIn(0f, chartHeight)
                val yRecovery = (chartHeight - ((point.recovery / maxVal) * chartHeight).toFloat()).coerceIn(0f, chartHeight)
                salesPoints.add(Offset(x, ySales))
                recoveryPoints.add(Offset(x, yRecovery))
            }

            // Function to generate smooth cubic bezier spline curve path
            fun buildSplinePath(points: List<Offset>, closeToBottom: Boolean = false): Path {
                val path = Path()
                if (points.isEmpty()) return path
                path.moveTo(points.first().x, points.first().y)
                for (i in 0 until points.size - 1) {
                    val p0 = points[i]
                    val p1 = points[i + 1]
                    val cx1 = (p0.x + p1.x) / 2f
                    val cy1 = p0.y
                    val cx2 = (p0.x + p1.x) / 2f
                    val cy2 = p1.y
                    path.cubicTo(cx1, cy1, cx2, cy2, p1.x, p1.y)
                }
                if (closeToBottom && points.isNotEmpty()) {
                    path.lineTo(points.last().x, chartHeight)
                    path.lineTo(points.first().x, chartHeight)
                    path.close()
                }
                return path
            }

            // 1. Subtle Area Fill Gradients under lines (Recharts Area/Line style)
            val salesAreaPath = buildSplinePath(salesPoints, closeToBottom = true)
            drawPath(
                path = salesAreaPath,
                brush = Brush.verticalGradient(
                    colors = listOf(AmberGold.copy(alpha = 0.22f), AmberGold.copy(alpha = 0.02f)),
                    startY = 0f,
                    endY = chartHeight
                )
            )

            val recoveryAreaPath = buildSplinePath(recoveryPoints, closeToBottom = true)
            drawPath(
                path = recoveryAreaPath,
                brush = Brush.verticalGradient(
                    colors = listOf(EmeraldSuccess.copy(alpha = 0.18f), EmeraldSuccess.copy(alpha = 0.01f)),
                    startY = 0f,
                    endY = chartHeight
                )
            )

            // 2. Active Scrub / Hover Vertical Guideline
            selectedIndex?.let { idx ->
                if (idx in 0 until count) {
                    val scrubX = idx * stepX
                    drawLine(
                        color = Color.White.copy(alpha = 0.4f),
                        start = Offset(scrubX, 0f),
                        end = Offset(scrubX, chartHeight),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                }
            }

            // 3. Draw Multi-Line Strokes (monotone bezier)
            val salesLinePath = buildSplinePath(salesPoints, closeToBottom = false)
            drawPath(
                path = salesLinePath,
                color = AmberGold,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            val recoveryLinePath = buildSplinePath(recoveryPoints, closeToBottom = false)
            drawPath(
                path = recoveryLinePath,
                color = EmeraldSuccess,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            // 4. Draw Dot Nodes at Coordinates (Recharts dot & activeDot)
            salesPoints.forEachIndexed { idx, pt ->
                val isSelected = selectedIndex == idx
                if (isSelected) {
                    // Outer glow halo
                    drawCircle(
                        color = AmberGold.copy(alpha = 0.35f),
                        radius = 9.dp.toPx(),
                        center = pt
                    )
                }
                drawCircle(
                    color = AmberGold,
                    radius = if (isSelected) 6.dp.toPx() else 4.dp.toPx(),
                    center = pt
                )
                drawCircle(
                    color = Color.White,
                    radius = if (isSelected) 3.5.dp.toPx() else 2.dp.toPx(),
                    center = pt
                )
            }

            recoveryPoints.forEachIndexed { idx, pt ->
                val isSelected = selectedIndex == idx
                if (isSelected) {
                    // Outer glow halo
                    drawCircle(
                        color = EmeraldSuccess.copy(alpha = 0.35f),
                        radius = 9.dp.toPx(),
                        center = pt
                    )
                }
                drawCircle(
                    color = EmeraldSuccess,
                    radius = if (isSelected) 6.dp.toPx() else 4.dp.toPx(),
                    center = pt
                )
                drawCircle(
                    color = Color.White,
                    radius = if (isSelected) 3.5.dp.toPx() else 2.dp.toPx(),
                    center = pt
                )
            }
        }
    }
}

@Composable
fun SalesRecoveryChart(
    data: List<MonthlyChartPoint>,
    modifier: Modifier = Modifier
) {
    var chartType by remember { mutableStateOf("line") } // "line" (Recharts Multi-Line) vs "bar"
    var selectedIndex by remember { mutableStateOf<Int?>(5) } // Default to Sep highlighted
    val maxVal = 1600000.0 // 1.6M PKR scale

    Card(
        modifier = modifier.testTag("sales_recovery_chart"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SlateSurfaceCard),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.verticalGradient(listOf(SlateBorder, SlateBorder.copy(alpha = 0.4f)))
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row with Title, Chart Type Toggle, and Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Sales & Recovery Trends",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = AmberGold.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = if (chartType == "line") "MULTI-LINE" else "BARS",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = AmberGold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
                    Text(
                        text = "Monthly comparative performance in PKR",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }

                // Type Toggle (Line vs Bar)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (chartType == "line") AmberGold else Color.Transparent)
                            .clickable { chartType = "line" }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("chart_toggle_line")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShowChart,
                            contentDescription = "Multi-Line Chart",
                            tint = if (chartType == "line") Color.Black else TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (chartType == "bar") AmberGold else Color.Transparent)
                            .clickable { chartType = "bar" }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("chart_toggle_bar")
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = "Bar Chart",
                            tint = if (chartType == "bar") Color.Black else TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Recharts Legend Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(AmberGold))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Sales", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                }

                Spacer(modifier = Modifier.width(14.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(EmeraldSuccess))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Recovery", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Chart Rendering area: Multi-Line or Bar
            if (chartType == "line") {
                SalesRecoveryMultiLineChart(
                    data = data,
                    selectedIndex = selectedIndex,
                    onSelectIndex = { selectedIndex = it },
                    modifier = Modifier.testTag("sales_recovery_multiline_chart")
                )
            } else {
                // Bar Chart rendering
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("1.6M", "1.2M", "800K", "400K", "0").forEach { label ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted,
                                    modifier = Modifier.width(38.dp),
                                    textAlign = TextAlign.End
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(1.dp)
                                        .background(SlateBorder.copy(alpha = 0.45f))
                                )
                            }
                        }
                    }

                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 46.dp, end = 12.dp, bottom = 10.dp, top = 6.dp)
                            .pointerInput(data) {
                                detectTapGestures { offset ->
                                    val step = size.width / data.size
                                    val index = (offset.x / step).toInt().coerceIn(0, data.lastIndex)
                                    selectedIndex = if (selectedIndex == index) null else index
                                }
                            }
                    ) {
                        val count = data.size
                        if (count == 0) return@Canvas
                        val groupWidth = size.width / count
                        val barWidth = (groupWidth * 0.32f).coerceAtMost(22.dp.toPx())
                        val barGap = 4.dp.toPx()

                        data.forEachIndexed { i, point ->
                            val groupCenterX = i * groupWidth + (groupWidth / 2)
                            val salesHeight = ((point.sales / maxVal) * size.height).toFloat().coerceIn(4f, size.height)
                            val recoveryHeight = ((point.recovery / maxVal) * size.height).toFloat().coerceIn(4f, size.height)

                            val salesLeft = groupCenterX - barWidth - (barGap / 2)
                            drawRoundRect(
                                color = AmberGold,
                                topLeft = Offset(salesLeft, size.height - salesHeight),
                                size = Size(barWidth, salesHeight),
                                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                            )

                            val recoveryLeft = groupCenterX + (barGap / 2)
                            drawRoundRect(
                                color = EmeraldSuccess,
                                topLeft = Offset(recoveryLeft, size.height - recoveryHeight),
                                size = Size(barWidth, recoveryHeight),
                                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                            )
                        }
                    }
                }
            }

            // X-Axis Month Labels
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 46.dp, end = 12.dp, top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                data.forEachIndexed { index, point ->
                    Text(
                        text = point.month,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (selectedIndex == index) FontWeight.ExtraBold else FontWeight.Normal,
                        color = if (selectedIndex == index) AmberGold else TextSecondary,
                        modifier = Modifier.clickable {
                            selectedIndex = if (selectedIndex == index) null else index
                        }
                    )
                }
            }

            // Recharts-style Tooltip Card (Month, Sales with Amber dot, Recovery with Emerald dot)
            selectedIndex?.let { idx ->
                val pt = data.getOrNull(idx)
                if (pt != null) {
                    val rate = if (pt.sales > 0) ((pt.recovery / pt.sales) * 100).toInt() else 0
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.horizontalGradient(listOf(SlateBorder, AmberGold.copy(alpha = 0.3f)))
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("chart_tooltip_card")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${pt.month} Month Trend Details",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Recovery Rate: $rate%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (rate >= 80) EmeraldSuccess else AmberGold
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(AmberGold))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Sales: Rs. ${(pt.sales / 1000).toInt()},000",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = AmberGold,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(EmeraldSuccess))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Recovery: Rs. ${(pt.recovery / 1000).toInt()},000",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = EmeraldSuccess,
                                        fontWeight = FontWeight.Bold
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

@Composable
fun SalesTeamPerformanceCard(
    name: String,
    percentage: Int,
    targetFormatted: String,
    currentSalesFormatted: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val progress = (percentage / 100f).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "progress")

    val progressColor = when {
        percentage >= 90 -> EmeraldSuccess
        percentage >= 75 -> AmberGold
        else -> SkyBlueAccent
    }

    Card(
        modifier = modifier
            .testTag("sales_officer_${name.lowercase()}")
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SlateSurfaceCard),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(SlateBorder, progressColor.copy(alpha = 0.25f))))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(progressColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = name.take(1),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = progressColor
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Target: $targetFormatted",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                // Percentage Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = progressColor.copy(alpha = 0.15f),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(progressColor.copy(alpha = 0.6f), progressColor.copy(alpha = 0.3f))))
                ) {
                    Text(
                        text = "$percentage%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = progressColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Linear Progress Bar
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = progressColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Achieved: $currentSalesFormatted",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
                Text(
                    text = "${100 - percentage}% to Target",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
        }
    }
}
