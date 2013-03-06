package fr.untitled2.business;

import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import fr.untitled2.business.beans.*;
import fr.untitled2.entities.Image;
import fr.untitled2.entities.Log;
import fr.untitled2.entities.PictureMap;
import fr.untitled2.entities.User;
import fr.untitled2.servlet.ImageDisplayMode;
import fr.untitled2.utils.CollectionUtils;
import fr.untitled2.utils.StatisticsUtils;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 2/10/13
 * Time: 10:51 AM
 * To change this template use File | Settings | File Templates.
 */
@Service
public class MapBusiness {

    private static Logger logger = LoggerFactory.getLogger(MapBusiness.class);

    public MapList getMapList(int pageNumber, User user) {
        Iterable<PictureMap> pictureMaps = ObjectifyService.ofy().load().type(PictureMap.class).filter("user", user);
        List<List<PictureMap>> pages = Lists.partition(Lists.newArrayList(Ordering.natural().reverse().onResultOf(new Function<PictureMap, LocalDate>() {
            @Override
            public LocalDate apply(PictureMap map) {
                return map.getPeriodStart();
            }
        }).sortedCopy(pictureMaps)), 20);
        logger.info("Pages(" + pages.size() + ") PageNumber(" + pageNumber + ")");
        if (CollectionUtils.isNotEmpty(pages) && pageNumber < pages.size()) {
            int nextPageNumber = pageNumber + 1;
            if (nextPageNumber >= pages.size()) nextPageNumber = 0;
            MapList mapList = new MapList(pageNumber, nextPageNumber);
            mapList.getPictureMap().addAll(pages.get(pageNumber));
            return mapList;
        }
        return null;
    }

    public MapMarkers getMapMarkers(DateTimeFormatter dateTimeFormatter, String mapKey, User user) {
        final PictureMap pictureMap = ObjectifyService.ofy().load().key(Key.create(PictureMap.class, mapKey)).get();

        if (CacheHelper.containsPictureMapImageList(pictureMap)) {
            logger.info("Liste des images de la Map cachee (" + pictureMap.getSharingKey() + ")");
            List<LightWeightImage> lightWeightImages = CacheHelper.getPictureMapImageList(pictureMap);
            MapMarkers mapMarkers = new MapMarkers();
            mapMarkers.getMarkers().addAll(Lists.transform(lightWeightImages, new MapMarkers.LightWeightImageToMarker(dateTimeFormatter)));
            if (user != null) {
                if (pictureMap.getUser().getUserId().equals(user.getUserId())) {
                    mapMarkers.setUserMapMarker(true);
                }
            }
            return mapMarkers;
        }


        Iterable<Image> userImages = ObjectifyService.ofy().load().type(Image.class).filter("user", pictureMap.getUser());
        userImages = Iterables.filter(userImages, StatisticsUtils.IMAGE_READY_FILTER);
        userImages = Iterables.filter(userImages, new Predicate<Image>() {
            @Override
            public boolean apply(Image image) {
                return image.getDateTaken().isAfter(pictureMap.getPeriodStart().toDateTimeAtStartOfDay().toLocalDateTime()) && image.getDateTaken().isBefore(pictureMap.getPeriodEnd().plusDays(1).toDateTimeAtStartOfDay().toLocalDateTime());
            }
        });
        userImages = Iterables.filter(userImages, new Predicate<Image>() {
            @Override
            public boolean apply(Image image) {
                return image.getLatitude() != null && image.getLongitude() != null;
            }
        });
        List<Image> sortedimages = Ordering.natural().onResultOf(new Function<Image, LocalDateTime>() {

            @Override
            public LocalDateTime apply(Image image) {
                return image.getDateTaken();
            }
        }).sortedCopy(userImages);

        List<LightWeightImage> lightWeightImages = Lists.newArrayList(Lists.transform(sortedimages, ImageBusiness.IMAGE_TO_LIGHTWEIGHTIMAGE_FUNCTION));

        CacheHelper.putPictureMapImageList(pictureMap, lightWeightImages);

        MapMarkers mapMarkers = new MapMarkers();
        mapMarkers.getMarkers().addAll(Lists.transform(lightWeightImages, new MapMarkers.LightWeightImageToMarker(dateTimeFormatter)));

        if (user != null) {
            if (pictureMap.getUser().getUserId().equals(user.getUserId())) {
                mapMarkers.setUserMapMarker(true);
            }
        }

        return mapMarkers;
    }

}
