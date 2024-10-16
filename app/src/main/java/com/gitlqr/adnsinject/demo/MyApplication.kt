package com.gitlqr.adnsinject.demo

import android.app.Application
import android.content.Context
import com.gitlqr.anossl.NoSSLSocketClient
import com.zhy.http.okhttp.OkHttpUtils
import com.zhy.http.okhttp.log.LoggerInterceptor
import me.weishu.reflection.Reflection
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * 自定义 Application
 *
 * @author LQR
 * @since 2024/10/16
 */
class MyApplication : Application() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        Reflection.unseal(base)
    }

    override fun onCreate() {
        super.onCreate()
        initOkhttpUtils()
    }

    private fun initOkhttpUtils() {
        // 设置可访问所有的https网站
        val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(LoggerInterceptor("OkHttpUtils"))
                .connectTimeout(5000L, TimeUnit.MILLISECONDS)
                .readTimeout(5000L, TimeUnit.MILLISECONDS)
                // SSL 证书
                // .sslSocketFactory(
                //     NoSSLSocketClient.getSSLSocketFactory(),
                //     NoSSLSocketClient.getX509TrustManager()
                // )
                // TLS 证书
                .sslSocketFactory(
                        NoSSLSocketClient.getTLSSocketFactory(),
                        NoSSLSocketClient.getX509TrustManager()
                )
                .hostnameVerifier(NoSSLSocketClient.getHostnameVerifier())
                .build()
        OkHttpUtils.initClient(okHttpClient)
    }
}