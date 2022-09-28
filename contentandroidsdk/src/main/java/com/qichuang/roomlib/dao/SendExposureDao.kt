package com.qichuang.roomlib.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.qichuang.roomlib.entity.SendExposureEntity

/**
 * author : xpSun
 * date : 12/20/21
 * description :
 */
@Dao
interface SendExposureDao {

    @Insert
    fun insert(vararg entity: SendExposureEntity)

    @Delete
    fun deleteAll(entity: MutableList<SendExposureEntity>)

    @Query("SELECT * FROM send_exposure order by id desc")
    fun loadAll(): MutableList<SendExposureEntity>?
}