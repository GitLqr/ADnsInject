package com.gitlqr.adnsinject.demo

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity

/**
 * web界面
 *
 * @author LQR
 * @since 2024/10/16
 */
class WebViewActivity : AppCompatActivity() {

    companion object {
        private const val PARAMS_URL = "url"

        fun launch(activity: Activity, url: String) {
            Intent(activity, WebViewActivity::class.java).apply {
                putExtra(PARAMS_URL, url)
            }.let {
                activity.startActivity(it)
            }
        }
    }

    private val webview by lazy {
        WebView(applicationContext).apply {
            // 支持js
            settings.javaScriptEnabled = true
            // 解决图片不显示
            settings.blockNetworkImage = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            }
            // 自适应屏幕
            settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
            // 不支持手势缩放
            settings.setSupportZoom(false)
            settings.builtInZoomControls = false
            // 解决白屏问题
            settings.domStorageEnabled = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val url = intent.getStringExtra(PARAMS_URL)
        if (TextUtils.isEmpty(url)) return finish()
        setContentView(R.layout.activity_webview)
        findViewById<FrameLayout>(R.id.flContainer).addView(webview)
        webview.loadUrl(url)
    }

    override fun onDestroy() {
        webview.loadUrl("about:blank")
        webview.parent?.let {
            (it as ViewGroup).removeView(webview)
        }
        webview.stopLoading()
        webview.settings?.javaScriptEnabled = false
        webview.clearHistory()
        webview.clearCache(true)
        webview.removeAllViewsInLayout()
        webview.removeAllViews()
        webview.webViewClient = null
        webview.webChromeClient = null
        webview.destroy()
        super.onDestroy()
    }

}