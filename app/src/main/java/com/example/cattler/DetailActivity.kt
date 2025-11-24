package com.example.cattler

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.cattler.databinding.ActivityDetailBinding
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private lateinit var mqttClient: MqttClientHelper
    private var currentCowId: String? = null

    // Store timestamps for X-axis labels
    private val timestamps = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val cowId = intent.getStringExtra("COW_ID")
        currentCowId = cowId

        if (cowId != null) {
            supportActionBar?.title = "Live Monitoring: $cowId"

            // 1. Load the last 20 points from history
            fetchCattleHistory(cowId)

            // 2. Connect to MQTT for live updates
            initMqtt(cowId)
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

    private fun initMqtt(cowId: String) {
        mqttClient = MqttClientHelper(this)
        mqttClient.connect(
            onSuccess = {
                mqttClient.subscribe("lora/data") { payload ->
                    addLiveEntryToGraphs(payload)
                }
            },
            onFailure = {
                Log.e("DetailActivity_MQTT", "Could not connect to broker.")
            }
        )
    }

    private fun addLiveEntryToGraphs(payload: String) {
        try {
            val json = JSONObject(payload)
            val cowId = json.getString("id")

            // Only update if message is for THIS cow
            if (cowId != currentCowId) return

            val distance = json.getDouble("distance").toFloat()
            val temp = json.getDouble("temperature").toFloat()

            // Generate timestamp for the new point
            val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            timestamps.add(timestamp)

            runOnUiThread {
                // Update Temperature Chart with Stock Effect
                updateChartWithStockEffect(binding.chartTemperature, temp)

                // Update Distance Chart with Stock Effect
                updateChartWithStockEffect(binding.chartDistance, distance)
            }
        } catch (e: Exception) {
            Log.e("DetailActivity_MQTT", "Failed to parse live data", e)
        }
    }

    /**
     * ✅ THE MAGIC FUNCTION: Adds a point and scrolls like a stock ticker.
     */
    private fun updateChartWithStockEffect(chart: com.github.mikephil.charting.charts.LineChart, newValue: Float) {
        val data = chart.data ?: return
        val set = data.getDataSetByIndex(0) ?: return

        // 1. Add new entry
        data.addEntry(Entry(set.entryCount.toFloat(), newValue), 0)

        // 2. Notify changes
        data.notifyDataChanged()
        chart.notifyDataSetChanged()

        // 3. STOCK MARKET EFFECT:
        // Limit view to 20 points. If we have 21, it zooms in.
        chart.setVisibleXRangeMaximum(20f)

        // Scroll to the very end (the newest point)
        chart.moveViewToX(data.entryCount.toFloat())
    }

    private fun fetchCattleHistory(cowId: String) {
        binding.progressBarDetail.visibility = View.VISIBLE
        binding.chartsContainer.visibility = View.INVISIBLE

        lifecycleScope.launch {
            try {
                // Fetch ALL history
                val fullHistory = RetrofitInstance.api.getCattleHistory(cowId)

                if (fullHistory.isNotEmpty()) {
                    // ✅ Take only the last 20 points for a clean start
                    val recentHistory = fullHistory.takeLast(20)

                    timestamps.clear()
                    timestamps.addAll(recentHistory.map { it.timestamp })

                    setupTemperatureChart(recentHistory)
                    setupDistanceChart(recentHistory)
                } else {
                    Log.w("DetailActivity", "Received empty history")
                }
            } catch (e: Exception) {
                Log.e("DetailActivity", "Error fetching history: ${e.message}", e)
            } finally {
                binding.progressBarDetail.visibility = View.GONE
                binding.chartsContainer.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::mqttClient.isInitialized) mqttClient.disconnect()
    }

    private fun setupTemperatureChart(historyData: List<DataPoint>) {
        val entries = historyData.mapIndexed { index, item ->
            Entry(index.toFloat(), item.temperature.toFloat())
        }

        val dataSet = LineDataSet(entries, "Temperature (°C)").apply {
            color = Color.parseColor("#FF6B6B")
            lineWidth = 3f

            // ✅ STOCK LOOK: Disable circles for a smooth line
            setDrawCircles(false)
            setDrawValues(false)

            setDrawFilled(true)
            fillColor = Color.parseColor("#FF6B6B")
            fillAlpha = 50
            mode = LineDataSet.Mode.CUBIC_BEZIER // Smooth curves
        }

        val lineData = LineData(dataSet)
        binding.chartTemperature.data = lineData

        styleChart(binding.chartTemperature, "Temperature")

        // Initial animation
        binding.chartTemperature.animateX(1000)
    }

    private fun setupDistanceChart(historyData: List<DataPoint>) {
        val entries = historyData.mapIndexed { index, item ->
            Entry(index.toFloat(), item.distance.toFloat())
        }

        val dataSet = LineDataSet(entries, "Distance (m)").apply {
            color = Color.parseColor("#4ECDC4")
            lineWidth = 3f

            // ✅ STOCK LOOK: Disable circles
            setDrawCircles(false)
            setDrawValues(false)

            setDrawFilled(true)
            fillColor = Color.parseColor("#4ECDC4")
            fillAlpha = 50
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }

        val lineData = LineData(dataSet)
        binding.chartDistance.data = lineData

        styleChart(binding.chartDistance, "Distance")
        binding.chartDistance.animateX(1000)
    }

    private fun styleChart(chart: com.github.mikephil.charting.charts.LineChart, description: String) {
        chart.apply {
            this.description.text = description
            this.description.textColor = Color.DKGRAY

            setDrawGridBackground(false)
            setBackgroundColor(Color.WHITE)
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)

            legend.isEnabled = true
            legend.textColor = Color.DKGRAY

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false) // Cleaner look
                textColor = Color.DKGRAY
                granularity = 1f

                // Custom Timestamp Formatter
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        val index = value.toInt()
                        return if (index >= 0 && index < timestamps.size) {
                            timestamps[index]
                        } else {
                            ""
                        }
                    }
                }
            }

            axisLeft.apply {
                setDrawGridLines(true)
                gridColor = Color.parseColor("#E0E0E0")
                textColor = Color.DKGRAY
            }

            axisRight.isEnabled = false

            // ✅ Set the view port to "zoom in" if we have a lot of data
            setVisibleXRangeMaximum(20f)

            // Move to the end immediately
            moveViewToX(data?.entryCount?.toFloat() ?: 0f)
        }
    }
}