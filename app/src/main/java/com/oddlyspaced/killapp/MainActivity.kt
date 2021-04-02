package com.oddlyspaced.killapp

import android.app.ActivityManager
import android.app.AppOpsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import java.util.*


class MainActivity : AppCompatActivity() {

    private companion object {
        const val TAG = "Killer"
    }

    private lateinit var usageStatsManager: UsageStatsManager
    private lateinit var activityManager: ActivityManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        Log.e(TAG, isUsageStatsGranted().toString())
        if (!isUsageStatsGranted()) {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            startActivity(intent)
        }
        else {
            killApp("com.techburner.burnerbits")
//            usageStatsBakchodi()
        }

    }

    private fun isUsageStatsGranted(): Boolean {
        return try {
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
            val appOpsManager = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            (appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, applicationInfo.uid, applicationInfo.packageName) == AppOpsManager.MODE_ALLOWED)
        } catch (e: Exception) {
            false
        }
    }

    private fun usageStatsBakchodi() {
        usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.SECOND, -1)
        val time = System.currentTimeMillis()
        usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, calendar.timeInMillis, time).forEach {
            if (filterRunningApps(it)) {
                Log.e(TAG, it.packageName)
            }
        }
    }

    private fun filterRunningApps(stat: UsageStats): Boolean {
        val blacklist = listOf("com.android", "com.google")
        blacklist.forEach {
            if (stat.packageName.startsWith(it)) {
                return false
            }
        }

        if (isAppSystem(stat.packageName)) {
            return false
        }

        if (usageStatsManager.isAppInactive(stat.packageName)) {
            return false
        }

        return true
    }

    private fun isAppSystem(packageName: String): Boolean {
        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
        } catch (e: Exception) {
            false
        }
    }

    private fun killApp(packageName: String) {
        activityManager.killBackgroundProcesses(packageName)
    }

    /*
    private boolean isAccessGranted() {
        try {
            PackageManager packageManager = getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(getPackageName(), 0);
            AppOpsManager appOpsManager = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
            int mode = 0;
            if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.KITKAT) {
                mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                        applicationInfo.uid, applicationInfo.packageName);
            }
            return (mode == AppOpsManager.MODE_ALLOWED);

        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
     */
}