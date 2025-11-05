package com.example.cattler

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.cattler.databinding.ActivityDetailBinding // ✅ ADDED: ViewBinding import
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DetailActivity : AppCompatActivity() {


    private lateinit var binding: ActivityDetailBinding



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val cowId = intent.getStringExtra("COW_ID")

        if (cowId != null) {
            supportActionBar?.title = "History for: $cowId"
            loadMockHistoryData(cowId)
            // fetchCattleHistory(cowId) // Uncomment for real data
        } else {
            Log.e("DetailActivity", "No COW_ID was passed to this activity.")
            finish()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun loadMockHistoryData(cowId: String) {
        // ✅ UPDATED: Use binding
        binding.progressBarDetail.visibility = View.VISIBLE
        binding.chartsContainer.visibility = View.INVISIBLE

        lifecycleScope.launch {
            delay(1000)
            val mockHistory = getMockHistoryForCow(cowId)

            if (mockHistory.isNotEmpty()) {
                setupTemperatureChart(mockHistory)
                setupDistanceChart(mockHistory)
            } else {
                Log.w("DetailActivity", "Received empty mock history for $cowId")
            }

            binding.progressBarDetail.visibility = View.GONE
            binding.chartsContainer.visibility = View.VISIBLE
        }
    }

    private fun getMockHistoryForCow(cowId: String): List<DataPoint> {
        return listOf(
            DataPoint("10:00", 38.5, 15),
            DataPoint("10:05", 38.6, 17),
            DataPoint("10:10", 38.5, 16),
            DataPoint("10:15", 38.7, 18),
            DataPoint("10:20", 38.8, 20)
        )
    }

    private fun fetchCattleHistory(cowId: String) {
        binding.progressBarDetail.visibility = View.VISIBLE
        binding.chartsContainer.visibility = View.INVISIBLE

        lifecycleScope.launch {
            try {
                val historyData = RetrofitInstance.api.getCattleHistory(cowId)

                if (historyData.isNotEmpty()) {
                    setupTemperatureChart(historyData)
                    setupDistanceChart(historyData)
                } else {
                    Log.w("DetailActivity", "Received empty history for $cowId")
                }

            } catch (e: Exception) {
                Log.e("DetailActivity", "Error fetching history: ${e.message}", e)
            } finally {
                binding.progressBarDetail.visibility = View.GONE
                binding.chartsContainer.visibility = View.VISIBLE
            }
        }
    }

    private fun setupTemperatureChart(history: List<DataPoint>) {
        val entries = history.mapIndexed { index, dataPoint ->
            Entry(index.toFloat(), dataPoint.temperature.toFloat())
        }
        val dataSet = LineDataSet(entries, "Temperature (°C)")
        dataSet.color = Color.RED
        dataSet.valueTextColor = Color.BLACK
        dataSet.setCircleColor(dataSet.color)
        dataSet.lineWidth = 2.5f
        dataSet.circleRadius = 4f
        dataSet.setDrawCircleHole(false)

        val lineData = LineData(dataSet)

        // ✅ UPDATED: Use binding.tempChart
        binding.chartTemperature.data = lineData
        binding.chartTemperature.description.text = "Temperature trend"
        binding.chartTemperature.description.textSize = 12f
        binding.chartTemperature.xAxis.position = XAxis.XAxisPosition.BOTTOM
        binding.chartTemperature.axisRight.isEnabled = false
        binding.chartTemperature.invalidate()
    }

    private fun setupDistanceChart(history: List<DataPoint>) {
        val entries = history.mapIndexed { index, dataPoint ->
            Entry(index.toFloat(), dataPoint.distance.toFloat())
        }
        val dataSet = LineDataSet(entries, "Distance (m)")
        dataSet.color = Color.BLUE
        dataSet.valueTextColor = Color.BLACK
        dataSet.setCircleColor(dataSet.color)
        dataSet.lineWidth = 2.5f
        dataSet.circleRadius = 4f
        dataSet.setDrawCircleHole(false)

        val lineData = LineData(dataSet)

        // ✅ UPDATED: Use binding.distChart
        binding.chartDistance.data = lineData
        binding.chartDistance.description.text = "Distance trend"
        binding.chartDistance.description.textSize = 12f
        binding.chartDistance.xAxis.position = XAxis.XAxisPosition.BOTTOM
        binding.chartDistance.axisRight.isEnabled = false
        binding.chartDistance.invalidate()
    }
}