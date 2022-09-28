package com.qichuang.retrofitlibs.retrofit

import android.text.TextUtils
import com.qichuang.commonlibs.utils.PhoneUtils
import com.qichuang.commonlibs.utils.PreferencesUtils
import com.qichuang.retrofitlibs.config.ApiConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.concurrent.TimeUnit

class ApiServiceFactory private constructor() {

    companion object {
        val instance: ApiServiceFactory by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            ApiServiceFactory()
        }
    }

    private var retrofit: Retrofit? = null

    init {
        init(null);
    }

    fun init(okHttpBuilder: OkHttpClient.Builder?) {
        val okHttpClient: OkHttpClient.Builder = okHttpBuilder ?: fetchBuilder()

        okHttpClient.addInterceptor(CommonBodyInterceptor())

        val httpLoggingInterceptor = HttpLoggingInterceptor(CommonHttpLogger())
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        okHttpBuilder?.addNetworkInterceptor(httpLoggingInterceptor)

        val proxyIp = PreferencesUtils.getString("ProxyIp")
        if(!TextUtils.isEmpty(proxyIp) && PhoneUtils.pingProxy(proxyIp)){
            val proxy = Proxy(Proxy.Type.HTTP, InetSocketAddress(proxyIp, 8888));
            okHttpClient.proxy(proxy)
        }

        val baseUrl: String = ApiConstants.fetchBaseUrl()

        retrofit = Retrofit.Builder()
                //添加Gson数据格式转换器支持
                .addConverterFactory(CommonJsonConverterFactory.create())
                //添加RxJava语言支持
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                ////指定网络请求client
                .client(okHttpClient.build())
                .baseUrl(baseUrl)
                .build()
    }

    private fun fetchBuilder(): OkHttpClient.Builder {
        val builder: OkHttpClient.Builder = OkHttpClient.Builder()
        builder.connectTimeout(ApiConfig.CONNECT_TIME_OUT, TimeUnit.MILLISECONDS)
        builder.readTimeout(ApiConfig.READ_TIME_OUT, TimeUnit.MILLISECONDS)
        builder.writeTimeout(ApiConfig.WRITE_TIME_OUT, TimeUnit.MILLISECONDS)
        return builder
    }

    fun getRetrofit(): Retrofit {
        checkNotNull(retrofit) { "Retrofit instance hasn't init!" }

        return retrofit!!
    }
}