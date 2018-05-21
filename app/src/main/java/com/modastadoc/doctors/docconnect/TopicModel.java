package com.modastadoc.doctors.docconnect;

/**
 * Created by vijay.hiremath on 10/11/16.
 */
public class TopicModel
{
    String id;
    String topicName;
    String topicAuthor;
    String updatedDate;
    String profilePic;
    boolean isEditable;

    public TopicModel(String id, String topicName, String topicAuthor, String updatedDate, String profilePic, boolean isEditable)
    {
        this.id = id;
        this.topicName = topicName;
        this.topicAuthor = topicAuthor;
        this.updatedDate = updatedDate;
        this.profilePic = profilePic;
        this.isEditable = isEditable;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getTopicName()
    {
        return topicName;
    }

    public void setTopicName(String topicName)
    {
        this.topicName = topicName;
    }

    public String getTopicAuthor()
    {
        return topicAuthor;
    }

    public void setTopicAuthor(String topicAuthor)
    {
        this.topicAuthor = topicAuthor;
    }

    public String getUpdatedDate()
    {
        return updatedDate;
    }

    public void setUpdatedDate(String updatedDate)
    {
        this.updatedDate = updatedDate;
    }

    public String getProfilePic()
    {
        return profilePic;
    }

    public void setProfilePic(String profilePic)
    {
        this.profilePic = profilePic;
    }

    public boolean isEditable()
    {
        return isEditable;
    }

    public void setIsEditable(boolean isEditable)
    {
        this.isEditable = isEditable;
    }
}


