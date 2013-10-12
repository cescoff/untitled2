package fr.untitled2.raspi.thread;

import fr.untitled2.raspi.utils.CommandLineUtils;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 9/28/13
 * Time: 11:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class RegisterProcessTest {
    @Test
    public void testRun() throws Exception {
        Pattern regexLoadAVG = Pattern.compile("Load\\sAvg\\:\\s[0-9]*\\.[0-9W]{2}\\,\\s([0-9]*\\.[0-9W]{2})\\,\\s[0-9]*\\.[0-9W]{2}\\s*");
        Pattern regexCPU = Pattern.compile("CPU\\susage\\:\\s([0-9]{2,3}\\.[0-9]{2})\\%\\suser\\,\\s[0-9]{2,3}\\.[0-9]{2}\\%\\ssys\\,\\s[0-9]{2,3}\\.[0-9]{2}\\%\\sidle\\s*");
        Pattern regexMem = Pattern.compile("PhysMem\\:\\s[0-9]*M\\swired\\,\\s[0-9]*M\\sactive\\,\\s[0-9]*M\\sinactive\\,\\s([0-9]*)M\\sused\\,\\s([0-9]*)M\\sfree\\.\\s*");

        CommandLine commandLine = new CommandLine("/usr/bin/top");
        commandLine.addArgument("-l").addArgument("1");

        String result = CommandLineUtils.executedCommandLine(commandLine, 10 * 60 * 1000);
        LineIterator lineIterator = new LineIterator(new StringReader(result));
        double loadAvg = -1.0;
        double cpuUsage = -1.0;
        double memUsage = -1.0;
        while (lineIterator.hasNext()) {
            String line = lineIterator.nextLine();

//            System.out.println("'" + line + "'");

            Matcher loadAVGMatcher = regexLoadAVG.matcher(line);
            Matcher cpuUsageMatcher = regexCPU.matcher(line);
            Matcher memMatcher = regexMem.matcher(line);

            if (loadAVGMatcher.matches() && loadAvg < 0) {
                loadAvg = Double.parseDouble(loadAVGMatcher.group(1));
                System.out.println("LOAD AVG : '" + line + "'");
            }

            if (cpuUsageMatcher.matches() && cpuUsage < 0) {
                cpuUsage = Double.parseDouble(cpuUsageMatcher.group(1));
                System.out.println("CPU : '" + line + "'");
            }

            if (memMatcher.matches() && memUsage < 0) {
                double memUsed = Integer.parseInt(memMatcher.group(1));
                double memFree = Integer.parseInt(memMatcher.group(2));
                memUsage = (memUsed / (memFree + memUsed)) * 100;
                System.out.println("MEM : '" + line + "'");
            }

            if (loadAvg > 0 && cpuUsage > 0 && memUsage > 0) break;

        }

        System.out.println("Load AVG : " + loadAvg);
        System.out.println("CPU : " + cpuUsage);
        System.out.println("Mem : " + memUsage);

    }

    @Test
    public void testLinux() throws Exception {
                                              //top     -    18       :12        :34         up   8        days  ,    6       :55        ,    3        users  ,    load   average  :   0       .01        , 0       .06         ,   0       . 05
        Pattern regexLoadAVG = Pattern.compile("top\\s\\-\\s[0-9]{2}\\:[0-9]{2}\\:[0-9]{2}\\sup\\s[0-9]+\\sdays\\,\\s*[0-9]+\\:[0-9]{2}\\,\\s*[0-9]*\\susers\\,\\s*load\\saverage\\:\\s[0-9]+\\.[0-9]{2}\\,([0-9]+\\.[0-9]{2})\\,\\s[0-9]+\\.[0-9]{2}\\s*");
        Pattern regexCPU = Pattern.compile("CPU\\susage\\:\\s([0-9]{2,3}\\.[0-9]{2})\\%\\suser\\,\\s[0-9]{2,3}\\.[0-9]{2}\\%\\ssys\\,\\s[0-9]{2,3}\\.[0-9]{2}\\%\\sidle\\s*");
        Pattern regexMem = Pattern.compile("PhysMem\\:\\s[0-9]*M\\swired\\,\\s[0-9]*M\\sactive\\,\\s[0-9]*M\\sinactive\\,\\s([0-9]*)M\\sused\\,\\s([0-9]*)M\\sfree\\.\\s*");

        String result = IOUtils.toString(new FileInputStream(new File("/Users/corentinescoffier/top.txt")));
        LineIterator lineIterator = new LineIterator(new StringReader(result));
        double loadAvg = -1.0;
        double cpuUsage = -1.0;
        double memUsage = -1.0;
        while (lineIterator.hasNext()) {
            String line = lineIterator.nextLine();

            System.out.println("'" + line + "'");

            Matcher loadAVGMatcher = regexLoadAVG.matcher(line);
            Matcher cpuUsageMatcher = regexCPU.matcher(line);
            Matcher memMatcher = regexMem.matcher(line);

            if (loadAVGMatcher.matches() && loadAvg < 0) {
                loadAvg = Double.parseDouble(loadAVGMatcher.group(1));
                System.out.println("LOAD AVG : '" + line + "'");
            }

            if (cpuUsageMatcher.matches() && cpuUsage < 0) {
                cpuUsage = Double.parseDouble(cpuUsageMatcher.group(1));
                System.out.println("CPU : '" + line + "'");
            }

            if (memMatcher.matches() && memUsage < 0) {
                double memUsed = Integer.parseInt(memMatcher.group(1));
                double memFree = Integer.parseInt(memMatcher.group(2));
                memUsage = (memUsed / (memFree + memUsed)) * 100;
                System.out.println("MEM : '" + line + "'");
            }

            if (loadAvg > 0 && cpuUsage > 0 && memUsage > 0) break;

        }

        System.out.println("Load AVG : " + loadAvg);
        System.out.println("CPU : " + cpuUsage);
        System.out.println("Mem : " + memUsage);

    }
}
