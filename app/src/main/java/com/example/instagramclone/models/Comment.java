package com.example.instagramclone.models;

import java.util.List;

public class Comment {

    private String comment;
    private String user_id;
    private String date_created;
    private List<Like> likes;

    public Comment(String comment, String user_id, String date_created, List<Like> likes) {
        this.comment = comment;
        this.user_id = user_id;
        this.date_created = date_created;
        this.likes = likes;
    }

    public Comment(){}

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getDate_created() {
        return date_created;
    }

    public void setDate_created(String date_created) {
        this.date_created = date_created;
    }

    public List<Like> getLikes() {
        return likes;
    }

    public void setLikes(List<Like> likes) {
        this.likes = likes;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "comment='" + comment + '\'' +
                ", user_id='" + user_id + '\'' +
                ", date_created='" + date_created + '\'' +
                ", likes=" + likes +
                '}';
    }
}
