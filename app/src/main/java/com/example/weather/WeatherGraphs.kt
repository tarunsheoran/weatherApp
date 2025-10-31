package com.example.weather

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter

class WeatherGraphs : AppCompatActivity() {

    private lateinit var lineChart: LineChart
    private lateinit var pieChart: PieChart
    private lateinit var forecastAccuracyText: TextView
    private lateinit var forecastMaxMinText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            enableEdgeToEdge()
            setContentView(R.layout.activity_weather_graphs)

            // Set up edge-to-edge display
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.titleTextView)) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }

            // Initialize charts and text views with the correct IDs from your XML
            lineChart = findViewById(R.id.weatherLineChart)
            pieChart = findViewById(R.id.weatherPieChart)
            forecastAccuracyText = findViewById(R.id.accuracyText)
            forecastMaxMinText = findViewById(R.id.maxMinText)

            // Get the passed data from MainActivity
            val temperature = intent.getFloatExtra("temperature", 20f)
            val humidity = intent.getFloatExtra("humidity", 50f)
            val seaLevel = intent.getFloatExtra("seaLevel", 1013f)

            // Get prediction data
            val nextDayTemp = intent.getFloatExtra("nextDayTemp", temperature + 1f)
            val dayAfterTemp = intent.getFloatExtra("dayAfterTemp", temperature + 2f)
            val maxTemp = intent.getFloatExtra("maxTemp", temperature + 3f)
            val minTemp = intent.getFloatExtra("minTemp", temperature - 2f)
            val accuracy = intent.getFloatExtra("accuracy", 80f)

            // Update text views
            forecastAccuracyText.text = "Forecast Accuracy: ${accuracy.toInt()}%"
            forecastMaxMinText.text = "Max: ${maxTemp.toInt()}°C | Min: ${minTemp.toInt()}°C"

            // Setup charts
            setupLineChart(temperature, nextDayTemp, dayAfterTemp, maxTemp, minTemp)
            setupPieChart(temperature, nextDayTemp, dayAfterTemp)
        } catch (e: Exception) {
            Log.e("WeatherGraphs", "Error in onCreate: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun setupLineChart(currentTemp: Float, nextDayTemp: Float, dayAfterTemp: Float, maxTemp: Float, minTemp: Float) {
        try {
            // Ensure we have valid data
            if (currentTemp <= 0f && nextDayTemp <= 0f && dayAfterTemp <= 0f) {
                Log.e("setupLineChart", "Invalid temperature data")
                return
            }

            // Create entries for the 3-day forecast
            val entries = ArrayList<Entry>()
            entries.add(Entry(0f, currentTemp))
            entries.add(Entry(1f, nextDayTemp))
            entries.add(Entry(2f, dayAfterTemp))

            // Create the dataset
            val dataSet = LineDataSet(entries, "Temperature Forecast (°C)").apply {
                color = ContextCompat.getColor(this@WeatherGraphs, R.color.temperatureColor)
                valueTextColor = Color.BLACK
                valueTextSize = 12f
                lineWidth = 2f
                setDrawCircles(true)
                circleRadius = 6f
                circleHoleRadius = 3f
                setDrawFilled(true)
                fillColor = ContextCompat.getColor(this@WeatherGraphs, R.color.temperatureColor)
                fillAlpha = 50
                setDrawValues(true)
            }

            // Set up the line chart
            lineChart.apply {
                data = LineData(dataSet)
                description.isEnabled = false
                legend.isEnabled = true

                // Add max and min lines
                val maxLine = LineDataSet(listOf(Entry(0f, maxTemp), Entry(2f, maxTemp)), "Max Temp")
                maxLine.color = Color.RED
                maxLine.lineWidth = 1f
                maxLine.setDrawCircles(false)
                maxLine.enableDashedLine(10f, 5f, 0f)

                val minLine = LineDataSet(listOf(Entry(0f, minTemp), Entry(2f, minTemp)), "Min Temp")
                minLine.color = Color.BLUE
                minLine.lineWidth = 1f
                minLine.setDrawCircles(false)
                minLine.enableDashedLine(10f, 5f, 0f)

                data.addDataSet(maxLine)
                data.addDataSet(minLine)

                // X-axis setup
                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    granularity = 1f
                    valueFormatter = IndexAxisValueFormatter(listOf("Today", "Tomorrow", "Day After"))
                }

                // Other chart configurations
                setTouchEnabled(true)
                isDragEnabled = true
                setScaleEnabled(true)
                setPinchZoom(true)

                // Animation
                animateX(1500)

                // Refresh the chart
                invalidate()
            }
        } catch (e: Exception) {
            Log.e("setupLineChart", "Error: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun setupPieChart(currentTemp: Float, nextDayTemp: Float, dayAfterTemp: Float) {
        try {
            // Ensure we have valid data
            if (currentTemp <= 0f && nextDayTemp <= 0f && dayAfterTemp <= 0f) {
                Log.e("setupPieChart", "Invalid temperature data")
                return
            }

            // Make sure all values are positive for pie chart
            val today = Math.abs(currentTemp) + 10f  // Add offset to ensure positive values
            val tomorrow = Math.abs(nextDayTemp) + 10f
            val dayAfter = Math.abs(dayAfterTemp) + 10f

            // Create pie chart entries
            val pieEntries = ArrayList<PieEntry>()
            pieEntries.add(PieEntry(today, "Today"))
            pieEntries.add(PieEntry(tomorrow, "Tomorrow"))
            pieEntries.add(PieEntry(dayAfter, "Day After"))

            // Create dataset for pie chart
            val pieDataSet = PieDataSet(pieEntries, "Temperature Distribution").apply {
                colors = listOf(
                    ContextCompat.getColor(this@WeatherGraphs, R.color.temperatureColor),
                    ContextCompat.getColor(this@WeatherGraphs, R.color.humidityColor),
                    ContextCompat.getColor(this@WeatherGraphs, R.color.seaLevelColor)
                )
                valueTextSize = 14f
                valueTextColor = Color.WHITE
                sliceSpace = 3f
            }

            // Configure the pie chart
            pieChart.apply {
                data = PieData(pieDataSet)
                description.isEnabled = false
                isDrawHoleEnabled = true
                setHoleColor(Color.WHITE)
                holeRadius = 35f
                transparentCircleRadius = 40f
                setUsePercentValues(true)
                setEntryLabelColor(Color.WHITE)
                setEntryLabelTextSize(12f)

                // Ensure legend is visible
                legend.isEnabled = true

                // Add some animation
                animateY(1500)

                // Refresh the chart
                invalidate()
            }
        } catch (e: Exception) {
            Log.e("setupPieChart", "Error: ${e.message}")
            e.printStackTrace()
        }
    }
}