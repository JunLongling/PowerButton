package com.example.powerbutton

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast

class PowerMenuAccessibilityService : AccessibilityService() {

    companion object {
        var instance: PowerMenuAccessibilityService? = null
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // No action needed here
    }

    override fun onInterrupt() {
        // No action needed here
    }

    fun showPowerMenu() {
        val result = performGlobalAction(GLOBAL_ACTION_POWER_DIALOG)
        if (!result) {
            Toast.makeText(this, "Failed to show power menu", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (instance === this) {
            instance = null
        }
    }
}
