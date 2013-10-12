package fr.untitled2.servlet.api.command;

import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 9/28/13
 * Time: 11:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class StatisticsRegisterTest {
    @Test
    public void testLoadAVG() throws Exception {
        String line = "Load Avg: 1.65, 1.47, 1.78";
        String regex = "Load\\sAvg\\:\\s[0-9]*\\.[0-9W]{2}\\,\\s([0-9]*\\.[0-9W]{2})\\,\\s[0-9]*\\.[0-9W]{2}";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(line);
        if (matcher.matches()) System.out.println("'" + matcher.group(1) + "'");
    }

    @Test
    public void testLoadCPU() throws Exception {
        String line = "CPU usage: 25.92% user, 48.14% sys, 25.92% idle";
        String regex = "CPU\\susage\\:\\s([0-9]{2,3}\\.[0-9]{2})\\%\\suser\\,\\s[0-9]{2,3}\\.[0-9]{2}\\%\\ssys\\,\\s[0-9]{2,3}\\.[0-9]{2}\\%\\sidle";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(line);
        if (matcher.matches()) System.out.println("'" + matcher.group(1) + "'");
    }

    @Test
    public void testLoadMemUsage() throws Exception {
        String line = "PhysMem: 1033M wired, 3638M active, 2756M inactive, 7427M used, 763M free.";
        String regex = "PhysMem\\:\\s[0-9]*M\\swired\\,\\s[0-9]*M\\sactive\\,\\s[0-9]*M\\sinactive\\,\\s([0-9]*)M\\sused\\,\\s([0-9]*)M\\sfree\\.";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(line);
        if (matcher.matches()) {
            System.out.println("'" + matcher.group(1) + "'");
            System.out.println("'" + matcher.group(2) + "'");
        }
    }

    @Test
    public void testLoadAVGLinux() throws Exception {
        String line = "0.01 0.04 0.05 1/151 21341";
        Pattern pattern = Pattern.compile("[0-9]+\\.[0-9]{2}\\s*([0-9]+\\.[0-9]{2})\\s*[0-9]+\\.[0-9]{2}\\s*[0-9]+\\/[0-9]+\\s[0-9]+\\s*");
        Matcher matcher = pattern.matcher(line);
        if (matcher.matches()) System.out.println("'" + matcher.group(1) + "'");
    }

    @Test
    public void testLoadCPULinux() throws Exception {
        String line = "21:19:12     all    0.23    0.00    0.19    0.01    0.00    0.02    0.00    0.00   99.54";
        String regex = "[0-9]+\\:[0-9]+\\:[0-9]+\\s*all\\s*([0-9]+\\.[0-9]+)\\s*[0-9]+\\.[0-9]+\\s*[0-9]+\\.[0-9]+\\s*[0-9]+\\.[0-9]+\\s*[0-9]+\\.[0-9]+\\s*[0-9]+\\.[0-9]+\\s*[0-9]+\\.[0-9]+\\s*[0-9]+\\.[0-9]+\\s*[0-9]+\\.[0-9]+\\s*";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(line);
        if (matcher.matches()) System.out.println("'" + matcher.group(1) + "'");
    }

    @Test
    public void testLoadMemUsageLinux() throws Exception {
        String line = "Mem:        448736     270700     178036          0      35272     192180";
        String regex = "Mem\\:\\s*[0-9]+\\s+([0-9]+)\\s+([0-9]+)\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+\\s*";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(line);
        if (matcher.matches()) {
            System.out.println("'" + matcher.group(1) + "'");
            System.out.println("'" + matcher.group(2) + "'");
        }
    }
}
