package com.maishuo.haohai.common

/**
 * author : xpSun
 * date : 12/17/21
 * description :
 */
enum class PlayerContentExposureEnum(
    val exposureValue: String,
    val exposureContent: String,
    val startDeviation: Double,
    val endDeviation: Double
) {
    EXPOSURE_START("start", "开始播放", 0.0, 0.3),
    EXPOSURE_FIRSTQUARTILE("firstQuartile", "播放到25%", 0.24, 0.26),
    EXPOSURE_MIDPOINT("midpoint", "播放50%", 0.49, 0.51),
    EXPOSURE_THIRDQUARTILE("thirdQuartile", "播放75%", 0.74, 0.76),
    EXPOSURE_COMPLETE("complete", "播放完成(即播放100%)", 0.9, 1.0)
}