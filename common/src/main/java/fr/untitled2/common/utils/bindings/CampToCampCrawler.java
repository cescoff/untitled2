package fr.untitled2.common.utils.bindings;

import java.io.*;
import java.net.URLEncoder;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.untitled2.utils.JAXBUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.javatuples.Pair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.common.collect.Lists;

import javax.xml.bind.JAXBException;

public class CampToCampCrawler {

	private static Pattern pattern = Pattern.compile("/huts\\/([0-9]*)\\/");
	
	public static void main(String[] args) throws IOException, ParseException, JAXBException {
		List<String> urls = Lists.newArrayList();
		for (int index = 1; index <= 61; index++) {
			urls.add("/huts/list/" + index);
		}
        PointOfInterests pointOfInterests = new PointOfInterests();
		for (String html : getHtmlPages(urls)) {
            System.out.println("<--------- NEW PAGE --------->");
			LineIterator lineIterator = new LineIterator(new StringReader(html));
			while (lineIterator.hasNext()) {
				String line = lineIterator.next();
				Matcher lineMatcher = pattern.matcher(line);
				while (lineMatcher.find()) {
					String id = lineMatcher.group(1);
					System.out.println("ID:" + id);
					PointOfInterest pointOfInterest = getPointOfInterest(getJsonElements(id));
                    if (pointOfInterest != null)  {
                        if (!pointOfInterest.getName().startsWith("C2C")) {
                            pointOfInterests.getPointOfInterests().add(pointOfInterest);
                        }
                    }
				}
			}
		}
        File outputFile = new File("/Users/corentinescoffier/Developpement/POI/huts.xml");
        JAXBUtils.marshal(pointOfInterests, outputFile, true);
        System.out.println(JAXBUtils.unmarshal(PointOfInterests.class, outputFile).getPointOfInterests().size());
	}

	private static List<Pair<Langue,String>> getJsonElements(String id) throws IOException {
		List<Pair<Langue, String>> urls = Lists.newArrayList();
		for (Langue language : Langue.values()) {
			urls.add(Pair.with(language, "/huts/" + language + "/" + id + ".json"));
		}
		List<Pair<Langue,String>> result = Lists.newArrayList();
		for (Pair<Langue, String> url : urls) {
			result.add(Pair.with(url.getValue0(), getJson(url.getValue1())));
		}
		return result;
	}
	
	private static PointOfInterest getPointOfInterest(List<Pair<Langue, String>> jsons) throws ParseException {
		PointOfInterest pointOfInterest = new PointOfInterest();
		for (Pair<Langue, String> json : jsons) {
			CampToCampJson campToCampJson = parseJSON(json.getValue1());
            if (campToCampJson == null) return null;
            pointOfInterest.setType(PointOfInterest.POIType.mountain_hut);
			pointOfInterest.setName(campToCampJson.properties.name);
			pointOfInterest.setLatitude(campToCampJson.geometry.coordinates[1]);
            pointOfInterest.setLongitude(campToCampJson.geometry.coordinates[0]);
            pointOfInterest.setAltitude(campToCampJson.geometry.coordinates[2]);
            if (StringUtils.isNotEmpty(campToCampJson.properties.description)) {
                PointOfInterest.LocalizedText localizedText = new PointOfInterest.LocalizedText();
                localizedText.setLanguage(campToCampJson.properties.culture);
                localizedText.setText(campToCampJson.properties.description);
                pointOfInterest.getDescriptions().add(localizedText);
            }
		}
		return pointOfInterest;
	}
	
	private static CampToCampJson parseJSON(String json) throws ParseException {
        try {
            CampToCampJson result = new CampToCampJson();
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(json);
            String type = (String) jsonObject.get("type");
            result.setType(type);

            if (jsonObject.containsKey("geometry")) {
                JSONObject geometryObject = (JSONObject) jsonObject.get("geometry");

                CampToCampJson.Geometry geometry = new CampToCampJson.Geometry();
                geometry.setType((String) geometryObject.get("type"));

                JSONArray coordinatesArray = (JSONArray) geometryObject.get("coordinates");

                Double[] coordinates = new Double[coordinatesArray.size()];

                for (int index = 0; index < coordinatesArray.size(); index++) {
                    if (coordinatesArray.get(index) instanceof Double) {
                        coordinates[index] = (Double) coordinatesArray.get(index);
                    } else if (coordinatesArray.get(index) instanceof Long) {
                        coordinates[index] = new Long((Long) coordinatesArray.get(index)).doubleValue();
                    }
                }

                geometry.setCoordinates(coordinates);
                result.setGeometry(geometry);
            }

            if (jsonObject.containsKey("properties")) {
                JSONObject propertiesObject = (JSONObject) jsonObject.get("properties");
                CampToCampJson.Properties properties = new CampToCampJson.Properties();
                properties.setId(propertiesObject.get("id") + "");
                properties.setModule((String) propertiesObject.get("module"));
                properties.setCulture((String) propertiesObject.get("culture"));
                properties.setDescription((String) propertiesObject.get("description"));
                properties.setName((String) propertiesObject.get("name"));
                result.setProperties(properties);
            }

            return result;
        } catch (Throwable t) {
            System.out.println("JSON parse error");
            t.printStackTrace();
        }
        return null;
	}
	
	private static String getJson(String url) throws IOException {
		HttpHost httpHost = new HttpHost("www.camptocamp.org", 80);
		
		HttpGet get = new HttpGet(url);
		
		HttpClient httpClient = new DefaultHttpClient();
		HttpResponse response = httpClient.execute(httpHost, get);
		StatusLine statusLine = response.getStatusLine();
		if (statusLine.getStatusCode() == 200) {
			return IOUtils.toString(response.getEntity().getContent());
		}
		return null;
	}
	
	private static List<String> getHtmlPages(List<String> urls) throws FileNotFoundException, IOException {
		List<String> result = Lists.newArrayList();
		for (String url : urls) {
			result.add(getHtml(url));
		}
		return result;
	}
	
	private static String getHtml(String url) throws FileNotFoundException, IOException {
		HttpHost httpHost = new HttpHost("www.camptocamp.org", 80);
		
		HttpGet get = new HttpGet(url);
		
		HttpClient httpClient = new DefaultHttpClient();
		HttpResponse response = httpClient.execute(httpHost, get);
		StatusLine statusLine = response.getStatusLine();
		if (statusLine.getStatusCode() == 200) {
			return IOUtils.toString(response.getEntity().getContent());
		}
		return null;
	}
	
	private enum Langue {
		fr,
		it,
		en,
		es
	}
	
	public static class CampToCampJson {
		
		private String type;
		
		private Geometry geometry;
		
		private Properties properties;
		
		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public Geometry getGeometry() {
			return geometry;
		}

		public void setGeometry(Geometry geometry) {
			this.geometry = geometry;
		}

		public Properties getProperties() {
			return properties;
		}

		public void setProperties(Properties properties) {
			this.properties = properties;
		}

		public static class Geometry {
			
			private String type;
			
			private Double[] coordinates;

			public String getType() {
				return type;
			}

			public void setType(String type) {
				this.type = type;
			}

            public Double[] getCoordinates() {
                return coordinates;
            }

            public void setCoordinates(Double[] coordinates) {
                this.coordinates = coordinates;
            }
        }
		
		public static class Properties {
			
			private String module;
			
			private String id;
			
			private String culture;
			
			private String name;
			
			private String description;

			public String getModule() {
				return module;
			}

			public void setModule(String module) {
				this.module = module;
			}

			public String getId() {
				return id;
			}

			public void setId(String id) {
				this.id = id;
			}

			public String getCulture() {
				return culture;
			}

			public void setCulture(String culture) {
				this.culture = culture;
			}

			public String getName() {
				return name;
			}

			public void setName(String name) {
				this.name = name;
			}

			public String getDescription() {
				return description;
			}

			public void setDescription(String description) {
				this.description = description;
			}
			
			
			
		}
		
	}
	
}
