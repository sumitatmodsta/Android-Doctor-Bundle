package com.modastadoc.doctors.view;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.modastadoc.doctors.R;
import com.modastadoc.doctors.model.Contact;
import com.modastadoc.doctors.model.LabTest;
import com.tokenautocomplete.TokenCompleteTextView;

/**
 * Created by kunasi on 14/08/17.
 */

public class ContactsCompletionView extends TokenCompleteTextView<LabTest> {
    public ContactsCompletionView(Context context) {
        super(context);
    }

    public ContactsCompletionView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ContactsCompletionView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected View getViewForObject(LabTest test) {
        LayoutInflater l = (LayoutInflater) getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View tokenView = l.inflate(R.layout.item_autocomplete_contact, (ViewGroup) getParent(), false);
        TokenTextView textView = (TokenTextView) tokenView.findViewById(R.id.token_text);
        //ImageView icon = (ImageView) tokenView.findViewById(R.id.icon);
        textView.setText(test.name);
        //icon.setImageResource(contact.getDrawableId());

        return tokenView;
    }

    @Override
    protected LabTest defaultObject(String completionText) {
        //Stupid simple example of guessing if we have an email or not
        /*int index = completionText.indexOf('@');
        if (index == -1) {
            return new Contact("id", completionText, completionText.replace(" ", "") + "@example.com");
        } else {
            return new Contact("id", completionText.substring(0, index), completionText);
        }*/
        return new LabTest("-1", completionText);
    }
}
