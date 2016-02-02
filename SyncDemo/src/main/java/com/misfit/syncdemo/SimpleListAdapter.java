package com.misfit.syncdemo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

/**
 * Created by Will Hou on 1/20/16.
 */
public abstract class SimpleListAdapter<T,VH extends SimpleListAdapter.ViewHolder> extends BaseAdapter {
    protected List<T> mData;
    private LayoutInflater mInflater;
    private int mLayoutId; // item's layout id

    public SimpleListAdapter(Context context, List<T> data, int layoutID) {
        super();
        mData = data;
        mLayoutId = layoutID;
        mInflater = LayoutInflater.from(context);
    }

    public LayoutInflater getmInflater() {
        return mInflater;
    }

    public int getmLayoutId() {
        return mLayoutId;
    }

    /**
     * generate View and its ViewHolder
     *
     * @param parent
     * @return
     */
    protected View createView(ViewGroup parent, int type) {
        View view = mInflater.inflate(mLayoutId, parent, false);
        VH holder = createViewHolder(view, type);
        view.setTag(holder);
        return view;
    }

    protected abstract VH createViewHolder(View itemView, int type);

    /**
     * bind data to the View
     *
     * @param holder   convertView's ViewHolder, use holder.getViewById() to get View
     * @param item
     * @param position
     */
    abstract protected void bindData(VH holder, T item, int position);

    /**
     * if you need, it only run when convertView is null
     *
     * @param holder
     * @param position
     */
    protected void onCreateView(ViewHolder holder, int position) {
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        VH holder = null;
        if (convertView == null) {
            convertView = createView(parent, getItemViewType(position));
            onCreateView((ViewHolder) convertView.getTag(), position);
        }
        holder = (VH) convertView.getTag();
        bindData(holder, getItem(position), position);
        return convertView;
    }

    @Override
    public int getCount() {
        if (mData == null) {
            return 0;
        } else {
            return mData.size();
        }
    }

    @Override
    public T getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void changeDataSources(List<T> data) {
        mData = data;
        notifyDataSetChanged();
    }

    public static class ViewHolder {
        protected View itemView;
        public ViewHolder(View itemView) {
            this.itemView = itemView;
        }
    }
}
