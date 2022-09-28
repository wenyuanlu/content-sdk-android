package com.qichuang.retrofitlibs.retrofit

enum class CommonResCodeEnum(
    val recCode: Int,
    val rawType: String
) {
    RES_CODE_200(200, ""),
    RES_CODE_100(100, "账户登录状态已失效,请重新登录"),
    RES_CODE_101(101, "token不能为空"),
    RES_CODE_102(102, "登录信息错误请重新登录"),
    RES_CODE_107(107, "账户已被他人登录若怀疑账户信息已泄露,请及时修改密码"),
    RES_CODE_111(111, "请求太频繁,请稍后"),
    RES_CODE_404(404, "找不到资源"),
    RES_CODE_502(502, "找不到资源")
}