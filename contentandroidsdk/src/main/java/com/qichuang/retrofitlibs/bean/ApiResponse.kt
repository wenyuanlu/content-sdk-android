package com.qichuang.retrofitlibs.bean

open class BasicResponse<T>(
    var data: T? = null,
    var msg: String? = null,
    var status: Int? = null
)

class CommonRetrofitBaseJsonResponse : BasicResponse<Any>()

abstract class CustomBasicParam