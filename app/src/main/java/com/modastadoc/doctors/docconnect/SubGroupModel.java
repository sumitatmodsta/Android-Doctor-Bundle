package com.modastadoc.doctors.docconnect;

/**
 * Created by vijay.hiremath on 10/11/16.
 */
public class SubGroupModel
{
    String id;
    String name;
    String freshness;
    String slug;
    String picUrl;
    boolean isJoined;
    String groupId;

    public SubGroupModel(String id, String name, String freshness, String slug, String picUrl, boolean isJoined, String groupId)
    {
        this.id = id;
        this.name = name;
        this.freshness = freshness;
        this.slug = slug;
        this.picUrl = picUrl;
        this.isJoined = isJoined;
        this.groupId = groupId;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getFreshness()
    {
        return freshness;
    }

    public void setFreshness(String freshness)
    {
        this.freshness = freshness;
    }

    public String getSlug()
    {
        return slug;
    }

    public void setSlug(String slug)
    {
        this.slug = slug;
    }

    public String getPicUrl()
    {
        return picUrl;
    }

    public void setPicUrl(String picUrl)
    {
        this.picUrl = picUrl;
    }

    public boolean isJoined()
    {
        return isJoined;
    }

    public void setIsJoined(boolean isJoined)
    {
        this.isJoined = isJoined;
    }

    public String getGroupId()
    {
        return groupId;
    }

    public void setGroupId(String groupId)
    {
        this.groupId = groupId;
    }
}
