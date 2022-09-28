package com.qichuang.roomlib

import android.annotation.SuppressLint
import android.content.Context
import com.qichuang.commonlibs.download.DownloadFileUtil
import com.qichuang.roomlib.database.AppDatabase
import com.qichuang.roomlib.entity.AdEntity
import com.qichuang.roomlib.entity.ListenCommonEntity
import com.qichuang.roomlib.entity.SendExposureEntity
import java.io.File
import java.util.*

/**
 * author ：Seven
 * date : 9/22/21
 * description :room数据库管理类
 */
@SuppressLint("StaticFieldLeak")
class RoomManager {

    companion object {
        private var instance: RoomManager? = null
        private var appDatabase: AppDatabase? = null
        fun getInstance(context: Context): RoomManager {
            if (instance == null) {
                instance = RoomManager()
                appDatabase = AppDatabase.getInstance(context)
            }
            return instance as RoomManager
        }
    }

    /**
     * 收听表-插入
     */
    fun insertListenCommon(entity: ListenCommonEntity?) {
        entity?.let {
            it.createTime = Date().time
            val midEntity = appDatabase?.listenDao()?.loadByProgramId(it.program_id)
            if (midEntity != null) {
                //更新之前先删除上次下载的内容，避免资源浪费,因为下载之前就会把数据插入所以要=3
                val oldFile = midEntity.mp3_url
                if (!oldFile.isNullOrBlank() && midEntity.download_status == 3) {
                    DownloadFileUtil().delete(File(oldFile).parent!!)
                }
                appDatabase?.listenDao()?.update(it)
            } else {
                appDatabase?.listenDao()?.insert(it)
            }
        }
    }

    /**
     * 广告表-插入
     */
    fun insertAd(entity: AdEntity?) {
        entity?.let {
            it.createTime = Date().time
            val midEntity = appDatabase?.adDao()?.loadByProgramId(it.program_id)
            if (midEntity != null) {
                //更新之前先删除上次下载的内容，避免资源浪费
                val oldFile = midEntity.mp3_url
                if (!oldFile.isNullOrBlank()) {
                    DownloadFileUtil().delete(File(oldFile).parent!!)
                }
                appDatabase?.adDao()?.update(it)
            } else {
                appDatabase?.adDao()?.insert(it)
            }
        }
    }

    /**
     * 收听表-删除所有
     */
    fun deleteAllListenCommon(entity: MutableList<ListenCommonEntity>?) {
        entity?.let { appDatabase?.listenDao()?.deleteAll(it) }
    }

    /**
     * 收听表-删除
     */
    fun deleteListenCommon(entity: ListenCommonEntity?) {
        entity?.let {
            val adBean = appDatabase?.adDao()?.loadByProgramId(it.program_id)
            if (adBean != null) {
                appDatabase?.adDao()?.delete(adBean)
            }
            appDatabase?.listenDao()?.delete(it)
        }
    }

    /**
     *  收听表-更新
     */
    fun updateListenCommon(entity: ListenCommonEntity?) {
        entity?.let { appDatabase?.listenDao()?.update(it) }
    }

    /**
     *  收听表-查询所有
     */
    fun loadAllListenCommon(): MutableList<ListenCommonEntity>? {
        val list = appDatabase?.listenDao()?.loadAll()
        if (!list.isNullOrEmpty()) {
            for (bean in list) {
                val adJson = appDatabase?.adDao()?.loadByProgramId(bean.program_id)
                bean.ad_json = adJson?.ad_json
            }
        }
        return list
    }

    /**
     *  收听表-查询所有失败数据
     */
    fun loadAllFailListenCommon(): MutableList<ListenCommonEntity>? {
        return appDatabase?.listenDao()?.loadAllFailData()
    }

    /**
     *  收听表-查询单个失败数据
     */
    fun loadSingleFailListenCommon(programId: Int?): ListenCommonEntity? {
        return appDatabase?.listenDao()?.loadBySingleFailData(programId)
    }

    /**
     *  收听表-根据类型查询所有
     */
    fun loadListenCommonByAlbumType(albumType: Int): MutableList<ListenCommonEntity>? {
        val list = appDatabase?.listenDao()?.loadByAlbumType(albumType)
        if (!list.isNullOrEmpty()) {
            for (bean in list) {
                val adJson = appDatabase?.adDao()?.loadByProgramId(bean.program_id)
                bean.ad_json = adJson?.ad_json
            }
        }
        return list
    }

    /**
     * 收听表-根据类型id查询数据
     */
    fun loadListenCommonByAlbumTypeAndId(
        albumType: Int?,
        albumId: Int?
    ): MutableList<ListenCommonEntity>? {
        val list = appDatabase?.listenDao()?.loadCountByType(albumType, albumId)
        if (!list.isNullOrEmpty()) {
            for (bean in list) {
                val adJson = appDatabase?.adDao()?.loadByProgramId(bean.program_id)
                bean.ad_json = adJson?.ad_json
            }
        }
        return list
    }

    /**
     *  收听表-查询单个所有状态
     */
    fun loadSingleListenCommon(programId: Int?): ListenCommonEntity? {
        val bean = appDatabase?.listenDao()?.loadByProgramId(programId)
        if (bean != null) {
            val adJson = appDatabase?.adDao()?.loadByProgramId(bean.program_id)
            bean.ad_json = adJson?.ad_json
        }
        return bean
    }

    /**
     *  收听表-查询单个完成状态
     */
    fun loadSingleListenCommonByStatus(programId: Int?): ListenCommonEntity? {
        val bean = appDatabase?.listenDao()?.loadByProgramIdAndStatus(programId)
        if (bean != null) {
            val adJson = appDatabase?.adDao()?.loadByProgramId(bean.program_id)
            bean.ad_json = adJson?.ad_json
        }
        return bean
    }

    /**
     *  收听表-根据类型和id查询数量
     */
    fun loadCountByType(albumType: Int?, albumId: Int?): Int? {
        val list = appDatabase?.listenDao()?.loadCountByType(albumType, albumId)
        return list?.size
    }

    /**
     *  广告表-查询所有
     */
    fun loadAllAd(): MutableList<AdEntity>? {
        return appDatabase?.adDao()?.loadAll()
    }

    /**
     *  广告表-查询单个
     */
    fun loadSingleAd(programId: Int?): AdEntity? {
        return appDatabase?.adDao()?.loadByProgramId(programId)
    }

    //*********************** 内容曝光 start ***********************

    /**
     * 内容曝光信息插入
     */
    fun insertSendExposure(entity: SendExposureEntity?) {
        entity?.let { appDatabase?.sendExposureDao()?.insert(it) }
    }

    /**
     * 内容曝光信息查询所有
     */
    fun loadAllSendExposure(): MutableList<SendExposureEntity>? {
        return appDatabase?.sendExposureDao()?.loadAll()
    }

    /**
     *内容曝光信息删除所有
     */
    fun deleteAllSendExposure(entity: MutableList<SendExposureEntity>?) {
        entity?.let {
            appDatabase?.sendExposureDao()?.deleteAll(it)
        }
    }

    //*********************** 内容曝光 end ***********************

}