package fr.untitled2.entities;

import com.google.common.collect.Sets;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import fr.untitled2.mvc.AppRole;

import java.io.Serializable;
import java.util.Collection;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 1/29/13
 * Time: 1:05 AM
 * To change this template use File | Settings | File Templates.
 */
@Entity @Cache
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String userId;

    private String email;

    private String nickName;

    private boolean enabled = true;

    private String timeZoneId = "Europe/Paris";

    private String dateFormat = "dd/MM/yyyy";

    private String locale = Locale.FRENCH.toString();

    private Collection<AppRole> roles = Sets.newHashSet();

    private AuthMode authMode;

    private Collection<String> knwonUserIds = Sets.newHashSet();

    public User() {
    }

    public User(String email) {
        this.email = email;
    }

    public User(String email, String nickName) {
        this.email = email;
        this.nickName = nickName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getTimeZoneId() {
        return timeZoneId;
    }

    public void setTimeZoneId(String timeZoneId) {
        this.timeZoneId = timeZoneId;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public Collection<AppRole> getRoles() {
        return roles;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public AuthMode getAuthMode() {
        return authMode;
    }

    public void setAuthMode(AuthMode authMode) {
        this.authMode = authMode;
    }

    public Collection<String> getKnwonUserIds() {
        return knwonUserIds;
    }

    public void setKnwonUserIds(Collection<String> knwonUserIds) {
        this.knwonUserIds = knwonUserIds;
    }

    @Override
    public String toString() {
        return "User{" +
                "email='" + email + '\'' +
                '}';
    }

    public enum AuthMode {
        SOCIAL,
        GOOGLE
    }

}
