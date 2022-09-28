package com.maishuo.haohai.person.ui

import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.maishuo.haohai.api.bean.MyFavoriteCountEvent
import com.maishuo.haohai.api.response.GetListResponse
import com.maishuo.haohai.databinding.ViewRecyclerBinding
import com.maishuo.haohai.main.ui.CustomPlayerPagerActivity
import com.maishuo.haohai.person.adapter.MyFavoriteAdapter
import com.maishuo.haohai.person.viewmodel.MyFavoriteViewModel
import com.qichuang.commonlibs.basic.CustomBaseFragment
import org.greenrobot.eventbus.EventBus

class MyFavoriteFragment(private val albumId: Int) : CustomBaseFragment<ViewRecyclerBinding>() {

    constructor():this(1)

    private var adapter: MyFavoriteAdapter? = null

    var viewModel: MyFavoriteViewModel? = null

    override fun initWidgets() {
        initViewModel()
        setRecyclerView()
        requestForData()
    }

    override fun initWidgetsEvent() {
        adapter?.setOnItemClickListener { adapter, _, position ->
            val bean = adapter.getItem(position) as GetListResponse
            CustomPlayerPagerActivity.start(activity, bean.album_type, bean, null)
        }
    }

    private fun setRecyclerView() {
        vb?.recyclerView?.setLayoutManager(LinearLayoutManager(context))
        adapter = MyFavoriteAdapter(viewModel!!)
        vb?.recyclerView?.setAdapter(adapter)
        vb?.recyclerView?.setRefreshListener { this.requestForData() }
    }

    private fun initViewModel() {
        viewModel = ViewModelProvider(this).get(MyFavoriteViewModel::class.java)
        viewModel?.myFavoriteLiveData?.observe(this) {
            when {
                it.success -> {
                    vb?.recyclerView?.handleSuccess(adapter, it.result)
                    EventBus.getDefault().post(MyFavoriteCountEvent(it.message?.toInt()))
                }
                else -> {
                    vb?.recyclerView?.handleFailure(it.message)
                }
            }
        }
    }

    private fun requestForData() {
        viewModel?.getMyFavorite(vb?.recyclerView?.start ?: 0, albumId)
    }
}