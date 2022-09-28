package com.qichuang.commonlibs.download

import android.annotation.SuppressLint
import android.content.Context
import com.arialyy.aria.core.Aria
import com.arialyy.aria.core.task.DownloadGroupTask
import com.maishuo.haohai.person.service.DownloadCallBack
import com.qichuang.commonlibs.utils.ToastUtil
import java.lang.Exception


/**
 * author ：Seven
 * date : 12/16/21
 * description :
 */
@SuppressLint("StaticFieldLeak")
class DownloadManager private constructor(private val context: Context) : DownloadGroupListener {

    companion object {
        @Volatile
        private var instance: DownloadManager? = null
        fun getInstance(context: Context) =
            instance ?: synchronized(this) {
                instance ?: DownloadManager(context).also { instance = it }
            }
    }


    fun download(url: String?, fileName: String?, callBack: DownloadCallBack?) {
        Aria.download(this).register()
        Aria.download(this).load(url)
            .setFilePath(DownloadFileUtil().getFilePath(context, fileName.toString())).create()
    }

    fun downloadGroup(
        alias: String?,
        dirName: String?,
        urlArray: MutableList<String>,
        fileNameArray: MutableList<String>,
        callBack: DownloadCallBack?
    ) {
        Aria.download(this).register()
        Aria.download(this)
            .loadGroup(urlArray)
            .setDirPath(DownloadFileUtil().getDirPath(context, "download/$dirName"))
            .setSubFileName(fileNameArray)
            .setGroupAlias(alias)
            .unknownSize()
            .create()
    }

    override fun onTaskComplete(task: DownloadGroupTask?) {
        super.onTaskComplete(task)

    }

    override fun onTaskStop(task: DownloadGroupTask?) {
        super.onTaskStop(task)

    }

    override fun onTaskFail(task: DownloadGroupTask?, e: Exception?) {
        super.onTaskFail(task, e)

    }

    fun unRegister() {
        Aria.download(this).unRegister()
    }
}
