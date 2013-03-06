package fr.untitled2.security;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.connect.support.ConnectionFactoryRegistry;
import org.springframework.social.facebook.connect.FacebookConnectionFactory;
import org.springframework.social.twitter.connect.TwitterConnectionFactory;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 3/5/13
 * Time: 1:36 AM
 * To change this template use File | Settings | File Templates.
 */
public class ConnectionFactoryLocatorFactory implements FactoryBean<ConnectionFactoryLocator> {

    private String facebookClientId;

    private String facebookClientSecret;

    private String twitterConsumerKey;

    private String twitterConsumerSecret;

    public String getFacebookClientId() {
        return facebookClientId;
    }

    public void setFacebookClientId(String facebookClientId) {
        this.facebookClientId = facebookClientId;
    }

    public String getFacebookClientSecret() {
        return facebookClientSecret;
    }

    public void setFacebookClientSecret(String facebookClientSecret) {
        this.facebookClientSecret = facebookClientSecret;
    }

    public String getTwitterConsumerKey() {
        return twitterConsumerKey;
    }

    public void setTwitterConsumerKey(String twitterConsumerKey) {
        this.twitterConsumerKey = twitterConsumerKey;
    }

    public String getTwitterConsumerSecret() {
        return twitterConsumerSecret;
    }

    public void setTwitterConsumerSecret(String twitterConsumerSecret) {
        this.twitterConsumerSecret = twitterConsumerSecret;
    }

    @Override
    public ConnectionFactoryLocator getObject() throws Exception {
        ConnectionFactoryRegistry registry = new ConnectionFactoryRegistry();

        if (StringUtils.isNotEmpty(facebookClientId) && StringUtils.isNotEmpty(facebookClientSecret)) {
            registry.addConnectionFactory(new FacebookConnectionFactory(
                    facebookClientId,
                    facebookClientSecret));
        }

        if (StringUtils.isNotEmpty(twitterConsumerKey) && StringUtils.isNotEmpty(twitterConsumerSecret)) {
            registry.addConnectionFactory(new TwitterConnectionFactory(
                    twitterConsumerKey,
                    twitterConsumerSecret));
        }

        return registry;
    }

    @Override
    public Class<?> getObjectType() {
        return ConnectionFactoryLocator.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
