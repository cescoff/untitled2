package fr.untitled2.android.sqlilite;

import fr.untitled2.common.entities.WifiDetection;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 17/12/13
 * Time: 21:58
 * To change this template use File | Settings | File Templates.
 */
public class WifiDetectionHolder {

    private Long id;

    private boolean stable;

    private WifiDetection wifiDetection;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isStable() {
        return stable;
    }

    public void setStable(boolean stable) {
        this.stable = stable;
    }

    public WifiDetection getWifiDetection() {
        return wifiDetection;
    }

    public void setWifiDetection(WifiDetection wifiDetection) {
        this.wifiDetection = wifiDetection;
    }
}
