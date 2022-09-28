package com.maishuo.haohai.main.ui

import android.Manifest
import android.view.Gravity
import androidx.recyclerview.widget.LinearLayoutManager
import com.maishuo.haohai.R
import com.maishuo.haohai.api.response.GetListResponse
import com.maishuo.haohai.audio.AudioPlayerManager
import com.maishuo.haohai.common.Constant
import com.maishuo.haohai.databinding.ViewPlayerPagerListSelectorBottomLayoutBinding
import com.maishuo.haohai.listener.OnDialogCallBackListener
import com.maishuo.haohai.main.adapter.PlayerPagerBottomListAdapter
import com.maishuo.haohai.main.viewmodel.CustomPlayerPagerViewModel
import com.maishuo.haohai.person.service.DownloadCallBack
import com.maishuo.haohai.person.service.DownloadService
import com.maishuo.haohai.utils.DialogUtils
import com.maishuo.haohai.utils.permission.PermissionUtil
import com.qichuang.commonlibs.basic.BaseDialog
import com.qichuang.commonlibs.utils.NetUtils
import com.qichuang.commonlibs.utils.ScreenUtils
import com.qichuang.commonlibs.utils.ToastUtil
import com.qichuang.roomlib.RoomManager

/**
 * author : xpSun
 * date : 11/12/21
 * description :
 */
class PlayerPagerListSelectorBottomDialog constructor(
    private val appCompatActivity: CustomPlayerPagerActivity?
) : BaseDialog<ViewPlayerPagerListSelectorBottomLayoutBinding>(appCompatActivity),
    DownloadCallBack {

    private var adapter: PlayerPagerBottomListAdapter? = null
    var responses: MutableList<GetListResponse>? = null
        set(value) {
            try {
                if (!value.isNullOrEmpty()) {
                    for (item in value.iterator()) {
                        appCompatActivity?.let {
                            val entity = RoomManager.getInstance(it)
                                .loadSingleListenCommonByStatus(item.album_id)
                            item.download_status = entity?.download_status ?:Constant.COMMON_DOWNLOAD_STATUS_1
                        }
                    }
                }
                field = value
                adapter?.setNewInstance(field)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    private var viewModel: CustomPlayerPagerViewModel? = null
    private var audioPlayerManager: AudioPlayerManager? = null
    private var onItemClickListener: ((position: Int) -> Unit)? = null
    private var onChangerPlayerListListener: ((responses: MutableList<GetListResponse>) -> Unit)? =
        null

    fun setOnItemClickListener(listener: ((position: Int) -> Unit)?) {
        onItemClickListener = listener
    }

    fun setChangerPlayerListener(listener: ((response: MutableList<GetListResponse>) -> Unit)?) {
        onChangerPlayerListListener = listener
    }

    override fun initWidgets() {
        val height = ScreenUtils.getRealyScreenHeight(activity) * 0.8
        setGravity(Gravity.BOTTOM, height = height.toInt())

        viewModel = CustomPlayerPagerViewModel(appCompatActivity)

        adapter = PlayerPagerBottomListAdapter()
        vb?.playerListRecycler?.layoutManager = LinearLayoutManager(activity)
        vb?.playerListRecycler?.adapter = adapter

        audioPlayerManager = AudioPlayerManager.getInstance()
        adapter?.currentMediaItemId = audioPlayerManager?.currentMediaItemId

        initWidgetEvent()
    }

    private fun initWidgetEvent() {
        vb?.playerListClose?.setOnClickListener {
            dismiss()
        }

        adapter?.setOnItemClickListener { _, _, position ->
            if (adapter?.data.isNullOrEmpty()) {
                return@setOnItemClickListener
            }

            val item = adapter?.data!![position]
            adapter?.currentMediaItemId = item.program_au.toString()

            onItemClickListener?.invoke(position)

            audioPlayerManager?.seekTo(position, 0)
            audioPlayerManager?.start()

            dismiss()
        }

        adapter?.addChildClickViewIds(
            R.id.player_list_bottom_dialog_item_del,
            R.id.player_list_bottom_dialog_item_download
        )
        adapter?.setOnItemChildClickListener { _, view, position ->
            try {
                if (adapter?.data.isNullOrEmpty()) {
                    return@setOnItemChildClickListener
                }

                when (view.id) {
                    R.id.player_list_bottom_dialog_item_del -> {
                        val playerList = adapter?.data
                        val item = playerList!![position]

                        if (audioPlayerManager?.currentMediaItemId == item.album_id.toString()) {
                            val newPosition = position.inc()

                            val responses = adapter?.data
                            if (responses.isNullOrEmpty() || newPosition > responses.size) {
                                return@setOnItemChildClickListener
                            }

                            val newItem = responses[newPosition]
                            adapter?.currentMediaItemId = newItem.album_id?.toString()
                            audioPlayerManager?.seekToNext()
                        }

                        viewModel?.removeMediaPlayer(position)
                        playerList.remove(item)
                        adapter?.notifyDataSetChanged()

                        onChangerPlayerListListener?.invoke(playerList)
                    }
                    R.id.player_list_bottom_dialog_item_download -> {
                        handleDownload(position)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 下载逻辑：非wifi提醒
     */
    private fun handleDownload(position: Int) {
        val permissionList = PermissionUtil.checkMorePermissions(
            appCompatActivity, arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        )

        if (!permissionList.isNullOrEmpty()) {
            ToastUtil.showToast("您未开启读写权限哦！")
            return
        }

        if (!NetUtils.isNetworkConnected(appCompatActivity)) {
            ToastUtil.showToast("操作失败")
            return
        }

        if (NetUtils.isWifiConnect(appCompatActivity) || DownloadService.authorized) {
            downloadSingle(position)
        } else {
            DialogUtils.showCommonDialog(
                appCompatActivity,
                "",
                "当前非WIFI网络，请问是否确认下载？",
                true,
                object : OnDialogCallBackListener {
                    override fun onConfirm(content: String?) {
                        DownloadService.authorized = true
                        downloadSingle(position)
                    }

                    override fun onCancel() {}
                })
        }
    }


    /**
     * 下载单个
     */
    private fun downloadSingle(position: Int) {
        val list = adapter?.data
        if (!list.isNullOrEmpty()) {
            val item = list[position]
            if (item.download_status == Constant.COMMON_DOWNLOAD_STATUS_1) {
                val urls = arrayListOf(item.mp3_url.toString(), item.album_cover.toString())
                val fileNames = arrayListOf("voice.mp3", "cover.jpg")
                val alias = item.album_id.toString()
                DownloadService.downloadCallBack = this
                DownloadService.contentMap?.put(alias, item)
                DownloadService.start(urls, fileNames, alias)
                //刷新下载图标显示状态
                item.download_status = Constant.COMMON_DOWNLOAD_STATUS_2
                adapter?.notifyItemChanged(position)

                ToastUtil.showToast("正在下载")
            }
        }
    }

    /**
     * 下载成功回调
     */
    override fun downloadComplete(alias: String?) {
        val list = adapter?.data
        if (!list.isNullOrEmpty()) {
            for (i in list.indices) {
                if (list[i].album_id.toString() == alias) {
                    list[i].download_status = Constant.COMMON_DOWNLOAD_STATUS_3
                    adapter?.notifyItemChanged(i)
                    break
                }

            }
        }
    }

    /**
     * 下载失败回调
     */
    override fun downloadFail(alias: String?) {
        val list = adapter?.data
        if (!list.isNullOrEmpty()) {
            for (i in list.indices) {
                if (list[i].album_id.toString() == alias) {
                    //刷新下载图标显示状态
                    list[i].download_status = Constant.COMMON_DOWNLOAD_STATUS_1
                    adapter?.notifyItemChanged(i)
                    break
                }
            }
        }
    }
}