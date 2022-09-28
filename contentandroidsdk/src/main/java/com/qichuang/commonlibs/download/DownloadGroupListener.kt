package com.qichuang.commonlibs.download

import com.arialyy.aria.core.download.DownloadGroupTaskListener
import com.arialyy.aria.core.task.DownloadGroupTask
import com.qichuang.commonlibs.utils.LogUtils

/**
 * author ：Seven
 * date : 12/16/21
 * description :
 */
interface DownloadGroupListener : DownloadGroupTaskListener {
    
    override fun onTaskRunning(task: DownloadGroupTask?) {
        LogUtils.LOGE("------onTaskRunning")
    }

    override fun onTaskComplete(task: DownloadGroupTask?) {
        LogUtils.LOGE("------onTaskComplete")
    }

    override fun onTaskFail(task: DownloadGroupTask?, e: Exception?) {
        LogUtils.LOGE("------onTaskFail")
    }

    override fun onWait(task: DownloadGroupTask?) {
        LogUtils.LOGE("------onWait")
    }

    override fun onPre(task: DownloadGroupTask?) {
        LogUtils.LOGE("------onPre")
    }

    override fun onTaskPre(task: DownloadGroupTask?) {
        LogUtils.LOGE("------onTaskPre")
    }

    override fun onTaskResume(task: DownloadGroupTask?) {
        LogUtils.LOGE("------onTaskResume")
    }

    override fun onTaskStart(task: DownloadGroupTask?) {
        LogUtils.LOGE("------onTaskStart")
    }

    override fun onTaskStop(task: DownloadGroupTask?) {
        LogUtils.LOGE("------onTaskStop")
    }

    override fun onTaskCancel(task: DownloadGroupTask?) {
        LogUtils.LOGE("------onTaskCancel")
    }

    override fun onNoSupportBreakPoint(task: DownloadGroupTask?) {
        LogUtils.LOGE("------onNoSupportBreakPoint")
    }
}