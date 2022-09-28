package com.maishuo.contentandroidsdk.utils;

import android.os.Environment;
import android.os.StatFs;

import com.maishuo.contentandroidsdk.common.AppApplication;

import java.io.File;

/**
 * 缓存目录utils
 *
 * @author haojinwei
 */
public class SysSDCardCacheDir {
    private static final String rootDir = "content";// 缓存根目录
    private static final String imgDir = "img";// 图片缓存根目录
    private static final String voiceDir = "voice";// 音频缓存根目录
    private static final String videoDir = "video";// 视频缓存根目录
    private static final String otherDir = "other";// 其他缓存根目录
    private static final String logDir = "log";

    /**
     * sd卡是否存在
     *
     * @return
     */
    private static final boolean isSdCardExist() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }

    /**
     * 缓存根目录
     *
     * @return File
     */
    public static final File getRootDir() {
        File file = null;
        if (isSdCardExist()) {
            file = new File(AppApplication.Companion.getInstance().getExternalFilesDir(null)
                    .getAbsolutePath() + File.separator + rootDir);
            if (!file.exists()) {
                file.mkdirs();
            }
        }
        return file;
    }

    public static final String getLogPath() {
        StringBuilder sb = new StringBuilder();
        if (isSdCardExist()) {
            sb.append(AppApplication.Companion.getInstance().getExternalFilesDir(null).getAbsolutePath());
            sb.append("/");
            sb.append(rootDir);
            sb.append("/");
            sb.append(logDir);
        }
        return sb.toString();
    }


    /**
     * 图片缓存根目录
     *
     * @return File
     */
    public static final File getImgDir() {
        File file = null;
        if (isSdCardExist()) {
            file = new File(AppApplication.Companion.getInstance().getExternalFilesDir(null)
                    .getAbsolutePath()
                    + File.separator
                    + rootDir
                    + File.separator + imgDir);
            if (!file.exists()) {
                file.mkdirs();
            }
        }
        return file;
    }

    /**
     * 音频缓存根目录
     *
     * @return File
     */
    public static final File getVoiceDir() {
        File file = null;
        if (isSdCardExist()) {
            file = new File(AppApplication.Companion.getInstance().getExternalFilesDir(null)
                    .getAbsolutePath()
                    + File.separator
                    + rootDir
                    + File.separator + voiceDir);
            if (!file.exists()) {
                file.mkdirs();
            }
        }
        return file;
    }

    /**
     * 视频缓存根目录
     *
     * @return File
     */
    public static final File getVideoDir() {
        File file = null;
        if (isSdCardExist()) {
            file = new File(AppApplication.Companion.getInstance().getExternalFilesDir(null)
                    .getAbsolutePath()
                    + File.separator
                    + rootDir
                    + File.separator + videoDir);
            if (!file.exists()) {
                file.mkdirs();
            }
        }
        return file;
    }

    /**
     * 其他缓存根目录
     *
     * @return File
     */
    public static final File getLogDir() {
        File file = null;
        if (isSdCardExist()) {
            file = new File(AppApplication.Companion.getInstance().getExternalFilesDir(null)
                    .getAbsolutePath()
                    + File.separator
                    + rootDir
                    + File.separator + logDir);
            if (!file.exists()) {
                file.mkdirs();
            }
        }
        return file;
    }

    /**
     * 其他缓存根目录
     *
     * @return File
     */
    public static final File getOtherDir() {
        File file = null;
        if (isSdCardExist()) {
            file = new File(AppApplication.Companion.getInstance().getExternalFilesDir(null)
                    .getAbsolutePath()
                    + File.separator
                    + rootDir
                    + File.separator + otherDir);
            if (!file.exists()) {
                file.mkdirs();
            }
        }
        return file;
    }

    /**
     * 获取手机SD卡空间大小信息
     *
     * @return
     */
    public static Long readSDCard() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            File sdcardDir = AppApplication.Companion.getInstance().getExternalFilesDir(null);
            StatFs sf = new StatFs(sdcardDir.getPath());
            long blockSize = sf.getBlockSize();
            // long blockCount = sf.getBlockCount();
            long availCount = sf.getAvailableBlocks();
            // LogUtils.d("block大小:" + blockSize + ",block数目:" + blockCount
            // + ",总大小:" + blockSize * blockCount / 1024 + "KB");
            // LogUtils.d("可用的block数目：:" + availCount + ",剩余空间:" + availCount
            // * blockSize / 1024 + "KB");
            return availCount * blockSize;
        }
        return null;
    }

    public static File getAgentUserPhotoDir() {
        File file = null;
        if (isSdCardExist()) {
            file = new File(AppApplication.Companion.getInstance().getExternalFilesDir(null)
                    .getAbsolutePath()
                    + File.separator
                    + rootDir
                    + File.separator + "photo" + File.separator + "user");
            if (!file.exists()) {
                file.mkdirs();
            }
        }
        return file;
    }

    public static File getAgentCustomerPhotoDir() {
        File file = null;
        if (isSdCardExist()) {
            file = new File(AppApplication.Companion.getInstance().getExternalFilesDir(null)
                    .getAbsolutePath()
                    + File.separator
                    + rootDir
                    + File.separator + "photo" + File.separator + "customer");
            if (!file.exists()) {
                file.mkdirs();
            }
        }
        return file;
    }
}
