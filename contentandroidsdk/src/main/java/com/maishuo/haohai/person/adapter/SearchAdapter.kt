package com.maishuo.haohai.person.adapter

import com.maishuo.haohai.R
import com.maishuo.haohai.api.response.GetListResponse
import com.qichuang.commonlibs.basic.CustomBaseAdapter
import com.qichuang.commonlibs.basic.CustomBaseViewHolder

/**
 * author : xpSun
 * date : 11/15/21
 * description :
 */
class SearchAdapter :
    CustomBaseAdapter<GetListResponse, CustomBaseViewHolder>(R.layout.item_search) {

    override fun onConvert(holder: CustomBaseViewHolder, item: GetListResponse?) {
        holder.setText(R.id.tv_search_item, item?.album_name)
    }
}