package fr.untitled2.common.utils.bindings;

import java.util.List;
import java.util.Locale;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.collect.Lists;

@XmlRootElement @XmlAccessorType(XmlAccessType.FIELD)
public class PointOfInterest {

	@XmlElement
	private POIType type;
	
	@XmlElement
	private String name;
	
	@XmlElement @XmlElementWrapper(name = "description")
	private List<LocalizedText> descriptions = Lists.newArrayList();

	@XmlElement
	private double latitude;
	
	@XmlElement
	private double longitude;
	
	@XmlElement
	private double altitude;
	
	public String getDescription(Locale locale) {
		String defaultLanguage = "en";
		String defaultLanguageDescription = null;
		for (LocalizedText localizedText : descriptions) {
			if (localizedText.language.equals(defaultLanguage)) defaultLanguageDescription = localizedText.text;
			else if (localizedText.language.equals(locale.getLanguage())) return localizedText.text;
		}
		return defaultLanguageDescription;
	}

    public POIType getType() {
        return type;
    }

    public void setType(POIType type) {
        this.type = type;
    }

    public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<LocalizedText> getDescriptions() {
		return descriptions;
	}

	public void setDescriptions(List<LocalizedText> descriptions) {
		this.descriptions = descriptions;
	}

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

	public double getAltitude() {
		return altitude;
	}

	public void setAltitude(double altitude) {
		this.altitude = altitude;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	public static class LocalizedText {
		
		@XmlElement
		private String language;
		
		@XmlElement
		private String text;

		public String getLanguage() {
			return language;
		}

		public void setLanguage(String language) {
			this.language = language;
		}

		public String getText() {
			return text;
		}

		public void setText(String text) {
			this.text = text;
		}
	}
	
	public enum POIType {
		restaurant,
		fast_food,
		gas_station,
		mountain_hut,
		car_park,
		hostel
	}
	
}
