package com.qichuang.commonlibs.basic

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.maishuo.haohai.R

/**
 * author : xpSun
 * date : 2021/3/16
 * description :
 */
abstract class BaseDialogFragment : DialogFragment,
        BasicCommonWidgetListener, View.OnClickListener {

    constructor()

    constructor(appCompatActivity: AppCompatActivity?) {
        this.appCompatActivity = appCompatActivity
    }

    var appCompatActivity: AppCompatActivity? = null

    var onDismissListener: DialogInterface.OnDismissListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.commonDialogFragmentStyle)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return fetchRootView()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnDismissListener {
            onDismissListener?.onDismiss(it)
        }
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initWidgets()
        initWidgetsEvent()
    }

    override fun initWidgetsEvent() {

    }

    override fun onClick(v: View) {}

    override fun isShowCustomTitle(visibility: Boolean?) {

    }

    @JvmOverloads
    fun initWidgetSize(
            gravity: Int,
            width: Int? = null,
            height: Int? = null,
            x: Int? = 0,
            y: Int? = 0
    ) {
        val dialog = dialog ?: return
        val window = dialog.window
        val attributes = window?.attributes
        if (0 != x) {
            attributes?.x = x
        }
        if (0 != y) {
            attributes?.y = y
        }
        attributes?.gravity = gravity
        window?.attributes = attributes
        dialog.window?.setLayout(
                width ?: ViewGroup.LayoutParams.MATCH_PARENT,
                height ?: ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    //适合底部显示
    fun setContainerBackgroundResource(res: Int?) {
        if (null != res) {
            fetchRootView()?.setBackgroundResource(res)
        }
    }

    //适合居中显示
    fun setWindowBackgroundResource(res: Int?) {
        if (null == res) {
            return
        }

        appCompatActivity?.let {
            dialog?.window?.setBackgroundDrawable(ContextCompat.getDrawable(it, res))
        }
    }

    @JvmOverloads
    open fun showDialog(tag: String? = "") {
        appCompatActivity?.let {
            show(it.supportFragmentManager, tag)
        }
    }

    @JvmOverloads
    open fun showNowDialog(tag: String? = "") {
        appCompatActivity?.let {
            showNow(it.supportFragmentManager, tag)
        }
    }

    fun isShowing(): Boolean {
        val dialog = dialog ?: return false
        return dialog.isShowing
    }
}