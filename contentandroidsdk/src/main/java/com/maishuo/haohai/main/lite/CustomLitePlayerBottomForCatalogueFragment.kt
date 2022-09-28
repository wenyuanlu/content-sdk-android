package com.maishuo.haohai.main.lite

import androidx.recyclerview.widget.LinearLayoutManager
import com.maishuo.haohai.audio.AudioPlayerManager
import com.maishuo.haohai.common.Constant
import com.maishuo.haohai.databinding.FragmentLitePlayerBottomCatalogueLayoutBinding
import com.maishuo.haohai.main.adapter.CustomPlayerCatalogueChapterAdapter
import com.maishuo.haohai.main.adapter.CustomPlayerCatalogueChapterContentAdapter
import com.maishuo.haohai.main.event.CatalogueClickEvent
import com.maishuo.haohai.main.event.GetProgramListParamEvent
import com.maishuo.haohai.main.event.GetProgramListResponseEvent
import com.qichuang.commonlibs.basic.CustomBaseFragment
import org.greenrobot.eventbus.EventBus

class CustomLitePlayerBottomForCatalogueFragment :
    CustomBaseFragment<FragmentLitePlayerBottomCatalogueLayoutBinding>() {

    private var catalogueChapterAdapter: CustomPlayerCatalogueChapterAdapter? = null
    private var catalogueChapterContentAdapter: CustomPlayerCatalogueChapterContentAdapter? = null
    private var onDismissListener: (() -> Unit)? = null

    fun setClickAdapterOnDismissListener(onDismissListener: (() -> Unit)?){
        this.onDismissListener = onDismissListener
    }

    var responses: GetProgramListResponseEvent? = null
        set(value) {
            field = value
            catalogueChapterAdapter?.setNewInstance(responses?.data?.section_number_list)
            catalogueChapterContentAdapter?.setNewInstance(responses?.data?.data)
        }

    override fun initWidgets() {
        catalogueChapterAdapter = CustomPlayerCatalogueChapterAdapter()
        catalogueChapterContentAdapter = CustomPlayerCatalogueChapterContentAdapter()

        vb?.let {
            it.catalogueChapterRecycler.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            it.catalogueChapterRecycler.adapter = catalogueChapterAdapter

            it.catalogueChapterRecyclerContent.layoutManager = LinearLayoutManager(context)
            it.catalogueChapterRecyclerContent.adapter = catalogueChapterContentAdapter

            catalogueChapterAdapter?.setNewInstance(responses?.data?.section_number_list)
            catalogueChapterContentAdapter?.setNewInstance(responses?.data?.data)

            catalogueChapterAdapter?.selectorPosition = Constant.currentChildPosition
            catalogueChapterContentAdapter?.currentMediaItemId = AudioPlayerManager.getInstance().currentMediaItemId
        }
    }

    override fun initWidgetsEvent() {
        catalogueChapterAdapter?.setOnItemClickListener { _, _, position ->
            Constant.currentChildPosition = position
            catalogueChapterAdapter?.selectorPosition = position

            val event = GetProgramListParamEvent()
            event.album_id = Constant.response?.album_id
            event.page_size = 10
            event.page = (Constant.currentChildPosition ?: 0).inc()
            event.playerStatus = Constant.PLAYER_STATUS_TAG_0
            EventBus.getDefault().post(event)
        }

        catalogueChapterContentAdapter?.setOnItemClickListener { _, _, position ->
            val item = catalogueChapterContentAdapter?.getItem(position)
            catalogueChapterContentAdapter?.currentMediaItemId = item?.program_lid

            val event = CatalogueClickEvent()
            event.responses = catalogueChapterContentAdapter?.data
            event.item = item
            event.position = position
            EventBus.getDefault().post(event)

            onDismissListener?.invoke()
        }
    }

}