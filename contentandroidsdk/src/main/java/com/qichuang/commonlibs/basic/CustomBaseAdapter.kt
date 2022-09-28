package com.qichuang.commonlibs.basic

import androidx.annotation.IntRange
import com.chad.library.adapter.base.BaseQuickAdapter

/**
 * author : xpSun
 * date : 9/22/21
 * description :
 */
abstract class CustomBaseAdapter<T, viewHolder : CustomBaseViewHolder> :
    BaseQuickAdapter<T, viewHolder> {

    constructor(layoutResId: Int, data: MutableList<T>?) : super(layoutResId, data)

    constructor(layoutResId: Int) : super(layoutResId, null)

    protected abstract fun onConvert(holder: viewHolder, item: T?)

    override fun convert(holder: viewHolder, item: T) {
        onConvert(holder, item)
    }

    fun clearData() {
        if (!data.isNullOrEmpty()) {
            data.clear()
        }
        notifyDataSetChanged()
    }

    fun removeItem(@IntRange(from = 0) position: Int) {
        if (data.isNullOrEmpty()) {
            return
        }

        // 如果是在for循环删除后要记得i--
        data.removeAt(position)
        // 告诉适配器删除数据的位置，会有动画效果
        notifyItemRemoved(position)
        // 刷新当前删除条目后面的item的下标
        if (data.size - position > 0) {
            notifyItemRangeChanged(position, data.size - position)
        }
    }

    open fun setItem(@IntRange(from = 0) position: Int, item: T) {
        data[position] = item
        notifyItemChanged(position)
    }

    fun addDatas(datas: MutableList<T>?) {
        if (datas?.isNullOrEmpty() == false) {
            data.addAll(datas)
            notifyDataSetChanged()
        }
    }

}