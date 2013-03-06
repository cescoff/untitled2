package fr.untitled2.business;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.googlecode.objectify.ObjectifyService;
import fr.untitled2.business.beans.ImageList;
import fr.untitled2.business.beans.LightWeightImage;
import fr.untitled2.entities.Image;
import fr.untitled2.entities.PictureMap;
import fr.untitled2.entities.User;
import fr.untitled2.utils.CollectionUtils;
import fr.untitled2.utils.StatisticsUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.cache.Cache;
import javax.cache.CacheManager;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 2/9/13
 * Time: 7:54 PM
 * To change this template use File | Settings | File Templates.
 */
@Service
public class ImageBusiness implements Serializable {

    private static Logger logger = LoggerFactory.getLogger(ImageBusiness.class);

    public static Predicate<Image> READY_PREDICATE = new Predicate<Image>() {
        @Override
        public boolean apply(Image image) {
            return image != null && image.isReady();
        }
    };

    public static Function<Image, LightWeightImage> IMAGE_TO_LIGHTWEIGHTIMAGE_FUNCTION = new Function<Image, LightWeightImage>() {
        @Override
        public LightWeightImage apply(Image image) {
        return new LightWeightImage(image);
        }
    };

    public ImageList getImageList(final LocalDate start, final LocalDate end, final User user) {
        logger.info("Liste des image du '" + start + "' au '" + end + "' pour le user '" + user.getUserId() + "'");
        List<Image> images = ObjectifyService.ofy().load().type(Image.class).filter("user", user).list();

        images = Lists.newArrayList(Collections2.filter(Collections2.filter(images, READY_PREDICATE), new Predicate<Image>() {
            @Override
            public boolean apply(Image image) {
                return start.toDateTimeAtStartOfDay().toLocalDateTime().isBefore(image.getDateTaken()) && end.plusDays(1).toDateTimeAtStartOfDay().toLocalDateTime().isAfter(image.getDateTaken());
            }
        }));

        if (CollectionUtils.isNotEmpty(images)) {
            images = StatisticsUtils.IMAGE_SORT.reverse().sortedCopy(images);
            images = Lists.partition(images, 100).get(0);

            images = Lists.transform(images, new Function<Image, Image>() {
                @Override
                public Image apply(Image image) {
                    Image result = image.clone();
                    String timeZoneId = user.getTimeZoneId();
                    if (StringUtils.isNotEmpty(image.getTimeZoneId())) {
                        timeZoneId = image.getTimeZoneId();
                    } else {
                        result.setTimeZoneId(timeZoneId);
                    }
                    result.setDateTaken(image.getDateTaken().toDateTime(DateTimeZone.UTC).toDateTime(DateTimeZone.forID(timeZoneId)).toLocalDateTime());
                    return result;

                }
            });

            ImageList imageList = new ImageList(0, 0);
            imageList.getImages().addAll(Lists.transform(images, IMAGE_TO_LIGHTWEIGHTIMAGE_FUNCTION));
            return imageList;
        }
        return null;
    }

    public ImageList getImageList(int pageNumber, final User user) {
        if (CacheHelper.containsImageList(user, pageNumber)) {
            logger.info("Image list cachee (" + user.getUserId() + ":" + pageNumber + ")");
            return CacheHelper.getImageList(user, pageNumber);
        }
        Iterable<Image> images = ObjectifyService.ofy().load().type(Image.class).filter("user", user);
        images = Iterables.filter(images, READY_PREDICATE);
        images = Iterables.transform(images, new Function<Image, Image>() {
            @Override
            public Image apply(Image image) {
                Image result = image.clone();
                String timeZoneId = user.getTimeZoneId();
                if (StringUtils.isNotEmpty(image.getTimeZoneId())) {
                    timeZoneId = image.getTimeZoneId();
                } else {
                    result.setTimeZoneId(timeZoneId);
                }
                result.setDateTaken(image.getDateTaken().toDateTime(DateTimeZone.UTC).toDateTime(DateTimeZone.forID(timeZoneId)).toLocalDateTime());
                return result;
            }
        });
        List<Image> page = StatisticsUtils.IMAGE_SORT.reverse().sortedCopy(images);
        List<List<Image>> pages = Lists.partition(page, 100);

        if (CollectionUtils.isNotEmpty(pages) && pageNumber < pages.size()) {
            int nextPageNumber = pageNumber + 1;
            if (nextPageNumber >= pages.size()) nextPageNumber = 0;
            ImageList imageList = new ImageList(pageNumber, nextPageNumber);
            imageList.getImages().addAll(Lists.transform(pages.get(pageNumber), IMAGE_TO_LIGHTWEIGHTIMAGE_FUNCTION));
            CacheHelper.putImageList(user, pageNumber, imageList);
            return imageList;
        }
        return null;
    }

    public boolean isInMap(final Image image) {
        if (CacheHelper.containsImageBelongsToMap(image)) return CacheHelper.isInMap(image);

        Collection<PictureMap> pictureMaps = ObjectifyService.ofy().load().type(PictureMap.class).list();
        pictureMaps = Collections2.filter(pictureMaps, new Predicate<PictureMap>() {
            @Override
            public boolean apply(PictureMap pictureMap) {
                return pictureMap.getPeriodStart().toDateTimeAtStartOfDay().toLocalDateTime().isBefore(image.getDateTaken()) && pictureMap.getPeriodEnd().plusDays(1).toDateTimeAtStartOfDay().toLocalDateTime().isAfter(image.getDateTaken());
            }
        });
        logger.info("Nombre de map trouvee : " + pictureMaps.size());
        CacheHelper.putImageBelongsToMap(image, CollectionUtils.isNotEmpty(pictureMaps));
        return CollectionUtils.isNotEmpty(pictureMaps);
    }

}
