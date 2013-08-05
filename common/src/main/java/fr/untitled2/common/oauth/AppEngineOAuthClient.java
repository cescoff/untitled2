package fr.untitled2.common.oauth;

import fr.untitled2.common.entities.FilmCounter;
import fr.untitled2.common.entities.LogRecording;
import fr.untitled2.common.entities.UserInfos;
import fr.untitled2.common.entities.UserPreferences;
import fr.untitled2.common.json.JSonMappings;
import fr.untitled2.utils.CollectionUtils;
import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.joda.time.LocalDateTime;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 3/15/13
 * Time: 8:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class AppEngineOAuthClient {

    private static final String appId = "x5-teak-clarity-4";

    public static final String appHost = appId + ".appspot.com";

    private static final String appUrl = "https://" + appHost;

    private static final String ouath_get_request_token_url = appUrl + "/_ah/OAuthGetRequestToken";

    private static final String ouath_authorize_token_url = appUrl + "/_ah/OAuthAuthorizeToken";

    private static final String oauth_get_access_token_url = appUrl + "/_ah/OAuthGetAccessToken";

    private static final String consumer_key = "1068597606057.apps.googleusercontent.com";

    private static final String consumer_secret = "zFwtAxv4h1ohMIlFaNl93FmS";

    private static final HttpHost commonsAppHost = new HttpHost(appHost, 443, "https");

    private String accessToken;

    private String tokenSecret;

    private OAuthConsumer consumer;

    private OAuthProvider provider;

    public AppEngineOAuthClient() {
        consumer = new CommonsHttpOAuthConsumer(consumer_key, consumer_secret);
    }

    public AppEngineOAuthClient(String accessToken, String tokenSecret) {
        this.accessToken = accessToken;
        this.tokenSecret = tokenSecret;
        this.consumer = new CommonsHttpOAuthConsumer(consumer_key, consumer_secret);
        this.consumer.setTokenWithSecret(accessToken, tokenSecret);
    }

    public boolean isConnected() {
        return StringUtils.isNotEmpty(accessToken) && StringUtils.isNotEmpty(tokenSecret);
    }

    public String getTokenValidationUrl() throws OAuthCommunicationException, OAuthExpectationFailedException, OAuthNotAuthorizedException, OAuthMessageSignerException {
        if (isConnected()) return null;
        provider = new CommonsHttpOAuthProvider(ouath_get_request_token_url, oauth_get_access_token_url, ouath_authorize_token_url);
        ((CommonsHttpOAuthProvider) provider).setHttpClient(getClient());
        String url = provider.retrieveRequestToken(consumer, OAuth.OUT_OF_BAND);
        this.accessToken = consumer.getToken();
        this.tokenSecret = consumer.getTokenSecret();
        return url;
    }

    public void validateTokens(String verificationCode) throws OAuthCommunicationException, OAuthExpectationFailedException, OAuthNotAuthorizedException, OAuthMessageSignerException {
        provider.retrieveAccessToken(consumer, verificationCode);
        this.accessToken = consumer.getToken();
        this.tokenSecret = consumer.getTokenSecret();
    }

    public UserInfos getUserInfos(UserPreferences userPreferences) throws IOException, OAuthCommunicationException, OAuthExpectationFailedException, OAuthMessageSignerException {
        HttpPost httpPost = new HttpPost(appUrl + "/api/userInfos");

        consumer.sign(httpPost);
        httpPost.setEntity(new ByteArrayEntity(JSonMappings.getJSON(userPreferences).getBytes()));
        HttpClient httpClient = getClient();
        HttpResponse httpResponse = httpClient.execute(commonsAppHost, httpPost);
        if (httpResponse.getStatusLine().getStatusCode() == 200) {
            String json = IOUtils.toString(httpResponse.getEntity().getContent());
            return JSonMappings.getUserInfos(json);
        }

        return null;
    }

    public Collection<LogRecording> getMatchingLogRecordings(LocalDateTime start, LocalDateTime end) throws OAuthCommunicationException, OAuthExpectationFailedException, OAuthMessageSignerException, IOException {
        HttpGet httpGet = new HttpGet(appUrl + "/api/getLog?startDate=" + start.toString() + "&endDate=" + end.toString());


        consumer.sign(httpGet);
        HttpClient httpClient = getClient();
        HttpResponse httpResponse = httpClient.execute(commonsAppHost, httpGet);
        if (httpResponse.getStatusLine().getStatusCode() == 200) {
            String json = IOUtils.toString(httpResponse.getEntity().getContent());
            return JSonMappings.getLogRecordings(json);
        }

        return Collections.EMPTY_LIST;
    }

    public UserPreferences getUserPreferences() throws OAuthCommunicationException, OAuthExpectationFailedException, OAuthMessageSignerException, IOException {
        HttpGet httpGet = new HttpGet(appUrl + "/api/getUserPreferences");

        consumer.sign(httpGet);
        HttpClient httpClient = getClient();
        HttpResponse httpResponse = httpClient.execute(commonsAppHost, httpGet);
        if (httpResponse.getStatusLine().getStatusCode() == 200) {
            String json = IOUtils.toString(httpResponse.getEntity().getContent());
            return JSonMappings.getUserPreferences(json);
        }

        return null;
    }

    public void pushLogRecording(LogRecording logRecording) throws IOException, OAuthCommunicationException, OAuthExpectationFailedException, OAuthMessageSignerException {
        if (CollectionUtils.isEmpty(logRecording.getRecords())) throw new IOException("No records contained in log recording");
        HttpPost httpPost = new HttpPost(appUrl + "/api/logUpload");

        consumer.sign(httpPost);
        httpPost.setEntity(new ByteArrayEntity(JSonMappings.getJSON(logRecording).getBytes()));
        HttpClient httpClient = getClient();
        HttpResponse httpResponse = httpClient.execute(commonsAppHost, httpPost);
        if (httpResponse.getStatusLine().getStatusCode() == 200) {
            if (!"OK".equals(IOUtils.toString(httpResponse.getEntity().getContent()))) throw new IOException("LogRecording has not been saved");
        } else throw new IOException("LogRecording has not been saved");
    }

    public void uploadImage(File file) throws IOException, OAuthCommunicationException, OAuthExpectationFailedException, OAuthMessageSignerException {
        HttpPost httpPost = new HttpPost(appUrl + "/api/imageUpload?fileName=" + file.getName());

        consumer.sign(httpPost);
        httpPost.setEntity(new ByteArrayEntity(IOUtils.toByteArray(new FileInputStream(file))));
        HttpClient httpClient = getClient();
        HttpResponse httpResponse = httpClient.execute(commonsAppHost, httpPost);
        if (httpResponse.getStatusLine().getStatusCode() == 200) {
            if (!"OK".equals(IOUtils.toString(httpResponse.getEntity().getContent()))) throw new IOException("Image has not been saved");
        } else throw new IOException("Image has not been saved");
    }

    public void pushFilmCounter(FilmCounter filmCounter) throws IOException, OAuthCommunicationException, OAuthExpectationFailedException, OAuthMessageSignerException {
        if (CollectionUtils.isEmpty(filmCounter.getPauses())) throw new IOException("No records contained in log recording");
        HttpPost httpPost = new HttpPost(appUrl + "/api/filmCounterUpload");

        consumer.sign(httpPost);
        httpPost.setEntity(new ByteArrayEntity(JSonMappings.getJSON(filmCounter).getBytes()));
        HttpClient httpClient = getClient();
        HttpResponse httpResponse = httpClient.execute(commonsAppHost, httpPost);
        if (httpResponse.getStatusLine().getStatusCode() == 200) {
            if (!"OK".equals(IOUtils.toString(httpResponse.getEntity().getContent()))) throw new IOException("FilmCounter has not been saved");
        } else throw new IOException("FilmCounter has not been saved");
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenSecret() {
        return tokenSecret;
    }

    public void setTokenSecret(String tokenSecret) {
        this.tokenSecret = tokenSecret;
    }

    private HttpClient getClient() {
        String proxyHost = System.getProperty("http.proxyHost");
        String proxyPort = System.getProperty("http.proxyPort");

        String proxyUser = System.getProperty("http.proxyUser");
        String proxyUserPassword = System.getProperty("http.proxyPassword");

        HttpClient httpClient = new DefaultHttpClient();
        if (StringUtils.isNotEmpty(proxyHost)) {
            if (StringUtils.isEmpty(proxyPort)) proxyPort = "80";
            HttpHost proxy = new HttpHost(proxyHost, Integer.parseInt(proxyPort));

            httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
        }

        if (StringUtils.isNotEmpty(proxyUser) && StringUtils.isNotEmpty(proxyUserPassword)) {
            DefaultHttpClient defaultHttpClient = (DefaultHttpClient) httpClient;
            CredentialsProvider credsProvider = defaultHttpClient.getCredentialsProvider();
            credsProvider.setCredentials(new AuthScope(proxyHost, Integer.parseInt(proxyPort)), new UsernamePasswordCredentials(proxyUser, proxyUserPassword));
            defaultHttpClient.setCredentialsProvider(credsProvider);
        }

        return httpClient;
    }

}
