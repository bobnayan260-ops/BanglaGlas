package com.example.banglaglass

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import java.util.Locale

class GlassService : Service(), TextToSpeech.OnInitListener {

    private lateinit var wm: WindowManager
    private lateinit var glass: TextView
    private lateinit var params: WindowManager.LayoutParams
    private var tts: TextToSpeech? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForeground(1, buildNotification())
        tts = TextToSpeech(this, this)
        wm = getSystemService(WINDOW_SERVICE) as WindowManager

        glass = TextView(this)
        glass.text = "🔍"
        glass.textSize = 36f
        glass.setBackgroundColor(Color.argb(160, 30, 30, 30))
        glass.setPadding(24, 24, 24, 24)

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
                    Toast.makeText(
                        this,
                        "এই সংস্করণে স্ক্রিনের লেখা পড়ার অংশ এখনো যোগ হয়নি",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            true
        }

        wm.addView(glass, params)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.forLanguageTag(MainActivity.targetLang)
        }
    }

    fun translateAndSpeak(text: String) {
        val target = TranslateLanguage.fromLanguageTag(MainActivity.targetLang) ?: return
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(target)
            .build()
        val translator = Translation.getClient(options)
        translator.downloadModelIfNeeded()
            .addOnSuccessListener {
                translator.translate(text).addOnSuccessListener { result ->
                    tts?.language = Locale.forLanguageTag(MainActivity.targetLang)
                    tts?.speak(result, TextToSpeech.QUEUE_FLUSH, null, "bg")
                }
            }
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
        tts?.shutdown()
    }
}
