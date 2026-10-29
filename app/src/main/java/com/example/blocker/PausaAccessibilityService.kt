package com.example.blocker

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

/**
 * Real-time Accessibility Interceptor for PAUSA.
 * Detects the instant a blocked social network (Instagram, TikTok, YouTube, Facebook, X, WhatsApp)
 * opens a window on screen and brings PAUSA's ModoPausaScreen to the front unless unlocked via PayPal.
 */
class PausaAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        PausaBlockerController.startGuardianMonitorService(applicationContext)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val pkgName = event.packageName?.toString() ?: return
            PausaBlockerController.interceptIfNeeded(applicationContext, pkgName)
        }
    }

    override fun onInterrupt() {
        // Required override for AccessibilityService
    }
}
