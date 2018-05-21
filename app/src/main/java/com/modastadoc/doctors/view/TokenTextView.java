package com.modastadoc.doctors.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.modastadoc.doctors.R;

/**
 * Created by kunasi on 14/08/17.
 */

public class TokenTextView extends TextView {
    public TokenTextView(Context context) {
        super(context);
    }

    public TokenTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        setCompoundDrawablesWithIntrinsicBounds(0, 0, selected ? R.drawable.cancel : 0, 0);
    }
}
