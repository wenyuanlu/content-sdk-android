package com.maishuo.haohai.main.lite

import android.content.Context
import android.content.Intent
import androidx.recyclerview.widget.LinearLayoutManager
import com.maishuo.haohai.api.bean.GetListResponsePackBean
import com.maishuo.haohai.api.response.GetListResponse
import com.maishuo.haohai.databinding.ViewCommonRefreshRecyclerLayoutBinding
import com.maishuo.haohai.main.adapter.CustomLiteOtherRecommendAdapter
import com.maishuo.haohai.main.event.RecommendClickEvent
import com.qichuang.commonlibs.basic.BasicPagingRefreshActivity
import com.qichuang.commonlibs.widgets.refresh.CommonRefreshView
import org.greenrobot.eventbus.EventBus

class CustomLiteOtherRecommendActivity :
    BasicPagingRefreshActivity<ViewCommonRefreshRecyclerLayoutBinding, GetListResponse>() {

    companion object {
        private const val COMMON_RESPONSE_TAG: String = "common_response_tag"

        fun start(context: Context?, response: GetListResponsePackBean?) {
            val intent = Intent(context, CustomLiteOtherRecommendActivity::class.java)
            intent.putExtra(COMMON_RESPONSE_TAG, response)
            context?.startActivity(intent)
        }
    }

    private var responses: GetListResponsePackBean? = null
    private var adapter: CustomLiteOtherRecommendAdapter? = null

    override fun initWidgets() {
        setTitle("相关推荐")

        vb?.commonRefreshView?.apply {
            setEnableRefresh(false)
            setEnableLoadMore(false)
        }

        responses = intent.getParcelableExtra(COMMON_RESPONSE_TAG)

        vb?.commonRecyclerView?.let {
            it.layoutManager = LinearLayoutManager(this)
            adapter = CustomLiteOtherRecommendAdapter()
            it.adapter = adapter
            adapter?.setNewInstance(responses?.datas)
        }
    }

    override fun initCommonWidgetEvent() {
        adapter?.setOnItemClickListener { _, _, position ->
            val item = adapter?.getItem(position)
            val event = RecommendClickEvent()
            event.item = item
            EventBus.getDefault().post(event)
            finish()
        }
    }

    override fun dataResponseObserver(datas: MutableList<GetListResponse>?) {

    }

    override fun onDataResponseEvent(pageNumber: Int, pageSize: Int) {

    }

    override fun fetchRefreshView(): CommonRefreshView? {
        return vb?.commonRefreshView
    }

}