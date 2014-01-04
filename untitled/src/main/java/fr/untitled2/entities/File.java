package fr.untitled2.entities;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 10/6/13
 * Time: 2:36 PM
 * To change this template use File | Settings | File Templates.
 */
@Entity @Cache
public class File {

    @Id
    private String id;

    private String gsFilePath;

    private int filePartCount = 1;

    @Index
    private Key<User> user;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGsFilePath() {
        return gsFilePath;
    }

    public void setGsFilePath(String gsFilePath) {
        this.gsFilePath = gsFilePath;
    }

    public int getFilePartCount() {
        return filePartCount;
    }

    public void setFilePartCount(int filePartCount) {
        this.filePartCount = filePartCount;
    }

    public User getUser() {
        return ObjectifyService.ofy().load().key(user).get();
    }

    public void setUser(User user) {
        this.user = Key.create(User.class, user.getUserId());
    }

    public boolean isLargeFile() {
        return filePartCount > 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        File file = (File) o;

        if (filePartCount != file.filePartCount) return false;
        if (gsFilePath != null ? !gsFilePath.equals(file.gsFilePath) : file.gsFilePath != null) return false;
        if (id != null ? !id.equals(file.id) : file.id != null) return false;
        if (user != null ? !user.equals(file.user) : file.user != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (gsFilePath != null ? gsFilePath.hashCode() : 0);
        result = 31 * result + filePartCount;
        result = 31 * result + (user != null ? user.hashCode() : 0);
        return result;
    }
}
