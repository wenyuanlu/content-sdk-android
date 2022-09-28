package com.qichuang.commonlibs.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.maishuo.haohai.utils.BlurTransformation
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

@SuppressLint("CheckResult")
object GlideUtils {

    fun loadImage(context: Context, url: String?, imageView: ImageView) {
        Glide.with(context)
                .load(url)
                .into(imageView)
    }

    fun loadImage(context: Context, url: String?, imageView: ImageView, defaultImage: Int) {
        Glide.with(context)
                .load(url)
                .placeholder(defaultImage)
                .into(imageView)
    }

    fun loadImage(context: Context, file: File, imageView: ImageView) {
        Glide.with(context)
                .load(file)
                .into(imageView)
    }

    fun loadImage(context: Context, resources: Int, imageView: ImageView) {
        Glide.with(context)
                .load(resources)
                .into(imageView)
    }

    fun loadImage(
            context: Context,
            resources: String?,
            imageView: ImageView,
            options: RequestOptions
    ) {
        Glide.with(context)
                .load(resources)
                .apply(options)
                .into(imageView)
    }

    fun initImageForCropTransform(
            context: Context?,
            url: String?,
            ivShow: ImageView
    ) {
        val requestOptions = RequestOptions.circleCropTransform()
        context?.let {
            Glide.with(it).load(url).apply(requestOptions).into(ivShow)
        }
    }

    fun initImageForBitmapTransform(context: Context?, url: String?, ivShow: ImageView) {
        context?.let {
            val requestOptions = RequestOptions.bitmapTransform(
                    BlurTransformation(
                            it,
                            radius = 5,
                            sampling = 5
                    )
            )
            Glide.with(it)
                    .load(url)
                    .apply(requestOptions)
                    .into(ivShow)
        }
    }

    fun loadUrlForBitmap(context: Context?, url: String?, width: Int?, height: Int?): Bitmap? {
        if (null == context) {
            return null
        }

        return Glide.with(context)
                .asBitmap()
                .load(url)
                .submit(width ?: 0, height ?: 0)
                .get()
    }

    fun loadUrlForBitmap(context: Context?, url: Int?, width: Int?, height: Int?): Bitmap? {
        if (null == context) {
            return null
        }

        return Glide.with(context)
                .asBitmap()
                .load(url)
                .submit(width ?: 0, height ?: 0)
                .get()
    }

    fun loadRoundedCorners(
            context: Context?,
            url: String?,
            roundingRadius: Int?,
            ivShow: ImageView
    ) {
        if (null == context) {
            return
        }

        val roundedCorners = RoundedCorners(roundingRadius ?: 0)
        val options = RequestOptions.bitmapTransform(roundedCorners)

        Glide.with(context)
                .load(url)
                .apply(options)
                .into(ivShow)
    }

    //网络下载图片
    fun saveNetworkFile(activity: AppCompatActivity, url: String?, name: String?) {
        Observable.create(ObservableOnSubscribe<File> {
            it.onNext(Glide.with(activity).asFile().load(url).submit().get())
            it.onComplete()
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<File> {
                    override fun onComplete() {

                    }

                    override fun onSubscribe(d: Disposable) {

                    }

                    override fun onNext(it: File) {
                        val file: File? = SysSDCardCacheDir.getImgDir()
                        file?.let { fileIt ->
                            val fileName: String = String.format(
                                    "%s.jpg", name
                                    ?: System.currentTimeMillis().toString()
                            )
                            val appDir = File(fileIt, fileName)
                            copy(it, appDir)
                            // 最后通知图库更新
                            activity.sendBroadcast(
                                    Intent(
                                            Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                                            Uri.fromFile(File(appDir.path))
                                    )
                            )
                            ToastUtil.showToast(String.format("图片保存到%s", appDir.path))
                        }
                    }

                    override fun onError(e: Throwable) {

                    }
                })
    }

    /**
     * 复制文件
     *
     * @param source 输入文件
     * @param target 输出文件
     */
    private fun copy(source: File, target: File) {
        try {
            FileInputStream(source).use { fileInputStream ->
                FileOutputStream(target).use { fileOutputStream ->
                    val buffer = ByteArray(1024)
                    while (fileInputStream.read(buffer) > 0) {
                        fileOutputStream.write(buffer)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}