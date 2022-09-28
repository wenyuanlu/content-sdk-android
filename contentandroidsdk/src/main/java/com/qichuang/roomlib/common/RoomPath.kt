package com.qichuang.roomlib.common

import android.content.Context
import com.qichuang.commonlibs.utils.PreferencesUtils
import java.io.File.separator

/**
 * author ：Seven
 * date : 9/24/21
 * description :room数据库存储路径
 */
class RoomPath(context: Context) {

    var newDbName = if (PreferencesUtils.getBoolean("PreferencesKey.ONLINE", false)) {
        "tingshuowan_" + PreferencesUtils.getString("PreferencesKey.USER_ID")
    } else ""

    val oldDbName = if (PreferencesUtils.getBoolean("PreferencesKey.ONLINE", false)) {
        context.getExternalFilesDir("db")?.path + separator + "10001630" + separator + "chat.db_10001630"
    } else ""
}
