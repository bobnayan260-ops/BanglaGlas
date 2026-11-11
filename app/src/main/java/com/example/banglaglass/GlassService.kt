package com.example.banglaglass

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
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

    override fun onCreate() {
        super.onCreate()
        startForeground(1, buildNotification())
        wm = getSystemService(WINDOW_SERVICE) as WindowManager

        glass = TextView(this)
        glass.text = "🔍"
        glass.textSize = 34f
        glass.gravity = Gravity.CENTER
        glass.typeface = Typeface.DEFAULT_BOLD
        glass.setPadding(28, 28, 28, 28)
        val bg = GradientDrawable()
        bg.shape = GradientDrawable.OVAL
        bg.setColor(Color.argb(210, 21, 101, 192))
        bg.setStroke(4, Color.WHITE)
        glass.background = bg

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
        params.x = 100
        params.y = 300

        var startX = 0
        var startY = 0
        var touchX = 0f
        var touchY = 0f

        glass.setOnTouchListener { _: View, e: MotionEvent ->
            when (e.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = params.x
                    startY = params.y
                    touchX = e.rawX
                    touchY = e.rawY
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = startX + (e.rawX - touchX).toInt()
                    params.y = startY + (e.rawY - touchY).toInt()
                    wm.updateViewLayout(glass, params)
                }
                MotionEvent.ACTION_UP -> {
                    val svc = ReaderService.instance
                    if (svc == null) {
                        Toast.makeText(
                            this,
                            "আগে Accessibility থেকে BanglaGlass চালু করুন",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        val x = e.rawX.toInt()
                        val y = e.rawY.toInt()
                        glass.visibility = View.INVISIBLE
                        glass.postDelayed({
                            svc.readAt(x, y)
                            glass.visibility = View.VISIBLE
                        }, 150)
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
                NotificationChannel(channelId, "BanglaGlass", NotificationManager.IMPORTANCE_LOW)
            )
        }
        val b = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            Notification.Builder(this, channelId)
        else
            Notification.Builder(this)
        return b.setContentTitle("BanglaGlass চালু আছে")
            .setSmallIcon(android.R.drawable.ic_menu_search)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::glass.isInitialized) wm.removeView(glass)
    }
}
