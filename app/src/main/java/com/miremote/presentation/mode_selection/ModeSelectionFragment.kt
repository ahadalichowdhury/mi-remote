package com.miremote.presentation.mode_selection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.miremote.R
import com.miremote.data.model.TvDevice
import com.miremote.databinding.FragmentModeSelectionBinding
import kotlinx.coroutines.launch

/**
 * Fragment for selecting connection mode (Wi-Fi or Bluetooth)
 */
class ModeSelectionFragment : Fragment() {
    
    private var _binding: FragmentModeSelectionBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: ModeSelectionViewModel by viewModels()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentModeSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupClickListeners()
        observeViewModel()
    }
    
    private fun setupClickListeners() {
        binding.cardWifi.setOnClickListener {
            viewModel.selectWifiMode()
        }
        
        binding.cardBluetooth.setOnClickListener {
            viewModel.selectBluetoothMode()
        }
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.selectedMode.collect { mode ->
                mode?.let {
                    navigateToDiscovery(it)
                    viewModel.onNavigationComplete()
                }
            }
        }
    }
    
    private fun navigateToDiscovery(mode: TvDevice.ConnectionType) {
        val action = ModeSelectionFragmentDirections
            .actionModeSelectionFragmentToDiscoveryFragment(mode.name)
        findNavController().navigate(action)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
