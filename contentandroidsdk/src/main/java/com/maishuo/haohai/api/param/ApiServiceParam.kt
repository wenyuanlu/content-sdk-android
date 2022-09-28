package com.maishuo.haohai.api.param

import com.qichuang.retrofitlibs.bean.CustomBasicParam

/**
 * author : xpSun
 * date : 11/11/21
 * description :
 */

data class VerifyCodeParam(
    var phone: String? = null
)

data class LoginParam(
    var phone: String? = null,
    var code: String? = null
)

data class BindPhoneParam(
    var phone: String? = null,
    var code: String? = null
)

//友盟一键
data class UMengLoginParam(
    var umengToken: String? = null,
) : CustomBasicParam()