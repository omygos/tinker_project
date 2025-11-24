package com.example.cattler

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cattler.databinding.ActivityMainBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var cattleAdapter: CattleAdapter
    private lateinit var mqttClient: MqttClientHelper

    // Permission launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) Log.d("MainActivity", "Notification permission granted")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()

        // Pull-to-refresh (Manual Reload)
        binding.swipeRefreshLayout.setOnRefreshListener {
            fetchCattleData()
        }

        // Initial Load
        fetchCattleData()

        // Notifications & MQTT
        askNotificationPermission()
        getAndLogFcmToken()
        initMqtt() // ✅ Live Updates
    }

    private fun initMqtt() {
        mqttClient = MqttClientHelper(this)
        mqttClient.connect(
            onSuccess = {
                mqttClient.subscribe("lora/data") { payload ->
                    updateListFromMqtt(payload)
                }
            },
            onFailure = { Log.e("MainActivity", "MQTT Connection Failed") }
        )
    }

    private fun updateListFromMqtt(payload: String) {
        try {
            val json = JSONObject(payload)
            val cowId = json.getString("id")
            val distance = json.getDouble("distance")
            val temp = json.getDouble("temperature")
            // Generate a timestamp if missing
            val timestamp = if(json.has("timestamp")) json.getString("timestamp") else "Live"

            runOnUiThread {
                val currentList = cattleAdapter.currentList.toMutableList()
                val index = currentList.indexOfFirst { it.id == cowId }

                if (index != -1) {
                    // Update existing cow
                    val updatedCow = currentList[index].copy(
                        distance = distance,
                        temperature = temp,
                        last_updated = timestamp
                    )
                    currentList[index] = updatedCow
                    cattleAdapter.submitList(currentList)
                } else {
                    // New cow found? Refresh full list to be safe
                    fetchCattleData()
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error parsing live data", e)
        }
    }

    private fun fetchCattleData() {
        if (!binding.swipeRefreshLayout.isRefreshing) {
            binding.progressBar.visibility = View.VISIBLE
        }
        binding.recyclerViewCattle.visibility = View.INVISIBLE

        lifecycleScope.launch {
            try {
                val cattleList = RetrofitInstance.api.getAllCattle()
                cattleAdapter.submitList(cattleList)
            } catch (e: Exception) {
                Log.e("MainActivity", "Error fetching data", e)
            } finally {
                binding.progressBar.visibility = View.GONE
                binding.swipeRefreshLayout.isRefreshing = false
                binding.recyclerViewCattle.visibility = View.VISIBLE
            }
        }
    }

    private fun setupRecyclerView() {
        cattleAdapter = CattleAdapter()
        binding.recyclerViewCattle.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = cattleAdapter
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun getAndLogFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("MY_FCM_TOKEN", "Token: ${task.result}")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::mqttClient.isInitialized) mqttClient.disconnect()
    }
}