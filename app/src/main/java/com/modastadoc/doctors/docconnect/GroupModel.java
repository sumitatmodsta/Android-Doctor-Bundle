package com.modastadoc.doctors.docconnect;

/**
 * Created by vijay.hiremath on 08/11/16.
 */
public class GroupModel
{
    String id;
    String groupName;
    String groupModified;
    String groupIcon;

    public GroupModel(String id, String groupName, String groupModified, String groupIcon)
    {
        this.id = id;
        this.groupName = groupName;
        this.groupModified = groupModified;
        this.groupIcon = groupIcon;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getGroupName()
    {
        return groupName;
    }

    public void setGroupName(String groupName)
    {
        this.groupName = groupName;
    }

    public String getGroupModified()
    {
        return groupModified;
    }

    public void setGroupModified(String groupModified)
    {
        this.groupModified = groupModified;
    }

    public String getGroupIcon()
    {
        return groupIcon;
    }

    public void setGroupIcon(String groupIcon)
    {
        this.groupIcon = groupIcon;
    }
}

