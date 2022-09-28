package com.maishuo.haohai.person.ui

import android.text.TextUtils
import android.view.View
import android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.maishuo.haohai.api.response.GetListResponse
import com.maishuo.haohai.databinding.ActivitySearchBinding
import com.maishuo.haohai.main.ui.CustomPlayerPagerActivity
import com.maishuo.haohai.person.adapter.SearchAdapter
import com.maishuo.haohai.person.viewmodel.SearchViewModel
import com.qichuang.commonlibs.basic.CustomBaseActivity

class SearchActivity : CustomBaseActivity<ActivitySearchBinding>() {

    var viewModel: SearchViewModel? = null
    private var adapter: SearchAdapter? = null

    override fun initWidgets() {
        initViewModel()
        setRecyclerView()
        requestForData()
    }

    @Suppress("DEPRECATED_IDENTITY_EQUALS")
    override fun initWidgetsEvent() {
        vb?.let {
            it.ivSearchBack.setOnClickListener {
                finish()
            }
            it.tvSearch.setOnClickListener {
                vb?.recyclerView?.start = 1
                requestForData()
            }
            it.etSearch.setOnEditorActionListener { _, i, _ ->
                if (i === IME_ACTION_SEARCH) {
                    vb?.recyclerView?.start = 1
                    requestForData()
                    return@setOnEditorActionListener true
                }
                false
            }
        }

        adapter?.setOnItemClickListener { adapter, _, position ->
            val bean = adapter.getItem(position) as GetListResponse
            CustomPlayerPagerActivity.start(this, bean.album_type, bean, null)
        }
    }

    private fun setRecyclerView() {
        vb?.recyclerView?.setLayoutManager(LinearLayoutManager(this))
        adapter = SearchAdapter()
        vb?.recyclerView?.setAdapter(adapter)
        vb?.recyclerView?.setRefreshListener { this.requestForData() }
        vb?.recyclerView?.rows = 20
    }

    private fun initViewModel() {
        viewModel = ViewModelProvider(this).get(SearchViewModel::class.java)
        viewModel?.searchLiveData?.observe(this) {
            vb?.recyclerView?.visibility = View.VISIBLE
            when {
                it.success -> {
                    vb?.recyclerView?.handleSuccess(adapter, it.result)
                }
                else -> {
                    vb?.recyclerView?.handleFailure(it.message)
                }
            }
        }
    }

    private fun requestForData() {
        if (!TextUtils.isEmpty(vb?.etSearch?.text.toString())) {
            viewModel?.fetchMainList(
                0,
                vb?.etSearch?.text.toString(),
                vb?.recyclerView?.rows ?: 20,
                vb?.recyclerView?.start ?: 1
            )
            hintSoftInput()
        }
    }

    /**
     * 隐藏输入法
     */
    private fun hintSoftInput() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(vb?.etSearch?.windowToken, 0) //强制隐藏键盘
    }

}