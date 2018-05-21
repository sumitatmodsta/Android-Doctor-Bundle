package com.modastadoc.doctors.docconnect.model;

/**
 * Created by vijay.hiremath on 02/12/16.
 */
public class ServerAttachmentModel
{
    String id;
    String img_url;

    public ServerAttachmentModel(String id, String img_url)
    {
        this.id = id;
        this.img_url = img_url;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getImg_url()
    {
        return img_url;
    }

    public void setImg_url(String img_url)
    {
        this.img_url = img_url;
    }
}
