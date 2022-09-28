package com.qichuang.commonlibs.basic

import android.view.LayoutInflater
import androidx.viewbinding.ViewBinding
import java.lang.reflect.ParameterizedType

/**
 * author : xpSun
 * date : 11/8/21
 * description :
 */
interface CustomInitRootViewListener {

    /**
     * 通过反射获取rootView
     */
    fun <T : ViewBinding?> rootViewByReflect(layoutInflater: LayoutInflater?): T? {
        var vb: T? = null
        val type = this.javaClass.genericSuperclass
        if (type is ParameterizedType) {
            try {
                val clazz = type.actualTypeArguments[0] as Class<T>
                val method = clazz.getMethod("inflate", LayoutInflater::class.java)
                vb = method.invoke(null, layoutInflater) as T
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return vb
    }
}