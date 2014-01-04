package fr.untitled2.entities;

import com.beust.jcommander.internal.Lists;
import com.google.common.collect.Sets;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Ignore;
import fr.untitled2.common.entities.KnownLocation;
import fr.untitled2.mvc.AppRole;
import fr.untitled2.utils.CollectionUtils;
import fr.untitled2.utils.JSonUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
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

    private static final Logger log = LoggerFactory.getLogger(User.class);

    private static final long serialVersionUID = 2L;

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

    private String knownLocationsJson;

    @Ignore
    private Collection<KnownLocation> knownLocations = Lists.newArrayList();

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

    public String getKnownLocationsJson() {
        return knownLocationsJson;
    }

    public void setKnownLocationsJson(String knownLocationsJson) {
        this.knownLocationsJson = knownLocationsJson;
    }

    public Collection<KnownLocation> getKnownLocations() {
        if (CollectionUtils.isNotEmpty(knownLocations)) {
            log.info("Known locations are not empty");
            return knownLocations;
        }
        if (StringUtils.isEmpty(getKnownLocationsJson())) {
            log.info("Known locations json is empty (" + knownLocationsJson + ")");
            return Lists.newArrayList();
        }
        KnownLocationsHolder knownLocationsHolder = null;
        try {
            knownLocationsHolder = JSonUtils.readJson(KnownLocationsHolder.class, knownLocationsJson);
        } catch (IOException e) {
            log.error("An error has occured while loading json '" + knownLocationsJson + "'", e);
            return Lists.newArrayList();
        }
        return knownLocationsHolder.getKnownLocations();
    }

    public void setKnownLocations(Collection<KnownLocation> knownLocations) {
        if (CollectionUtils.isNotEmpty(knownLocations)) {
            KnownLocationsHolder knownLocationsHolder = new KnownLocationsHolder();
            knownLocationsHolder.getKnownLocations().addAll(knownLocations);
            try {
                knownLocationsJson = JSonUtils.writeJson(knownLocationsHolder);
            } catch (IOException e) {
                log.error("An error has occured while storing json", e);
            }
        } else knownLocationsJson = null;
        this.knownLocations = knownLocations;

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (enabled != user.enabled) return false;
        if (authMode != user.authMode) return false;
        if (dateFormat != null ? !dateFormat.equals(user.dateFormat) : user.dateFormat != null) return false;
        if (email != null ? !email.equals(user.email) : user.email != null) return false;
        if (knownLocationsJson != null ? !knownLocationsJson.equals(user.knownLocationsJson) : user.knownLocationsJson != null)
            return false;
        if (knwonUserIds != null ? !knwonUserIds.equals(user.knwonUserIds) : user.knwonUserIds != null) return false;
        if (locale != null ? !locale.equals(user.locale) : user.locale != null) return false;
        if (nickName != null ? !nickName.equals(user.nickName) : user.nickName != null) return false;
        if (roles != null ? !roles.equals(user.roles) : user.roles != null) return false;
        if (timeZoneId != null ? !timeZoneId.equals(user.timeZoneId) : user.timeZoneId != null) return false;
        if (userId != null ? !userId.equals(user.userId) : user.userId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = userId != null ? userId.hashCode() : 0;
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + (nickName != null ? nickName.hashCode() : 0);
        result = 31 * result + (enabled ? 1 : 0);
        result = 31 * result + (timeZoneId != null ? timeZoneId.hashCode() : 0);
        result = 31 * result + (dateFormat != null ? dateFormat.hashCode() : 0);
        result = 31 * result + (locale != null ? locale.hashCode() : 0);
        result = 31 * result + (roles != null ? roles.hashCode() : 0);
        result = 31 * result + (authMode != null ? authMode.hashCode() : 0);
        result = 31 * result + (knwonUserIds != null ? knwonUserIds.hashCode() : 0);
        result = 31 * result + (knownLocationsJson != null ? knownLocationsJson.hashCode() : 0);
        return result;
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

    @XmlRootElement @XmlAccessorType(XmlAccessType.FIELD)
    public static class KnownLocationsHolder {

        @XmlElement
        private List<KnownLocation> knownLocations = Lists.newArrayList();

        public List<KnownLocation> getKnownLocations() {
            return knownLocations;
        }

        public void setKnownLocations(List<KnownLocation> knownLocations) {
            this.knownLocations = knownLocations;
        }
    }

}
