package com.example.banglaglass

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    companion object {
        var targetLang: String = "bn"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(48, 96, 48, 48)

        val title = TextView(this)
        title.text = "BanglaGlass"
        title.textSize = 28f
        layout.addView(title)

        val toggle = Switch(this)
        toggle.text = "গ্লাস চালু করুন"
        toggle.textSize = 20f
        layout.addView(toggle)

        val label = TextView(this)
        label.text = "\nযে ভাষায় শুনতে চান:"
        label.textSize = 18f
        layout.addView(label)

        val names = listOf("বাংলা", "English", "हिन्दी", "العربية", "Español", "Français")
        val codes = listOf("bn", "en", "hi", "ar", "es", "fr")
        val spinner = Spinner(this)
        spinner.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, names
        )
        spinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: android.widget.AdapterView<*>?, v: android.view.View?, pos: Int, id: Long) {
                targetLang = codes[pos]
            }
            override fun onNothingSelected(p: android.widget.AdapterView<*>?) {}
        }
        layout.addView(spinner)

        setContentView(layout)

        toggle.setOnCheckedChangeListener { _, isOn ->
            if (isOn) {
                if (!Settings.canDrawOverlays(this)) {
                    Toast.makeText(this, "অনুমতি দিন, তারপর ফিরে এসে আবার চালু করুন", Toast.LENGTH_LONG).show()
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
                }
            } else {
                stopService(Intent(this, GlassService::class.java))
            }
        }
    }
}
