package com.modastadoc.doctors.docconnect;

import com.modastadoc.doctors.docconnect.model.ServerAttachmentModel;

import java.util.ArrayList;

/**
 * Created by vijay.hiremath on 11/11/16.
 */
public class CommentModel
{
    String id;
    String comment;
    String author;
    String postDate;
    ArrayList<ServerAttachmentModel> imageList;
    ArrayList<String> videoList;
    boolean editable;

    public CommentModel(String id, String comment, String author, String postDate, ArrayList<ServerAttachmentModel> imageList, ArrayList<String> videoList, boolean editable)
    {
        this.id = id;
        this.comment = comment;
        this.author = author;
        this.postDate = postDate;
        this.imageList = imageList;
        this.videoList = videoList;
        this.editable = editable;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getComment()
    {
        return comment;
    }

    public void setComment(String comment)
    {
        this.comment = comment;
    }

    public String getAuthor()
    {
        return author;
    }

    public void setAuthor(String author)
    {
        this.author = author;
    }

    public String getPostDate()
    {
        return postDate;
    }

    public void setPostDate(String postDate)
    {
        this.postDate = postDate;
    }

    public ArrayList<ServerAttachmentModel> getImageList()
    {
        return imageList;
    }

    public void setImageList(ArrayList<ServerAttachmentModel> imageList)
    {
        this.imageList = imageList;
    }

    public ArrayList<String> getVideoList()
    {
        return videoList;
    }

    public void setVideoList(ArrayList<String> videoList)
    {
        this.videoList = videoList;
    }

    public boolean isEditable()
    {
        return editable;
    }

    public void setEditable(boolean editable)
    {
        this.editable = editable;
    }
}
