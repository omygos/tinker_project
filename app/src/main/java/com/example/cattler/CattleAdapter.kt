package com.example.cattler

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.cattler.databinding.ItemCattleCardBinding

// ✅ CHANGED: Now extends ListAdapter for better performance
class CattleAdapter : ListAdapter<Cattle, CattleAdapter.CattleViewHolder>(CattleDiffCallback()) {

    // ✅ UPDATED: ViewHolder now uses ViewBinding
    class CattleViewHolder(private val binding: ItemCattleCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(cattle: Cattle) {
            binding.textViewCowId.text = "Cow ID: ${cattle.id}"
            binding.textViewTemperature.text = "Temp: ${cattle.temperature}°C"
            binding.textViewDistance.text = "Distance: ${cattle.distance}m"

            // --- Click Listener ---
            itemView.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, DetailActivity::class.java)
                intent.putExtra("COW_ID", cattle.id)
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CattleViewHolder {
        // ✅ UPDATED: Inflate using ViewBinding
        val binding = ItemCattleCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CattleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CattleViewHolder, position: Int) {
        // ✅ UPDATED: Get item from ListAdapter
        val cattle = getItem(position)
        holder.bind(cattle)
    }
}

// ✅ ADDED: This class tells the adapter how to efficiently calculate changes
class CattleDiffCallback : DiffUtil.ItemCallback<Cattle>() {
    override fun areItemsTheSame(oldItem: Cattle, newItem: Cattle): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Cattle, newItem: Cattle): Boolean {
        return oldItem == newItem
    }
}