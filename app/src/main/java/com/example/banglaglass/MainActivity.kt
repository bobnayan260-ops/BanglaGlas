package com.example.banglaglass

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    companion object {
        var targetLang: String = "bn"
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    private fun card(): LinearLayout {
        val c = LinearLayout(this)
        c.orientation = LinearLayout.VERTICAL
        c.setPadding(dp(18), dp(16), dp(18), dp(16))
        val bg = GradientDrawable()
        bg.setColor(Color.WHITE)
        bg.cornerRadius = dp(16).toFloat()
        c.background = bg
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        lp.setMargins(0, 0, 0, dp(14))
        c.layoutParams = lp
        return c
    }

    private fun label(t: String, size: Float, color: Int, bold: Boolean): TextView {
        val tv = TextView(this)
        tv.text = t
        tv.textSize = size
        tv.setTextColor(color)
        if (bold) tv.typeface = Typeface.DEFAULT_BOLD
        return tv
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(Color.parseColor("#EEF3FB"))

        val header = LinearLayout(this)
        header.orientation = LinearLayout.VERTICAL
        header.setPadding(dp(22), dp(48), dp(22), dp(24))
        header.setBackgroundColor(Color.parseColor("#1565C0"))
        header.addView(label("BanglaGlass", 28f, Color.WHITE, true))
        header.addView(label("ইংরেজি লেখা বাংলায় পড়ুন ও শুনুন", 15f, Color.parseColor("#D6E4F7"), false))
        root.addView(header)

        val body = LinearLayout(this)
        body.orientation = LinearLayout.VERTICAL
        body.setPadding(dp(16), dp(16), dp(16), dp(16))

        val c1 = card()
        val status = label("গ্লাস বন্ধ আছে", 20f, Color.parseColor("#C62828"), true)
        c1.addView(status)
        val toggle = Switch(this)
        toggle.text = "গ্লাস চালু করুন"
        toggle.textSize = 18f
        toggle.setPadding(0, dp(10), 0, 0)
        c1.addView(toggle)
        body.addView(c1)
        val c2 = card()
        c2.addView(label("যে ভাষায় শুনতে চান", 16f, Color.parseColor("#37474F"), true))
        val names = listOf("বাংলা", "English", "हिन्दी", "العربية", "Español", "Français")
        val codes = listOf("bn", "en", "hi", "ar", "es", "fr")
        val spinner = Spinner(this)
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, names)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                targetLang = codes[pos]
            }
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }
        c2.addView(spinner)
        body.addView(c2)

        val c3 = card()
        c3.addView(label("লেখা পড়ার অনুমতি", 16f, Color.parseColor("#37474F"), true))
        c3.addView(label("একবার চালু করলেই হবে", 13f, Color.parseColor("#78909C"), false))
        val accBtn = Button(this)
        accBtn.text = "Accessibility"
        accBtn.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
        c3.addView(accBtn)
        body.addView(c3)

        val c4 = card()
        c4.addView(label("কীভাবে ব্যবহার করবেন", 16f, Color.parseColor("#37474F"), true))
        c4.addView(
            label(
                "১. গ্লাস চালু করুন\n২. মেসেঞ্জারে ইংরেজি মেসেজ খুলুন\n৩. নীল গ্লাস টেনে লেখার উপর ছেড়ে দিন\n৪. বাংলা অনুবাদ শুনুন",
                15f, Color.parseColor("#455A64"), false
            )
        )
        body.addView(c4)

        val scroll = ScrollView(this)
        scroll.addView(body)
        root.addView(scroll)
        setContentView(root)

        toggle.setOnCheckedChangeListener { _, isOn ->
            if (isOn) {
                if (!Settings.canDrawOverlays(this)) {
                    Toast.makeText(this, "Permission needed", Toast.LENGTH_LONG).show()
                    startActivity(
                        Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:$packageName")
                        )
                    )
                    toggle.isChecked = false
                } else {
                    val i = Intent(this, GlassService::class.java)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(i) else startService(i)
                    status.text = "গ্লাস চালু আছে"
                    status.setTextColor(Color.parseColor("#2E7D32"))
                }
            } else {
                stopService(Intent(this, GlassService::class.java))
                status.text = "গ্লাস বন্ধ আছে"
                status.setTextColor(Color.parseColor("#C62828"))
            }
        }
    }
}
