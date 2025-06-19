package com.example.powerbutton

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView

class ScreenOverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: View
    private lateinit var overlayParams: WindowManager.LayoutParams
    private var isOverlayVisible = false

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        createOverlayView()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        showOverlay()
        return START_STICKY
    }

    private fun createOverlayView() {
        // Use a FrameLayout as a safe parent context for layout inflation
        val fakeParent = FrameLayout(this)
        overlayView = LayoutInflater.from(this).inflate(R.layout.floating_bubble, fakeParent, false)

        overlayParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        overlayParams.gravity = Gravity.TOP or Gravity.START
        overlayParams.x = 100
        overlayParams.y = 300

        val dragArea = overlayView.findViewById<ImageView>(R.id.drag_handle)
        dragArea.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f
            private var isClick = false

            override fun onTouch(v: View?, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        isClick = true
                        initialX = overlayParams.x
                        initialY = overlayParams.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = (event.rawX - initialTouchX).toInt()
                        val dy = (event.rawY - initialTouchY).toInt()
                        if (dx * dx + dy * dy > 25) isClick = false
                        overlayParams.x = initialX + dx
                        overlayParams.y = initialY + dy
                        windowManager.updateViewLayout(overlayView, overlayParams)
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        if (isClick) {
                            v?.performClick() // for accessibility
                            if (MainActivity.isVisible) {
                                MainActivity.instance?.closeIfVisible()
                            } else {
                                val intent = Intent(this@ScreenOverlayService, MainActivity::class.java).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                                }
                                startActivity(intent)
                            }
                        }
                        return true
                    }
                }
                return false
            }
        })

        // Optional override for accessibility compliance
        dragArea.setOnClickListener {
            if (MainActivity.isVisible) {
                MainActivity.instance?.closeIfVisible()
            } else {
                val intent = Intent(this@ScreenOverlayService, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                startActivity(intent)
            }
        }
    }

    private fun showOverlay() {
        if (isOverlayVisible) return
        windowManager.addView(overlayView, overlayParams)
        isOverlayVisible = true
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isOverlayVisible) {
            windowManager.removeView(overlayView)
            isOverlayVisible = false
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
