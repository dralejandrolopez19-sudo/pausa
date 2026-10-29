package com.example.blocker

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Process
import android.provider.Settings
import androidx.core.content.edit
import com.example.MainActivity
import com.example.data.AppLimitEntity
import java.util.Locale

object PausaBlockerController {

    const val EXTRA_INTERCEPTED_APP_NAME = "extra_intercepted_app_name"
    private const val EXTRA_INTERCEPTED_PACKAGE = "extra_intercepted_package"
    private const val PREFS_NAME = "pausa_blocker_sync_prefs"
    private const val KEY_ENABLED_IDS = "enabled_app_ids"
    private const val KEY_CUSTOM_NAMES = "custom_app_names"
    private const val KEY_PAUSE_ACTIVE = "is_pause_active"
    private const val KEY_PAUSE_END_MS = "pause_end_ms"
    private const val KEY_UNLOCK_PREFIX = "unlock_until_"

    @Volatile
    private var lastInterceptTimeMs: Long = 0L

    @Volatile
    private var lastInterceptedPkg: String = ""

    data class SocialTarget(
        val id: String,
        val displayName: String,
        val packages: Set<String>,
        val keywords: Set<String>
    )

    private val knownSocialTargets: List<SocialTarget> = listOf(
        SocialTarget(
            id = "instagram",
            displayName = "Instagram",
            packages = setOf(
                "com.instagram.android",
                "com.instagram.lite",
                "com.instagram.barcelona"
            ),
            keywords = setOf("instagram")
        ),
        SocialTarget(
            id = "tiktok",
            displayName = "TikTok",
            packages = setOf(
                "com.zhiliaoapp.musically",
                "com.ss.android.ugc.trill",
                "com.zhiliaoapp.musically.go",
                "com.ss.android.ugc.aweme"
            ),
            keywords = setOf("tiktok", "musically")
        ),
        SocialTarget(
            id = "youtube",
            displayName = "YouTube",
            packages = setOf(
                "com.google.android.youtube",
                "com.google.android.apps.youtube.music",
                "com.huawei.himovie.overseas"
            ),
            keywords = setOf("youtube")
        ),
        SocialTarget(
            id = "facebook",
            displayName = "Facebook",
            packages = setOf(
                "com.facebook.katana",
                "com.facebook.lite",
                "com.facebook.orca",
                "com.facebook.mlite"
            ),
            keywords = setOf("facebook")
        ),
        SocialTarget(
            id = "twitter",
            displayName = "X (Twitter)",
            packages = setOf(
                "com.twitter.android",
                "com.twitter.android.lite"
            ),
            keywords = setOf("twitter")
        ),
        SocialTarget(
            id = "whatsapp",
            displayName = "WhatsApp",
            packages = setOf(
                "com.whatsapp",
                "com.whatsapp.w4b"
            ),
            keywords = setOf("whatsapp")
        )
    )

    private val whitelistedSystemOrPaymentPackages = setOf(
        "com.android.systemui",
        "com.android.settings",
        "com.huawei.systemmanager",
        "com.huawei.android.launcher",
        "com.google.android.apps.nexuslauncher",
        "com.sec.android.app.launcher",
        "com.miui.home",
        "com.paypal.android.p2pmobile",
        "com.android.chrome",
        "com.huawei.browser",
        "org.mozilla.firefox",
        "com.microsoft.emmx"
    )

    fun syncAppLimitsToBlocker(context: Context, limits: List<AppLimitEntity>) {
        val enabledIds = limits.filter { it.isEnabled }.map { it.id.lowercase(Locale.ROOT) }.toSet()
        val customNames = limits
            .filter { it.isEnabled && it.iconType == "custom" }
            .map { it.appName.trim().lowercase(Locale.ROOT) }
            .filter { it.isNotBlank() }
            .toSet()

        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            putStringSet(KEY_ENABLED_IDS, enabledIds)
            putStringSet(KEY_CUSTOM_NAMES, customNames)
        }
    }

    fun setPauseTimerActiveState(context: Context, isRunning: Boolean, remainingSeconds: Int) {
        val endMs = if (isRunning && remainingSeconds > 0) {
            System.currentTimeMillis() + (remainingSeconds * 1000L)
        } else {
            0L
        }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            putBoolean(KEY_PAUSE_ACTIVE, isRunning && remainingSeconds > 0)
            putLong(KEY_PAUSE_END_MS, endMs)
        }
    }

    /**
     * Grants a temporary unlock window (default 15 minutes) for a social network
     * ONLY after the user pays the $1.00 USD PayPal penalty or completes a full pause.
     */
    fun grantPaidUnlockPass(context: Context, appNameOrId: String, durationMinutes: Int = 15) {
        val normalized = normalizeAppId(appNameOrId)
        val unlockUntil = System.currentTimeMillis() + (durationMinutes * 60_000L)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            putLong(KEY_UNLOCK_PREFIX + normalized, unlockUntil)
            putBoolean(KEY_PAUSE_ACTIVE, false)
            putLong(KEY_PAUSE_END_MS, 0L)
        }
    }

    fun revokeAllUnlockPasses(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val keysToRemove = prefs.all.keys.filter { it.startsWith(KEY_UNLOCK_PREFIX) }
        prefs.edit {
            keysToRemove.forEach { remove(it) }
        }
    }

    private fun isAppCurrentlyUnlockedByPayment(context: Context, targetId: String): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val unlockUntil = prefs.getLong(KEY_UNLOCK_PREFIX + targetId.lowercase(Locale.ROOT), 0L)
        return System.currentTimeMillis() < unlockUntil
    }

    private fun normalizeAppId(appNameOrId: String): String {
        val lower = appNameOrId.trim().lowercase(Locale.ROOT)
        knownSocialTargets.forEach { target ->
            if (target.id == lower ||
                target.displayName.lowercase(Locale.ROOT) == lower ||
                target.keywords.any { lower.contains(it) }
            ) {
                return target.id
            }
        }
        return lower
    }

    fun matchBlockedSocialApp(context: Context, packageName: String): String? {
        val pkgLower = packageName.trim().lowercase(Locale.ROOT)
        if (pkgLower.isBlank() ||
            pkgLower == context.packageName.lowercase(Locale.ROOT) ||
            pkgLower in whitelistedSystemOrPaymentPackages ||
            pkgLower.contains("launcher") ||
            pkgLower.contains("inputmethod") ||
            pkgLower.contains("keyboard") ||
            pkgLower.contains("permissioncontroller")
        ) {
            return null
        }

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val enabledIds = prefs.getStringSet(KEY_ENABLED_IDS, setOf("instagram", "tiktok", "youtube"))
            ?: setOf("instagram", "tiktok", "youtube")
        val customNames = prefs.getStringSet(KEY_CUSTOM_NAMES, emptySet()) ?: emptySet()
        val isPauseRunning = prefs.getBoolean(KEY_PAUSE_ACTIVE, false) &&
            System.currentTimeMillis() < prefs.getLong(KEY_PAUSE_END_MS, 0L)

        // 1. Check known social media packages
        for (target in knownSocialTargets) {
            val isPkgMatch = pkgLower in target.packages || target.keywords.any { pkgLower.contains(it) }
            if (isPkgMatch) {
                val shouldBlock = isPauseRunning || (target.id in enabledIds)
                if (shouldBlock && !isAppCurrentlyUnlockedByPayment(context, target.id)) {
                    return target.displayName
                }
                return null
            }
        }

        // 2. Check custom apps added by user
        for (custom in customNames) {
            if (custom.isNotBlank() && pkgLower.contains(custom)) {
                if (!isAppCurrentlyUnlockedByPayment(context, custom)) {
                    return custom.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
                    }
                }
            }
        }

        return null
    }

    fun interceptIfNeeded(context: Context, foregroundPackage: String) {
        val displayName = matchBlockedSocialApp(context, foregroundPackage) ?: return

        val now = System.currentTimeMillis()
        if (foregroundPackage == lastInterceptedPkg && (now - lastInterceptTimeMs) < 1200L) {
            return
        }
        lastInterceptTimeMs = now
        lastInterceptedPkg = foregroundPackage

        runCatching {
            val blockIntent = Intent(context, MainActivity::class.java).apply {
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP or
                        Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                )
                putExtra(EXTRA_INTERCEPTED_APP_NAME, displayName)
                putExtra(EXTRA_INTERCEPTED_PACKAGE, foregroundPackage)
            }
            context.startActivity(blockIntent)
        }
    }

    fun queryCurrentForegroundPackage(context: Context): String? {
        if (!hasUsageStatsPermission(context)) return null
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager ?: return null
        val endTime = System.currentTimeMillis()
        val startTime = endTime - 15_000L
        val usageEvents = usm.queryEvents(startTime, endTime) ?: return null
        val event = UsageEvents.Event()
        var latestPkg: String? = null
        var latestTimestamp = 0L

        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED &&
                event.timeStamp >= latestTimestamp
            ) {
                latestTimestamp = event.timeStamp
                latestPkg = event.packageName
            }
        }
        return latestPkg
    }

    @Suppress("DEPRECATION")
    fun hasUsageStatsPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        } else {
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun hasOverlayPermission(context: Context): Boolean {
        return Settings.canDrawOverlays(context)
    }

    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val expectedComponent = ComponentName(context, PausaAccessibilityService::class.java)
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return enabledServices.split(':').any {
            ComponentName.unflattenFromString(it)?.equals(expectedComponent) == true
        }
    }

    fun resolvePackagesForLimit(limit: AppLimitEntity): Set<String> {
        val normId = normalizeAppId(limit.id)
        val matchedTarget = knownSocialTargets.firstOrNull {
            it.id == normId || it.displayName.equals(limit.appName, ignoreCase = true)
        }
        return matchedTarget?.packages ?: emptySet()
    }

    fun queryTodayUsageMinutesForPackages(context: Context, packages: Set<String>): Int {
        if (packages.isEmpty() || !hasUsageStatsPermission(context)) return 0
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager ?: return 0
        val now = System.currentTimeMillis()
        val cal = java.util.Calendar.getInstance().apply {
            timeInMillis = now
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val startOfDay = cal.timeInMillis
        val statsList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startOfDay, now) ?: return 0
        val totalForegroundMs = statsList
            .filter { it.packageName.lowercase(Locale.ROOT) in packages }
            .sumOf { it.totalTimeInForeground.coerceAtLeast(0L) }
        return (totalForegroundMs / 60_000L).toInt()
    }

    fun startGuardianMonitorService(context: Context) {
        runCatching {
            val intent = Intent(context, PausaMonitorService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
}
