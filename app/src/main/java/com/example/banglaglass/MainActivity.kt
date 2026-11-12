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
        root.addView(tv("নয়ন", 22f, coral, true, true))
