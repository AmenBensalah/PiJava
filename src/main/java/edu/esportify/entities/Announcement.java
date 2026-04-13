package edu.esportify.entities;

import java.time.LocalDateTime;
import java.util.Objects;

public class Announcement {
    private int id;
    private String title;
    private String content;
    private String tag;
    private String link;
    private String mediaType;
    private String mediaFilename;
    private LocalDateTime createdAt;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
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

    public String getDisplayTitle() {
        if (title != null && !title.isBlank()) {
            return title;
        }
        if (content == null || content.isBlank()) {
            return "Annonce";
        }
        return content.length() > 42 ? content.substring(0, 42) + "..." : content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Announcement that)) {
            return false;
        }
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
