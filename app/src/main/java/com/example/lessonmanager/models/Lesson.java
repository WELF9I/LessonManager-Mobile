package com.example.lessonmanager.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Lesson {
    private String LessonId;
    private String userId;
    private String title;
    private String description;
    private String subject;
    private Date date;
    private String status; // "upcoming" or "completed"
    private List<String> pdfUrls;
    private Date createdAt;
    private Date updatedAt;

    // Empty constructor for Firebase
    public Lesson() {
        pdfUrls = new ArrayList<>();
    }

    public Lesson(String userId, String title, String description, String subject, Date date) {
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.subject = subject;
        this.date = date;
        this.status = "upcoming";
        this.pdfUrls = new ArrayList<>();
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    // Getters and Setters
    public String getLessonId() { return LessonId; }
    public void setLessonId(String id) { this.LessonId = LessonId; }


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

    public List<String> getPdfUrls() { return pdfUrls; }
    public void setPdfUrls(List<String> pdfUrls) { this.pdfUrls = pdfUrls; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}