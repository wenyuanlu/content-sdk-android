package com.maishuo.haohai.audio

/**
 * author : xpSun
 * date : 11/19/21
 * description :
 */
enum class PlayerSpeedEnum(
    val rewType: String,
    val value: Float
) {
    CUSTOM_PLAYER_SPEED_1("0.5倍", 0.5f),
    CUSTOM_PLAYER_SPEED_2("0.75倍", 0.75f),
    CUSTOM_PLAYER_SPEED_3("正常", 1f),
    CUSTOM_PLAYER_SPEED_4("1.25倍", 1.25f),
    CUSTOM_PLAYER_SPEED_5("1.5倍", 1.5f),
    CUSTOM_PLAYER_SPEED_6("1.75倍", 1.75f),
    CUSTOM_PLAYER_SPEED_7("2.0倍", 2f),
}