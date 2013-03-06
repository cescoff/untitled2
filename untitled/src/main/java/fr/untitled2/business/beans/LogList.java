package fr.untitled2.business.beans;

import com.google.common.collect.Lists;
import fr.untitled2.entities.Log;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 2/10/13
 * Time: 10:50 AM
 * To change this template use File | Settings | File Templates.
 */
public class LogList {

    private int pageNumber;

    private int nextPageNumber;

    private List<Log> logs = Lists.newArrayList();

    public LogList(int pageNumber, int nextPageNumber) {
        this.pageNumber = pageNumber;
        this.nextPageNumber = nextPageNumber;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public int getNextPageNumber() {
        return nextPageNumber;
    }

    public List<Log> getLogs() {
        return logs;
    }
}
