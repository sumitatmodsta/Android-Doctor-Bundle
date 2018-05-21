package com.modastadoc.doctors.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.modastadoc.doctors.R;
import com.modastadoc.doctors.model.Language;

import java.util.ArrayList;

/**
 * Created by kunasi on 16/10/17.
 */

public class LanguageAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater inflater;
    private TextView mSelectedLang;
    private ArrayList<Language> mLanguages;

    public LanguageAdapter(Context context, TextView selectedLang) {
        this.mContext = context;
        this.mSelectedLang = selectedLang;
        inflater = (LayoutInflater.from(mContext));
    }

    @Override
    public int getCount() {
        if(mLanguages != null)
            return mLanguages.size();
        return 0;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public boolean isEnabled(int position) {
        return position != 0 && position != 12;
    }

    @Override
    public View getView(final int position, View view, ViewGroup viewGroup) {
        if(position == 0 || position == 12) {
            view = inflater.inflate(R.layout.list_item_language_header, viewGroup, false);
            TextView header = (TextView) view.findViewById(R.id.header);
            header.setText(mLanguages.get(position).name);
        }else {
            view = inflater.inflate(R.layout.list_item_language, viewGroup, false);
            CheckBox checkBox = (CheckBox) view.findViewById(R.id.value);
            checkBox.setText(mLanguages.get(position).name);
            checkBox.setChecked(mLanguages.get(position).isSelected);

            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Language l = mLanguages.get(position);
                    if(l.isSelected) {
                        l.isSelected = false;
                    }else {
                        l.isSelected = true;
                    }
                    update();
                }
            });
        }

        return view;
    }

    public void refresh(ArrayList<Language> list) {
        this.mLanguages = list;
        notifyDataSetChanged();
    }

    private void update() {
        String l = getSelected();
        if(l.isEmpty()) {
            mSelectedLang.setText("Select Languages");
        }else {
            mSelectedLang.setText(l.substring(0, l.length()-1));
        }
    }

    public String getSelected() {
        String selected = "";
        if(mLanguages != null) {
            for (Language lang : mLanguages) {
                if (lang.isSelected) {
                    selected += lang.name + ",";
                }
            }
        }

        return selected.isEmpty()?"":selected.substring(0, selected.length()-1);
    }

}
