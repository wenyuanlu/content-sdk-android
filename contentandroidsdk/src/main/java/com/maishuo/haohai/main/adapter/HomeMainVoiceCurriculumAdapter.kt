package com.maishuo.haohai.main.adapter

import android.app.Activity
import android.view.View
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import com.corpize.sdk.ivoice.admanager.QcAdManager
import com.maishuo.haohai.R
import com.maishuo.haohai.api.response.GetListResponse
import com.maishuo.haohai.listener.OnHomeFetchAdListener
import com.maishuo.haohai.main.viewmodel.HomeMainViewModel
import com.qichuang.commonlibs.basic.CustomBaseAdapter
import com.qichuang.commonlibs.basic.CustomBaseViewHolder
import com.qichuang.commonlibs.utils.GlideUtils

/**
 * author : xpSun
 * date : 11/9/21
 * description :
 */
class HomeMainVoiceCurriculumAdapter : CustomBaseAdapter<GetListResponse,
        CustomBaseViewHolder>(R.layout.view_home_main_voice_curriculum_item_layout) {

    override fun onConvert(holder: CustomBaseViewHolder, item: GetListResponse?) {
        val itemRootView = holder.getView<RelativeLayout>(R.id.curriculum_item_root_view)
        val adRootView = holder.getView<FrameLayout>(R.id.curriculum_item_ad_view)
        if (1 != item?.isAd) {
            itemRootView.visibility = View.VISIBLE
            adRootView.visibility = View.GONE

            GlideUtils.loadImage(
                holder.itemView.context,
                item?.album_cover,
                holder.getView(R.id.curriculum_item_iv_show),
                R.mipmap.home_item_default_icon
            )

            holder.setText(R.id.curriculum_item_text_name, item?.album_name ?: "")
            holder.setText(R.id.curriculum_item_voice, item?.anchor_name ?: "")
            holder.setText(
                R.id.curriculum_item_desc,
                "已经更新${item?.upprogram_num ?: ""}讲/共${item?.program_num ?: ""}讲"
            )
            holder.getView<CheckBox>(R.id.curriculum_item_keep_status).isChecked =
                item?.keep_status ?: 0 == 1
        } else {
            itemRootView.visibility = View.GONE
            adRootView.visibility = View.VISIBLE
            adRootView.removeAllViews()

            val context = holder.itemView.context
            if (context is AppCompatActivity) {
                val viewModel = HomeMainViewModel(context)
                viewModel.fetchHomeAd(object : OnHomeFetchAdListener {
                    override fun onAdReceive(manager: QcAdManager?, view: View?) {
                        adRootView.addView(view)
                    }

                    override fun onRollAdClickClose() {
                        data.removeAt(holder.adapterPosition)
                        notifyDataSetChanged()
                    }

                    override fun onAdError() {
                        adRootView.visibility = View.GONE
                    }
                })
            }
        }
    }
}