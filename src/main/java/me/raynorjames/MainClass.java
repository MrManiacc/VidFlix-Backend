package me.raynorjames;

import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.core.har.HarEntry;
import net.lightbody.bmp.proxy.CaptureType;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.List;

public class MainClass {
    public static String driverPath;
    public static String binPath;

    public static void main(String[] args) throws InterruptedException, URISyntaxException, UnknownHostException {
        driverPath = args[0];
        binPath = args[1];
        if(!new File(driverPath).exists())
            System.out.println("DOESN'T EXSIST: " + driverPath);

        if(!new File(binPath).exists())
            System.out.println("DOESN'T EXSIST: " + binPath);

         ConnectionManager connectionManager = new ConnectionManager(3000);
       // new Video("https://solarmovie.id/movie/solo-a-star-wars-story/OdGaSB7i", new SolarGrabber(false, "Star wars"));

    }

}
