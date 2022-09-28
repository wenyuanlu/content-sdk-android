package com.qichuang.retrofitlibs.dialog

import androidx.appcompat.app.AppCompatActivity
import com.qichuang.commonlibs.utils.LoggerUtils

/**
 * 耗时对话框工具类
 */
class ProgressDialogUtil {

    companion object {
        val instance: ProgressDialogUtil by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            ProgressDialogUtil()
        }
    }

    private val loadingDialog: MutableList<ProgressDialog> = mutableListOf()

    /**
     * 弹出耗时对话框
     */
    @JvmOverloads
    fun showProgressDialog(activity: AppCompatActivity?, cancelable: Boolean = false, message: String? = null) {
        try {
            if (null == activity) {
                return
            }

            val dialog = ProgressDialog(activity)
            dialog.message = message ?: "请稍候..."
            dialog.cancelable = cancelable
            dialog.showDialog()
            loadingDialog.add(dialog)
        } catch (e: Exception) {
            LoggerUtils.e(e.toString())
        }
    }

    /**
     * 隐藏耗时对话框
     */
    fun dismiss() {
        dismissLoadingDialog()
    }

    private fun dismissLoadingDialog() {
        try {
            for (dialog in loadingDialog.iterator()) {
                if (dialog.isShowing()) {
                    dialog.dismiss()
                }
            }

            loadingDialog.clear()
        } catch (e: Exception) {
            LoggerUtils.e(e.toString())
        }
    }
}