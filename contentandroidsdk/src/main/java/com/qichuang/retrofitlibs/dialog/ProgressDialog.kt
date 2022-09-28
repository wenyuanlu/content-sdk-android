package com.qichuang.retrofitlibs.dialog

import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.maishuo.haohai.R
import com.maishuo.haohai.databinding.ViewProgressDialogLayoutBinding
import com.qichuang.commonlibs.basic.BaseDialog

/**
 * author : xpSun
 * date : 10/25/21
 * description : 通用的等待加载框
 */
class ProgressDialog constructor(appCompatActivity: AppCompatActivity?) :
    BaseDialog<ViewProgressDialogLayoutBinding>(appCompatActivity, R.style.transparentDialogStyle) {

    var message: String? = null
        set(value) {
            field = value
            vb?.progressTvShow?.text = value
        }

    var cancelable: Boolean? = null
        set(value) {
            field = value
            setCancelable(cancelable ?: false)
        }

    override fun initWidgets() {
        setGravity(
            width = ViewGroup.LayoutParams.WRAP_CONTENT,
            height = ViewGroup.LayoutParams.WRAP_CONTENT
        )

        try {
            activity?.let {
                Glide.with(it).asGif().load(R.mipmap.load_gif).into(vb?.progressLoad!!)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun showDialog() {
        super.showDialog()
        try {
            val drawable = vb?.progressLoad?.drawable

            if (drawable is GifDrawable) {
                if (!drawable.isRunning) {
                    drawable.start()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun dismiss() {
        super.dismiss()

        try {
            val drawable = vb?.progressLoad?.drawable

            if (drawable is GifDrawable) {
                if (drawable.isRunning) {
                    drawable.stop()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}