package com.qichuang.retrofitlibs.retrofit

/**
 * 网络请求失败原因
 */
enum class CommonExceptionReason {
    //网络问题
    BAD_NETWORK,
    //连接错误
    CONNECT_ERROR,
    //连接超时
    CONNECT_TIMEOUT,
    //解析数据失败
    PARSE_ERROR,
    //未知错误
    UNKNOWN_ERROR
}