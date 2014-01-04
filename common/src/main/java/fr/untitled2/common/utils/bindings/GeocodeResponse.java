package fr.untitled2.common.utils.bindings;

import java.net.URLEncoder;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.google.common.collect.Lists;

@XmlRootElement(name = "GeocodeResponse")
@XmlAccessorType(XmlAccessType.FIELD)
public class GeocodeResponse {
	
	@XmlElement
	private String status;

	@XmlElement
	private Result result;
	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Result getResult() {
		return result;
	}

	public void setResult(Result result) {
		this.result = result;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	public static class Result {

		@XmlElement
		private String type;
		
		@XmlElement(name = "formatted_address")
		private String formatedAddress;
		
		@XmlElement(name = "address_component")
		private List<AdressComponent> adressComponents = Lists.newArrayList();
		
		@XmlElement(name = "partial_match")
		private boolean partialMatch;
		
		@XmlElement
		private Geometry geometry;
		
		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getFormatedAddress() {
			return formatedAddress;
		}

		public void setFormatedAddress(String formatedAddress) {
			this.formatedAddress = formatedAddress;
		}

		public List<AdressComponent> getAdressComponents() {
			return adressComponents;
		}

		public void setAdressComponents(List<AdressComponent> adressComponents) {
			this.adressComponents = adressComponents;
		}

		public boolean isPartialMatch() {
			return partialMatch;
		}

		public void setPartialMatch(boolean partialMatch) {
			this.partialMatch = partialMatch;
		}

		public Geometry getGeometry() {
			return geometry;
		}

		public void setGeometry(Geometry geometry) {
			this.geometry = geometry;
		}

		@XmlAccessorType(XmlAccessType.FIELD)
		public static class AdressComponent {
			
			private String longName;
			
			private String shortName;
			
			private String type;

			public String getLongName() {
				return longName;
			}

			public void setLongName(String longName) {
				this.longName = longName;
			}

			public String getShortName() {
				return shortName;
			}

			public void setShortName(String shortName) {
				this.shortName = shortName;
			}

			public String getType() {
				return type;
			}

			public void setType(String type) {
				this.type = type;
			}
			
		}
		
		@XmlAccessorType(XmlAccessType.FIELD)
		public static class Geometry {
			
			@XmlElement
			private Location location;
			
			@XmlElement(name = "viewport")
			private ViewPort viewPort;
			
			public Location getLocation() {
				return location;
			}

			public void setLocation(Location location) {
				this.location = location;
			}

			public ViewPort getViewPort() {
				return viewPort;
			}

			public void setViewPort(ViewPort viewPort) {
				this.viewPort = viewPort;
			}

			@XmlAccessorType(XmlAccessType.FIELD)
			public static class Location {
				
				@XmlElement(name = "lat")
				private double latitude;
				
				@XmlElement(name = "lng")
				private double longitude;

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
			
			@XmlAccessorType(XmlAccessType.FIELD)
			public static class ViewPort {
				
				@XmlElement
				private Location southwest;
				
				@XmlElement
				private Location northeast;

				public Location getSouthwest() {
					return southwest;
				}

				public void setSouthwest(Location southwest) {
					this.southwest = southwest;
				}

				public Location getNortheast() {
					return northeast;
				}

				public void setNortheast(Location northeast) {
					this.northeast = northeast;
				}
				
				
				
			}
			
		}
		
	}
	
}
