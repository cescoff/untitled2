package fr.untitled2.common.entities;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import org.joda.time.LocalDateTime;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 3/20/13
 * Time: 12:05 PM
 * To change this template use File | Settings | File Templates.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class FilmCounter {

    public static Ordering<Pause> PAUSE_ORDERING = Ordering.natural().onResultOf(new Function<Pause, Integer>() {
        @Override
        public Integer apply(Pause pause) {
            return pause.getPosition();
        }
    });

    @XmlTransient
    private long id;

    @XmlElement
    private String filmId;

    @XmlElement(name = "pause") @XmlElementWrapper(name = "pauses")
    private List<Pause> pauses = Lists.newArrayList();

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFilmId() {
        return filmId;
    }

    public void setFilmId(String filmId) {
        this.filmId = filmId;
    }

    public List<Pause> getPauses() {
        return pauses;
    }

    public void setPauses(List<Pause> pauses) {
        this.pauses = pauses;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Pause {

        @XmlElement
        private int position;

        @XmlElement
        private LocalDateTime pauseDateTime;

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

        public LocalDateTime getPauseDateTime() {
            return pauseDateTime;
        }

        public void setPauseDateTime(LocalDateTime pauseDateTime) {
            this.pauseDateTime = pauseDateTime;
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
