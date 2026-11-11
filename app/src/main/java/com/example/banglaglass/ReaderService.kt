package com.example.banglaglass

import android.accessibilityservice.AccessibilityService
import android.graphics.Rect
import android.speech.tts.TextToSpeech
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import java.util.Locale

class ReaderService : AccessibilityService(), TextToSpeech.OnInitListener {

    companion object {
        var instance: ReaderService? = null
    }

    private var tts: TextToSpeech? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        tts = TextToSpeech(this, this)
    }

    override fun onInit(status: Int) {}
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}

    override fun onDestroy() {
        instance = null
        tts?.shutdown()
        super.onDestroy()
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    fun readAt(x: Int, y: Int) {
        val root = rootInActiveWindow
        val text = findText(root, x, y)
        if (text == null) {
            toast("No text here")
            return
        }
        translateAndSpeak(text)
    }

    private fun findText(node: AccessibilityNodeInfo?, x: Int, y: Int): String? {
        if (node == null) return null
        val r = Rect()
        node.getBoundsInScreen(r)
        if (!r.contains(x, y)) return null
        for (i in 0 until node.childCount) {
            val found = findText(node.getChild(i), x, y)
            if (found != null) return found
        }
        val t = node.text?.toString() ?: node.contentDescription?.toString()
        return if (!t.isNullOrBlank()) t else null
    }

    private fun speak(text: String, lang: String) {
        tts?.language = Locale.forLanguageTag(lang)
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "bg")
    }

    private fun translateAndSpeak(text: String) {
        val lang = MainActivity.targetLang
        val target = TranslateLanguage.fromLanguageTag(lang)
        if (target == null || lang == "en") {
            speak(text, "en")
            return
        }
        toast("Translating...")
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(target)
            .build()
        val translator = Translation.getClient(options)
        translator.downloadModelIfNeeded()
            .addOnSuccessListener {
                translator.translate(text)
                    .addOnSuccessListener { result ->
                        toast(result)
                        speak(result, lang)
                    }
                    .addOnFailureListener {
                        toast("Translation failed")
                    }
            }
            .addOnFailureListener {
                toast("Language download failed. Check internet")
            }
    }
}
