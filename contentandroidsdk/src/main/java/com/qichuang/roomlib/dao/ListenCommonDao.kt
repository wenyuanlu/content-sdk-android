package com.qichuang.roomlib.dao

import androidx.room.*
import com.qichuang.roomlib.entity.ListenCommonEntity


/**
 * author ：Seven
 * date : 11/26/21
 * description :收听历史表增删改查
 */
@Dao
interface ListenCommonDao {

    @Insert
    fun insert(vararg entity: ListenCommonEntity)

    @Delete
    fun delete(vararg entity: ListenCommonEntity)

    @Delete
    fun deleteAll(entity: MutableList<ListenCommonEntity>)

    @Update
    fun update(entity: ListenCommonEntity)

    @Query("SELECT * FROM listen_common order by createTime desc")
    fun loadAll(): MutableList<ListenCommonEntity>?

    @Query("SELECT * FROM listen_common WHERE album_type = :albumType and download_status= 3 order by program_au asc")
    fun loadByAlbumType(albumType: Int?): MutableList<ListenCommonEntity>?

    @Query("SELECT * FROM listen_common WHERE program_id = :programId")
    fun loadByProgramId(programId: Int?): ListenCommonEntity?

    @Query("SELECT * FROM listen_common WHERE program_id = :programId and download_status= 3")
    fun loadByProgramIdAndStatus(programId: Int?): ListenCommonEntity?

    @Query("SELECT * FROM listen_common WHERE album_type = :albumType and album_id= :albumId and download_status= 3 order by program_au asc")
    fun loadCountByType(albumType: Int?, albumId: Int?): MutableList<ListenCommonEntity>?

    @Query("SELECT * FROM listen_common WHERE download_status != 3")
    fun loadAllFailData(): MutableList<ListenCommonEntity>?

    @Query("SELECT * FROM listen_common WHERE program_id = :programId and download_status != 3")
    fun loadBySingleFailData(programId: Int?): ListenCommonEntity?
}