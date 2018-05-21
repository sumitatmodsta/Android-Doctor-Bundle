package com.modastadoc.doctors.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.modastadoc.doctors.R;
import com.modastadoc.doctors.model.LabTest;
import com.tokenautocomplete.FilteredArrayAdapter;

import java.util.List;

/**
 * Created by kunai on 14/08/17.
 */

public class FilterAdapter extends FilteredArrayAdapter<LabTest> {

    public FilterAdapter(Context context, int resource, List<LabTest> objects) {
        super(context, resource,  objects);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {

            LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_item_labtest, parent, false);
        }

        LabTest contact = getItem(position);
        ((TextView) convertView.findViewById(R.id.name)).setText(contact != null ? contact.name : null);

        return convertView;
    }

    @Override
    protected boolean keepObject(LabTest test, String mask) {
        mask = mask.toLowerCase();
        return test.name.toLowerCase().contains(mask);
    }
}