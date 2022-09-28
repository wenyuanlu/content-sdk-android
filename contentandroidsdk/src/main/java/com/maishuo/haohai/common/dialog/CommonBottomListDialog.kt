package com.maishuo.haohai.common.dialog

import android.app.Dialog
import android.view.Gravity
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.maishuo.haohai.R
import com.maishuo.haohai.api.bean.DialogBottomMoreBean
import com.maishuo.haohai.common.adapter.DialogBottomMoreAdapter
import com.maishuo.haohai.databinding.ViewBottomListLayoutBinding
import com.maishuo.haohai.listener.OnCommonBottomDialogItemClickListener
import com.qichuang.commonlibs.basic.BaseDialog

/**
 * author : xpSun
 * date : 10/13/21
 * description :
 */
class CommonBottomListDialog(activity: AppCompatActivity?) :
    BaseDialog<ViewBottomListLayoutBinding>(activity, R.style.NoBackGroundDialog) {

    private var adapter: DialogBottomMoreAdapter? = null

    var onCommonBottomDialogItemClick: OnCommonBottomDialogItemClickListener? = null

    var dataList: MutableList<DialogBottomMoreBean>? = null
        set(value) {
            field = value
            adapter?.setNewInstance(value)
        }

    override fun initWidgets() {
        setGravity(Gravity.BOTTOM, resId = R.style.dialog_animation)

        vb?.let {
            it.commonBottomRecycler.layoutManager = LinearLayoutManager(activity)
            adapter = DialogBottomMoreAdapter()
            adapter?.setOnItemClickListener { _, view, position ->
                onCommonBottomDialogItemClick?.onItemClick(view, position, dialog)
            }
            it.commonBottomRecycler.adapter = adapter

            it.commonBottomCancel.setOnClickListener {
                dismiss()
            }
        }
    }
}