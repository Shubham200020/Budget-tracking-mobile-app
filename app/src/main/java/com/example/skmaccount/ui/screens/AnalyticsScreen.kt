package com.example.skmaccount.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.skmaccount.ui.theme.CoralRed
import com.example.skmaccount.ui.theme.EmeraldGreen
import com.example.skmaccount.ui.theme.SurfaceDark
import com.example.skmaccount.ui.theme.CategoryHelper
import com.example.skmaccount.ui.viewmodel.FinanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(viewModel: FinanceViewModel) {
    val categories by viewModel.allCategories.collectAsState()
    val categoryBreakdowns by viewModel.categoryBreakdowns.collectAsState()
    val sixMonthTrend by viewModel.sixMonthTrend.collectAsState()
    val currentMonth by viewModel.currentMonthSelected.collectAsState()

    val formatter = java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale.getDefault())
    val currentMonthText = formatter.format(currentMonth.time)

    // Build pie slices from category breakdown data
    val totalSpent = categoryBreakdowns.values.sum().coerceAtLeast(1.0)
    val pieSlices = categoryBreakdowns.entries.mapIndexed { index, entry ->
        val category = categories.find { it.id == entry.key }
        val color = try {
            Color(android.graphics.Color.parseColor(category?.colorHex ?: "#607D8B"))
        } catch (e: Exception) {
            categoryColors[index % categoryColors.size]
        }
        PieSlice(
            label = category?.name ?: "Other",
            amount = entry.value,
            percentage = (entry.value / totalSpent * 100).toFloat(),
            color = color
        )
    }.sortedByDescending { it.amount }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analytics", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Month label
            item {
                Text(
                    text = currentMonthText,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            // ── PIE CHART ──
            item {
                SectionHeader(title = "Spending by Category")
                Spacer(Modifier.height(12.dp))

                if (pieSlices.isEmpty()) {
                    EmptyChartState(message = "No spending data this month")
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            DonutChart(
                                slices = pieSlices,
                                modifier = Modifier.size(200.dp)
                            )
                            Spacer(Modifier.height(24.dp))
                            // Legend
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                pieSlices.forEach { slice ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(12.dp)
                                                    .clip(CircleShape)
                                                    .background(slice.color)
                                            )
                                            Text(slice.label, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
                                        }
                                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                            Text(
                                                CategoryHelper.formatCurrency(slice.amount),
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onBackground
                                            )
                                            Text(
                                                "${String.format("%.1f", slice.percentage)}%",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── BAR CHART: 6-Month Trend ──
            item {
                SectionHeader(title = "6-Month Trend")
                Spacer(Modifier.height(12.dp))

                val hasData = sixMonthTrend.any { it.income > 0 || it.spent > 0 }
                if (!hasData) {
                    EmptyChartState(message = "Add expenses across multiple months to see your trend")
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            // Legend
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.padding(bottom = 16.dp)
                            ) {
                                LegendDot(color = EmeraldGreen, label = "Income")
                                LegendDot(color = CoralRed, label = "Expenses")
                            }
                            BarChart(
                                data = sixMonthTrend,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                            )
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

// ── DATA CLASSES ──
data class PieSlice(val label: String, val amount: Double, val percentage: Float, val color: Color)

// ── DONUT CHART ──
@Composable
fun DonutChart(slices: List<PieSlice>, modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = size.minDimension * 0.15f
            val radius = (size.minDimension - strokeWidth) / 2
            val topLeft = Offset((size.width - radius * 2) / 2, (size.height - radius * 2) / 2)
            val arcSize = Size(radius * 2, radius * 2)

            var startAngle = -90f
            slices.forEach { slice ->
                val sweep = slice.percentage / 100f * 360f
                drawArc(
                    color = slice.color,
                    startAngle = startAngle,
                    sweepAngle = sweep - 2f, // small gap between slices
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                )
                startAngle += sweep
            }
        }
        // Center text
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = CategoryHelper.formatCurrency(slices.sumOf { it.amount }),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Total",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.6f)
            )
        }
    }
}

// ── BAR CHART ──
@Composable
fun BarChart(
    data: List<FinanceViewModel.MonthSummary>,
    modifier: Modifier = Modifier
) {
    val maxVal = data.maxOf { maxOf(it.income, it.spent) }.coerceAtLeast(1f)

    Column(modifier = modifier) {
        // Bars
        Row(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            data.forEach { month ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        // Income bar
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(month.income / maxVal)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(EmeraldGreen.copy(alpha = 0.85f))
                        )
                        // Expense bar
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(month.spent / maxVal)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(CoralRed.copy(alpha = 0.85f))
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(6.dp))

        // X-axis labels
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            data.forEach { month ->
                Text(
                    text = month.label,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ── HELPERS ──
@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
fun EmptyChartState(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
            Text(message, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
    }
}

// Default palette fallback
private val categoryColors = listOf(
    Color(0xFFFF5722), Color(0xFF2196F3), Color(0xFFE91E63),
    Color(0xFF4CAF50), Color(0xFF9C27B0), Color(0xFF00BCD4),
    Color(0xFFFF9800), Color(0xFF607D8B)
)
