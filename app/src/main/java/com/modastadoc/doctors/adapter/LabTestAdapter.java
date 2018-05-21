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
import java.util.List;

/**
 * Created by vijay.hiremath on 05/10/16.
 */
public class LabTestAdapter extends RecyclerView.Adapter<LabTestAdapter.ViewHolder> implements Filterable
{

    private ArrayList<LabTestModel> orignalList = new ArrayList<>();
    private ArrayList<LabTestModel> items;
    private static OptionClickListener mOptionClickListener;
    Context mContext;
    private List<LabTestModel> orig;
    private ArrayList<LabTestModel> selectedLabs = new ArrayList<>();
    private ArrayList<String> selectedLabTestIdList = new ArrayList<>();

    public LabTestAdapter(ArrayList<LabTestModel> mDataset, Context context)
    {
        this.items = mDataset;
        orignalList.addAll(items);
        this.mContext = context;
    }

    @Override
    public Filter getFilter()
    {
        return new Filter()
        {
            @Override
            protected FilterResults performFiltering(CharSequence constraint)
            {
                final FilterResults oReturn = new FilterResults();
                final List<LabTestModel> results = new ArrayList<LabTestModel>();

                if (orig == null) orig = items;

                if (constraint != null)
                {
                    if (orig != null & orig.size() > 0)
                    {
                        for (final LabTestModel g : orig)
                        {
                            if (g.getLab().toLowerCase().contains(constraint.toString()))
                                results.add(g);
                        }
                    }

                    oReturn.values = results;
                }
                return oReturn;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results)
            {
                items = (ArrayList<LabTestModel>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        TextView tv_title;
        CheckBox tv_file_type;
        public ViewHolder(final View itemView)
        {
            super(itemView);
            tv_title = (TextView) itemView.findViewById(R.id.tv_labtest);
            tv_file_type = (CheckBox) itemView.findViewById(R.id.cb_labtest);
        }

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.labtest_item_layout, parent, false);

        ViewHolder dataObjectHolder = new ViewHolder(view);

        return dataObjectHolder;
    }

    private boolean containsSelected(int id) {
        String labId = String.valueOf(id);
        for (int i = 0; i < selectedLabTestIdList.size(); i++) {
            if (labId.equalsIgnoreCase(selectedLabTestIdList.get(i))) {
                return true;
            }
        }
        return false;
    }

    public void setSelectedId(ArrayList<String> idList) {
        if (selectedLabTestIdList.size() > 0) {
            selectedLabTestIdList.clear();
        }
        selectedLabTestIdList.addAll(idList);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position)
    {


        String lab        = items.get(position).getLab();
        boolean isChecked = items.get(position).isChecked();

        holder.tv_title.setText(lab);

        holder.tv_file_type.setOnCheckedChangeListener(null);

        if (checkedSelectedLabTest(items.get(position)) || isChecked )
        {
            holder.tv_file_type.setChecked(true);
        }
        else
        {
            holder.tv_file_type.setChecked(false);
        }

        holder.tv_file_type.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                mOptionClickListener.onItemClick( position , isChecked);
            }
        });
    }

    private boolean checkedSelectedLabTest(LabTestModel model) {
        if (containsSelected(Integer.parseInt(model.getId()))) {
            selectedLabs.add(model);
            return true;
        }
        return false;
    }

    @Override
    public int getItemCount()
    {
        return items.size();
    }

    public interface OptionClickListener
    {
        void onItemClick(int positions, boolean isChecked);
    }

    public void setOnItemClickListener(OptionClickListener myClickListener)
    {
        this.mOptionClickListener = myClickListener;
    }

    public void addThisItem(int position)
    {
        selectedLabs.add(items.get(position));
        selectedLabTestIdList.add(items.get(position).getId());
    }

    public void removeThisItem(int position)
    {
        selectedLabTestIdList.remove(items.get(position).getId());
        String lab = items.get(position).getLab();
        for (int i = 0; i < selectedLabs.size(); i++)
        {
            if (lab.equalsIgnoreCase(selectedLabs.get(i).getLab()))
            {
                selectedLabs.remove(i);
                break;
            }
        }
    }

    public ArrayList<LabTestModel> getSelectedLabs() {
        ArrayList<LabTestModel> selectedTest = new ArrayList<>();
        for (int i = 0; i < selectedLabTestIdList.size(); i++) {
            for (int j = 0; j < orignalList.size(); j++) {
                if (selectedLabTestIdList.get(i).equalsIgnoreCase(orignalList.get(j).getId())) {
                    selectedTest.add(orignalList.get(j));
                }
            }
        }
        return selectedTest;
    }
}
