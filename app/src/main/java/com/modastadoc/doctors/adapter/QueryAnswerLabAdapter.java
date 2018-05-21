package com.modastadoc.doctors.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.modastadoc.doctors.R;
import com.modastadoc.doctors.model.LabTestModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class QueryAnswerLabAdapter extends RecyclerView.Adapter<QueryAnswerLabAdapter.ViewHolder> implements Filterable {

    private Context mContext;

    private ArrayList<LabTestModel> items, original;
    private HashSet<String> mSelectedLabIdList = new HashSet<>();
    private ArrayList<String> mSelectedIdsArray;


    public QueryAnswerLabAdapter(ArrayList<LabTestModel> mDataset, Context context) {
        this.original = mDataset;
        this.items = mDataset;
        this.mContext = context;
    }

    public void SetSelectedLabLestList(HashSet<String> list) {
        if (mSelectedIdsArray != null) {
            mSelectedIdsArray.clear();
            mSelectedIdsArray = null;
        }
        mSelectedLabIdList = list;
        mSelectedIdsArray = new ArrayList<>(mSelectedLabIdList);
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.labtest_item_layout, parent, false);

        ViewHolder dataObjectHolder = new ViewHolder(view);

        return dataObjectHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        String lab = items.get(position).getLab();
        //boolean isChecked = items.get(position).isChecked();

        holder.tv_title.setText(lab);

        holder.tv_file_type.setOnCheckedChangeListener(null);

        if (checkedSelectedLabTest(items.get(position))) {
            holder.tv_file_type.setChecked(true);
        } else {
            holder.tv_file_type.setChecked(false);
        }


        holder.tv_file_type.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (position < items.size()) {
                        mSelectedLabIdList.add(items.get(position).getId());
                        mSelectedIdsArray.add(items.get(position).getId());
                    }
                } else {
                    if (position < items.size()) {
                        mSelectedLabIdList.remove(items.get(position).getId());
                        mSelectedIdsArray.remove(items.get(position).getId());
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                final FilterResults oReturn = new FilterResults();
                final List<LabTestModel> results = new ArrayList<LabTestModel>();

                if (constraint != null) {
                    if (original != null & original.size() > 0) {
                        for (final LabTestModel g : original) {
                            if (g.getLab().toLowerCase().contains(constraint.toString()))
                                results.add(g);
                        }
                    }

                    oReturn.values = results;
                }
                return oReturn;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                items = (ArrayList<LabTestModel>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_title;
        CheckBox tv_file_type;

        public ViewHolder(final View itemView) {
            super(itemView);
            tv_title = (TextView) itemView.findViewById(R.id.tv_labtest);
            tv_file_type = (CheckBox) itemView.findViewById(R.id.cb_labtest);
        }

    }

    public HashSet<String> getmSelectedLabIdList() {
        return mSelectedLabIdList;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////      Private Methods                             /////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////

    private boolean checkedSelectedLabTest(LabTestModel model) {
        if (mSelectedIdsArray == null) {
            return false;
        }

        for (String id :
                mSelectedIdsArray) {
            if (id.equalsIgnoreCase(model.getId())) {
                return true;
            }
        }
        return false;
    }
}
