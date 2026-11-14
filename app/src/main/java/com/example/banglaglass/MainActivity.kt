package com.example.banglaglass

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    companion object {
        var targetLang: String = "bn"
        var uiBn: Boolean = true
        var on: Boolean = false
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    private fun t(bn: String, en: String): String = if (uiBn) bn else en

    private fun tv(text: String, size: Float, color: Int, bold: Boolean = false, center: Boolean = false): TextView {
        val v = TextView(this)
        v.text = text
        v.textSize = size
        v.setTextColor(color)
        if (bold) v.typeface = Typeface.DEFAULT_BOLD
        if (center) v.gravity = Gravity.CENTER_HORIZONTAL
        return v
    }

    private fun pill(text: String, sel: Boolean, click: () -> Unit): TextView {
        val p = tv(text, 13f, Color.parseColor(if (sel) "#0C447C" else "#333333"), sel, false)
        p.gravity = Gravity.CENTER
        val bg = GradientDrawable()
        bg.cornerRadius = dp(16).toFloat()
        bg.setColor(Color.parseColor(if (sel) "#E6F1FB" else "#FFFFFF"))
        bg.setStroke(dp(1), Color.parseColor(if (sel) "#85B7EB" else "#CCCCCC"))
        p.background = bg
        p.setPadding(dp(4), dp(9), dp(4), dp(9))
        val lp = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        lp.setMargins(dp(3), dp(3), dp(3), dp(3))
        p.layoutParams = lp
        p.setOnClickListener { click() }
        return p
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        render()
    }

    private fun render() {
        val blue = Color.parseColor("#0C447C")
        val coral = Color.parseColor("#D85A30")

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(Color.WHITE)

        val top = LinearLayout(this)
        top.orientation = LinearLayout.HORIZONTAL
        top.gravity = Gravity.CENTER_VERTICAL
        top.setPadding(dp(20), dp(40), dp(20), 0)
        top.addView(tv("LENS", 13f, blue, true))
        val sp = View(this)
        sp.layoutParams = LinearLayout.LayoutParams(0, 1, 1f)
        top.addView(sp)
        val lb = tv(if (uiBn) "English" else "বাংলা", 14f, blue, true)
        lb.setOnClickListener {
            uiBn = !uiBn
            render()
        }
        top.addView(lb)
        root.addView(top)

        val name = tv("Nayan", 48f, blue, true, true)
        name.letterSpacing = 0.08f
        name.setPadding(0, dp(28), 0, 0)
        root.addView(name)

        val bar = View(this)
        val bl = LinearLayout.LayoutParams(dp(56), dp(3))
        bl.gravity = Gravity.CENTER_HORIZONTAL
        bl.setMargins(0, dp(8), 0, dp(6))
        bar.layoutParams = bl
        bar.setBackgroundColor(coral)
        root.addView(bar)
        root.addView(tv("noyon", 22f, coral, true, true))
        val swLp = LinearLayout.LayoutParams(dp(130), dp(66))
        swLp.gravity = Gravity.CENTER_HORIZONTAL
        swLp.setMargins(0, dp(30), 0, dp(14))
        val sw = FrameLayout(this)
        sw.layoutParams = swLp
        val swBg = GradientDrawable()
        swBg.cornerRadius = dp(33).toFloat()
        swBg.setColor(Color.parseColor(if (on) "#4CAF50" else "#E4E4E8"))
        sw.background = swBg
        val knob = View(this)
        val kd = GradientDrawable()
        kd.shape = GradientDrawable.OVAL
        kd.setColor(Color.WHITE)
        kd.setStroke(dp(1), Color.parseColor("#CCCCCC"))
        knob.background = kd
        val kLp = FrameLayout.LayoutParams(dp(54), dp(54))
        kLp.topMargin = dp(6)
        kLp.leftMargin = if (on) dp(70) else dp(6)
        knob.layoutParams = kLp
        sw.addView(knob)
        root.addView(sw)

        sw.setOnClickListener {
            if (!on) {
                if (!Settings.canDrawOverlays(this)) {
                    Toast.makeText(this, t("অনুমতি দিন, তারপর ফিরে এসে আবার চাপুন", "Allow the permission, then come back and tap again"), Toast.LENGTH_LONG).show()
                    startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
                } else {
                    val svcIntent = Intent(this, GlassService::class.java)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(svcIntent) else startService(svcIntent)
                    on = true
                    render()
                }
            } else {
                stopService(Intent(this, GlassService::class.java))
                on = false
                render()
            }
        }

        val status = tv(if (on) t("চালু আছে", "On") else t("বন্ধ আছে", "Off"), 22f, Color.parseColor(if (on) "#2E7D32" else "#1A1A1A"), true, true)
        root.addView(status)
        val hint = tv(if (on) t("গ্লাস পাশে ভাসছে", "The lens is floating at the edge") else t("সুইচ চালু করলে গ্লাস ভেসে উঠবে", "Turn on to show the floating lens"), 13f, Color.parseColor("#666666"), false, true)
        hint.setPadding(dp(24), dp(4), dp(24), dp(18))
        root.addView(hint)

        val card = LinearLayout(this)
        card.orientation = LinearLayout.VERTICAL
        card.setBackgroundColor(Color.parseColor("#FAFAFA"))
        card.setPadding(dp(14), dp(16), dp(14), dp(28))
        card.addView(tv(t("ভাষা মডেল", "Language model"), 16f, Color.parseColor("#1A1A1A"), true))
        val sub = tv(t("যে ভাষায় শুনতে চান বেছে নিন", "Choose the language you want to hear"), 12f, Color.parseColor("#666666"))
        sub.setPadding(0, 0, 0, dp(8))
        card.addView(sub)

        val names = listOf("বাংলা", "English", "हिन्दी", "اردو", "العربية", "中文", "日本語", "한국어", "Español", "Français", "Deutsch", "Русский", "Türkçe", "Indonesia", "Italiano", "Português", "தமிழ்", "తెలుగు", "मराठी", "ગુજરાતી", "ಕನ್ನಡ", "Tiếng Việt", "ไทย", "فارسی")
        val codes = listOf("bn", "en", "hi", "ur", "ar", "zh", "ja", "ko", "es", "fr", "de", "ru", "tr", "id", "it", "pt", "ta", "te", "mr", "gu", "kn", "vi", "th", "fa")
        var idx = 0
        while (idx < names.size) {
            val row = LinearLayout(this)
            row.orientation = LinearLayout.HORIZONTAL
            for (j in idx until minOf(idx + 3, names.size)) {
                val code = codes[j]
                row.addView(pill(names[j], code == targetLang) {
                    targetLang = code
                    render()
                })
            }
            card.addView(row)
            idx += 3
        }

        val acc = tv(t("লেখা পড়ার অনুমতি চালু করুন", "Enable text reading"), 14f, blue, true, true)
        val accBg = GradientDrawable()
        accBg.cornerRadius = dp(10).toFloat()
        accBg.setColor(Color.parseColor("#E6F1FB"))
        acc.background = accBg
        acc.setPadding(0, dp(12), 0, dp(12))
        val accLp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        accLp.setMargins(0, dp(16), 0, 0)
        acc.layoutParams = accLp
        acc.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
        card.addView(acc)

        val yt = tv("YouTube", 15f, Color.parseColor("#791F1F"), true, true)
        val ytBg = GradientDrawable()
        ytBg.cornerRadius = dp(10).toFloat()
        ytBg.setColor(Color.parseColor("#FCEBEB"))
        yt.background = ytBg
        yt.setPadding(0, dp(12), 0, dp(12))
        val ytLp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        ytLp.setMargins(0, dp(10), 0, 0)
        yt.layoutParams = ytLp
        yt.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://youtube.com/@usaproxtv")))
        }
        card.addView(yt)

        root.addView(card)
        val scroll = ScrollView(this)
        scroll.setBackgroundColor(Color.WHITE)
        scroll.addView(root)
        setContentView(scroll)
    }
}
