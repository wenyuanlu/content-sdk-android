package com.maishuo.haohai.main.ui

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.maishuo.haohai.api.response.GetListResponse
import com.maishuo.haohai.common.Constant
import com.maishuo.haohai.databinding.FragmentMainVoiceHeadlinesLayoutBinding
import com.maishuo.haohai.listener.OnDialogCallBackListener
import com.maishuo.haohai.main.adapter.HomeMainVoiceHeadlinesAdapter
import com.maishuo.haohai.main.viewmodel.HomeMainViewModel
import com.maishuo.haohai.person.service.DownloadService
import com.maishuo.haohai.utils.DialogUtils
import com.maishuo.haohai.utils.permission.PermissionUtil
import com.qichuang.commonlibs.basic.CustomBaseFragment
import com.qichuang.commonlibs.utils.NetUtils
import com.qichuang.commonlibs.utils.ToastUtil
import com.qichuang.roomlib.RoomManager

/**
 * author : xpSun
 * date : 11/8/21
 * description :听头条
 */
class HomeMainVoiceHeadlinesFragment :
    CustomBaseFragment<FragmentMainVoiceHeadlinesLayoutBinding> {

    private var adapter: HomeMainVoiceHeadlinesAdapter? = null
    private var viewModel: HomeMainViewModel? = null
    private var currentContentPagerSize: Int = 1
    private var onResponseListener: ((response: GetListResponse?) -> Unit)? = null

    constructor()

    constructor(listener: ((response: GetListResponse?) -> Unit)?) {
        onResponseListener = listener
    }

    override fun initWidgets() {
        var appCompatActivity: AppCompatActivity? = null
        if (activity is AppCompatActivity) {
            appCompatActivity = activity as AppCompatActivity
        }
        viewModel = HomeMainViewModel(appCompatActivity)
        viewModel?.let {
            it.fetchMainListLiveData.observe(this) { responseIt ->
                if (Constant.LIVE_DATA_STATUS_SUCCESS == responseIt.status) {
                    adapter?.setNewInstance(responseIt.data)

                    if (null == Constant.response && !responseIt.data.isNullOrEmpty()) {
                        onResponseListener?.invoke(responseIt.data!![0])
                        Constant.responses = responseIt.data
                    }

//                    val adInterval =
//                        PreferencesUtils.getInt(PreferencesKey.TOUTIAO_POSITION_AD_INTERVAL)
//                    viewModel?.initResponseAd(responseIt.data, adInterval)
                } else {
                }
            }
            it.initResponseAdLiveData.observe(this) { responseIt ->
                adapter?.setNewInstance(responseIt)
            }
        }

        vb?.homeMainVoiceHeadlinesRecyclerView?.layoutManager = LinearLayoutManager(context)
        adapter = HomeMainVoiceHeadlinesAdapter()
        vb?.homeMainVoiceHeadlinesRecyclerView?.adapter = adapter

        if (NetUtils.isNetworkConnected(context)) {
            viewModel?.fetchMainList(Constant.HEADER_LINES_TAG, "", 20, currentContentPagerSize)
        } else {
            viewModel?.fetchOffLineDatas(Constant.HEADER_LINES_TAG) { resIt ->
                adapter?.setNewInstance(resIt)
            }
        }
    }

    override fun initWidgetsEvent() {
        vb?.homeMainVoiceHeadlinesAllPlayer?.setOnClickListener {
            if (adapter?.data.isNullOrEmpty()) {
                return@setOnClickListener
            }

            viewModel?.onItemClickClickListener(
                Constant.HEADER_LINES_TAG,
                responses = adapter?.data
            )
        }

        vb?.homeMainVoiceHeadlinesRefresh?.setOnClickListener {
            if (!NetUtils.isNetworkConnected(context)) {
                viewModel?.fetchOffLineDatas(Constant.HEADER_LINES_TAG) { resIt ->
                    adapter?.setNewInstance(resIt)
                }
                return@setOnClickListener
            }

            currentContentPagerSize = currentContentPagerSize.inc()
            viewModel?.fetchMainList(
                Constant.HEADER_LINES_TAG,
                "",
                20,
                currentContentPagerSize
            )
        }

        adapter?.setOnItemClickListener { _, _, position ->
            if (adapter?.data.isNullOrEmpty()) {
                return@setOnItemClickListener
            }

            val item = adapter?.data!![position]

            viewModel?.onItemClickClickListener(
                Constant.HEADER_LINES_TAG,
                item,
                adapter?.data
            )
        }

        vb?.homeMainVoiceHeadlinesRecyclerView?.setOnRefreshClickListener {
            currentContentPagerSize = 1
            viewModel?.fetchMainList(Constant.HEADER_LINES_TAG, "", 20, currentContentPagerSize)
        }

        vb?.homeMainVoiceHeadlinesDownload?.setOnClickListener { _ ->
            if (adapter?.data.isNullOrEmpty()) {
                return@setOnClickListener
            }
            handleDownload()
        }
    }

    /**
     * 下载逻辑：非wifi提醒
     */
    private fun handleDownload() {
        val permissionList = PermissionUtil.checkMorePermissions(
            context, arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        )

        if (!permissionList.isNullOrEmpty()) {
            ToastUtil.showToast("您未开启读写权限哦！")
            return
        }

        if (!NetUtils.isNetworkConnected(context)) {
            ToastUtil.showToast("操作失败")
            return
        }

        if (NetUtils.isWifiConnect(context) || DownloadService.authorized) {
            downloadAll()
        } else {
            DialogUtils.showCommonDialog(
                activity as AppCompatActivity?,
                "",
                "当前非WIFI网络，请问是否确认下载？",
                true,
                object : OnDialogCallBackListener {
                    override fun onConfirm(content: String?) {
                        DownloadService.authorized = true
                        downloadAll()
                    }

                    override fun onCancel() {}
                })
        }

    }

    /**
     * 下载全部
     */
    private fun downloadAll() {
        context?.let {
            var hint = ""
            for (bean in adapter?.data!!) {
                val entity =
                    RoomManager.getInstance(it).loadSingleListenCommonByStatus(bean.album_id)
                if (entity == null) {
                    val urls = arrayListOf(bean.mp3_url.toString(), bean.album_cover.toString())
                    val fileNames = arrayListOf("voice.mp3", "cover.jpg")
                    val alias = bean.album_id.toString()
                    DownloadService.downloadCallBack = null
                    DownloadService.contentMap?.put(alias, bean)
                    DownloadService.start(urls, fileNames, alias)

                    bean.download_status = Constant.COMMON_DOWNLOAD_STATUS_2
                    hint = "已加入下载队列"
                }
            }
            ToastUtil.showToast(if (hint.isEmpty()) "当前没有可下载的内容" else hint)
        }
    }

}