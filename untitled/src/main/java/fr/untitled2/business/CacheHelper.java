package fr.untitled2.business;

import fr.untitled2.business.beans.ImageList;
import fr.untitled2.business.beans.LightWeightImage;
import fr.untitled2.entities.Image;
import fr.untitled2.entities.PictureMap;
import fr.untitled2.entities.User;
import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 2/15/13
 * Time: 4:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class CacheHelper {

    private static Logger logger = LoggerFactory.getLogger(CacheHelper.class);

    private static Cache cache = null;

    static {
        try {
            cache = CacheManager.getInstance().getCacheFactory().createCache(Collections.emptyMap());
        } catch (CacheException e) {
            logger.error("Impossible d'instancier le cache", e);
        }
    }

    public static boolean containsImageList(User user, int page) {
        Cache currentCache = getCache();
        if (currentCache == null) {
            logger.info("Le cache ne fonctionne pas");
            return false;
        }
        boolean result = currentCache.containsKey(getImageListKey(user, page));
        if (result) logger.info("ImageList is cached (" + user + ":" + page + ")");
        return result;
    }

    public static ImageList getImageList(User user, int page) {
        Cache currentCache = getCache();
        if (currentCache == null) {
            logger.info("Le cache ne fonctionne pas");
            return null;
        }
        return (ImageList) currentCache.get(getImageListKey(user, page));
    }

    public static void putImageList(User user, int page, ImageList imageList) {
        Cache currentCache = getCache();
        if (currentCache == null) {
            logger.info("Le cache ne fonctionne pas");
            return;
        }
        currentCache.put(getImageListKey(user, page), imageList);
    }

    public static void removeImageList(User user, int page) {
        Cache currentCache = getCache();
        if (currentCache == null) {
            logger.info("Le cache ne fonctionne pas");
            return;
        }
        currentCache.remove(getImageListKey(user, page));
    }

    public static boolean containsPictureMapImageList(PictureMap pictureMap) {
        Cache currentCache = getCache();
        if (currentCache == null) {
            logger.info("Le cache ne fonctionne pas");
            return false;
        }
        return currentCache.containsKey(getPictureMapImageListKey(pictureMap));
    }

    public static List<LightWeightImage> getPictureMapImageList(PictureMap pictureMap) {
        Cache currentCache = getCache();
        if (currentCache == null) {
            logger.info("Le cache ne fonctionne pas");
            return null;
        }
        return (List<LightWeightImage>) currentCache.get(getPictureMapImageListKey(pictureMap));
    }

    public static void putPictureMapImageList(PictureMap pictureMap, List<LightWeightImage> images) {
        Cache currentCache = getCache();
        if (currentCache == null) {
            logger.info("Le cache ne fonctionne pas");
            return;
        }
        currentCache.put(getPictureMapImageListKey(pictureMap), images);
    }

    public static void removePictureMapImageList(PictureMap pictureMap) {
        Cache currentCache = getCache();
        if (currentCache == null) {
            logger.info("Le cache ne fonctionne pas");
            return;
        }
        currentCache.remove(getPictureMapImageListKey(pictureMap));

    }

    public static void removeImageBelongsToMap(Image image) {
        Cache currentCache = getCache();
        if (currentCache == null) {
            logger.info("Le cache ne fonctionne pas");
            return;
        }
        currentCache.remove(getImageBelongsToMapKey(image));
    }

    public static boolean containsImageBelongsToMap(Image image) {
        Cache currentCache = getCache();
        if (currentCache == null) {
            logger.info("Le cache ne fonctionne pas");
            return false;
        }
        return cache.containsKey(getImageBelongsToMapKey(image));
    }

    public static Boolean isInMap(Image image) {
        Cache currentCache = getCache();
        if (currentCache == null) {
            logger.info("Le cache ne fonctionne pas");
            return false;
        }
        return (Boolean) cache.get(getImageBelongsToMapKey(image));
    }
    public static void putImageBelongsToMap(Image image, Boolean value) {
        Cache currentCache = getCache();
        if (currentCache == null) {
            logger.info("Le cache ne fonctionne pas");
            return;
        }
        currentCache.put(getImageBelongsToMapKey(image), value);
    }

    private static String getImageBelongsToMapKey(Image image) {
        return new StringBuilder().append("Image2Map:").append(image.getImageKey()).toString();
    }

    private static String getPictureMapImageListKey(PictureMap pictureMap) {
        return new StringBuilder().append("PictureMap:").append(pictureMap.getSharingKey()).toString();
    }

    private static String getImageListKey(User user, int page) {
        return new StringBuilder().append("ImageList:User:").append(user.getUserId()).append(":Page:").append(page).toString();
    }

    private static Cache getCache() {
        if (cache != null) return cache;

        try {
            cache = CacheManager.getInstance().getCacheFactory().createCache(Collections.emptyMap());
        } catch (CacheException e) {
            logger.error("Impossible de creer un cache", e);
        }

        return cache;
    }

}
