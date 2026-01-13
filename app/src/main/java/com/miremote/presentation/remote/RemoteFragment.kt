package com.miremote.presentation.remote

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.miremote.R
import com.miremote.data.model.ConnectionState
import com.miremote.domain.model.KeyEvent
import com.miremote.databinding.FragmentRemoteBinding
import kotlinx.coroutines.launch

/**
 * Fragment for remote control interface
 */
class RemoteFragment : Fragment() {
    
    private var _binding: FragmentRemoteBinding? = null
    private val binding get() = _binding!!
    
    private val args: RemoteFragmentArgs by navArgs()
    private val viewModel: RemoteViewModel by viewModels {
        RemoteViewModelFactory(requireContext())
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRemoteBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val device = com.miremote.data.model.TvDevice(
            name = args.deviceName,
            address = args.deviceAddress,
            type = com.miremote.data.model.TvDevice.ConnectionType.valueOf(args.deviceType),
            isConnected = false
        )
        viewModel.setConnectedDevice(device)
        
        setupClickListeners()
        observeViewModel()
        
        binding.tvDeviceName.text = device.name
    }
    
    private fun setupClickListeners() {
        // D-pad buttons
        binding.btnUp.setOnClickListener {
            viewModel.sendKeyEvent(KeyEvent.DPAD_UP)
        }
        
        binding.btnDown.setOnClickListener {
            viewModel.sendKeyEvent(KeyEvent.DPAD_DOWN)
        }
        
        binding.btnLeft.setOnClickListener {
            viewModel.sendKeyEvent(KeyEvent.DPAD_LEFT)
        }
        
        binding.btnRight.setOnClickListener {
            viewModel.sendKeyEvent(KeyEvent.DPAD_RIGHT)
        }
        
        binding.btnOk.setOnClickListener {
            viewModel.sendKeyEvent(KeyEvent.DPAD_CENTER)
        }
        
        // Control buttons
        binding.btnBack.setOnClickListener {
            viewModel.sendKeyEvent(KeyEvent.BACK)
        }
        
        binding.btnHome.setOnClickListener {
            viewModel.sendKeyEvent(KeyEvent.HOME)
        }
        
        // Volume buttons
        binding.btnVolumeUp.setOnClickListener {
            viewModel.sendKeyEvent(KeyEvent.VOLUME_UP)
        }
        
        binding.btnVolumeDown.setOnClickListener {
            viewModel.sendKeyEvent(KeyEvent.VOLUME_DOWN)
        }
        
        // Disconnect button
        binding.btnDisconnect.setOnClickListener {
            viewModel.disconnect()
            findNavController().popBackStack()
        }
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.connectionState.collect { state ->
                updateConnectionStatus(state)
            }
        }
        
        lifecycleScope.launch {
            viewModel.errorMessage.collect { error ->
                error?.let {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                    viewModel.clearError()
                }
            }
        }
    }
    
    private fun updateConnectionStatus(state: ConnectionState) {
        when (state) {
            is ConnectionState.Connected -> {
                binding.tvConnectionStatus.text = getString(R.string.connected)
                binding.tvConnectionStatus.setTextColor(
                    resources.getColor(R.color.status_connected, null)
                )
            }
            is ConnectionState.Connecting -> {
                binding.tvConnectionStatus.text = getString(R.string.connecting)
                binding.tvConnectionStatus.setTextColor(
                    resources.getColor(R.color.status_connecting, null)
                )
            }
            is ConnectionState.Disconnected -> {
                binding.tvConnectionStatus.text = getString(R.string.disconnected)
                binding.tvConnectionStatus.setTextColor(
                    resources.getColor(R.color.status_disconnected, null)
                )
            }
            is ConnectionState.Error -> {
                binding.tvConnectionStatus.text = state.message
                binding.tvConnectionStatus.setTextColor(
                    resources.getColor(R.color.status_disconnected, null)
                )
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
