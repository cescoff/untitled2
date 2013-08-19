package fr.untitled2.entities;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;

/**
 * Created with IntelliJ IDEA.
 * User: escoffier_c
 * Date: 19/08/13
 * Time: 12:27
 * To change this template use File | Settings | File Templates.
 */
@Entity
public class LogPersistenceJob {

    private String key;

    private Key<User> userKey;

    private String filePath;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Key<User> getUserKey() {
        return userKey;
    }

    public void setUserKey(Key<User> userKey) {
        this.userKey = userKey;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
