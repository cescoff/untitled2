package fr.untitled2.android.i18n;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 3/12/13
 * Time: 1:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class I18nResourceKey {

    private String bundleName;

    private String key;

    private String defaultValue;

    public I18nResourceKey(String bundleName, String key, String defaultValue) {
        this.bundleName = bundleName;
        this.key = key;
        this.defaultValue = defaultValue;
    }

    public String getBundleName() {
        return bundleName;
    }

    public String getKey() {
        return key;
    }

    public String getDefaultValue() {
        return defaultValue;
    }
}
