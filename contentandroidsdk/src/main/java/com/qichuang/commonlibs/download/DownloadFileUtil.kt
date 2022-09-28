package com.qichuang.commonlibs.download

import android.content.Context
import android.os.Environment
import com.qichuang.commonlibs.utils.LogUtils
import java.io.File

/**
 * author ：Seven
 * date : 12/16/21
 * description :
 */
class DownloadFileUtil {

    fun getFilePath(context: Context, fileName: String): String {
        if (fileName.isEmpty()) {
            return ""
        }
        return getDirPath(context, "download") + File.separator + fileName
    }

    fun getDirPath(context: Context, dirName: String): String {
        var directoryPath = ""
        directoryPath =
            if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) { //判断外部存储是否可用
                context.getExternalFilesDir(dirName)!!.absolutePath
            } else { //没外部存储就使用内部存储
                context.filesDir.toString() + File.separator + dirName
            }
        val file = File(directoryPath)
        if (!file.exists()) { //判断文件目录是否存在
            file.mkdirs()
        }
        return directoryPath
    }

    fun getInternalFilePath(context: Context, fileName: String): String {
        if (fileName.isEmpty()) {
            return ""
        }
        return getInternalDirPath(context, "download") + File.separator + fileName
    }

    fun getInternalDirPath(context: Context, dirName: String): String {
        val directoryPath = context.filesDir.toString() + File.separator + dirName
        val file = File(directoryPath)
        if (!file.exists()) { //判断文件目录是否存在
            file.mkdirs()
        }
        return directoryPath
    }

    /** 删除文件，可以是文件或文件夹
     * @param delFile 要删除的文件夹或文件名
     * @return 删除成功返回true，否则返回false
     */
    fun delete(delFile: String): Boolean {
        val file = File(delFile)
        return if (!file.exists()) {
            //            Toast.makeText(HnUiUtils.getContext(), "删除文件失败:" + delFile + "不存在！", Toast.LENGTH_SHORT).show();
            LogUtils.LOGE("删除文件失败:" + delFile + "不存在！")
            false
        } else {
            if (file.isFile) deleteSingleFile(delFile) else deleteDirectory(delFile)
        }
    }

    /** 删除单个文件
     * @param fileName 要删除的文件的文件名
     * @return 单个文件删除成功返回true，否则返回false
     */
    private fun deleteSingleFile(fileName: String): Boolean {
        val file = File(fileName)
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        return if (file.exists() && file.isFile) {
            if (file.delete()) {
                LogUtils.LOGE(
                    "--Method--",
                    "Copy_Delete.deleteSingleFile: 删除单个文件" + fileName + "成功！"
                )
                true
            } else {
                LogUtils.LOGE("删除单个文件" + fileName + "失败！")
                false
            }
        } else {
            LogUtils.LOGE("删除单个文件失败：" + fileName + "不存在！")
            false
        }
    }

    /** 删除目录及目录下的文件
     * @param filePath 要删除的目录的文件路径
     * @return 目录删除成功返回true，否则返回false
     */
    private fun deleteDirectory(filePath: String): Boolean {
        // 如果dir不以文件分隔符结尾，自动添加文件分隔符
        var filePath = filePath
        if (!filePath.endsWith(File.separator)) filePath += File.separator
        val dirFile = File(filePath)
        // 如果dir对应的文件不存在，或者不是一个目录，则退出
        if (!dirFile.exists() || !dirFile.isDirectory) {
            LogUtils.LOGE("删除目录失败：" + filePath + "不存在！")
            return false
        }
        var flag = true
        // 删除文件夹中的所有文件包括子目录
        val files = dirFile.listFiles()
        for (file in files) {
            // 删除子文件
            if (file.isFile) {
                flag = deleteSingleFile(file.absolutePath)
                if (!flag) break
            } else if (file.isDirectory) {
                flag = deleteDirectory(file.absolutePath)
                if (!flag) break
            }
        }
        if (!flag) {
            LogUtils.LOGE("删除目录失败！")
            return false
        }
        // 删除当前目录
        return if (dirFile.delete()) {
            LogUtils.LOGE("--Method--", "Copy_Delete.deleteDirectory: 删除目录" + filePath + "成功！")
            true
        } else {
            LogUtils.LOGE("删除目录：" + filePath + "失败！")
            false
        }
    }
}