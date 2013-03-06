package fr.untitled2.servlet;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 2/12/13
 * Time: 6:26 PM
 * To change this template use File | Settings | File Templates.
 */
public enum ImageDisplayMode {
    low(1),
    lowSquare(2),
    medium(3),
    high(4),
    orginal(5);

    private int code;


    private ImageDisplayMode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
