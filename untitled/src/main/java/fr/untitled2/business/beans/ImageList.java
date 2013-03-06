package fr.untitled2.business.beans;

import com.google.common.collect.Lists;
import fr.untitled2.entities.Image;

import java.io.Serializable;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 2/9/13
 * Time: 7:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class ImageList implements Serializable {

    private int pageNumber;

    private int nextPageNumber;

    private List<LightWeightImage> images = Lists.newArrayList();

    public ImageList(int pageNumber, int nextPageNumber) {
        this.pageNumber = pageNumber;
        this.nextPageNumber = nextPageNumber;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public int getNextPageNumber() {
        return nextPageNumber;
    }

    public void setNextPageNumber(int nextPageNumber) {
        this.nextPageNumber = nextPageNumber;
    }

    public List<LightWeightImage> getImages() {
        return images;
    }
}
