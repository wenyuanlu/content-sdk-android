package com.maishuo.haohai.widgets.refresh;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewParent;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.maishuo.haohai.R;
import com.qichuang.commonlibs.basic.CustomBaseAdapter;
import com.qichuang.commonlibs.widgets.refresh.CommonRefreshView;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener;

import java.util.List;

import static com.qichuang.commonlibs.basic.BasicCommonRefreshListener.DEFAULT_DATA_LIST_PAGE;
import static com.qichuang.commonlibs.basic.BasicCommonRefreshListener.DEFAULT_PAGING_LIST_SIZE;

public class RefreshView extends FrameLayout implements View.OnClickListener {

    private int start = DEFAULT_DATA_LIST_PAGE;
    private int rows = DEFAULT_PAGING_LIST_SIZE;

    /**
     * 空数据提示
     */
    private String emptyDataTips = "暂无数据";

    /**
     * 是否禁止上拉（默认可用）
     */
    private Boolean loadMoreEnable = true;

    /**
     * 是否显示没有更多数据View（默认可用）
     */
    private Boolean showMoreDataView = true;
    /**
     * 点击是否重进加载数据
     **/
    private Boolean isClickReloadData = true;
    /**
     * 刷新还是加载
     */
    private Boolean isRefresh = true;

    private RecyclerView recycler_view;
    private EmptyView emptyView;
    private View view_load_all_finish;
    private CommonRefreshView refreshLayout;

    public void setRefreshListener(OnRefreshListener listener) {
        this.listener = listener;
    }

    private OnRefreshListener listener;

    public RefreshView(Context context) {
        this(context, null);
    }

    public RefreshView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
        initData();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_recycler_refresh, this);
        recycler_view = findViewById(R.id.recycler_view);
        refreshLayout = findViewById(R.id.refreshLayout);
//        refreshLayout.setRefreshHeader(new ClassicsHeader(getContext()));
//        refreshLayout.setRefreshFooter(new ClassicsFooter(getContext()));
        view_load_all_finish = LayoutInflater.from(getContext()).inflate(R.layout.view_recycler_finish, null);

        setLayoutManager();
    }

    private void initData() {

        refreshLayout.setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                isRefresh = false;
                loadData();
            }

            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                isRefresh = true;
                start = 1;
                loadData();
            }
        });

        emptyView = findViewById(R.id.agent_empty_view);

        emptyView.bind(refreshLayout);
        emptyView.setOnReloadListener(this);
    }

    public RecyclerView getRecyclerView() {
        return recycler_view;
    }

    /**
     * 获取 emptyView 布局
     *
     * @return
     */
    public EmptyView getemptyView() {
        return emptyView;
    }

    public void setAdapter(@Nullable RecyclerView.Adapter adapter) {
        recycler_view.setAdapter(adapter);
    }

    public void setLayoutManager() {
        this.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    public void setLayoutManager(int spanCount) {
        this.setLayoutManager(new GridLayoutManager(getContext(), spanCount));
    }

    public void setLayoutManager(@Nullable RecyclerView.LayoutManager layout) {
        recycler_view.setLayoutManager(layout);
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    /**
     * 是否禁止 下拉刷新 （默认可用）
     *
     * @param refreshEnable
     */
    public void setRefreshEnable(Boolean refreshEnable) {
        refreshLayout.setEnableRefresh(refreshEnable);
    }

    /**
     * 是否禁止 上拉加载 （默认可用）
     *
     * @param enableFooter
     */
    public void setLoadMoreEnable(Boolean enableFooter) {
        loadMoreEnable = enableFooter;
    }

    /**
     * 是否显示 所有数据加载完成提示 （默认可用）
     *
     * @param moreData
     */
    public void setShowMoreDataView(Boolean moreData) {
        this.showMoreDataView = moreData;
    }

    /**
     * 是否点击重新加载 （默认可用）
     *
     * @param isLoadData
     */
    public void setClickReloadData(Boolean isLoadData) {
        this.isClickReloadData = isLoadData;
    }

    public void handleFailure(String errorMessage) {
        emptyView.loadFail(errorMessage);
        if (isRefresh) {
            refreshLayout.finishRefresh(false);
        } else {
            refreshLayout.finishLoadMore(false);
        }
    }

    public void handleSuccess(CustomBaseAdapter adapter, List datas) {
        if (isRefresh) {
            refreshLayout.finishRefresh();
            removeFooterLoadAllView(adapter);
        } else {
            refreshLayout.finishLoadMore();
        }
        if (start == 1) {
            if (setEmptyData(datas)) return;
            adapter.setList(datas);
        } else {
            adapter.addData(datas);
        }
        emptyView.loadSuccess();

        if (datas.size() < rows || !loadMoreEnable) {
            refreshLayout.setEnableLoadMore(false);
            if (start > 1) {
                addFooterLoadAllView(adapter);
            }
        } else {
            refreshLayout.setEnableLoadMore(true);
        }
        start++;
    }

    private boolean setEmptyData(List datas) {
        if (datas == null || datas.isEmpty()) {
            handleFailure(emptyDataTips);
            return true;
        }
        return false;
    }

    private void addFooterLoadAllView(CustomBaseAdapter adapter) {
        if (!showMoreDataView) {
            return;
        }

        ViewParent parent = view_load_all_finish.getParent();
        if (parent != null) {
            adapter.removeFooterView(view_load_all_finish);
        }
        adapter.addFooterView(view_load_all_finish);
    }

    private void removeFooterLoadAllView(CustomBaseAdapter adapter) {
        adapter.removeFooterView(view_load_all_finish);
    }

    @Override
    public void onClick(View v) {
        if (!isClickReloadData) {
            return;
        }
        reLoadData();
    }

    /**
     * 重新加载数据
     */
    public void reLoadData() {
        emptyView.loading();
        start = 1;
        loadData();
    }

    private void loadData() {
        if (listener == null) {
            return;
        }
        listener.onLoadData();
    }

    /**
     * 回调接口
     */
    public interface OnRefreshListener {
        //加载数据
        void onLoadData();
    }

    /**
     * 设置 空数据提示
     *
     * @param tips
     */
    public void setEmptyDataTips(String tips) {
        this.emptyDataTips = tips;
    }

    /**
     * 设置 空数据资源Id
     *
     * @param resId
     */
    public void setEmptyDataResId(int resId) {
        emptyView.setEmptyDataResId(resId);
    }

    /**
     * 获取 空数据提示
     *
     * @return
     */
    public String getEmptyDataTips() {
        return emptyDataTips;
    }

}
