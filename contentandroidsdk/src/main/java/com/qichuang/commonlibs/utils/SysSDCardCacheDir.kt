package com.qichuang.commonlibs.utils

import android.os.Build
import android.os.Environment
import android.os.StatFs
import com.qichuang.commonlibs.basic.CustomBasicApplication

import java.io.File

/**
 * 缓存目录utils
 * @author  xpSun
 */
object SysSDCardCacheDir {

    private const val rootDir = "shitou"// 缓存根目录
    private const val imgDir = "image"// 图片缓存根目录
    private const val voiceDir = "voice"// 音频缓存根目录
    private const val videoDir = "video"// 视频缓存根目录
    private const val otherDir = "other"// 其他缓存根目录
    private const val logDir = "log"

    private const val FILE_NAME_PNG = "%s.png"

    /**
     * 保存图片文件的文件名
     * @return
     */
    val saveImageFileName: String
        get() = String.format(getImgDir()!!.absolutePath + FILE_NAME_PNG, System.currentTimeMillis())

    /**
     * sd卡是否存在
     *
     * @return
     */
    private val isSdCardExist: Boolean
        get() = Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED

    val logPath: String
        get() {
            val sb = StringBuilder()
            if (isSdCardExist) {
                sb.append(Environment.getExternalStorageDirectory().absolutePath)
                sb.append("/")
                sb.append(rootDir)
                sb.append("/")
                sb.append(logDir)
            }
            return sb.toString()
        }

    val agentUserPhotoDir: File?
        get() {
            var file: File? = null
            if (isSdCardExist) {
                file = File(Environment.getExternalStorageDirectory()
                        .absolutePath
                        + File.separator
                        + rootDir
                        + File.separator + "photo" + File.separator + "user")
                fileExists(file)
            }
            return file
        }

    val agentCustomerPhotoDir: File?
        get() {
            var file: File? = null
            if (isSdCardExist) {
                file = File(Environment.getExternalStorageDirectory()
                        .absolutePath
                        + File.separator
                        + rootDir
                        + File.separator + "photo" + File.separator + "customer")
                fileExists(file)
            }
            return file
        }

    /**
     * sd卡根目录
     */
    fun getSuperDir(): File? {
        var file: File? = null
        if (isSdCardExist) {
            file = File(Environment.getExternalStorageDirectory()
                    .absolutePath + File.separator)

            if (null != file) {
                fileExists(file)
            }
        }
        return file
    }

    /**
     * 缓存根目录
     *
     * @return File
     */
    fun getRootDir(): File? {
        var file: File? = null
        if (isSdCardExist) {
            file = File(Environment.getExternalStorageDirectory()
                    .absolutePath + File.separator + rootDir)
            fileExists(file)
        }
        return file
    }


    /**
     * 图片缓存根目录
     *
     * @return File
     */
    fun getImgDir(): File? {
        var file: File? = null
        if (isSdCardExist) {
            file = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                CustomBasicApplication.instance?.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            } else {
                File(Environment.getExternalStorageDirectory()
                        .absolutePath
                        + File.separator
                        + rootDir
                        + File.separator
                        + imgDir
                        + File.separator)
            }

            if (null != file) {
                fileExists(file)
            }
        }
        return file
    }

    /**
     * 音频缓存根目录
     *
     * @return File
     */
    fun getVoiceDir(): File? {
        var file: File? = null
        if (isSdCardExist) {
            file = File(Environment.getExternalStorageDirectory()
                    .absolutePath
                    + File.separator
                    + rootDir
                    + File.separator + voiceDir)
            fileExists(file)
        }
        return file
    }

    /**
     * 视频缓存根目录
     *
     * @return File
     */
    fun getVideoDir(): File? {
        var file: File? = null
        if (isSdCardExist) {
            file = File(Environment.getExternalStorageDirectory()
                    .absolutePath
                    + File.separator
                    + rootDir
                    + File.separator + videoDir)
            fileExists(file)
        }
        return file
    }

    /**
     * 其他缓存根目录
     *
     * @return File
     */
    fun getLogDir(): File? {
        var file: File? = null
        if (isSdCardExist) {
            file = File(Environment.getExternalStorageDirectory()
                    .absolutePath
                    + File.separator
                    + rootDir
                    + File.separator + logDir)
            fileExists(file)
        }
        return file
    }

    /**
     * 其他缓存根目录
     *
     * @return File
     */
    fun getOtherDir(): File? {
        var file: File? = null
        if (isSdCardExist) {
            file = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                CustomBasicApplication.instance?.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            } else {
                File(Environment.getExternalStorageDirectory()
                        .absolutePath
                        + File.separator
                        + rootDir
                        + File.separator
                        + otherDir
                        + File.separator)
            }

            if (null != file) {
                fileExists(file)
            }
        }
        return file
    }

    fun fileExists(file: File) {
        if (!file.exists()) {
            file.mkdirs()
        }
    }

    /**
     * 获取手机SD卡空间大小信息
     *
     * @return
     */
    fun readSDCard(): Long? {
        val state = Environment.getExternalStorageState()
        if (Environment.MEDIA_MOUNTED == state) {
            val sdcardDir = Environment.getExternalStorageDirectory()
            val sf = StatFs(sdcardDir.path)
            val blockSize = sf.blockSize.toLong()
            val availCount = sf.availableBlocks.toLong()
            return availCount * blockSize
        }
        return null
    }
}
