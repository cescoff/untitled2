package fr.untitled2.entities;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.files.*;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.annotation.*;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.io.Serializable;
import java.nio.channels.Channels;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 2/6/13
 * Time: 1:39 PM
 * To change this template use File | Settings | File Templates.
 */
@Entity @Cache
public class Image implements Serializable {

    private static Logger logger = LoggerFactory.getLogger(Image.class);

    @Id
    private String imageKey;

    private ImageStorageType imageStorageType;

    private String name;

    @Index @Translate(LocalDateTimeTranslatorFactory.class)
    private LocalDateTime dateTaken = LocalDateTime.now();

    @Translate(LocalDateTimeTranslatorFactory.class)
    private LocalDateTime importDate = LocalDateTime.now().toDateTime(DateTimeZone.UTC).toLocalDateTime();

    private BlobKey highResolutionPreview;

    private BlobKey mediumResolutionPreview;

    private BlobKey lowResolutionPreview;

    private BlobKey squareLowResolutionPreview;

    private Double latitude;

    private Double longitude;

    private boolean ready;

    private boolean error;

    @Index
    private Key<User> user;

    @Ignore
    private User realUser;

    private int width;

    private int height;

    private String timeZoneId;

    public String getImageKey() {
        return imageKey;
    }

    public void setImageKey(String imageKey) {
        this.imageKey = imageKey;
    }

    public ImageStorageType getImageStorageType() {
        return imageStorageType;
    }

    public void setImageStorageType(ImageStorageType imageStorageType) {
        this.imageStorageType = imageStorageType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getDateTaken() {
        return dateTaken;
    }

    public void setDateTaken(LocalDateTime dateTaken) {
        this.dateTaken = dateTaken;
    }

    public LocalDateTime getImportDate() {
        return importDate;
    }

    public void setImportDate(LocalDateTime importDate) {
        this.importDate = importDate;
    }

    public BlobKey getHighResolutionPreview() throws Exception {
        return this.highResolutionPreview;
    }

    public void setHighResolutionPreview(byte[] highResolutionPreview) throws Exception {
        FileService fileService = FileServiceFactory.getFileService();
        AppEngineFile appEngineFile = fileService.createNewBlobFile("image/jpg", "high_res_" + getName());
        FileWriteChannel writeChannel = fileService.openWriteChannel(appEngineFile, true);
        OutputStream outputStream = Channels.newOutputStream(writeChannel);
        outputStream.write(highResolutionPreview);
        outputStream.close();
        writeChannel.closeFinally();
        this.highResolutionPreview = fileService.getBlobKey(appEngineFile);
    }

    public BlobKey getLowResolutionPreview() {
        return lowResolutionPreview;
    }

    public void setLowResolutionPreview(byte[] lowResolutionPreview) throws Exception {
        FileService fileService = FileServiceFactory.getFileService();
        AppEngineFile appEngineFile = fileService.createNewBlobFile("image/jpg", "low_res_" + getName());
        FileWriteChannel writeChannel = fileService.openWriteChannel(appEngineFile, true);
        OutputStream outputStream = Channels.newOutputStream(writeChannel);
        outputStream.write(lowResolutionPreview);
        outputStream.close();
        writeChannel.closeFinally();
        this.lowResolutionPreview = fileService.getBlobKey(appEngineFile);
    }

    public BlobKey getSquareLowResolutionPreview() {
        return squareLowResolutionPreview;
    }

    public void setSquareLowResolutionPreview(byte[] squareLowResolutionPreview) throws Exception {
        FileService fileService = FileServiceFactory.getFileService();
        AppEngineFile appEngineFile = fileService.createNewBlobFile("image/jpg", "square_low_res_" + getName());
        FileWriteChannel writeChannel = fileService.openWriteChannel(appEngineFile, true);
        OutputStream outputStream = Channels.newOutputStream(writeChannel);
        outputStream.write(squareLowResolutionPreview);
        outputStream.close();
        writeChannel.closeFinally();
        this.squareLowResolutionPreview = fileService.getBlobKey(appEngineFile);
    }

    public BlobKey getMediumResolutionPreview() {
        return mediumResolutionPreview;
    }

    public void setMediumResolutionPreview(byte[] mediumResolutionPreview) throws Exception {
        FileService fileService = FileServiceFactory.getFileService();
        AppEngineFile appEngineFile = fileService.createNewBlobFile("image/jpg", "medium_res_" + getName());
        FileWriteChannel writeChannel = fileService.openWriteChannel(appEngineFile, true);
        OutputStream outputStream = Channels.newOutputStream(writeChannel);
        outputStream.write(mediumResolutionPreview);
        outputStream.close();
        writeChannel.closeFinally();
        this.mediumResolutionPreview = fileService.getBlobKey(appEngineFile);
    }

    public User getUser() {
        User result = ObjectifyService.ofy().load().key(user).get();
        if (result == null) {
            logger.info("Aucun utilisateur trouve pour '" + user + "'");
            result = ObjectifyService.ofy().load().key(Key.create(User.class, user.getString())).get();
        }
        return result;
    }

    public void setUser(User user) {
        this.realUser = user;
        this.user = Key.create(User.class, user.getUserId());
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public User getRealUser() {
        return realUser;
    }

    public void setRealUser(User realUser) {
        this.realUser = realUser;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public String getTimeZoneId() {
        return timeZoneId;
    }

    public void setTimeZoneId(String timeZoneId) {
        this.timeZoneId = timeZoneId;
    }

    public Image clone() {
        Image result = new Image();
        result.name = this.name;
        result.ready = this.ready;
        result.dateTaken = this.dateTaken;
        result.error = this.error;
        result.width = this.width;
        result.height = this.height;
        result.highResolutionPreview = this.highResolutionPreview;
        result.imageKey = this.imageKey;
        result.latitude = this.latitude;
        result.longitude = this.longitude;
        result.lowResolutionPreview = this.lowResolutionPreview;
        result.mediumResolutionPreview = this.mediumResolutionPreview;
        result.realUser = this.realUser;
        result.squareLowResolutionPreview = this.squareLowResolutionPreview;
        result.timeZoneId = this.timeZoneId;
        result.user = this.user;
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Image image = (Image) o;

        if (error != image.error) return false;
        if (height != image.height) return false;
        if (ready != image.ready) return false;
        if (width != image.width) return false;
        if (dateTaken != null ? !dateTaken.equals(image.dateTaken) : image.dateTaken != null) return false;
        if (imageKey != null ? !imageKey.equals(image.imageKey) : image.imageKey != null) return false;
        if (highResolutionPreview != null ? !highResolutionPreview.equals(image.highResolutionPreview) : image.highResolutionPreview != null)
            return false;
        if (importDate != null ? !importDate.equals(image.importDate) : image.importDate != null) return false;
        if (latitude != null ? !latitude.equals(image.latitude) : image.latitude != null) return false;
        if (longitude != null ? !longitude.equals(image.longitude) : image.longitude != null) return false;
        if (lowResolutionPreview != null ? !lowResolutionPreview.equals(image.lowResolutionPreview) : image.lowResolutionPreview != null)
            return false;
        if (mediumResolutionPreview != null ? !mediumResolutionPreview.equals(image.mediumResolutionPreview) : image.mediumResolutionPreview != null)
            return false;
        if (name != null ? !name.equals(image.name) : image.name != null) return false;
        if (realUser != null ? !realUser.equals(image.realUser) : image.realUser != null) return false;
        if (squareLowResolutionPreview != null ? !squareLowResolutionPreview.equals(image.squareLowResolutionPreview) : image.squareLowResolutionPreview != null)
            return false;
        if (timeZoneId != null ? !timeZoneId.equals(image.timeZoneId) : image.timeZoneId != null) return false;
        if (user != null ? !user.equals(image.user) : image.user != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = imageKey != null ? imageKey.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (dateTaken != null ? dateTaken.hashCode() : 0);
        result = 31 * result + (importDate != null ? importDate.hashCode() : 0);
        result = 31 * result + (highResolutionPreview != null ? highResolutionPreview.hashCode() : 0);
        result = 31 * result + (mediumResolutionPreview != null ? mediumResolutionPreview.hashCode() : 0);
        result = 31 * result + (lowResolutionPreview != null ? lowResolutionPreview.hashCode() : 0);
        result = 31 * result + (squareLowResolutionPreview != null ? squareLowResolutionPreview.hashCode() : 0);
        result = 31 * result + (latitude != null ? latitude.hashCode() : 0);
        result = 31 * result + (longitude != null ? longitude.hashCode() : 0);
        result = 31 * result + (ready ? 1 : 0);
        result = 31 * result + (error ? 1 : 0);
        result = 31 * result + (user != null ? user.hashCode() : 0);
        result = 31 * result + (realUser != null ? realUser.hashCode() : 0);
        result = 31 * result + width;
        result = 31 * result + height;
        result = 31 * result + (timeZoneId != null ? timeZoneId.hashCode() : 0);
        return result;
    }
}
