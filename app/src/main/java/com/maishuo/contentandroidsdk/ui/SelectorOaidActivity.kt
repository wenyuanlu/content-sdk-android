package com.maishuo.contentandroidsdk.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.maishuo.contentandroidsdk.R
import com.maishuo.contentandroidsdk.base.BaseActivity
import com.maishuo.contentandroidsdk.common.SimpleConstant
import com.maishuo.contentandroidsdk.utils.PreferencesUtils
import com.maishuo.haohai.common.ContentAndroidSDK

/**
 * author : xpSun
 * date : 12/30/21
 * description :
 */
class SelectorOaidActivity : BaseActivity() {

    companion object {
        private val oaid_list: MutableList<String> =
            mutableListOf("aaaabbbbcccc1111", "bbbbaaaacccc1111", "ccccaaaabbbb1111")
    }

    private var selectorOaidRecycler: RecyclerView? = null
    private var adapter: SelectorOaidAdapter? = null
    private var input: EditText? = null
    private var confirm: TextView? = null

    override fun fetchRootViewById(): Int {
        return R.layout.activity_selector_oaid_layout
    }

    override fun initWidgets() {
        selectorOaidRecycler = findViewById(R.id.selector_oaid_recycler)
        input = findViewById(R.id.selector_oaid_input)
        confirm = findViewById(R.id.selector_oaid_confirm)

        selectorOaidRecycler?.let {
            it.layoutManager = LinearLayoutManager(this)
            adapter = SelectorOaidAdapter()
            it.adapter = adapter
            adapter?.selectorOaids = oaid_list
        }
    }

    override fun initWidgetsEvent() {
        adapter?.setOnClickListener { _, position, view ->
            if (null == position) {
                return@setOnClickListener
            }

            val item = oaid_list[position]

            PreferencesUtils.putString("oaid",item)

            Toast.makeText(this, "设置成功,重启app生效,OAID为:${item}", Toast.LENGTH_SHORT).show()
        }

        confirm?.setOnClickListener {
            val item = input?.text.toString()

            PreferencesUtils.putString("oaid",item)

            Toast.makeText(this, "设置成功,重启app生效,OAID为:${item}", Toast.LENGTH_SHORT).show()
        }
    }

    private inner class SelectorOaidAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var selectorOaids: MutableList<String>? = null
            set(value) {
                field = value
                notifyDataSetChanged()
            }

        private var onItemClickListener: ((
            adapter: RecyclerView.Adapter<*>,
            position: Int?,
            view: View
        ) -> Unit)? = null

        fun setOnClickListener(
            onItemClickListener: ((
                adapter: RecyclerView.Adapter<*>,
                position: Int?,
                view: View
            ) -> Unit)?
        ) {
            this.onItemClickListener = onItemClickListener
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.view_selector_oaid_item_layout, parent, false)
            return object : RecyclerView.ViewHolder(view) {}
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (selectorOaids.isNullOrEmpty()) {
                return
            }

            val item = selectorOaids!![position]
            val tvShow = holder.itemView.findViewById<TextView>(R.id.selector_oaid_item_tv_show)
            tvShow.text = item

            tvShow.setOnClickListener {
                onItemClickListener?.invoke(this, position, it)
            }
        }

        override fun getItemCount(): Int {
            return selectorOaids?.size ?: 0
        }
    }
}