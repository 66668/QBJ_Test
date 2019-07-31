package com.thinkernote.ThinkerNote.bean.main;

import android.nfc.Tag;

import com.thinkernote.ThinkerNote.bean.CommonBean;

import java.io.Serializable;
import java.util.List;

/**
 * 获取笔记
 * 格式{code msg data {}}
 */

public class GetNoteByNoteIdBean implements Serializable {
    long id;
    int folder_id;
    String content_digest;
    String title;
    String trash;
    String address;
    String content;
    String create_at;
    String update_at;
    int longitude;
    int latitude;
    int radius;
    List<TagBean> tags;
    List<Attachments> attachments;

    public class TagBean implements Serializable {
        String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public class Attachments implements Serializable {
        long id;
        String digest;
        String name;
        int type;
        int size;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDigest() {
            return digest;
        }

        public void setDigest(String digest) {
            this.digest = digest;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getFolder_id() {
        return folder_id;
    }

    public void setFolder_id(int folder_id) {
        this.folder_id = folder_id;
    }

    public String getContent_digest() {
        return content_digest;
    }

    public void setContent_digest(String content_digest) {
        this.content_digest = content_digest;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTrash() {
        return trash;
    }

    public void setTrash(String trash) {
        this.trash = trash;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCreate_at() {
        return create_at;
    }

    public void setCreate_at(String create_at) {
        this.create_at = create_at;
    }

    public String getUpdate_at() {
        return update_at;
    }

    public void setUpdate_at(String update_at) {
        this.update_at = update_at;
    }

    public int getLongitude() {
        return longitude;
    }

    public void setLongitude(int longitude) {
        this.longitude = longitude;
    }

    public int getLatitude() {
        return latitude;
    }

    public void setLatitude(int latitude) {
        this.latitude = latitude;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public List<TagBean> getTags() {
        return tags;
    }

    public void setTags(List<TagBean> tags) {
        this.tags = tags;
    }

    public List<Attachments> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<Attachments> attachments) {
        this.attachments = attachments;
    }

    @Override
    public String toString() {
        return "GetNoteByNoteIdBean{" +
                "id=" + id +
                ", folder_id=" + folder_id +
                ", content_digest='" + content_digest + '\'' +
                ", title='" + title + '\'' +
                ", trash='" + trash + '\'' +
                ", address='" + address + '\'' +
                ", content='" + content + '\'' +
                ", create_at='" + create_at + '\'' +
                ", update_at='" + update_at + '\'' +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", radius=" + radius +
                ", tags=" + tags +
                ", attachments=" + attachments +
                '}';
    }
}
