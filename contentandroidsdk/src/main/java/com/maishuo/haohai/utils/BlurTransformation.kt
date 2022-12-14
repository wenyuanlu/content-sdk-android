package com.maishuo.haohai.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.renderscript.RSRuntimeException
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import jp.wasabeef.glide.transformations.internal.FastBlur
import jp.wasabeef.glide.transformations.internal.RSBlur
import java.nio.charset.Charset
import java.security.MessageDigest

/**
 * 虚化Transformation
 * 更多效果参考：https://github.com/wasabeef/glide-transformations
 */
class BlurTransformation @JvmOverloads constructor(
    private val mContext: Context,
    pool: BitmapPool = Glide.get(mContext).bitmapPool,
    radius: Int = MAX_RADIUS,
    sampling: Int = DEFAULT_DOWN_SAMPLING
) : BitmapTransformation() {
    private val mBitmapPool: BitmapPool = pool
    private val mRadius: Int = radius
    private val mSampling: Int = sampling

    override fun transform(
        pool: BitmapPool,
        toTransform: Bitmap,
        outWidth: Int,
        outHeight: Int
    ): Bitmap {
        val width = toTransform.width
        val height = toTransform.height
        val scaledWidth = width / mSampling
        val scaledHeight = height / mSampling
        var bitmap = mBitmapPool[scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888]
        val canvas = Canvas(bitmap)
        canvas.scale(1 / mSampling.toFloat(), 1 / mSampling.toFloat())
        val paint = Paint()
        paint.flags = Paint.FILTER_BITMAP_FLAG
        canvas.drawBitmap(toTransform, 0f, 0f, paint)
        bitmap = try {
            RSBlur.blur(mContext, bitmap, mRadius)
        } catch (e: RSRuntimeException) {
            FastBlur.blur(bitmap, mRadius, true)
        }
        return bitmap
    }

    override fun hashCode(): Int {
        return ID.hashCode()
    }

    override fun equals(obj: Any?): Boolean {
        return obj is BlurTransformation
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update(ID_BYTES)
    }

    companion object {
        private const val STRING_CHARSET_NAME = "UTF-8"
        private const val ID = "BlurTransformation"
        private val CHARSET = Charset.forName(STRING_CHARSET_NAME)
        private val ID_BYTES = ID.toByteArray(CHARSET)
        private const val MAX_RADIUS = 25
        private const val DEFAULT_DOWN_SAMPLING = 1
    }

}