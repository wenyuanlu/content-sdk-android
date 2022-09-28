package com.maishuo.haohai.common.dialog

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.maishuo.haohai.R
import com.maishuo.haohai.databinding.ViewCommonDialogLayoutBinding
import com.maishuo.haohai.listener.OnDialogCallBackListener
import com.qichuang.commonlibs.basic.BaseDialog
import com.qichuang.commonlibs.utils.ScreenUtils

/**
 * author : xpSun
 * date : 10/13/21
 * description :
 */
class CommonDialog constructor(activity: AppCompatActivity?) :
    BaseDialog<ViewCommonDialogLayoutBinding>(activity, R.style.CustomDialog) {

    var showLeft: Boolean? = null
        set(value) {
            field = value
            vb?.btDialogCommonCancle?.visibility =
                if (value == true)
                    View.VISIBLE
                else
                    View.GONE
        }
    var showRight: Boolean? = null
        set(value) {
            field = value
            vb?.btDialogCommonSure?.visibility =
                if (value == true)
                    View.VISIBLE
                else
                    View.GONE
        }
    var title: String? = null
        set(value) {
            field = value
            vb?.tvDialogCommonTitle?.text = value
        }
    var content: String? = null
        set(value) {
            field = value
            vb?.tvDialogCommonContent?.text = value
        }
    var leftText: String? = null
        set(value) {
            field = value
            vb?.btDialogCommonCancle?.text = value
        }
    var rightText: String? = null
        set(value) {
            field = value
            vb?.btDialogCommonSure?.text = value
        }
    var onDialogBackListener: OnDialogCallBackListener? = null

    override fun initWidgets() {
        val width = ScreenUtils.getScreenWidth(activity) * 0.9
        setGravity(width = width.toInt())

        vb?.let {
            it.btDialogCommonCancle.setOnClickListener {
                onDialogBackListener?.onCancel()
                dismiss()
            }
            it.btDialogCommonSure.setOnClickListener {
                onDialogBackListener?.onConfirm("")
                dismiss()
            }
        }
    }
}