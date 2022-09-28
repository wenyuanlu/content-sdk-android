package com.qichuang.roomlib.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.qichuang.roomlib.dao.AdDao
import com.qichuang.roomlib.dao.ListenCommonDao
import com.qichuang.roomlib.dao.SendExposureDao
import com.qichuang.roomlib.entity.AdEntity
import com.qichuang.roomlib.entity.ListenCommonEntity
import com.qichuang.roomlib.entity.SendExposureEntity


/**
 * author ：Seven
 * date : 9/22/21
 * description :使用entities来映射相关的实体类
 *              使用了单例模式来返回Database，以防止新建过多的实例造成内存的浪费
 */
@Database(entities = [ListenCommonEntity::class, SendExposureEntity::class, AdEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun listenDao(): ListenCommonDao
    abstract fun sendExposureDao(): SendExposureDao
    abstract fun adDao(): AdDao

    companion object {
        private var instance: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase {
            if (instance == null) {
                instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "content_sdk.db"
                )
                    .allowMainThreadQueries()
                    .build()


            }
            return instance as AppDatabase
        }

        private val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
//                database.execSQL("ALTER TABLE agent "
//                        + " ADD COLUMN agentTest TEXT")
                database.execSQL(
                    "INSERT INTO chat (id, messageId, time,text, type, subType,imagePath, voicePath, voiceDuration,giftName, giftAnimate, versions, uid, toUid, self,mediaId, thumbImagePath, imageWidth,imageHeight,read,sendTime,sendStatus) SELECT _id, MESSAGE_ID, TIME,TEXT, TYPE, SUB_TYPE,IMAGE_PATH, VOICE_PATH, VOICE_DURATION,GIFT_NAME, GIFT_ANIMATE, VERSIONS, UID, TO_UID, IS_SELF,MEDIA_ID, THUMB_IMAGE_PATH, IMAGE_WIDTH,IMAGE_HEIGHT,IS_READ,SEND_TIME,SEND_STATUS FROM CHAT_LOCAL_BEAN"
                )
            }
        }
    }

}
