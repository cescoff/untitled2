package fr.untitled2.entities;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import fr.untitled2.utils.SignUtils;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionData;
import org.springframework.social.connect.ConnectionKey;
import org.springframework.social.connect.UserProfile;
import org.springframework.social.facebook.api.Facebook;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 3/5/13
 * Time: 12:06 AM
 * To change this template use File | Settings | File Templates.
 */
@Entity
public class UserConnection {

    @Id
    private String key;

    private String userId;

    private String providerId;

    private String providerUserId;

    private String displayName;

    private String profileUrl;

    private String imageUrl;

    private String accessToken;

    private  String secret;

    private String refreshToken;

    private Long expireTime;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public String getProviderUserId() {
        return providerUserId;
    }

    public void setProviderUserId(String providerUserId) {
        this.providerUserId = providerUserId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Long expireTime) {
        this.expireTime = expireTime;
    }

    public ConnectionData toConnectionData() {
        return new ConnectionData(providerId, providerUserId, displayName, profileUrl, imageUrl, accessToken, secret, refreshToken, expireTime);
    }

    public static UserConnection fromConnection(Connection<?> connection, String userId) {
        ConnectionData connectionData = connection.createData();
        UserConnection userConnection = new UserConnection();
        userConnection.setAccessToken(connectionData.getAccessToken());
        userConnection.setDisplayName(connectionData.getDisplayName());
        userConnection.setExpireTime(connectionData.getExpireTime());
        userConnection.setImageUrl(connectionData.getImageUrl());
        userConnection.setProfileUrl(connectionData.getProfileUrl());
        userConnection.setProviderId(connectionData.getProviderId());
        userConnection.setProviderUserId(connectionData.getProviderUserId());
        userConnection.setRefreshToken(connectionData.getRefreshToken());
        userConnection.setSecret(connectionData.getSecret());
        userConnection.setUserId(userId);
        userConnection.setKey(SignUtils.calculateSha1Digest(userConnection.getUserId() + "-" + userConnection.getProviderId() + "-" + userConnection.getProviderUserId()));
        return userConnection;
    }

}
