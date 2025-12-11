package com.tumbaspos.app.domain.manager

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Manages session timeout based on user inactivity
 */
class SessionTimeoutManager(
    private val scope: CoroutineScope,
    private val timeoutDurationMs: Long = 30 * 60 * 1000 // 30 minutes default
) {
    private var lastActivityTime = System.currentTimeMillis()
    private val _isTimedOut = MutableStateFlow(false)
    val isTimedOut: StateFlow<Boolean> = _isTimedOut.asStateFlow()
    
    private val _showWarning = MutableStateFlow(false)
    val showWarning: StateFlow<Boolean> = _showWarning.asStateFlow()
    
    private var isMonitoring = false
    
    /**
     * Record user activity
     */
    fun recordActivity() {
        lastActivityTime = System.currentTimeMillis()
        _isTimedOut.value = false
        _showWarning.value = false
    }
    
    /**
     * Start monitoring for inactivity
     */
    fun startMonitoring() {
        if (isMonitoring) return
        
        isMonitoring = true
        scope.launch {
            while (isMonitoring) {
                delay(10000) // Check every 10 seconds
                
                val inactiveTime = System.currentTimeMillis() - lastActivityTime
                val warningThreshold = timeoutDurationMs - (2 * 60 * 1000) // 2 minutes before timeout
                
                when {
                    inactiveTime >= timeoutDurationMs -> {
                        _isTimedOut.value = true
                        _showWarning.value = false
                        stopMonitoring()
                    }
                    inactiveTime >= warningThreshold -> {
                        _showWarning.value = true
                    }
                }
            }
        }
    }
    
    /**
     * Stop monitoring
     */
    fun stopMonitoring() {
        isMonitoring = false
        _showWarning.value = false
    }
    
    /**
     * Reset the timeout
     */
    fun reset() {
        lastActivityTime = System.currentTimeMillis()
        _isTimedOut.value = false
        _showWarning.value = false
    }
}
