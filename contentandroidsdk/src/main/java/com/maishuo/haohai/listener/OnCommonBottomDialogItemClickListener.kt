package com.maishuo.haohai.listener

import android.app.Dialog
import android.view.View

interface OnCommonBottomDialogItemClickListener {

    fun onItemClick(itemView: View?, position: Int, dialog: Dialog?)
}