package com.example.cattler

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cattler.databinding.ActivityMainBinding // ✅ ADDED: ViewBinding import
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    // ✅ ADDED: ViewBinding reference
    private lateinit var binding: ActivityMainBinding
    private lateinit var cattleAdapter: CattleAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ✅ UPDATED: Inflate and set content view using ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // --- Setup RecyclerView ---
        setupRecyclerView()

        // --- Setup Pull-to-Refresh ---
        binding.swipeRefreshLayout.setOnRefreshListener {
            Log.d("MainActivity", "Pull-to-refresh initiated.")
            loadMockData()
            // fetchCattleData() // Uncomment for real data
        }

        // --- Load initial data ---
        loadMockData()
        // fetchCattleData() // Uncomment for real data
    }

    private fun setupRecyclerView() {
        // ✅ UPDATED: Adapter now uses ListAdapter's simple constructor
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
            val mockList = getMockCattleData()

            // ✅ UPDATED: Use submitList for ListAdapter
            cattleAdapter.submitList(mockList)

            binding.progressBar.visibility = View.GONE
            binding.swipeRefreshLayout.isRefreshing = false
            binding.recyclerViewCattle.visibility = View.VISIBLE
            Log.d("MainActivity", "Loaded mock data into the adapter.")
        }
    }

    private fun getMockCattleData(): List<Cattle> {
        return listOf(
            Cattle(id = "C-001", temperature = 38.5, distance = 15),
            Cattle(id = "C-002", temperature = 39.1, distance = 22),
            Cattle(id = "C-003", temperature = 38.2, distance = 12)
        )
    }

    private fun fetchCattleData() {
        if (!binding.swipeRefreshLayout.isRefreshing) {
            binding.progressBar.visibility = View.VISIBLE
        }
        binding.recyclerViewCattle.visibility = View.INVISIBLE

        lifecycleScope.launch {
            try {
                val cattleList = RetrofitInstance.api.getAllCattle()

                // ✅ UPDATED: Use submitList for ListAdapter
                cattleAdapter.submitList(cattleList)

                Log.d("MainActivity", "Successfully fetched real data.")
            } catch (e: Exception) {
                Log.e("MainActivity", "Error fetching real data: ${e.message}", e)
            } finally {
                binding.progressBar.visibility = View.GONE
                binding.swipeRefreshLayout.isRefreshing = false
                binding.recyclerViewCattle.visibility = View.VISIBLE
            }
        }
    }
}