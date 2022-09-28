package com.maishuo.haohai.person.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.maishuo.haohai.R
import com.maishuo.haohai.databinding.ActivityInterestBinding
import com.maishuo.haohai.person.viewmodel.InterestViewModel
import com.qichuang.commonlibs.basic.CustomBaseActivity
import com.qichuang.commonlibs.utils.ToastUtil

class InterestActivity : CustomBaseActivity<ActivityInterestBinding>() {

    var viewModel: InterestViewModel? = null

    override fun initWidgets() {
        setTitle("好嗨")
        setTitleRightMenu("跳过", Color.parseColor("#909099"))
        initViewModel()
        getInterestTag()
    }

    override fun onClickByTitleRightMenu() {
        super.onClickByTitleRightMenu()
//        val intent = Intent(this, SimpleMainActivity::class.java)
//        startActivity(intent)
    }

    override fun initWidgetsEvent() {
        vb?.tvComing?.setOnClickListener {
            var tags = ""
            val lines = vb?.flowLayout?.lines
            if (!lines.isNullOrEmpty()) {
                for (line in lines) {
                    for (view in line.childData) {
                        if (view.isSelected) {
                            tags += String.format("%d,", view.tag)
                        }
                    }
                }
            }
            if (tags.isEmpty()) {
                ToastUtil.showToast("请挑选你感兴趣的标签")
            } else {
                putInterestTag(tags.substringBeforeLast(","))
            }
        }
    }

    private fun initViewModel() {
        viewModel = ViewModelProvider(this).get(InterestViewModel::class.java)
    }

    /**
     * 获取标签列表
     */
    private fun getInterestTag() {
        viewModel?.getInterestTag()
        viewModel?.getInterestLiveData?.observe(this) {
            if (!it.isNullOrEmpty()) {
                for (interestResponse in it) {
                    val view =
                        LayoutInflater.from(this).inflate(R.layout.view_text, vb?.flowLayout, false)
                    val tv = view.findViewById<TextView>(R.id.text)
                    tv.text = interestResponse.tags_name
                    view.tag = interestResponse.id
                    vb?.flowLayout?.addView(view)
                    view.setOnClickListener { v ->
                        v.isSelected = !v.isSelected
                    }
                }
            }
        }
    }

    /**
     * 提交选中标签
     */
    private fun putInterestTag(tags: String) {
        viewModel?.putInterestTag(tags)
        viewModel?.putInterestLiveData?.observe(this) {
            when (it.success) {
                true -> finish()
                false -> ToastUtil.showToast(it.message)
            }
        }
    }

}