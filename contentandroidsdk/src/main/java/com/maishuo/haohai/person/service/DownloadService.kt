package com.maishuo.haohai.person.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.arialyy.aria.core.Aria
import com.arialyy.aria.core.download.DownloadGroupEntity
import com.arialyy.aria.core.task.DownloadGroupTask
import com.corpize.sdk.ivoice.QCiVoiceSdk
import com.corpize.sdk.ivoice.QcCustomTemplateAttr
import com.corpize.sdk.ivoice.admanager.QcAdManager
import com.corpize.sdk.ivoice.bean.response.AdAudioBean
import com.corpize.sdk.ivoice.bean.response.CompanionBean
import com.corpize.sdk.ivoice.listener.QcCustomTemplateListener
import com.maishuo.haohai.api.response.GetListResponse
import com.maishuo.haohai.common.Constant
import com.qichuang.commonlibs.basic.CustomBasicApplication
import com.qichuang.commonlibs.common.PreferencesKey
import com.qichuang.commonlibs.download.DownloadFileUtil
import com.qichuang.commonlibs.download.DownloadGroupListener
import com.qichuang.commonlibs.utils.GsonUtils
import com.qichuang.commonlibs.utils.LogUtils
import com.qichuang.commonlibs.utils.PreferencesUtils
import com.qichuang.roomlib.RoomManager
import com.qichuang.roomlib.entity.AdEntity
import com.qichuang.roomlib.entity.ListenCommonEntity


/**
 * author ：Seven
 * date : 12/29/21
 * description :
 */
class DownloadService : Service(), DownloadGroupListener {

    companion object {
        var authorized: Boolean = false//是否已经同意过非wifi下载
        var contentMap: MutableMap<String, GetListResponse>? = mutableMapOf()//下载的内容
        var adMap: MutableMap<String, AdAudioBean>? = mutableMapOf()//下载的广告
        var downloadCallBack: DownloadCallBack? = null

        fun start(
            urls: ArrayList<String>? = null,
            fileNames: ArrayList<String>? = null,
            alias: String? = null
        ) {
            val intent = Intent(CustomBasicApplication.firstActivity(), DownloadService::class.java)
            intent.putStringArrayListExtra("urls", urls)
            intent.putStringArrayListExtra("fileNames", fileNames)
            intent.putExtra("alias", alias)
            CustomBasicApplication.firstActivity()?.startService(intent)
        }

        fun stop() {
            val intent = Intent(CustomBasicApplication.firstActivity(), DownloadService::class.java)
            CustomBasicApplication.firstActivity()?.stopService(intent)
        }

        /**
         * 获取任务列表
         */
        fun getTaskList(): MutableList<DownloadGroupEntity> {
            val list: MutableList<DownloadGroupEntity>
            try {
                list = Aria.download(this).getGroupTaskList(1, 1000) ?: return mutableListOf()
            } catch (e: Exception) {
                return mutableListOf()
            }
            return list
        }

        /**
         * 是否有任务在执行
         */
        fun taskIsRunning(): Boolean {
            val group = Aria.download(this).dgRunningTask
            return !group.isNullOrEmpty()
        }

        /**
         * 根据id取消任务
         * id=0取消所有任务
         */
        fun cancelTask(id: Long?) {
            if (id != null) {
                if (id == 0L) {
                    for (bean in getTaskList()) {
                        Aria.download(this).loadGroup(bean.id).cancel(false)
                    }
                } else {
                    for (bean in getTaskList()) {
                        if (bean.state == 1 || bean.id == id) {
                            Aria.download(this).loadGroup(bean.id).cancel(false)
                        }
                    }
                }
            }
        }

        /**
         * 根据id暂停任务
         * id=0暂停所有任务
         */
        fun pauseTask(id: Long) {
            if (id == 0L) {
                for (bean in getTaskList()) {
                    if (!bean.isComplete) {
                        Aria.download(this).loadGroup(bean.id).stop()
                    }
                }
            } else {
                Aria.download(this).loadGroup(id).stop()
            }
        }

        /**
         * 根据id重启任务
         * id=0重启所有任务
         */
        fun resumeTask(id: Long?) {
            if (id != null) {
                if (id == 0L) {
                    for (bean in getTaskList()) {
                        if (!bean.isComplete) {
                            Aria.download(this).loadGroup(bean.id).resume()
                        }
                    }
                } else {
                    Aria.download(this).loadGroup(id).resume()
                }
            }
        }

        /**
         * 根据id设置等待任务
         * id=0所有任务设为等待状态
         */
        fun waitTask(id: Long) {
            if (id == 0L) {
                for (bean in getTaskList()) {
                    if (!bean.isComplete) {
                        Aria.download(this).loadGroup(bean.id).resetState()
                    }
                }
            } else {
                Aria.download(this).loadGroup(id).resetState()
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        try {
            Aria.download(this).register()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            Aria.download(this).unRegister()
            contentMap?.clear()
            adMap?.clear()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            val urlArray = intent?.getStringArrayListExtra("urls")
            val fileNameArray = intent?.getStringArrayListExtra("fileNames")
            val alias = intent?.getStringExtra("alias")
            if (!urlArray.isNullOrEmpty() && !fileNameArray.isNullOrEmpty() && !alias.isNullOrBlank()) {
                //下载前先把数据状态全部改成下载中并插入数据库
                if (!contentMap.isNullOrEmpty()) {
                    val bean = contentMap!![alias]
                    insertContentRoom(bean, 2)
                }
                val dirName = String.format("%s%s", alias, System.currentTimeMillis())
                Aria.download(this)
                    .loadGroup(urlArray)
                    .setDirPath(
                        DownloadFileUtil().getDirPath(
                            applicationContext,
                            "download/$dirName"
                        )
                    )
                    .setSubFileName(fileNameArray)
                    .setGroupAlias(alias)
                    .unknownSize()
                    .create()
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onTaskRunning(task: DownloadGroupTask?) {
        super.onTaskRunning(task)
        if (downloadCallBack != null) {
            downloadCallBack?.downloadRunning(task)
        }
    }

    override fun onTaskComplete(task: DownloadGroupTask?) {
        super.onTaskComplete(task)
        afterDownloadComplete(task)
    }

    override fun onTaskStop(task: DownloadGroupTask?) {
        super.onTaskStop(task)
        handleDownloadStatus(task, 1)
    }

    override fun onTaskFail(task: DownloadGroupTask?, e: Exception?) {
        super.onTaskFail(task, e)
        handleDownloadStatus(task, 1)
    }

    /**
     * 处理内容下载成功和广告下载成功逻辑
     */
    private fun afterDownloadComplete(task: DownloadGroupTask?) {
        try {
            //存本地数据库
            if (!task?.entity?.urls.isNullOrEmpty() && !task?.entity?.alias.isNullOrBlank()) {
                if (!contentMap.isNullOrEmpty() && contentMap?.containsKey(task?.entity?.alias) == true) {
                    handleContent(task)
                } else {
                    handleAd(task)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 处理内容下载
     */
    private fun handleContent(task: DownloadGroupTask?) {
        val bean = contentMap!![task?.entity?.alias]
        val response = deepCopy(bean)

        //替换播放和封面图地址
        for (url in task?.entity?.urls!!) {
            when {
                url.contains("mp3") -> response.mp3_url =
                    task.entity?.dirPath + "/voice.mp3"
                url.contains("jpg") || url.contains("png") -> response.album_cover =
                    task.entity?.dirPath + "/cover.jpg"
            }
        }
        if (downloadCallBack != null) {
            downloadCallBack?.downloadComplete(task.entity?.alias)
        }
        handleDownloadStatus(task, 3)
    }

    /**
     * 下载前或者下载失败插入状态
     * status：1-失败，2-下载中，3下载完成
     */
    private fun handleDownloadStatus(task: DownloadGroupTask?, status: Int?) {
        try {
            if (!contentMap.isNullOrEmpty() && contentMap?.containsKey(task?.entity?.alias) == true) {
                val bean = contentMap!![task?.entity?.alias]
                insertContentRoom(bean, status)
                //1是内容下载失败
                if (status == 1 && downloadCallBack != null) {
                    downloadCallBack?.downloadFail(task?.entity?.alias)
                }
                //3是内容下载成功
                if (status == 3) {
                    //所有内容任务下载完成通知下载广告
                    val group = Aria.download(this).dgRunningTask
                    val runningTaskCount = if (!group.isNullOrEmpty()) {
                        group.size
                    } else {
                        0
                    }
                    LogUtils.LOGE("内容下载任务剩余：$runningTaskCount")
                    if (runningTaskCount == 0) {
                        getAdData()
                    }
                }
            } else if (!adMap.isNullOrEmpty() && adMap?.containsKey(task?.entity?.alias) == true) {
                //1是广告下载失败
                if (status == 1) {
                    //移除下载任务
                    cancelTask(task?.entity?.id)
                    //已下载就移出
                    if (!adMap.isNullOrEmpty() && adMap?.containsKey(task?.entity?.alias) == true) {
                        adMap?.remove(task?.entity?.alias)
                    }
                    getAdData()
                }
            } else {
                if (status == 1 && downloadCallBack != null) {
                    downloadCallBack?.downloadFail(task?.entity?.alias)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 插入到内容数据库
     */
    private fun insertContentRoom(response: GetListResponse?, status: Int?) {
        try {
            if (response != null) {
                val responseCopy = deepCopy(response)
                //如果是头条需要给program_id设值
                if (responseCopy.album_type == 2) {
                    responseCopy.program_id = responseCopy.album_id
                }
                responseCopy.download_status = status
                val dataBase = responseToInsertDataBase(responseCopy)
                RoomManager.getInstance(applicationContext).insertListenCommon(dataBase)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 把数据深拷贝
     */
    private fun deepCopy(response: GetListResponse?): GetListResponse {
        return GsonUtils.fetchGson()
            .fromJson(GsonUtils.toJson(response), GetListResponse::class.java)
            ?: GetListResponse()
    }

    /**
     * 转换数据为数据库支持结构
     */
    private fun responseToInsertDataBase(response: GetListResponse?): ListenCommonEntity? {
        val responseJson = GsonUtils.toJson(response)
        var entity: ListenCommonEntity? = null
        try {
            entity = GsonUtils.fetchGson()
                .fromJson(
                    responseJson,
                    ListenCommonEntity::class.java
                )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return entity
    }

    /**
     * 处理广告下载
     */
    private fun handleAd(task: DownloadGroupTask?) {
        if (task?.entity?.alias != null) {
            if (task.entity?.alias?.contains("first_voice") == true) {
                if (downloadCallBack != null) {
                    downloadCallBack?.downloadFirstAdComplete(GsonUtils.toJson(mergeFirstAdData(task)))
                }
            } else {
                //处理广告数据
                val adBean = mergeAdData(task)
                //找到绑定的内容加入广告数据然后存库
                val entity = AdEntity()
                entity.program_id = task.entity?.alias?.toInt()
                entity.mp3_url = adBean?.audiourl
                entity.ad_json = GsonUtils.toJson(adBean)
                insertAdRoom(entity)
                getAdData()
            }
            //已下载就移出
            if (!adMap.isNullOrEmpty() && adMap?.containsKey(task.entity?.alias) == true) {
                adMap?.remove(task.entity?.alias)
            }
            //取消下载队列
            cancelTask(task.entity?.id)
        }
    }

    /**
     * 插入到广告数据库
     */
    private fun insertAdRoom(entity: AdEntity?) {
        try {
            RoomManager.getInstance(applicationContext).insertAd(entity)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 重组广告数据（主要把联网地址改成本地下载地址）
     */
    private fun mergeAdData(task: DownloadGroupTask?): AdAudioBean? {
        if (!adMap.isNullOrEmpty() && adMap?.containsKey(task?.entity?.alias) == true) {
            val adBean = adMap!![task?.entity?.alias]
            for (url in task?.entity?.urls!!) {
                when {
                    url.contains("mp3") -> {
                        adBean?.audiourl =
                            task.entity?.dirPath + "/ad_voice.mp3"
                    }
                    url.contains("jpg") || url.contains("png") -> {
                        adBean?.firstimg =
                            task.entity?.dirPath + "/ad_cover.jpg"
                    }
                }
            }
            val companionBean = CompanionBean()
            companionBean.url = adBean?.firstimg
            adBean?.companion = companionBean

            return adBean
        }

        return AdAudioBean()
    }

    /**
     * 重组首听广告数据（主要把联网地址改成本地下载地址）
     */
    private fun mergeFirstAdData(task: DownloadGroupTask?): AdAudioBean? {
        if (!adMap.isNullOrEmpty() && adMap?.containsKey(task?.entity?.alias) == true) {
            val adBean = adMap!![task?.entity?.alias]
            for (url in task?.entity?.urls!!) {
                when {
                    url.contains("mp3") -> {
                        adBean?.audiourl =
                            task.entity?.dirPath + "/first_voice.mp3"
                    }
                    url.contains("gif") -> {
                        adBean?.logo =
                            task.entity?.dirPath + "/logo.gif"
                    }
                }
            }
            return adBean
        }
        return AdAudioBean()
    }

    /**
     * 获取广告数据
     */
    private fun getAdData() {
        if (!contentMap.isNullOrEmpty()) {
            for (alias in contentMap!!.keys) {
                val entity = RoomManager.getInstance(applicationContext)
                    .loadSingleListenCommonByStatus(alias.toInt())
                if (entity != null) {
                    LogUtils.LOGE("开始请求广告：${contentMap?.size}")
                    val adId = PreferencesUtils.getString(PreferencesKey.COMMON_TEMPLATE_AD_TAG)
                    QCiVoiceSdk.get().addCustomTemplateAd(
                        CustomBasicApplication.firstActivity(),
                        fetchAdAttr(),
                        adId,
                        null,
                        Constant.response?.company,
                        0,
                        AdListener()
                    )
                    break
                }
            }
        }
    }

    private inner class AdListener : QcCustomTemplateListener {
        override fun onFetchApiResponse(adAudioBean: AdAudioBean?) {
            if (!contentMap.isNullOrEmpty()) {
                for (adAlias in contentMap!!.keys) {
                    val entity = RoomManager.getInstance(applicationContext)
                        .loadSingleListenCommonByStatus(adAlias.toInt())
                    if (entity != null) {
                        val urls =
                            arrayListOf(
                                adAudioBean?.audiourl.toString(),
                                adAudioBean?.firstimg.toString()
                            )
                        val fileNames = arrayListOf("ad_voice.mp3", "ad_cover.jpg")
                        LogUtils.LOGE("开始下载广告：$adAlias")
                        adMap?.put(adAlias, adAudioBean!!)
                        start(urls, fileNames, adAlias)
                        //广告下载成功失败都要移出下载队列（广告根据内容对应下载）
                        contentMap?.remove(adAlias)
                        break
                    }
                }
            }
        }

        override fun onAdClick() {}

        override fun onAdCompletion() {}

        override fun onAdError(p0: String?) {
            LogUtils.LOGE("下载获取广告失败")
        }

        override fun onAdReceive(p0: QcAdManager?, p1: View?) {}

        override fun onAdExposure() {}

        override fun fetchMainTitle(p0: String?) {}

        override fun onAdSkipClick() {}

        override fun onFetchAdContentView(
            p0: TextView?,
            p1: LinearLayout?,
            p2: TextView?,
            p3: TextView?,
            p4: TextView?
        ) {
        }
    }

    private fun fetchAdAttr(): QcCustomTemplateAttr {
        val attr = QcCustomTemplateAttr()
        //设置封面属性,单位dp,设置MATCH_PARENT 则交由外部容器控制
        val coverStyle = QcCustomTemplateAttr.CoverStyle()
        coverStyle.width = RelativeLayout.LayoutParams.MATCH_PARENT
        coverStyle.height = RelativeLayout.LayoutParams.MATCH_PARENT
        coverStyle.radius = 20 //设置封面圆角,单位dp

        attr.coverStyle = coverStyle

        //设置广告icon,单位dp
        val iconStyle = QcCustomTemplateAttr.IconStyle()
        iconStyle.width = 30
        iconStyle.height = 30
        iconStyle.radius = 10 //设置icon圆角,单位dp

        iconStyle.layoutGravity = Gravity.BOTTOM //设置icon位置,具体参见Gravity方法

        iconStyle.isEnableMargin = true
        iconStyle.marginLeft = 15
        iconStyle.marginBottom = 13

        attr.iconStyle = iconStyle
        attr.isEnableSkip = true //是否启用右上角跳过,默认启用
        attr.isSkipAutoClose = false //设置倒计时结束后是否自动关闭广告,默认false
        attr.skipTipValue = "关闭" //设置右上角跳过文案,默认为跳过

        return attr
    }

}

interface DownloadCallBack {
    fun downloadRunning(task: DownloadGroupTask?) {}
    fun downloadComplete(alias: String?) {}
    fun downloadFail(alias: String?) {}
    fun downloadFirstAdComplete(adBeanJson: String?) {}
}