package com.noticeboard.model;

import java.sql.Timestamp;

public class Notice {
    private int id;
    private String title;
    private String content;
    private String category;
    private String priority;
    private String attachmentName;
    private byte[] attachmentData;
    private String attachmentType;
    private int postedBy;
    private String postedByName;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public Notice() {}

    public Notice(String title, String content, String category, String priority, int postedBy) {
        this.title = title;
        this.content = content;
        this.category = category;
        this.priority = priority;
        this.postedBy = postedBy;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getAttachmentName() { return attachmentName; }
    public void setAttachmentName(String attachmentName) { this.attachmentName = attachmentName; }

    public byte[] getAttachmentData() { return attachmentData; }
    public void setAttachmentData(byte[] attachmentData) { this.attachmentData = attachmentData; }

    public String getAttachmentType() { return attachmentType; }
    public void setAttachmentType(String attachmentType) { this.attachmentType = attachmentType; }

    public int getPostedBy() { return postedBy; }
    public void setPostedBy(int postedBy) { this.postedBy = postedBy; }

    public String getPostedByName() { return postedByName; }
    public void setPostedByName(String postedByName) { this.postedByName = postedByName; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public boolean hasAttachment() {
        return attachmentName != null && !attachmentName.isEmpty();
    }

    public static final String[] CATEGORIES = {"General", "Academic", "Event", "Exam", "Holiday", "Sports", "Other"};
    public static final String[] PRIORITIES = {"Low", "Medium", "High", "Urgent"};
}
