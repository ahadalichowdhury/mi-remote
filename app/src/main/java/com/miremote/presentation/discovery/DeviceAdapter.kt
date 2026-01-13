package com.miremote.presentation.discovery

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.miremote.data.model.TvDevice
import com.miremote.databinding.ItemDeviceBinding

/**
 * Adapter for displaying discovered devices
 */
class DeviceAdapter(
    private val onConnectClick: (TvDevice) -> Unit
) : ListAdapter<TvDevice, DeviceAdapter.DeviceViewHolder>(DeviceDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val binding = ItemDeviceBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DeviceViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class DeviceViewHolder(
        private val binding: ItemDeviceBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(device: TvDevice) {
            binding.tvDeviceName.text = device.name
            binding.tvDeviceAddress.text = device.address
            
            binding.btnConnect.setOnClickListener {
                onConnectClick(device)
            }
        }
    }
    
    class DeviceDiffCallback : DiffUtil.ItemCallback<TvDevice>() {
        override fun areItemsTheSame(oldItem: TvDevice, newItem: TvDevice): Boolean {
            return oldItem.address == newItem.address
        }
        
        override fun areContentsTheSame(oldItem: TvDevice, newItem: TvDevice): Boolean {
            return oldItem == newItem
        }
    }
}
