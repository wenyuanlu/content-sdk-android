package com.qichuang.commonlibs.utils

/**
 * author : xpSun
 * date : 11/25/21
 * description :
 */
object CustomPreferencesUtils {

    private const val CUSTOM_ENABLE_AD = "custom_enable_ad"
    private const val CURRENT_PLAYER_GROUP_ID = "current_player_group_id"

    fun putEnableAd(value: Int?) {
        PreferencesUtils.putInt(CUSTOM_ENABLE_AD, value ?: 0)
    }

    fun fetchEnableAd(): Int {
        return PreferencesUtils.getInt(CUSTOM_ENABLE_AD)
    }

    fun putCurrentPlayerGroupId(id: Int?) {
        PreferencesUtils.putInt(CURRENT_PLAYER_GROUP_ID, id ?: 0)
    }

    fun fetchCurrentPlayerGroupId(): Int {
        return PreferencesUtils.getInt(CURRENT_PLAYER_GROUP_ID)
    }
}