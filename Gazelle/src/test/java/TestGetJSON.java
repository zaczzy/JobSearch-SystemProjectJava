package test.java;
import main.java.APIServer;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class TestGetJSON {
    @Test
    public void testPrintTime() {
        System.out.println(APIServer.formatTime(1557681683));
    }
    @Test
    public void testWalmartAbnormal() throws IOException {
        String query = "air";
        URL url = new URL("http://api.walmartlabs.com/v1/search?apiKey=**REMOVED**" + query);
        URLConnection con = url.openConnection();
        InputStream in = con.getInputStream();
        String encoding = con.getContentEncoding();
        encoding = encoding == null ? "UTF-8" : encoding;
        String body = IOUtils.toString(in, encoding);
        System.out.println(body);
    }
    @Test
    public void testWeather()  {
        System.out.println(APIServer.getWeather("new york"));
    }
    @Test
    public void testWalmart() {
        System.out.println(APIServer.getWalmart("nothing"));
    }
}
