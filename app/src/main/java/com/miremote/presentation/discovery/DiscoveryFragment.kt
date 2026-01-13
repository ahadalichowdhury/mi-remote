package com.miremote.presentation.discovery

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.miremote.R
import com.miremote.data.model.TvDevice
import com.miremote.databinding.FragmentDiscoveryBinding
import com.miremote.utils.PermissionManager
import kotlinx.coroutines.launch

/**
 * Fragment for discovering and connecting to TV devices
 */
class DiscoveryFragment : Fragment() {
    
    private var _binding: FragmentDiscoveryBinding? = null
    private val binding get() = _binding!!
    
    private val args: DiscoveryFragmentArgs by navArgs()
    private val viewModel: DiscoveryViewModel by viewModels {
        DiscoveryViewModelFactory(requireContext())
    }
    
    private lateinit var deviceAdapter: DeviceAdapter
    
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        if (allGranted) {
            // Permissions granted, start scanning
            val mode = try {
                TvDevice.ConnectionType.valueOf(args.connectionMode)
            } catch (e: Exception) {
                TvDevice.ConnectionType.WIFI
            }
            viewModel.setMode(mode)
        } else {
            Toast.makeText(
                requireContext(),
                getString(R.string.error_permission_required),
                Toast.LENGTH_LONG
            ).show()
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDiscoveryBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val mode = try {
            TvDevice.ConnectionType.valueOf(args.connectionMode)
        } catch (e: Exception) {
            TvDevice.ConnectionType.WIFI
        }
        
        setupRecyclerView()
        setupClickListeners()
        observeViewModel(mode)
        
        // Check and request permissions
        checkPermissions(mode)
    }
    
    private fun setupRecyclerView() {
        deviceAdapter = DeviceAdapter { device ->
            viewModel.connectToDevice(device)
        }
        
        binding.rvDevices.layoutManager = LinearLayoutManager(requireContext())
        binding.rvDevices.adapter = deviceAdapter
    }
    
    private fun checkPermissions(mode: TvDevice.ConnectionType) {
        val requiredPermissions = when (mode) {
            TvDevice.ConnectionType.WIFI -> PermissionManager.getWifiPermissions()
            TvDevice.ConnectionType.BLUETOOTH -> PermissionManager.getBluetoothPermissions()
        }
        
        val missingPermissions = requiredPermissions.filter { permission ->
            ContextCompat.checkSelfPermission(
                requireContext(),
                permission
            ) != PackageManager.PERMISSION_GRANTED
        }
        
        if (missingPermissions.isEmpty()) {
            // All permissions granted, start scanning
            viewModel.setMode(mode)
        } else {
            // Request missing permissions
            permissionLauncher.launch(missingPermissions.toTypedArray())
        }
    }
    
    private fun setupClickListeners() {
        binding.btnRefresh.setOnClickListener {
            val mode = try {
                TvDevice.ConnectionType.valueOf(args.connectionMode)
            } catch (e: Exception) {
                TvDevice.ConnectionType.WIFI
            }
            checkPermissions(mode)
        }
        
        binding.btnConnectManual.setOnClickListener {
            val ip = binding.etIpAddress.text?.toString()?.trim()
            if (ip.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Please enter an IP address", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.connectToManualIp(ip)
            }
        }
    }
    
    private fun observeViewModel(mode: TvDevice.ConnectionType) {
        lifecycleScope.launch {
            viewModel.devices.collect { devices ->
                deviceAdapter.submitList(devices)
                binding.tvEmptyState.visibility = if (devices.isEmpty() && !viewModel.isScanning.value) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            }
        }
        
        lifecycleScope.launch {
            viewModel.isScanning.collect { isScanning ->
                binding.progressBar.visibility = if (isScanning) View.VISIBLE else View.GONE
            }
        }
        
        lifecycleScope.launch {
            viewModel.errorMessage.collect { error ->
                error?.let {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                    viewModel.clearError()
                }
            }
        }
        
        lifecycleScope.launch {
            viewModel.navigateToRemote.collect { device ->
                device?.let {
                    navigateToRemote(it)
                    viewModel.onNavigationComplete()
                }
            }
        }
    }
    
    private fun navigateToRemote(device: TvDevice) {
        val action = DiscoveryFragmentDirections
            .actionDiscoveryFragmentToRemoteFragment(
                deviceName = device.name,
                deviceAddress = device.address,
                deviceType = device.type.name
            )
        findNavController().navigate(action)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
