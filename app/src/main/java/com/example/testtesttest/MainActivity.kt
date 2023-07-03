package com.example.testtesttest

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebView
import android.view.KeyEvent
import android.view.View
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Инициализация WebView и SharedPreferences
        webView = findViewById(R.id.WedView)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        // Инициализация Firebase Remote Config
        val remoteConfig = Firebase.remoteConfig

        // Установка значений по умолчанию для Remote Config
        val configDefaults = hashMapOf<String, Any>(
            "url" to "https://example.com"
        )
        remoteConfig.setDefaultsAsync(configDefaults)

        // Загрузка и активация конфигурации из Firebase Remote Config
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val url = remoteConfig.getString("url")
                    handleUrl(url) // Обработка полученной ссылки
                } else {
                    val url = "https://example.com"
                    handleUrl(url) // Обработка ссылки по умолчанию
                }
            }
    }

    // Функция обработки ссылки
    private fun handleUrl(url: String) {
        val savedUrl = sharedPreferences.getString("saved_url", null)
        if (savedUrl != null) {
            loadUrl(savedUrl) // Загрузка сохраненной ссылки
        } else {
            if (url.isEmpty() || isGoogleBrand() || isEmulator()) {
                loadStubUrl() // Загрузка экрана заглушки
            } else {
                sharedPreferences.edit().putString("saved_url", url).apply()
                loadUrl(url) // Загрузка полученной ссылки
            }
        }

        if (webView.url == url) {
            webView.setOnKeyListener(View.OnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP && webView.canGoBack()) {
                    webView.goBack()
                    return@OnKeyListener true
                }
                false
            })
        } else {
            webView.setOnKeyListener(null)
        }
    }

    // Функция загрузки ссылки в WebView
    private fun loadUrl(url: String) {
        webView.loadUrl(url)
    }

    // Функция проверки, является ли устройство брендом Google
    private fun isGoogleBrand(): Boolean {
        val brand = android.os.Build.BRAND
        return brand.equals("google", ignoreCase = true)
    }

    // Функция проверки, работает ли приложение на эмуляторе
    private fun isEmulator(): Boolean {
        return android.os.Build.FINGERPRINT.contains("generic")
    }

    // Функция загрузки экрана заглушки
    private fun loadStubUrl() {
        val intent = Intent(this, StubActivity::class.java)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        val savedUrl = sharedPreferences.getString("saved_url", null)
        if (savedUrl != null) {
            loadUrl(savedUrl)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        webView.saveState(outState) // Сохранение состояния WebView при повороте экрана
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        webView.restoreState(savedInstanceState) // Восстановление состояния WebView после поворота экрана
    }
}
