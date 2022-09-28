package com.maishuo.haohai.utils;

import android.media.MediaMetadata;

import com.maishuo.haohai.api.response.GetListResponse;
import com.maishuo.haohai.common.Constant;

import java.util.ArrayList;
import java.util.List;

public class MusicConvertUtil {

    public static <T extends GetListResponse> ArrayList<MediaMetadata> convertToMediaMetadataList(List<T> list) {
        // 创建MediaMetadataCompat 播放队列
        ArrayList<MediaMetadata> metaList = new ArrayList<>(list.size());
        // 队列中添加数据
        for (T item : list) {
            metaList.add(convertToMediaMetadata(item));
        }
        return metaList;
    }

    public static MediaMetadata convertToMediaMetadata(GetListResponse response) {
        return new MediaMetadata.Builder()
                .putString(MediaMetadata.METADATA_KEY_MEDIA_ID, Constant.HEADER_LINES_TAG == response.getAlbum_type() ? response.getAlbum_lid() : response.getProgram_lid())
                //
                .putString(MediaMetadata.METADATA_KEY_MEDIA_URI, response.getMp3_url())
                //专辑标题
                .putString(MediaMetadata.METADATA_KEY_ALBUM, response.getAlbum_name())
                //媒体的艺术家。
                .putString(MediaMetadata.METADATA_KEY_ARTIST, response.getAuthor_name())
                //专辑说明
                .putString(MediaMetadata.METADATA_KEY_DISPLAY_DESCRIPTION, response.getSummary())
                //音频总时长
                .putLong(MediaMetadata.METADATA_KEY_DURATION, null == response ? 0L : response.getDuration() == null ? 0L : response.getDuration())
                //媒体的类型。
                .putString(MediaMetadata.METADATA_KEY_GENRE, response.getAlbum_type().toString())
                //封面图
                .putString(MediaMetadata.METADATA_KEY_ART_URI, response.getAlbum_cover())
                //作为Uri的媒体原始源的相册的插图
                .putString(MediaMetadata.METADATA_KEY_ALBUM_ART_URI, response.getAlbum_cover())
                //主标题
                .putString(MediaMetadata.METADATA_KEY_TITLE, response.getAlbum_name())
                .build();
    }
}
