package com.miremote.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Splash screen
 */
class SplashViewModel : ViewModel() {
    
    private val _navigateToModeSelection = MutableStateFlow(false)
    val navigateToModeSelection: StateFlow<Boolean> = _navigateToModeSelection
    
    init {
        viewModelScope.launch {
            delay(2000) // 2 seconds delay
            _navigateToModeSelection.value = true
        }
    }
    
    fun onNavigationComplete() {
        _navigateToModeSelection.value = false
    }
}
