package com.example.moneymanager.ui.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.startAxis
import com.patrykandpatrick.vico.core.entry.entryOf
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.horizontal.bottomAxis
import androidx.compose.runtime.remember
import com.github.mikephil.charting.components.Legend
import android.graphics.Color
import androidx.compose.ui.platform.LocalContext
import android.view.ViewGroup
import com.github.mikephil.charting.formatter.PercentFormatter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.toArgb





/* ---------------- PIE CHART (MPAndroidChart) ---------------- */
@Composable
fun CategoryPieChart(
    categoryData: Map<String, Float>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // ✅ CORRECT SOURCE OF COLOR
    val labelColor = MaterialTheme.colorScheme.onBackground.toArgb()

    AndroidView(
        modifier = modifier,
        factory = {
            PieChart(context).apply {

                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    700
                )

                description.isEnabled = false
                isDrawHoleEnabled = false
                setDrawEntryLabels(false)

                isRotationEnabled = false
                setBackgroundColor(Color.TRANSPARENT)

                setExtraOffsets(16f, 16f, 16f, 16f)
            }
        },
        update = { chart ->

            chart.legend.apply {
                verticalAlignment = Legend.LegendVerticalAlignment.CENTER
                horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
                orientation = Legend.LegendOrientation.VERTICAL
                setDrawInside(false)

                textColor = labelColor
                textSize = 13f

                form = Legend.LegendForm.CIRCLE
                formSize = 10f

                isWordWrapEnabled = true
                maxSizePercent = 0.45f
            }

            chart.clear()

            val entries = categoryData
                .filter { it.value > 0f }
                .map { PieEntry(it.value, it.key) }

            val dataSet = PieDataSet(entries, "").apply {
                sliceSpace = 1f
                setDrawValues(false)

                colors = listOf(
                    Color.parseColor("#2196F3"),
                    Color.parseColor("#03D9F4"),
                    Color.parseColor("#FF5722"),
                    Color.parseColor("#FFC107"),
                    Color.parseColor("#9C27B0"),
                    Color.parseColor("#F44336"),
                    Color.parseColor("#4CAF50"),
                    Color.parseColor("#607D8B")
                )
            }

            chart.data = PieData(dataSet)
            chart.invalidate()
        }
    )
}

/* ---------------------------------------------------------
   MONTHLY BAR CHART
--------------------------------------------------------- */

@Composable
fun MonthlyBarChart(
    monthlyData: List<Pair<String, Float>>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // ✅ SAME FIX HERE
    val labelColor = MaterialTheme.colorScheme.onBackground.toArgb()

    AndroidView(
        modifier = modifier,
        factory = {
            BarChart(context).apply {

                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    600
                )

                description.isEnabled = false
                setDrawGridBackground(false)
                setFitBars(true)
                axisRight.isEnabled = false
                legend.isEnabled = false

                setBackgroundColor(Color.TRANSPARENT)
                setExtraOffsets(16f, 16f, 16f, 16f)
            }
        },
        update = { chart ->

            chart.axisLeft.apply {
                axisMinimum = 0f
                textColor = labelColor
                gridColor = Color.GRAY
                setDrawGridLines(true)
            }

            chart.xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                textColor = labelColor
                setDrawGridLines(false)
            }

            val entries = monthlyData.mapIndexed { index, pair ->
                BarEntry(index.toFloat(), pair.second)
            }

            val dataSet = BarDataSet(entries, "").apply {
                color = Color.parseColor("#4CAF50")
                valueTextSize = 13f
                setValueTextColor(labelColor)
            }

            chart.data = BarData(dataSet).apply {
                barWidth = 0.6f
            }

            chart.xAxis.valueFormatter =
                IndexAxisValueFormatter(monthlyData.map { it.first })

            chart.invalidate()
        }
    )
}