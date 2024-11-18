package com.example.lessonmanager.models;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Date;

public class Lesson implements Parcelable {
    private String lessonId;
    private String userId;
    private String title;
    private String description;
    private String subject;
    private Date date;
    private String status; // "upcoming" or "completed"
    private Date createdAt;
    private Date updatedAt;

    // Empty constructor for Firebase
    public Lesson() {
    }

    public Lesson(String userId, String title, String description, String subject, Date date) {
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.subject = subject;
        this.date = date;
        this.status = "upcoming";
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    // Getters and Setters
    public String getLessonId() { return lessonId; }
    public void setLessonId(String lessonId) { this.lessonId = lessonId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    // Parcelable implementation
    protected Lesson(Parcel in) {
        lessonId = in.readString();
        userId = in.readString();
        title = in.readString();
        description = in.readString();
        subject = in.readString();
        date = new Date(in.readLong());
        status = in.readString();
        createdAt = new Date(in.readLong());
        updatedAt = new Date(in.readLong());
    }

    public static final Creator<Lesson> CREATOR = new Creator<Lesson>() {
        @Override
        public Lesson createFromParcel(Parcel in) {
            return new Lesson(in);
        }

        @Override
        public Lesson[] newArray(int size) {
            return new Lesson[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(lessonId);
        dest.writeString(userId);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(subject);
        dest.writeLong(date.getTime());
        dest.writeString(status);
        dest.writeLong(createdAt.getTime());
        dest.writeLong(updatedAt.getTime());
    }
}