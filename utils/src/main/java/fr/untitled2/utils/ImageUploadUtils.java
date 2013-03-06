package fr.untitled2.utils;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.*;
import java.net.*;
import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 2/12/13
 * Time: 8:26 AM
 * To change this template use File | Settings | File Templates.
 */
public class ImageUploadUtils {

    public static class ImageUpload {

        @Parameter(names = { "-e", "--email" }, description = "The email of the target user")
        public String userEmail;

        @Parameter(names = { "-id", "--image-dir" }, description = "The source image dir")
        public File imageDir;

    }

    public static void main(String[] args) {
        ImageUpload imageUpload = new ImageUpload();
        JCommander jCommander = new JCommander(imageUpload);
        jCommander.parse(args);

        JCommander.getConsole().println("Sur le point d'uploader les images avec les infos suivantes");
        JCommander.getConsole().println(imageUpload.userEmail);
        JCommander.getConsole().println(imageUpload.imageDir.getPath());

        Collection<File> files = FileUtils.listFiles(imageUpload.imageDir, new String[] {"jpg", "nef", "JPG", "NEF"}, true);
        for (File file : files) {
            try {
                DefaultHttpClient httpClient = new DefaultHttpClient();
                JCommander.getConsole().println("Sur le point d'uploader : " + file.getPath());
                HttpPost post = new HttpPost("http://x5-teak-clarity-4.appspot.com/imageUpload");

                MultipartEntity entity = new MultipartEntity();
                entity.addPart("userFile", new FileBody(file));
                entity.addPart("userEmail", new StringBody(imageUpload.userEmail));
                post.setEntity(entity);

                HttpResponse response = httpClient.execute(post);
                StatusLine statusLine = response.getStatusLine();
                JCommander.getConsole().println(statusLine.getStatusCode() + " : " + statusLine.getReasonPhrase());
            } catch (Throwable t) {
                JCommander.getConsole().println("An error has occured");
                t.printStackTrace();
            }
        }

    }

    public int sendFile(String surl, String file1) {

        int rtn = 1;

        HttpURLConnection conn = null;
        BufferedReader br = null;
        DataOutputStream dos = null;
        DataInputStream inStream = null;

        InputStream is = null;
        OutputStream os = null;
        boolean ret = false;
        String StrMessage = "";
        String exsistingFileName = file1;
        File fFile2Snd = new File(exsistingFileName);

        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "***232404jkg4220957934FW**";

        int bytesRead, bytesAvailable, bufferSize;

        byte[] buffer;

        int maxBufferSize = 1 * 1024 * 1024;

        String responseFromServer = "";

        String urlString = surl;// "http://localhost:81/FileUpload/requestupload";
// urlString =
// "http://a.com/sel2in/prjs/php/p12/skewmypic/v1/getBytes.php";

        try {
// ------------------ CLIENT REQUEST

            FileInputStream fileInputStream = new FileInputStream(new File(
                    exsistingFileName));
            rtn++;

// open a URL connection to the Servlet

            URL url = new URL(urlString);
            rtn++;

// Open a HTTP connection to the URL

            conn = (HttpURLConnection) url.openConnection();

// Allow Inputs
            conn.setDoInput(true);

// Allow Outputs
            conn.setDoOutput(true);

// Don't use a cached copy.
            conn.setUseCaches(false);

// Use a post method.
            conn.setRequestMethod("POST");

            conn.setRequestProperty("Connection", "Keep-Alive");

            conn.setRequestProperty("Content-Type",
                    "multipart/form-data;boundary=" + boundary);

            dos = new DataOutputStream(conn.getOutputStream());

            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"userfile\";"
                    + " filename=\"" + fFile2Snd.getName() + "\"" + lineEnd);
            dos.writeBytes(lineEnd);

            rtn++;

// create a buffer of maximum size

            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];

// read file and write it into form...

            bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            while (bytesRead > 0) {
                dos.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }

// send multipart form data necesssary after file data...

            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

// close streams

            fileInputStream.close();
            dos.flush();
            dos.close();

        } catch (MalformedURLException ex) {
            System.out.println("From ServletCom2 CLIENT REQUEST:" + ex);
        }

        catch (IOException ioe) {
            System.out.println("From ServletCom2 CLIENT REQUEST:" + ioe);
        }

// ------------------ read the SERVER RESPONSE

        try {
            System.out.println("Server response is: \n");
            inStream = new DataInputStream(conn.getInputStream());
            String str;
            while ((str = inStream.readLine()) != null) {
                System.out.println(str);
                System.out.println("");
            }
            inStream.close();
            System.out.println("\nEND Server response ");

        } catch (IOException ioex) {
            System.out.println("From (ServerResponse): " + ioex);

        }
        rtn = 0;
        return rtn;

    }

}
