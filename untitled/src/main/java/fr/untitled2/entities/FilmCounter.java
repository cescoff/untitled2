package fr.untitled2.entities;

import com.google.common.collect.Lists;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.*;
import fr.untitled2.common.entities.jaxb.LocalDateTimeAdapter;
import fr.untitled2.utils.JSonUtils;
import fr.untitled2.utils.SignUtils;
import org.joda.time.LocalDateTime;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 3/29/13
 * Time: 5:56 PM
 * To change this template use File | Settings | File Templates.
 */
@Entity
public class FilmCounter {

    @Id
    private String internalId;

    @Index
    private String name;

    @Index
    private Key<User> user;

    @Ignore
    private Collection<Pause> pauses = Lists.newArrayList();

    private String jsonPauses;

    @Ignore
    private User realUser;

    public String getInternalId() {
        return internalId;
    }

    public void setInternalId(String internalId) {
        this.internalId = internalId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Key<User> getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = Key.create(User.class, user.getUserId());
        this.realUser = user;
    }

    public Collection<Pause> getPauses() {
        return pauses;
    }

    public void setPauses(Collection<Pause> pauses) {
        this.pauses = pauses;
    }

    public String getJsonPauses() {
        return jsonPauses;
    }

    public void setJsonPauses(String jsonPauses) {
        this.jsonPauses = jsonPauses;
    }

    public User getRealUser() {
        return realUser;
    }

    @OnSave
    public void prepersist() {
        PauseHolder pauseHolder = new PauseHolder();
        pauseHolder.setPauses(this.pauses);
        try {
            this.jsonPauses = JSonUtils.writeJson(pauseHolder);
        } catch (Throwable t) {
            throw new IllegalStateException("Enable to generate json for trackpoints", t);
        }
        this.internalId = SignUtils.calculateSha1Digest(realUser.getUserId() + name);
    }

    @OnLoad
    public void postload() {
        try {
            PauseHolder pauseHolder = JSonUtils.readJson(PauseHolder.class, jsonPauses);
            this.pauses = pauseHolder.pauses;
        } catch (Throwable t) {
            throw new IllegalStateException("Enable to read json trackpoints", t);
        }
    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class PauseHolder {

        @XmlElement(name = "pause") @XmlElementWrapper(name = "pauses")
        private Collection<Pause> pauses = Lists.newArrayList();

        public Collection<Pause> getPauses() {
            return pauses;
        }

        public void setPauses(Collection<Pause> pauses) {
            this.pauses = pauses;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Pause {

        @XmlElement
        private int position;

        @XmlElement @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
        private LocalDateTime localDateTime;

        @XmlElement
        private double latitude;

        @XmlElement
        private double longitude;

        public int getPosition() {
            return position;
        }

        public void setPosition(int position) {
            this.position = position;
        }

        public LocalDateTime getLocalDateTime() {
            return localDateTime;
        }

        public void setLocalDateTime(LocalDateTime localDateTime) {
            this.localDateTime = localDateTime;
        }

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }
    }

}
