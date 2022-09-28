package com.maishuo.haohai.main.lite

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.maishuo.haohai.databinding.DialogLitePlayerBottomLayoutBinding
import com.maishuo.haohai.main.adapter.CommonViewPagerAdapter
import com.maishuo.haohai.main.event.GetProgramListResponseEvent
import com.qichuang.commonlibs.basic.BaseDialogFragment
import com.qichuang.commonlibs.utils.ScreenUtils
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class CustomLitePlayerBottomDialog constructor(appCompatActivity: AppCompatActivity?) :
    BaseDialogFragment(appCompatActivity) {

    private var fragments: MutableList<Fragment> = mutableListOf()
    private var titles: MutableList<String> = mutableListOf()
    private var adapter: CommonViewPagerAdapter? = null
    private var vb: DialogLitePlayerBottomLayoutBinding? = null

    private var catalogueFragment: CustomLitePlayerBottomForCatalogueFragment? = null
    private var descFragment: CustomLitePlayerBottomForDescFragment? = null

    var responses: GetProgramListResponseEvent? = null

    override fun fetchRootView(): View? {
        vb = DialogLitePlayerBottomLayoutBinding.inflate(LayoutInflater.from(appCompatActivity))
        return vb?.root
    }

    override fun initWidgets() {
        EventBus.getDefault().register(this)

        catalogueFragment = CustomLitePlayerBottomForCatalogueFragment()
        descFragment = CustomLitePlayerBottomForDescFragment()

        fragments.add(catalogueFragment!!)
        fragments.add(descFragment!!)

        titles.add("播放列表")
        titles.add("详情")

        catalogueFragment?.responses = responses

        val desc = if (responses?.data?.data.isNullOrEmpty())
            ""
        else
            responses?.data?.data?.get(0)?.summary
        descFragment?.desc = desc

        vb?.let {
            adapter = CommonViewPagerAdapter(childFragmentManager, fragments, titles)
            it.dialogLitePlayerBottomVp.adapter = adapter
            it.dialogLitePlayerBottomTablayout.viewPager = it.dialogLitePlayerBottomVp
            it.dialogLitePlayerBottomVp.offscreenPageLimit = fragments.size

            it.dialogLitePlayerBottomDismiss.setOnClickListener {
                dismiss()
            }
        }

        catalogueFragment?.setClickAdapterOnDismissListener {
            dismiss()
        }
    }

    override fun onResume() {
        super.onResume()
        val width = ViewGroup.LayoutParams.MATCH_PARENT
        val height = ScreenUtils.getRealyScreenHeight(appCompatActivity) * 0.6
        initWidgetSize(Gravity.BOTTOM, width, height.toInt())
    }

    //获取播放列表
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: GetProgramListResponseEvent?) {
        responses = event
        catalogueFragment?.responses = responses
    }

    override fun onDestroyView() {
        super.onDestroyView()
        EventBus.getDefault().unregister(this)
    }
}