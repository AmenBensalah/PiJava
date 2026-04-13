package edu.esportify.entities;

import java.time.LocalDateTime;
import java.util.Objects;

public class FilActualite {
    private int id;
    private String content;
    private String mediaType;
    private String mediaFilename;
    private LocalDateTime createdAt;
    private String imagePath;
    private String videoUrl;
    private boolean event;
    private String eventTitle;
    private LocalDateTime eventDate;
    private String eventLocation;
    private Integer maxParticipants;
    private Integer authorId;

    public FilActualite() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getMediaFilename() {
        return mediaFilename;
    }

    public void setMediaFilename(String mediaFilename) {
        this.mediaFilename = mediaFilename;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public boolean isEvent() {
        return event;
    }

    public void setEvent(boolean event) {
        this.event = event;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }

    public LocalDateTime getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDateTime eventDate) {
        this.eventDate = eventDate;
    }

    public String getEventLocation() {
        return eventLocation;
    }

    public void setEventLocation(String eventLocation) {
        this.eventLocation = eventLocation;
    }

    public Integer getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(Integer maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public Integer getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Integer authorId) {
        this.authorId = authorId;
    }

    public String getDisplayTitle() {
        if (event && eventTitle != null && !eventTitle.isBlank()) {
            return eventTitle;
        }
        if (content == null || content.isBlank()) {
            return "Publication sans texte";
        }
        return content.length() > 42 ? content.substring(0, 42) + "..." : content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FilActualite that)) {
            return false;
        }
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
