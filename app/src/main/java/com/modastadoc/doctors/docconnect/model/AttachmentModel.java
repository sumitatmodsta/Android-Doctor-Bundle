package com.modastadoc.doctors.docconnect.model;

import android.net.Uri;

/**
 * Created by vijay.hiremath on 23/11/16.
 */
public class AttachmentModel
{
    Uri imageUri;
    String displayName;

    public AttachmentModel(Uri imageUri, String displayName)
    {
        this.imageUri = imageUri;
        this.displayName = displayName;
    }

    public Uri getImageUri()
    {
        return imageUri;
    }

    public void setImageUri(Uri imageUri)
    {
        this.imageUri = imageUri;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public void setDisplayName(String displayName)
    {
        this.displayName = displayName;
    }
}
