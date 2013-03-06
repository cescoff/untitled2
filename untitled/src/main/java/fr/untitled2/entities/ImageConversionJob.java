package fr.untitled2.entities;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 2/6/13
 * Time: 11:15 PM
 * To change this template use File | Settings | File Templates.
 */
@Entity
public class ImageConversionJob {

    @Id
    private String hashCode;

    @Index
    private Key<Image> image;

    private boolean onError;

    public String getHashCode() {
        return hashCode;
    }

    public void setHashCode(String hashCode) {
        this.hashCode = hashCode;
    }

    public Image getImage() {
        return ObjectifyService.ofy().load().key(image).get();
    }

    public void setImage(Image image) {
        this.image = Key.create(Image.class, image.getImageKey());
    }

    public boolean isOnError() {
        return onError;
    }

    public void setOnError(boolean onError) {
        this.onError = onError;
    }
}
