package com.moonpi.swiftnotes;

/**
 * Created by Ayushi on 11/29/2017.
 */

public class Note {
    String Body;
    String Title;

    public Note(String body, String title) {
        Body = body;
        Title = title;
    }

    public String getBody() {
        return Body;
    }

    public String getTitle() {
        return Title;
    }
}
