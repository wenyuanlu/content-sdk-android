package com.maishuo.haohai.person.ui

import android.app.Activity
import android.content.Intent
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.gyf.immersionbar.ImmersionBar
import com.maishuo.haohai.R
import com.maishuo.haohai.api.bean.GetListResponsePackBean
import com.maishuo.haohai.api.response.GetListResponse
import com.maishuo.haohai.common.Constant
import com.maishuo.haohai.databinding.ActivityDownloadDetailsBinding
import com.maishuo.haohai.main.ui.CustomPlayerPagerActivity
import com.maishuo.haohai.person.adapter.DownloadDetailsAdapter
import com.maishuo.haohai.person.adapter.DownloadDetailsAdapterCallBack
import com.qichuang.commonlibs.basic.CustomBaseActivity
import com.qichuang.commonlibs.utils.GlideUtils
import com.qichuang.commonlibs.utils.GsonUtils
import com.qichuang.roomlib.RoomManager
import com.qichuang.roomlib.entity.ListenCommonEntity

class DownloadDetailsActivity : CustomBaseActivity<ActivityDownloadDetailsBinding>(),
    DownloadDetailsAdapterCallBack {
    private var adapter: DownloadDetailsAdapter? = null
    private var albumId: Int? = null
    private var albumType: Int? = null

    private var itemCheckBoxChangeTag: Boolean? = false

    companion object {
        private const val ALBUM_TYPE: String = "album_type"
        private const val ALBUM_ID: String = "album_id"
        private const val BOOK_COVER: String = "book_cover"
        private const val BOOK_NAME: String = "book_name"
        private const val BOOK_SUM: String = "book_sum"

        fun start(
            activity: Activity?,
            albumType: Int?,
            albumId: Int?,
            bookCover: String?,
            bookName: String?,
            bookSum: String?,
        ) {
            val intent = Intent(activity, DownloadDetailsActivity::class.java)
            intent.putExtra(ALBUM_TYPE, albumType)
            intent.putExtra(ALBUM_ID, albumId)
            intent.putExtra(BOOK_COVER, bookCover)
            intent.putExtra(BOOK_NAME, bookName)
            intent.putExtra(BOOK_SUM, bookSum)
            activity?.startActivity(intent)
        }
    }

    override fun initWidgets() {
        ImmersionBar.with(this).init()
        initIntent()
        initRecyclerView()
        initLocalData()
    }

    private fun initIntent() {
        albumId = intent.getIntExtra(ALBUM_ID, 0)
        albumType = intent.getIntExtra(ALBUM_TYPE, 0)
        val bookCover = intent.getStringExtra(BOOK_COVER)
        val bookName = intent.getStringExtra(BOOK_NAME)
        val bookSum = intent.getStringExtra(BOOK_SUM)
        GlideUtils.loadImage(
            this,
            bookCover,
            vb?.ivDownloadDetailsCover!!,
            R.mipmap.home_item_default_icon
        )
        vb?.tvDownloadDetailsContent?.text = bookName
        vb?.tvDownloadDetailsSum?.text = bookSum
    }

    override fun initWidgetsEvent() {
        adapter?.setOnItemClickListener { adapter, _, position ->
            val bean = adapter.getItem(position) as ListenCommonEntity
            if (bean.showCheck == false) {
                val item = GetListResponsePackBean()
                item.datas = entityToGetListResponses()

                val response = entityToGetListResponse(bean)
                CustomPlayerPagerActivity.start(
                    this,
                    bean.album_type,
                    response,
                    item
                )
                Constant.responses = entityToGetListResponses()
            }
        }
        vb?.let {
            it.ivDownloadDetailsArrow.setOnClickListener {
                finish()
            }
            it.tvDownloadDetailsMultipleDelete.setOnClickListener { _ ->
                it.tvDownloadDetailsMultipleDelete.visibility = View.GONE
                it.tvDownloadDetailsNum.visibility = View.GONE
                it.ckDownloadDetails.visibility = View.VISIBLE
                it.tvDownloadDetailsSingleDelete.visibility = View.VISIBLE
                it.tvDownloadDetailsCancel.visibility = View.VISIBLE
                setCheckBoxStatus(showCheck = true, isCheck = false)
            }
            it.tvDownloadDetailsCancel.setOnClickListener { _ ->
                it.tvDownloadDetailsMultipleDelete.visibility = View.VISIBLE
                it.tvDownloadDetailsNum.visibility = View.VISIBLE
                it.ckDownloadDetails.visibility = View.GONE
                it.tvDownloadDetailsSingleDelete.visibility = View.GONE
                it.tvDownloadDetailsCancel.visibility = View.GONE
                setCheckBoxStatus(showCheck = false, isCheck = false)

            }
            it.ckDownloadDetails.setOnCheckedChangeListener { _, isChecked ->
                if (itemCheckBoxChangeTag == false) {
                    setCheckBoxStatus(true, isChecked)
                }
            }
            it.tvDownloadDetailsSingleDelete.setOnClickListener {
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
        vb?.recyclerView?.setLayoutManager(LinearLayoutManager(this))
        vb?.recyclerView?.setRefreshEnable(false)
        vb?.recyclerView?.setLoadMoreEnable(false)
        adapter = DownloadDetailsAdapter(this)
        vb?.recyclerView?.setAdapter(adapter)
    }

    private fun initLocalData() {
        val data =
            RoomManager.getInstance(this).loadListenCommonByAlbumTypeAndId(albumType, albumId)
        if (!data.isNullOrEmpty()) {
            vb?.tvDownloadDetailsNum?.text =
                String.format("已下载%s%s", data.size, if (albumType == 1) "集" else "讲")
            vb?.recyclerView?.handleSuccess(adapter, data)
        }
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
        val getListResponse = GsonUtils.fetchGson().fromJson(jsonValue, GetListResponse::class.java)
        getListResponse.download_status = 3
        return getListResponse
    }


    override fun initStatusBar(): Boolean {
        return false
    }

    override fun deleteComplete() {
        val dataSize =
            RoomManager.getInstance(this).loadCountByType(albumType, albumId)
        if (dataSize != null && dataSize > 0) {
            vb?.tvDownloadDetailsNum?.text =
                String.format("已下载%s%s", dataSize, if (albumType == 1) "集" else "讲")
        } else {
            vb?.rlDownloadDetailsContainer?.visibility = View.GONE
            vb?.recyclerView?.handleFailure("暂无数据")
        }
    }

    override fun checkBoxChange() {
        val list = adapter?.data
        if (!list.isNullOrEmpty()) {
            itemCheckBoxChangeTag = true
            vb?.ckDownloadDetails?.isChecked = true
            for (bean in list) {
                if (bean.isCheck == false) {
                    vb?.ckDownloadDetails?.isChecked = false
                    break
                }
            }
            itemCheckBoxChangeTag = false
        }
    }
}