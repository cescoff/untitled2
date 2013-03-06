package fr.untitled2.security;

import com.google.appengine.labs.repackaged.com.google.common.base.Function;
import com.google.appengine.labs.repackaged.com.google.common.collect.Lists;
import com.googlecode.objectify.ObjectifyService;
import fr.untitled2.entities.UserConnection;
import fr.untitled2.utils.CollectionUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.social.connect.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 3/5/13
 * Time: 12:37 AM
 * To change this template use File | Settings | File Templates.
 */
public class ObjectifyConnectionRepository implements ConnectionRepository {

    private final String userId;

    private final ConnectionFactoryLocator connectionFactoryLocator;

    private final Function<UserConnection, ConnectionData> USER_CONNECTION_TO_CONNECTION_DATA = new Function<UserConnection, ConnectionData>() {
        @Override
        public ConnectionData apply(UserConnection userConnection) {
            return userConnection.toConnectionData();
        }
    };

    private final Function<ConnectionData, Connection<?>> CONNECTION_DATA_TO_CONNECTION = new Function<ConnectionData, Connection<?>>() {
        @Override
        public Connection<?> apply(ConnectionData connectionData) {
            ConnectionFactory<?> connectionFactory = connectionFactoryLocator.getConnectionFactory(connectionData.getProviderId());
            return connectionFactory.createConnection(connectionData);
        }
    };

    private final Function<UserConnection, Connection<?>> USER_CONNECTION_TO_CONNECTION = new Function<UserConnection, Connection<?>>() {
        @Override
        public Connection<?> apply(UserConnection userConnection) {
            return CONNECTION_DATA_TO_CONNECTION.apply(USER_CONNECTION_TO_CONNECTION_DATA.apply(userConnection));
        }
    };

    public ObjectifyConnectionRepository(String userId, ConnectionFactoryLocator connectionFactoryLocator) {
        this.userId = userId;
        this.connectionFactoryLocator = connectionFactoryLocator;
    }

    @Override
    public MultiValueMap<String, Connection<?>> findAllConnections() {
        List<Connection<?>> resultList = Lists.transform(ObjectifyService.ofy().load().type(UserConnection.class).list(), USER_CONNECTION_TO_CONNECTION);
        MultiValueMap<String, Connection<?>> connections = new LinkedMultiValueMap<String, Connection<?>>();
        Set<String> registeredProviderIds = connectionFactoryLocator.registeredProviderIds();
        for (String registeredProviderId : registeredProviderIds) {
            connections.put(registeredProviderId, Collections.<Connection<?>>emptyList());
        }
        for (Connection<?> connection : resultList) {
            String providerId = connection.getKey().getProviderId();
            if (connections.get(providerId).size() == 0) {
                connections.put(providerId, new LinkedList<Connection<?>>());
            }
            connections.add(providerId, connection);
        }
        return connections;
    }

    @Override
    public List<Connection<?>> findConnections(String providerId) {
        return Lists.transform(ObjectifyService.ofy().load().type(UserConnection.class).filter("userId", userId).filter("providerId", providerId).list(), USER_CONNECTION_TO_CONNECTION);
    }

    @Override
    public <A> List<Connection<A>> findConnections(Class<A> apiType) {
        List<?> connections = findConnections(getProviderId(apiType));
        return (List<Connection<A>>) connections;
    }

    @Override
    public MultiValueMap<String, Connection<?>> findConnectionsToUsers(MultiValueMap<String, String> providerUsers) {
        if (providerUsers == null || providerUsers.isEmpty()) {
            throw new IllegalArgumentException("Unable to execute find: no providerUsers provided");
        }
        MultiValueMap<String, Connection<?>> connectionsForUsers = new LinkedMultiValueMap<String, Connection<?>>();

        List<UserConnection> connectionsForUser = ObjectifyService.ofy().load().type(UserConnection.class).filter("userId", userId).list();

        for (UserConnection userConnection : connectionsForUser) {
            if (providerUsers.containsKey(userConnection.getProviderId())) {
                if (providerUsers.get(userConnection.getProviderId()).contains(userConnection.getProviderUserId())) {
                    if (!connectionsForUsers.containsKey(userConnection.getProviderId())) {
                        List<Connection<?>> connections = Lists.newArrayList();
                        connectionsForUsers.put(userConnection.getProviderId(), connections);
                    }
                    connectionsForUsers.get(userConnection.getProviderId()).add(USER_CONNECTION_TO_CONNECTION.apply(userConnection));
                }
            }
        }

        return connectionsForUsers;
    }

    @Override
    public Connection<?> getConnection(ConnectionKey connectionKey) {
        List<Connection<?>> connections = Lists.transform(
                ObjectifyService.ofy().load().type(UserConnection.class).filter("userId", userId).filter("providerId", connectionKey.getProviderId()).filter("providerUserId", connectionKey.getProviderUserId()).list(),
                USER_CONNECTION_TO_CONNECTION);
        if (CollectionUtils.isNotEmpty(connections)) return connections.get(0);
        return null;
    }

    @Override
    public <A> Connection<A> getConnection(Class<A> apiType, String providerUserId) {
        String providerId = getProviderId(apiType);
        return (Connection<A>) getConnection(new ConnectionKey(providerId, providerUserId));
    }

    @Override
    public <A> Connection<A> getPrimaryConnection(Class<A> apiType) {
        String providerId = getProviderId(apiType);
        Connection<A> connection = (Connection<A>) findPrimaryConnection(providerId);
        if (connection == null) {
            throw new NotConnectedException(providerId);
        }
        return connection;
    }

    @Override
    public <A> Connection<A> findPrimaryConnection(Class<A> apiType) {
        String providerId = getProviderId(apiType);
        return (Connection<A>) findPrimaryConnection(providerId);
    }

    @Override
    public void addConnection(Connection<?> connection) {
        ObjectifyService.ofy().save().entity(UserConnection.fromConnection(connection, userId));
    }

    @Override
    public void updateConnection(Connection<?> connection) {
        ConnectionData connectionData = connection.createData();
        List<UserConnection> connections = ObjectifyService.ofy().load().type(UserConnection.class).filter("userId", userId).filter("providerId", connectionData.getProviderId()).filter("providerUserId", connectionData.getProviderUserId()).list();
        if (CollectionUtils.isNotEmpty(connections)) {
            UserConnection oldConnection = connections.get(0);
            oldConnection.setDisplayName(connectionData.getDisplayName());
            oldConnection.setProfileUrl(connectionData.getProfileUrl());
            oldConnection.setAccessToken(connectionData.getAccessToken());
            oldConnection.setSecret(connectionData.getSecret());
            oldConnection.setRefreshToken(connectionData.getRefreshToken());
            oldConnection.setExpireTime(connectionData.getExpireTime());
            ObjectifyService.ofy().save().entity(oldConnection).now();
        }
    }

    @Override
    public void removeConnections(String providerId) {
        List<UserConnection> connections = ObjectifyService.ofy().load().type(UserConnection.class).filter("userId", userId).filter("providerId", providerId).list();
        for (UserConnection connection : connections) {
            ObjectifyService.ofy().delete().entity(connection);
        }
    }

    @Override
    public void removeConnection(ConnectionKey connectionKey) {
        List<UserConnection> connections = ObjectifyService.ofy().load().type(UserConnection.class).filter("userId", userId).filter("providerId", connectionKey.getProviderId()).filter("providerUserId", connectionKey.getProviderUserId()).list();
        for (UserConnection connection : connections) {
            ObjectifyService.ofy().delete().entity(connection);
        }
    }

    private Connection<?> findPrimaryConnection(String providerId) {
        List<Connection<?>> connections = Lists.transform(ObjectifyService.ofy().load().type(UserConnection.class).filter("userId", userId).filter("providerId", providerId).list(), USER_CONNECTION_TO_CONNECTION);
        if (connections.size() > 0) {
            return connections.get(0);
        } else {
            return null;
        }
    }

    private <A> String getProviderId(Class<A> apiType) {
        return connectionFactoryLocator.getConnectionFactory(apiType).getProviderId();
    }

}
