package fr.untitled2;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 06/12/13
 * Time: 00:35
 * To change this template use File | Settings | File Templates.
 */
public class TestStringParser {

    @Test
    public void testString() throws Exception {
        File htmlFile = new File("/Users/corentinescoffier/Developpement/test.html");
        String line = new String(IOUtils.toByteArray(new FileInputStream(htmlFile)));
        String verificationCode = null;
        if (StringUtils.contains(line, "<span id=\"verification_code\">")) {
            verificationCode = StringUtils.substring(line, StringUtils.indexOf(line, "<span id=\"verification_code\">") + "<span id=\"verification_code\">".length(), StringUtils.indexOf(line, "</span>", StringUtils.indexOf(line, "<span id=\"verification_code\">")));
            System.out.println(StringUtils.substring(line, StringUtils.indexOf(line, "<span id=\"verification_code\">") + "<span id=\"verification_code\">".length()));
            System.out.println((StringUtils.indexOf(line, "<span id=\"verification_code\">") + "<span id=\"verification_code\">".length()) + "<->" + StringUtils.indexOf(line, "</span>", StringUtils.indexOf(line, "<span id=\"verification_code\">")));
        }
        System.out.println("'" + verificationCode + "'");
    }

}
