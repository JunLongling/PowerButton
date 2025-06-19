package com.example.powerbutton

import ButtonItem
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.powerbutton.com.example.powerbutton.ButtonAdapter

class MainActivity : AppCompatActivity() {

    companion object {
        var isVisible = false
        var instance: MainActivity? = null
    }

    private lateinit var deviceAdminLauncher: ActivityResultLauncher<Intent>
    private lateinit var devicePolicyManager: DevicePolicyManager
    private lateinit var componentName: ComponentName

    private val buttonItems = listOf(
        ButtonItem(R.drawable.ic_lock, "LOCK SCREEN", R.drawable.bg_lock),
        ButtonItem(R.drawable.ic_volume, "VOLUME", R.drawable.bg_volume),
        ButtonItem(R.drawable.ic_screen_overlay, "SCREEN OVERLAY", R.drawable.bg_floating),
        ButtonItem(R.drawable.ic_power, "POWER MENU", R.drawable.bg_power),
        ButtonItem(R.drawable.ic_close, "CLOSE APP", R.drawable.bg_close)
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instance = this
        setContentView(R.layout.activity_main)

        // Initialize DevicePolicyManager and ComponentName
        devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        componentName = ComponentName(this, MyDeviceAdminReceiver::class.java)

        // Register launcher for admin permission result
        deviceAdminLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (devicePolicyManager.isAdminActive(componentName)) {
                lockScreen()
            } else {
                Toast.makeText(this, "Admin permission not granted", Toast.LENGTH_SHORT).show()
            }
        }

        // Set window size and position (half screen)
        val metrics = resources.displayMetrics
        val width = metrics.widthPixels
        val height = metrics.heightPixels
        val params = window.attributes

        if (width < height) {
            params.width = WindowManager.LayoutParams.MATCH_PARENT
            params.height = height / 2
            params.gravity = Gravity.BOTTOM
        } else {
            params.width = width / 2
            params.height = WindowManager.LayoutParams.MATCH_PARENT
            params.gravity = Gravity.END
        }
        window.attributes = params

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 3)
        recyclerView.adapter = ButtonAdapter(this, buttonItems) { item: ButtonItem ->
            when (item.label) {
                "LOCK SCREEN" -> {
                    if (devicePolicyManager.isAdminActive(componentName)) {
                        lockScreen()
                    } else {
                        requestDeviceAdmin()
                    }
                }
                "VOLUME" -> showSystemVolumeUI()
                "SCREEN OVERLAY" -> startScreenOverlay()
                "POWER MENU" -> showPowerMenu()
                "CLOSE APP" -> closeAppCompletely()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        isVisible = true
    }

    override fun onStop() {
        super.onStop()
        isVisible = false
    }

    override fun onDestroy() {
        super.onDestroy()
        if (instance === this) {
            instance = null
        }
    }

    fun closeIfVisible() {
        if (isVisible) {
            finish()
        }
    }

    private fun requestDeviceAdmin() {
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
            putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "This app needs device admin permission to lock the screen."
            )
        }
        deviceAdminLauncher.launch(intent)
    }

    private fun lockScreen() {
        devicePolicyManager.lockNow()
        finishAndRemoveTask()
    }

    private fun showSystemVolumeUI() {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.adjustStreamVolume(
            AudioManager.STREAM_MUSIC,
            AudioManager.ADJUST_SAME,
            AudioManager.FLAG_SHOW_UI
        )
        finishAndRemoveTask()
    }

    private fun startScreenOverlay() {
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Please grant overlay permission", Toast.LENGTH_LONG).show()
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            startActivity(intent)
            return
        }
        val intent = Intent(this, ScreenOverlayService::class.java)
        startService(intent)
        finish()
    }

    private fun closeAppCompletely() {
        val intent = Intent(this, ScreenOverlayService::class.java)
        stopService(intent)
        finishAndRemoveTask()
        System.exit(0)
    }

    private fun showPowerMenu() {
        val service = PowerMenuAccessibilityService.instance
        if (service != null) {
            service.showPowerMenu()
        } else {
            Toast.makeText(this, "Please enable Accessibility Service", Toast.LENGTH_LONG).show()
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
    }
}
