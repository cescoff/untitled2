package fr.untitled2.common.utils;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
/*
import fr.untitled2.common.oauth.AppEngineOAuthClient;
import fr.untitled2.common.utils.bindings.ElevationResponse;
import fr.untitled2.utils.JAXBUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
*/
import fr.untitled2.common.oauth.AppEngineOAuthClient;
import fr.untitled2.common.utils.bindings.ElevationResponse;
import fr.untitled2.common.utils.bindings.GeocodeResponse;
import fr.untitled2.utils.JAXBUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.javatuples.Pair;
import org.javatuples.Quartet;
import org.javatuples.Triplet;
import org.joda.time.LocalDateTime;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URLEncoder;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 14/12/13
 * Time: 09:17
 * To change this template use File | Settings | File Templates.
 */
public class GoogleMapsUtils {

    public static List<Quartet<LocalDateTime, Double, Double, Double>> getAltitudes(List<Triplet<LocalDateTime, Double, Double>> locations) {
        try {
            List<Quartet<LocalDateTime, Double, Double, Double>> result = Lists.newArrayList();
            int counter = 1;
            for (List<Triplet<LocalDateTime, Double, Double>> locationsPage : Lists.partition(locations, 50)) {
                try {
                    result.addAll(getAltitudeForPage(locationsPage));
                    if (counter % 5 == 0) {
                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                        }
                    }
                    counter++;
                } catch (Throwable e) {
                    throw new IllegalStateException("Network error occured while loading elevations", e);
                }
            }
            return result;
        } catch (Throwable t) {
            throw new IllegalStateException("Error occured while gettings elevations", t);
        }
/*
        return Lists.newArrayList(Iterables.transform(locations, new Function<Triplet<LocalDateTime, Double, Double>, Quartet<LocalDateTime, Double, Double, Double>>() {
            @Override
            public Quartet<LocalDateTime, Double, Double, Double> apply(Triplet<LocalDateTime, Double, Double> objects) {
                return Quartet.with(objects.getValue0(), objects.getValue1(), objects.getValue2(), 0.0);
            }
        }));
*/
    }

    private static List<Quartet<LocalDateTime, Double, Double, Double>> getAltitudeForPage(List<Triplet<LocalDateTime, Double, Double>> page) throws IOException {
        HttpHost commonsAppHost = new HttpHost("maps.googleapis.com", 80, "http");
        StringBuilder locationsParameter = new StringBuilder();

        for (int index = 0; index < page.size(); index++) {
            locationsParameter.append(page.get(index).getValue1()).append(",").append(page.get(index).getValue2());
            if (index < page.size() - 1) locationsParameter.append("%7C");
        }

        HttpPost httpGet = new HttpPost("http://maps.googleapis.com/maps/api/elevation/json?locations=" + locationsParameter.toString() + "&sensor=true");
        HttpClient httpClient = AppEngineOAuthClient.getClient();
        HttpResponse httpResponse = httpClient.execute(commonsAppHost, httpGet);
        int status = httpResponse.getStatusLine().getStatusCode();
        if (status != 200) {
            throw new IllegalStateException("Elevation query return HTTP status " + status);
        }

        String json = IOUtils.toString(httpResponse.getEntity().getContent());
        ElevationResponse elevationResponse;

        try {
            elevationResponse = unmarshallJSON(json);
        } catch (Throwable t) {
            throw new IllegalStateException("Elevation api returned bad xml", t);
        }
        if (elevationResponse.getStatus() != ElevationResponse.Status.OK) {
            throw new IllegalStateException("Elevation api returned status '" + elevationResponse.getStatus() + "'");
        }
        if (elevationResponse.getResult().size() != page.size()) {
            throw new IllegalStateException("Page result size is " + elevationResponse.getResult().size() + " when input size is " + page.size());
        }

        List<Quartet<LocalDateTime, Double, Double, Double>> result = Lists.newArrayList();
        for (int index = 0; index < page.size(); index++) {
            result.add(Quartet.with(page.get(index).getValue0(), page.get(index).getValue1(), page.get(index).getValue2(), elevationResponse.getResult().get(index).getElevation()));
        }
        return result;
/*
        return Lists.newArrayList(Iterables.transform(page, new Function<Triplet<LocalDateTime, Double, Double>, Quartet<LocalDateTime, Double, Double, Double>>() {
            @Override
            public Quartet<LocalDateTime, Double, Double, Double> apply(Triplet<LocalDateTime, Double, Double> objects) {
                return Quartet.with(objects.getValue0(), objects.getValue1(), objects.getValue2(), 0.0);
            }
        }));
*/
    }


    private static ElevationResponse unmarshallJSON(String json) throws IOException, ParseException {
        ElevationResponse elevationResponse = new ElevationResponse();
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject) jsonParser.parse(new StringReader(json));
        ElevationResponse.Status status = ElevationResponse.Status.valueOf((String) jsonObject.get("status"));
        elevationResponse.setStatus(status);
        JSONArray jsonArray = (JSONArray) jsonObject.get("results");

        for (Object object : jsonArray) {
            JSONObject resultObject = (JSONObject) object;
            ElevationResponse.Result result = new ElevationResponse.Result();
            JSONObject locationObject = (JSONObject) resultObject.get("location");
            ElevationResponse.Result.Location location = new ElevationResponse.Result.Location();
            location.setLatitude((Double) locationObject.get("lat"));
            location.setLongitude((Double) locationObject.get("lng"));
            result.setLocation(location);
            result.setElevation((Double) resultObject.get("elevation"));
            result.setResolution((Double) resultObject.get("resolution"));
            elevationResponse.getResult().add(result);
        }
        return elevationResponse;
    }

    public static Pair<Double, Double> getGeocodes(String address) {
        try {
            HttpHost httpHost = new HttpHost("maps.google.com", 80);

            HttpGet get = new HttpGet("/maps/api/geocode/xml?address=" + URLEncoder.encode(address, "UTF-8") + "&sensor=false");

            HttpClient httpClient = new DefaultHttpClient();

            HttpResponse response = httpClient.execute(httpHost, get);
            StatusLine statusLine = response.getStatusLine();

            int statusCode = statusLine.getStatusCode();
            if (statusCode != 200) {
                throw new IllegalStateException("Google Goecoding return HTTP status code '" + statusCode);
            }
            String xml = IOUtils.toString(response.getEntity().getContent());
            GeocodeResponse geocodeResponse = JAXBUtils.unmarshal(GeocodeResponse.class, xml);
            if (!"ok".equalsIgnoreCase(geocodeResponse.getStatus())) {
                throw  new IllegalStateException("Google Geocoding returned status '" + geocodeResponse.getStatus() + "'");
            }
            return Pair.with(geocodeResponse.getResult().getGeometry().getLocation().getLatitude(), geocodeResponse.getResult().getGeometry().getLocation().getLongitude());
        } catch (Throwable t) {
            throw new IllegalStateException("Erro occured while getting latitude and longitude of an address", t);
        }
    }

}
