package com.maishuo.haohai.utils

import com.google.android.exoplayer2.MediaItem
import com.maishuo.haohai.api.response.GetListResponse
import com.maishuo.haohai.common.Constant

/**
 * author : xpSun
 * date : 12/6/21
 * description :
 */
object MediaPlayerUtils {

    fun responseToMedia(
        albumType: Int?,
        responses: MutableList<GetListResponse>?
    ): MutableList<MediaItem> {
        val mediaItems: MutableList<MediaItem> = mutableListOf()

        try {
            if (!responses.isNullOrEmpty()) {
                for (bean in responses.iterator()) {
                    val mediaId = when (albumType) {
                        Constant.VOICE_NOVEL_TAG,
                        Constant.VOICE_CURRICULUM_TAG -> {
                            bean.program_lid ?: ""
                        }
                        Constant.HEADER_LINES_TAG -> {
                            bean.album_lid ?: ""
                        }
                        else -> {
                            ""
                        }
                    }
                    val mediaItem = MediaItem
                        .Builder()
                        .setUri(bean.mp3_url)
                        .setMediaId(mediaId)
                        .build()
                    mediaItems.add(mediaItem)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return mediaItems
    }
}