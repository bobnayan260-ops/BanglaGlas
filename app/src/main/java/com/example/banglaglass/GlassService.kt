package com.example.banglaglass

import android.animation.ValueAnimator
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast

class GlassService : Service() {

    private lateinit var wm: WindowManager
    private lateinit var glass: TextView
    private lateinit var params: WindowManager.LayoutParams

    override fun onBind(intent: Intent?): IBinder? = null

    private fun glassWidth(): Int = if (glass.width > 0) glass.width else 160

    private fun edgeX(): Int {
        val sw = resources.displayMetrics.widthPixels
        val w = glassWidth()
        return if (params.x + w / 2 < sw / 2) 0 else sw - w
    }

    private val snapRunnable = Runnable { snapToEdge() }

    private fun snapToEdge() {
        val a = ValueAnimator.ofInt(params.x, edgeX())
        a.duration = 250
        a.addUpdateListener {
            params.x = it.animatedValue as Int
            if (glass.isAttachedToWindow) wm.updateViewLayout(glass, params)
        }
        a.start()
    }

    override fun onCreate() {
        super.onCreate()
        startForeground(1, buildNotification())
        wm = getSystemService(WINDOW_SERVICE) as WindowManager

        glass = TextView(this)
        glass.text = "🔍"
        glass.textSize = 46f
        glass.setPadding(12, 12, 12, 12)

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.START
        params.x = resources.displayMetrics.widthPixels - 160
        params.y = 600

        var startX = 0
        var startY = 0
        var touchX = 0f
        var touchY = 0f
        var moved = false

        glass.setOnTouchListener { _: View, e: MotionEvent ->
            when (e.action) {
                MotionEvent.ACTION_DOWN -> {
                    glass.removeCallbacks(snapRunnable)
                    startX = params.x
                    startY = params.y
                    touchX = e.rawX
                    touchY = e.rawY
                    moved = false
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (e.rawX - touchX).toInt()
                    val dy = (e.rawY - touchY).toInt()
                    if (Math.abs(dx) > 15 || Math.abs(dy) > 15) moved = true
                    params.x = startX + dx
                    params.y = startY + dy
                    wm.updateViewLayout(glass, params)
                }
                MotionEvent.ACTION_UP -> {
                    if (!moved) {
                        snapToEdge()
                    } else {
                        val svc = ReaderService.instance
                        if (svc == null) {
                            Toast.makeText(
                                this,
                                "আগে Accessibility থেকে চালু করুন",
                                Toast.LENGTH_LONG
                            ).show()
                            snapToEdge()
                        } else {
                            val x = e.rawX.toInt()
                            val y = e.rawY.toInt()
                            glass.visibility = View.INVISIBLE
                            glass.postDelayed({
                                svc.readAt(x, y)
                                glass.visibility = View.VISIBLE
                            }, 150)
                            glass.postDelayed(snapRunnable, 3500)
                        }
                    }
                }
            }
            true
        }

        wm.addView(glass, params)
    }

    private fun buildNotification(): Notification {
        val channelId = "glass"
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(
                NotificationChannel(channelId, "Nayan Lens", NotificationManager.IMPORTANCE_LOW)
            )
        }
        val b = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            Notification.Builder(this, channelId)
        else
            Notification.Builder(this)
        return b.setContentTitle("Nayan Lens is on")
            .setSmallIcon(android.R.drawable.ic_menu_search)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::glass.isInitialized) {
            glass.removeCallbacks(snapRunnable)
            wm.removeView(glass)
        }
    }
}
