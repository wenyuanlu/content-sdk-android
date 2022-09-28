package com.maishuo.haohai.person.ui

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.maishuo.haohai.api.bean.GetListResponsePackBean
import com.maishuo.haohai.api.response.GetListResponse
import com.maishuo.haohai.common.Constant
import com.maishuo.haohai.databinding.ViewRecyclerBinding
import com.maishuo.haohai.main.ui.CustomPlayerPagerActivity
import com.maishuo.haohai.person.adapter.DownloadAdapter
import com.maishuo.haohai.person.adapter.DownloadAdapterCallBack
import com.qichuang.commonlibs.basic.CustomBaseFragment
import com.qichuang.commonlibs.utils.GsonUtils
import com.qichuang.roomlib.RoomManager
import com.qichuang.roomlib.entity.ListenCommonEntity

class DownloadFragment(private val albumType: Int) : CustomBaseFragment<ViewRecyclerBinding>(),
    DownloadAdapterCallBack {

    constructor():this(1)

    private var adapter: DownloadAdapter? = null

    private var itemCheckBoxChangeTag: Boolean? = false

    override fun initWidgets() {
        initRecyclerView()
    }

    override fun initWidgetsEvent() {
        adapter?.setOnItemClickListener { adapter, _, position ->
            val bean = adapter.getItem(position) as ListenCommonEntity
            context?.let {
                if (albumType == 2) {
                    if (bean.showCheck == false) {
                        goPlayPager(bean)
                    }
                } else {
                    val sum =
                        String.format("共%s%s", bean.program_num, if (albumType == 1) "集" else "讲")
                    DownloadDetailsActivity.start(
                        activity,
                        albumType,
                        bean.album_id,
                        bean.album_cover,
                        bean.album_name,
                        sum
                    )
                }
            }
        }

        vb?.let {
            it.tvDownloadPlay.setOnClickListener {
                if (adapter != null) {
                    val bean = adapter?.getItem(0) as ListenCommonEntity
                    goPlayPager(bean)
                }
            }
            it.tvDownloadMultipleDelete.setOnClickListener { _ ->
                it.tvDownloadMultipleDelete.visibility = View.GONE
                it.tvDownloadPlay.visibility = View.GONE
                it.ckDownload.visibility = View.VISIBLE
                it.tvDownloadSingleDelete.visibility = View.VISIBLE
                it.tvDownloadCancel.visibility = View.VISIBLE
                setCheckBoxStatus(showCheck = true, isCheck = false)
            }
            it.tvDownloadCancel.setOnClickListener { _ ->
                it.tvDownloadMultipleDelete.visibility = View.VISIBLE
                it.tvDownloadPlay.visibility = View.VISIBLE
                it.ckDownload.visibility = View.GONE
                it.tvDownloadSingleDelete.visibility = View.GONE
                it.tvDownloadCancel.visibility = View.GONE
                setCheckBoxStatus(showCheck = false, isCheck = false)

            }
            it.ckDownload.setOnCheckedChangeListener { _, isChecked ->
                if (itemCheckBoxChangeTag == false) {
                    setCheckBoxStatus(true, isChecked)
                }
            }
            it.tvDownloadSingleDelete.setOnClickListener {
                adapter?.deleteMultipleItem()
            }
        }

    }

    private fun setCheckBoxStatus(showCheck: Boolean, isCheck: Boolean) {
        val list = adapter?.data
        if (list != null) {
            for (bean in list) {
                bean.showCheck = showCheck
                bean.isCheck = isCheck
            }
        }
        adapter?.notifyDataSetChanged()
    }

    private fun initRecyclerView() {
        vb?.recyclerView?.setLayoutManager(LinearLayoutManager(context))
        vb?.recyclerView?.setRefreshEnable(false)
        vb?.recyclerView?.setLoadMoreEnable(false)
        adapter = DownloadAdapter(this)
        vb?.recyclerView?.setAdapter(adapter)
    }

    private fun initLocalData() {
        context?.let {
            val data = RoomManager.getInstance(it).loadListenCommonByAlbumType(albumType)
            val set = mutableListOf<Int>()
            val resultData = mutableListOf<ListenCommonEntity>()
            if (data != null) {
                for (item in data) {
                    if (!set.contains(item.album_id)) {
                        set.add(item.album_id!!)
                        val count = RoomManager.getInstance(it)
                            .loadCountByType(albumType, item.album_id)
                        item.count = count
                        resultData.add(item)
                    }
                }
            }
            vb?.recyclerView?.start = 1
            vb?.recyclerView?.handleSuccess(adapter, resultData)

            //头条是否显示多选删除
            if (albumType == 2 && !resultData.isNullOrEmpty()) {
                vb?.rlDownloadContainer?.visibility = View.VISIBLE
            } else {
                vb?.rlDownloadContainer?.visibility = View.GONE
            }
        }
    }

    private fun goPlayPager(bean: ListenCommonEntity) {
        val item = GetListResponsePackBean()
        item.datas = entityToGetListResponses()
        val response = entityToGetListResponse(bean)
        CustomPlayerPagerActivity.start(
            activity,
            bean.album_type,
            response,
            item
        )
        Constant.responses = entityToGetListResponses()
    }

    private fun entityToGetListResponses(): MutableList<GetListResponse> {
        val changeList: MutableList<GetListResponse> = mutableListOf()
        val list = adapter?.data
        if (!list.isNullOrEmpty()) {
            for (bean in list) {
                changeList.add(entityToGetListResponse(bean))
            }
        }
        return changeList
    }

    private fun entityToGetListResponse(entity: ListenCommonEntity?): GetListResponse {
        val jsonValue = GsonUtils.toJson(entity)
        return GsonUtils.fetchGson().fromJson(jsonValue, GetListResponse::class.java)
    }

    override fun onStart() {
        super.onStart()
        initLocalData()
    }

    override fun deleteComplete() {
        initLocalData()
    }

    override fun checkBoxChange() {
        val list = adapter?.data
        if (!list.isNullOrEmpty()) {
            itemCheckBoxChangeTag = true
            vb?.ckDownload?.isChecked = true
            for (bean in list) {
                if (bean.isCheck == false) {
                    vb?.ckDownload?.isChecked = false
                    break
                }
            }
            itemCheckBoxChangeTag = false
        }
    }

}