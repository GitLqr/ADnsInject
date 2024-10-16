package com.gitlqr.adnsinject.demo

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.gitlqr.adnsinject.ADnsInject
import com.zhy.http.okhttp.OkHttpUtils
import com.zhy.http.okhttp.callback.StringCallback
import okhttp3.Call
import java.lang.Exception

/**
 * 主界面
 *
 * @author LQR
 * @since 2024/10/16
 */
class MainActivity : AppCompatActivity(), View.OnClickListener {

    private val hostMap by lazy {
        hashMapOf("www.baidu.com" to "8.8.4.8")
    }

    private val httpUrl = "https://www.baidu.com/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        listOf<View>(
                findViewById(R.id.btnInject),
                findViewById(R.id.btnHttp),
                findViewById(R.id.btnWebView)
        ).forEach { it.setOnClickListener(this) }
        startADnsInject()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopADnsInject()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnInject -> {
                ADnsInject.get().setHostMap(hostMap)
            }
            R.id.btnHttp -> {
                OkHttpUtils.get()
                        .url(httpUrl)
                        .build()
                        .execute(object : StringCallback() {
                            override fun onResponse(response: String?, id: Int) {
                                Log.e("ANoSSL", response)
                                Toast.makeText(this@MainActivity, "请求成功", Toast.LENGTH_SHORT).show()
                            }

                            override fun onError(call: Call?, e: Exception?, id: Int) {
                                e?.printStackTrace()
                                Toast.makeText(this@MainActivity, "请求失败", Toast.LENGTH_SHORT).show()
                            }
                        })
            }
            R.id.btnWebView -> {
                // TODO: 2024/10/16 目前 dns 注入暂无法对 webview 生效
                WebViewActivity.launch(this, "https://www.baidu.com")
            }
        }
    }

    /**
     * 开启 dns 注入功能
     * 注：如果希望长期有效，且不会关闭，那么建议在自定义 Application 中启用，这样可以在多进程场景下都能生效。
     */
    private fun startADnsInject() {
        ADnsInject.get().setLoggerEnable(true)
        ADnsInject.get().start()
    }

    /**
     * 停用 dns 注入功能
     */
    private fun stopADnsInject() {
        ADnsInject.get().stop()
    }
}