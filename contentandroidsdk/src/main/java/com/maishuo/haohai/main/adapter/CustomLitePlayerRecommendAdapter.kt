package com.maishuo.haohai.main.adapter

import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.maishuo.haohai.R
import com.maishuo.haohai.api.response.GetListResponse
import com.maishuo.haohai.api.response.RecommendListResponse
import com.qichuang.commonlibs.basic.CustomBaseAdapter
import com.qichuang.commonlibs.basic.CustomBaseViewHolder
import com.qichuang.commonlibs.widgets.CommonRecyclerView

/**
 * author : xpSun
 * date : 2022/3/14
 * description :
 */
class CustomLitePlayerRecommendAdapter : CustomBaseAdapter<RecommendListResponse,
        CustomBaseViewHolder>(R.layout.view_lite_player_recommend_item_layout) {

    private var onChildClickListener: ((response: GetListResponse?) -> Unit)? = null

    fun setOnChildClickListener(clickListener: ((response: GetListResponse?) -> Unit)?) {
        onChildClickListener = clickListener
    }

    override fun onConvert(holder: CustomBaseViewHolder, item: RecommendListResponse?) {
        val title = holder.getView<TextView>(R.id.lite_player_recommend_item_title)
        val recycler = holder.getView<CommonRecyclerView>(R.id.lite_player_recommend_item_recycler)

        title.text = item?.topic_name ?: ""

        val adapter = CustomLitePlayerRecommendChildAdapter()
        recycler.layoutManager =
            LinearLayoutManager(holder.view.context, LinearLayoutManager.HORIZONTAL, false)
        recycler.isEnableEmptyView = false
        recycler.adapter = adapter
        adapter.setNewInstance(item?.list)

        adapter.setOnItemClickListener { _, _, position ->
            val response = adapter.getItem(position)
            onChildClickListener?.invoke(response)
        }
    }
}