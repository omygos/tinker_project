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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.google.firebase.messaging.FirebaseMessaging
import com.google.android.gms.tasks.OnCompleteListener // ✅ THIS IS THE MISSING IMPORT

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var cattleAdapter: CattleAdapter

    // ✅ Notification permission launcher for Android 13+
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("MainActivity", "✅ Notification permission granted.")
        } else {
            Log.w("MainActivity", "❌ Notification permission denied.")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()

        binding.swipeRefreshLayout.setOnRefreshListener {
            Log.d("MainActivity", "Pull-to-refresh initiated.")
            loadMockData()
            // fetchCattleData() // uncomment for real backend later
        }

//        loadMockData()
         fetchCattleData() // uncomment for real backend later

        askNotificationPermission()   // ✅ Ask notification permission
        testFirebaseConnection()      // ✅ TEST Firebase setup
    }

    // ✅ Test Firebase setup by subscribing to a topic
    private fun testFirebaseConnection() {
        FirebaseMessaging.getInstance().subscribeToTopic("test")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("FirebaseTest", "✅ Firebase connected & topic subscribed")
                } else {
                    Log.e("FirebaseTest", "❌ Firebase NOT connected", task.exception)
                }
            }

        // --- This is your new "guaranteed" token finder ---
        Log.d("FirebaseTest", "Actively fetching FCM Token...")
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.e("FirebaseTest", "❌ Fetching FCM token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Log the token with a NEW, EASY-TO-FIND tag
            Log.d("MY_FCM_TOKEN", "Token is: $token")
        })
    }

    // ✅ Ask notification permission (Android 13+)
    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
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

    private fun loadMockData() {
        if (!binding.swipeRefreshLayout.isRefreshing) {
            binding.progressBar.visibility = View.VISIBLE
        }

        binding.recyclerViewCattle.visibility = View.INVISIBLE

        lifecycleScope.launch {
            delay(1000)

            val mockList = listOf(
                Cattle(id = "C-001", temperature = 38.5, distance = 15),
                Cattle(id = "C-002", temperature = 39.1, distance = 22),
                Cattle(id = "C-003", temperature = 38.2, distance = 12)
            )

            cattleAdapter.submitList(mockList)

            binding.progressBar.visibility = View.GONE
            binding.swipeRefreshLayout.isRefreshing = false
            binding.recyclerViewCattle.visibility = View.VISIBLE
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
                Log.d("MainActivity", "✅ Real data fetched")
            } catch (e: Exception) {
                Log.e("MainActivity", "❌ Error fetching data: ${e.message}")
            } finally {
                binding.progressBar.visibility = View.GONE
                binding.swipeRefreshLayout.isRefreshing = false
                binding.recyclerViewCattle.visibility = View.VISIBLE
            }
        }
    }
}