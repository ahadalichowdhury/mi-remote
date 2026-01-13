package com.miremote.presentation.splash

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.miremote.MainActivity
import com.miremote.R
import kotlinx.coroutines.launch

/**
 * Splash screen activity
 */
class SplashActivity : AppCompatActivity() {
    
    private val viewModel: SplashViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        
        lifecycleScope.launch {
            viewModel.navigateToModeSelection.collect { shouldNavigate ->
                if (shouldNavigate) {
                    navigateToMain()
                    viewModel.onNavigationComplete()
                }
            }
        }
    }
    
    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
