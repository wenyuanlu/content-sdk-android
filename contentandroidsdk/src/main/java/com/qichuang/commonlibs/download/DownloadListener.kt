package com.qichuang.commonlibs.download

import com.arialyy.aria.core.download.DownloadTaskListener
import com.arialyy.aria.core.task.DownloadTask
import com.qichuang.commonlibs.utils.LogUtils

/**
 * author ：Seven
 * date : 12/16/21
 * description :
 */
interface DownloadListener : DownloadTaskListener {
    override fun onTaskRunning(task: DownloadTask?) {
        LogUtils.LOGE("------onTaskRunning")
    }

    override fun onTaskComplete(task: DownloadTask?) {
        LogUtils.LOGE("------onTaskComplete")
    }

    override fun onTaskFail(task: DownloadTask?, e: Exception?) {
        LogUtils.LOGE("------onTaskFail")
    }

    override fun onWait(task: DownloadTask?) {
        LogUtils.LOGE("------onWait")
    }

    override fun onPre(task: DownloadTask?) {
        LogUtils.LOGE("------onPre")
    }

    override fun onTaskPre(task: DownloadTask?) {
        LogUtils.LOGE("------onTaskPre")
    }

    override fun onTaskResume(task: DownloadTask?) {
        LogUtils.LOGE("------onTaskResume")
    }

    override fun onTaskStart(task: DownloadTask?) {
        LogUtils.LOGE("------onTaskStart")
    }

    override fun onTaskStop(task: DownloadTask?) {
        LogUtils.LOGE("------onTaskStop")
    }

    override fun onTaskCancel(task: DownloadTask?) {
        LogUtils.LOGE("------onTaskCancel")
    }

    override fun onNoSupportBreakPoint(task: DownloadTask?) {
        LogUtils.LOGE("------onNoSupportBreakPoint")
    }
}