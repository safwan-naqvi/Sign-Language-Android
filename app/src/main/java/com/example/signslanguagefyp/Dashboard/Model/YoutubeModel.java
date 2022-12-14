package com.example.signslanguagefyp.Dashboard.Model;

public class YoutubeModel {
    String video_title,video_desc,video_id,video_thumbnail;

    public YoutubeModel(){}

    public YoutubeModel(String video_title, String video_desc, String video_id,String video_thumbnail) {
        this.video_title = video_title;
        this.video_desc = video_desc;
        this.video_id = video_id;
        this.video_thumbnail = video_thumbnail;
    }

    public String getVideo_thumbnail() {
        return video_thumbnail;
    }

    public void setVideo_thumbnail(String video_thumbnail) {
        this.video_thumbnail = video_thumbnail;
    }

    public String getVideo_title() {
        return video_title;
    }

    public void setVideo_title(String video_title) {
        this.video_title = video_title;
    }

    public String getVideo_desc() {
        return video_desc;
    }

    public void setVideo_desc(String video_desc) {
        this.video_desc = video_desc;
    }

    public String getVideo_id() {
        return video_id;
    }

    public void setVideo_id(String video_id) {
        this.video_id = video_id;
    }
}
