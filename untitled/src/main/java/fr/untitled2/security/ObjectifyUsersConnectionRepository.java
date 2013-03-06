package fr.untitled2.security;

import com.google.appengine.labs.repackaged.com.google.common.base.Function;
import com.google.appengine.labs.repackaged.com.google.common.base.Predicate;
import com.google.appengine.labs.repackaged.com.google.common.collect.Collections2;
import com.google.appengine.labs.repackaged.com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;
import fr.untitled2.entities.UserConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.social.connect.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 3/5/13
 * Time: 12:16 AM
 * To change this template use File | Settings | File Templates.
 */
public class ObjectifyUsersConnectionRepository implements UsersConnectionRepository {

    private static Logger logger = LoggerFactory.getLogger(ObjectifyUsersConnectionRepository.class);

    private final ConnectionFactoryLocator connectionFactoryLocator;

    private ConnectionSignUp connectionSignUp;

    public ObjectifyUsersConnectionRepository(ConnectionFactoryLocator connectionFactoryLocator) {
        this.connectionFactoryLocator = connectionFactoryLocator;
    }

    public void setConnectionSignUp(ConnectionSignUp connectionSignUp) {
        this.connectionSignUp = connectionSignUp;
    }

    @Override
    public List<String> findUserIdsWithConnection(Connection<?> connection) {
        logger.info("findUserIdsWithConnection");
        ConnectionKey key = connection.getKey();
        List<String> localUserIds = Lists.transform(ObjectifyService.ofy().load().type(UserConnection.class).filter("providerId", key.getProviderId()).filter("providerUserId", key.getProviderUserId()).list(), new Function<UserConnection, String>() {
            @Override
            public String apply(UserConnection userConnection) {
                return userConnection.getUserId();
            }
        });
        if (localUserIds.size() == 0 && connectionSignUp != null) {
            String newUserId = connectionSignUp.execute(connection);
            if (newUserId != null)
            {
                createConnectionRepository(newUserId).addConnection(connection);
                return Arrays.asList(newUserId);
            }
        }
        return localUserIds;
    }

    @Override
    public Set<String> findUserIdsConnectedTo(String providerId, final Set<String> providerUserIds) {
        logger.info("findUserIdsConnectedTo");
        return Sets.newHashSet(Collections2.transform(Collections2.filter(ObjectifyService.ofy().load().type(UserConnection.class).filter("providerId", providerId).list(), new Predicate<UserConnection>() {
            @Override
            public boolean apply(UserConnection userConnection) {
                return providerUserIds.contains(userConnection.getProviderUserId());
            }
        }), new Function<UserConnection, String>() {
            @Override
            public String apply(UserConnection userConnection) {
                return userConnection.getUserId();
            }
        }));
    }

    @Override
    public ConnectionRepository createConnectionRepository(String userId) {
        logger.info("createConnectionRepository");
        if (userId == null) {
            throw new IllegalArgumentException("userId cannot be null");
        }
        return new ObjectifyConnectionRepository(userId, connectionFactoryLocator);
    }
}
