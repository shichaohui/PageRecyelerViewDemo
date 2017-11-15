package com.example.sch.myapplication;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by shichaohui on 2015/7/9 0009.
 * <p>
 * 横向分页的GridView效果
 * </p>
 * <p>
 * 默认为1行，每页3列，如果要自定义行数和列数，请在调用{@link PageRecyclerView#setAdapter(Adapter)}方法前调用
 * {@link PageRecyclerView#setPageSize(int, int)}方法自定义行数
 * </p>
 */
public class PageRecyclerView extends RecyclerView {

    private Context mContext = null;

    private PageAdapter myAdapter = null;

    private int shortestDistance; // 超过此距离的滑动才有效
    private float slideDistance = 0; // 滑动的距离
    private float scrollX = 0; // X轴当前的位置

    private int spanRow = 1; // 行数
    private int spanColumn = 3; // 每页列数
    private int totalPage = 0; // 总页数
    private int currentPage = 1; // 当前页

    private int pageMargin = 0; // 页间距

    private PageIndicatorView mIndicatorView = null; // 指示器布局

    private int realPosition = 0; // 真正的位置（从上到下从左到右的排列方式变换成从左到右从上到下的排列方式后的位置）

    /*
	 * 0: 停止滚动且手指移开; 1: 开始滚动; 2: 手指做了抛的动作（手指离开屏幕前，用力滑了一下）
	 */
    private int scrollState = 0; // 滚动状态

    private AutoGridLayoutManager mAutoGridLayoutManager;

    public PageRecyclerView(Context context) {
        this(context, null);
    }

    public PageRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PageRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        defaultInit(context);
    }

    // 默认初始化
    private void defaultInit(Context context) {
        this.mContext = context;
        this.mAutoGridLayoutManager = new AutoGridLayoutManager(
                mContext, spanRow, AutoGridLayoutManager.HORIZONTAL, false);
        setLayoutManager(mAutoGridLayoutManager);
        setOverScrollMode(OVER_SCROLL_NEVER);
    }

    /**
     * 设置行数和每页列数
     *
     * @param spanRow    行数，<=0表示使用默认的行数
     * @param spanColumn 每页列数，<=0表示使用默认每页列数
     */
    public void setPageSize(int spanRow, int spanColumn) {
        this.spanRow = spanRow <= 0 ? this.spanRow : spanRow;
        this.spanColumn = spanColumn <= 0 ? this.spanColumn : spanColumn;

        this.mAutoGridLayoutManager = new AutoGridLayoutManager(
                mContext, this.spanRow, AutoGridLayoutManager.HORIZONTAL, false);
        setLayoutManager(mAutoGridLayoutManager);
    }

    /**
     * 设置页间距
     *
     * @param pageMargin 间距(px)
     */
    public void setPageMargin(int pageMargin) {
        this.pageMargin = pageMargin;
    }

    /**
     * 设置指示器
     *
     * @param indicatorView 指示器布局
     */
    public void setIndicator(PageIndicatorView indicatorView) {
        this.mIndicatorView = indicatorView;
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);
        shortestDistance = getMeasuredWidth() / 3;
    }

    @Override
    public void setAdapter(Adapter adapter) {
        super.setAdapter(adapter);
        this.myAdapter = (PageAdapter) adapter;
        update();
    }

    // 更新页码指示器和相关数据
    private void update() {
        // 计算总页数
        int temp = ((int) Math.ceil(myAdapter.dataList.size() / (double) (spanRow * spanColumn)));
        if (temp != totalPage) {
            mIndicatorView.initIndicator(temp);
            // 页码减少且当前页为最后一页
            if (temp < totalPage && currentPage == totalPage) {
                currentPage = temp;
                // 执行滚动
                smoothScrollBy(-getWidth(), 0);
            }
            mIndicatorView.setSelectedPage(currentPage - 1);
            totalPage = temp;
        }

        // 当页面为1时不显示指示器
        if (totalPage > 1 ){
            mIndicatorView.setVisibility(VISIBLE);
        }else {
            mIndicatorView.setVisibility(GONE);
        }
        mAutoGridLayoutManager.setTotalPages(temp);
    }

    @Override
    public void onScrollStateChanged(int state) {
        switch (state) {
            case 2:
                scrollState = 2;
                break;
            case 1:
                scrollState = 1;
                break;
            case 0:
                if (slideDistance == 0) {
                    break;
                }
                scrollState = 0;
                if (slideDistance < 0) { // 上页
                    currentPage = (int) Math.ceil(scrollX / getWidth());
                    if (currentPage * getWidth() - scrollX < shortestDistance) {
                        currentPage += 1;
                    }
                } else { // 下页
                    currentPage = (int) Math.ceil(scrollX / getWidth()) + 1;
                    if (currentPage <= totalPage) {
                        if (scrollX - (currentPage - 2) * getWidth() < shortestDistance) {
                            // 如果这一页滑出距离不足，则定位到前一页
                            currentPage -= 1;
                        }
                    } else {
                        currentPage = totalPage;
                    }
                }
                // 执行自动滚动
                smoothScrollBy((int) ((currentPage - 1) * getWidth() - scrollX), 0);
                // 修改指示器选中项
                mIndicatorView.setSelectedPage(currentPage - 1);
                slideDistance = 0;
                break;
        }
        super.onScrollStateChanged(state);
    }

    @Override
    public void onScrolled(int dx, int dy) {
        scrollX += dx;
        if (scrollState == 1) {
            slideDistance += dx;
        }

        super.onScrolled(dx, dy);
    }

    /**
     * 数据适配器
     */
    public class PageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private List<?> dataList = null;
        private CallBack mCallBack = null;
        private int itemWidth = 0;
        private int itemCount = 0;

        /**
         * 实例化适配器
         *
         * @param data
         * @param callBack
         */
        public PageAdapter(List<?> data, CallBack callBack) {
            this.dataList = data;
            this.mCallBack = callBack;
            itemCount = dataList.size() + spanRow * spanColumn;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (itemWidth <= 0) {
                // 计算Item的宽度
                itemWidth = (parent.getWidth() - pageMargin * 2) / spanColumn;
            }

            RecyclerView.ViewHolder holder = mCallBack.onCreateViewHolder(parent, viewType);

            holder.itemView.measure(0, 0);
            holder.itemView.getLayoutParams().width = itemWidth;
            holder.itemView.getLayoutParams().height = holder.itemView.getMeasuredHeight();

            return holder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (spanColumn == 1) {
                // 每个Item距离左右两侧各pageMargin
                holder.itemView.getLayoutParams().width = itemWidth + pageMargin * 2;
                holder.itemView.setPadding(pageMargin, 0, pageMargin, 0);
            } else {
                int m = position % (spanRow * spanColumn);
                if (m < spanRow) {
                    // 每页左侧的Item距离左边pageMargin
                    holder.itemView.getLayoutParams().width = itemWidth + pageMargin;
                    holder.itemView.setPadding(pageMargin, 0, 0, 0);
                } else if (m >= spanRow * spanColumn - spanRow) {
                    // 每页右侧的Item距离右边pageMargin
                    holder.itemView.getLayoutParams().width = itemWidth + pageMargin;
                    holder.itemView.setPadding(0, 0, pageMargin, 0);
                } else {
                    // 中间的正常显示
                    holder.itemView.getLayoutParams().width = itemWidth;
                    holder.itemView.setPadding(0, 0, 0, 0);
                }
            }

            countRealPosition(position);

            holder.itemView.setTag(realPosition);

            setListener(holder);

            if (realPosition < dataList.size()) {
                holder.itemView.setVisibility(View.VISIBLE);
                mCallBack.onBindViewHolder(holder, realPosition);
            } else {
                holder.itemView.setVisibility(View.INVISIBLE);
            }

        }

        @Override
        public int getItemCount() {
            return itemCount;
        }

        private void countRealPosition(int position) {
            // 为了使Item从左到右从上到下排列，需要position的值
            int m = position % (spanRow * spanColumn);
            switch (m) {
                case 1:
                case 5:
                    realPosition = position + 2;
                    break;
                case 3:
                case 7:
                    realPosition = position - 2;
                    break;
                case 2:
                    realPosition = position + 4;
                    break;
                case 6:
                    realPosition = position - 4;
                    break;
                case 0:
                case 4:
                case 8:
                    realPosition = position;
                    break;
            }
        }

        private void setListener(ViewHolder holder) {
            // 设置监听
            holder.itemView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCallBack.onItemClickListener(v, (Integer) v.getTag());
                }
            });

            holder.itemView.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mCallBack.onItemLongClickListener(v, (Integer) v.getTag());
                    return true;
                }
            });
        }

        /**
         * 删除Item
         * @param position 位置
         */
        public void remove(int position) {
            if (position < dataList.size()) {
                // 删除数据
                dataList.remove(position);
                itemCount--;
                // 删除Item
                notifyItemRemoved(position);
                // 更新界面上发生改变的Item
                notifyItemRangeChanged((currentPage - 1) * spanRow * spanColumn, currentPage * spanRow * spanColumn);
                // 更新页码指示器
                update();
            }
        }

    }

    public interface CallBack {

        /**
         * 创建VieHolder
         *
         * @param parent
         * @param viewType
         */
        RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType);

        /**
         * 绑定数据到ViewHolder
         *
         * @param holder
         * @param position
         */
        void onBindViewHolder(RecyclerView.ViewHolder holder, int position);

        void onItemClickListener(View view, int position);
        void onItemLongClickListener(View view, int position);

    }

}