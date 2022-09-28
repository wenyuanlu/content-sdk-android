package com.qichuang.roomlib.dao

import androidx.room.*
import com.qichuang.roomlib.entity.AdEntity
import com.qichuang.roomlib.entity.ListenCommonEntity
import com.qichuang.roomlib.entity.SendExposureEntity

/**
 * author : Seven
 * date : 12/21/21
 * description :
 */
@Dao
interface AdDao {

    @Insert
    fun insert(vararg entity: AdEntity)

    @Update
    fun update(entity: AdEntity)

    @Delete
    fun delete(vararg entity: AdEntity)

    @Delete
    fun deleteAll(entity: MutableList<AdEntity>)

    @Query("SELECT * FROM ad WHERE program_id = :programId")
    fun loadByProgramId(programId: Int?): AdEntity?

    @Query("SELECT * FROM ad")
    fun loadAll(): MutableList<AdEntity>?
}