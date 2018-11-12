package me.raynorjames;

import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import net.lightbody.bmp.core.har.*;
import net.lightbody.bmp.proxy.CaptureType;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class SolarGrabber {

    private WebDriver driver;
    private boolean isQuery;
    private long videoTimeout;
    private BrowserMobProxy proxy;
    public SolarGrabber(boolean isQuery, long videoTimeout) throws UnknownHostException, URISyntaxException {
        this.isQuery = isQuery;
        this.videoTimeout = videoTimeout;
        initFireFox();
    }

    private void init(){
        System.setProperty("webdriver.chrome.driver", "C:\\windowsdrivers\\chromedriver.exe");
        ChromeOptions options = new ChromeOptions();
//        options.addArguments("--headless");
//        options.addArguments("--no-sandbox");
//        options.setAcceptInsecureCerts(true);
//        options.addArguments("--ignore-ssl-errors=true");
//        options.addArguments("--ssl-protocol=any");
//        options.addArguments("--no-startup-window");
//        options.addArguments("--remote-debugging-port=9223");
        options.addArguments("--mute-audio");
        options.addArguments("--window-position=-32000,-32000");

        this.proxy = new BrowserMobProxyServer();
        proxy.start(0);

        DesiredCapabilities cap = DesiredCapabilities.chrome();
        Proxy seleniumProxy = ClientUtil.createSeleniumProxy(proxy);
        LoggingPreferences logPrefs = new LoggingPreferences();
        logPrefs.enable(LogType.PERFORMANCE, Level.ALL);
        cap.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);
        cap.setCapability(ChromeOptions.CAPABILITY, options);
        cap.setCapability(CapabilityType.PROXY, seleniumProxy);

        proxy.enableHarCaptureTypes(CaptureType.REQUEST_CONTENT, CaptureType.RESPONSE_CONTENT);

        WebDriver driver = new ChromeDriver(cap);
        this.driver = driver;
    }
    private BrowserMobProxy getProxyServer() {
        BrowserMobProxy proxy = new BrowserMobProxyServer();
        proxy.setTrustAllServers(true);
        proxy.start();
        return proxy;
    }


    private Proxy getSeleniumProxy(BrowserMobProxy proxyServer) {
        Proxy seleniumProxy = ClientUtil.createSeleniumProxy(proxyServer);
        try {
            String hostIp = Inet4Address.getLocalHost().getHostAddress();
            seleniumProxy.setHttpProxy(hostIp+":" + proxyServer.getPort());
            seleniumProxy.setSslProxy(hostIp+":" + proxyServer.getPort());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return seleniumProxy;
    }

    private void initFireFox() throws UnknownHostException, URISyntaxException {

        System.out.println(new File(SolarGrabber.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath());
        System.setProperty("webdriver.gecko.driver", MainClass.driverPath);

        BrowserMobProxy proxy = new BrowserMobProxyServer();
        proxy.start(0);

        Proxy seleniumProxy = ClientUtil.createSeleniumProxy(proxy);

        FirefoxOptions options = new FirefoxOptions();
        options.setHeadless(true);
        options.addArguments("--disable-notifications");
        FirefoxProfile firefoxProfile = new FirefoxProfile();
        firefoxProfile.setPreference("media.volume_scale", "0.0");
        options.setProfile(firefoxProfile);

        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability(CapabilityType.PROXY, seleniumProxy);
        capabilities.setCapability("acceptInsecureCerts",true);
        capabilities.setCapability(FirefoxOptions.FIREFOX_OPTIONS, options);
        System.setProperty(FirefoxDriver.SystemProperty.DRIVER_USE_MARIONETTE,"true");
        System.setProperty(FirefoxDriver.SystemProperty.BROWSER_LOGFILE,"/dev/null");
        WebDriver driver = new FirefoxDriver(capabilities);


        proxy.enableHarCaptureTypes(CaptureType.REQUEST_CONTENT, CaptureType.RESPONSE_CONTENT);

        proxy.newHar("Solarmovies");
        proxy.blacklistRequests("https://stream-3-2.loadshare.org/custom/VideoID-siI01lMn/sol480.mp4", 404);
        proxy.blacklistRequests("https://stream-2-2.loadshare.org/custom/VideoID-siI01lMn/sol480.mp4", 404);
        proxy.blacklistRequests("https://stream-1-2.loadshare.org/custom/VideoID-siI01lMn/sol480.mp4", 404);
        this.proxy = proxy;

        this.driver = driver;



//
//        DesiredCapabilities capabilities = new DesiredCapabilities();
//        capabilities.setCapability(CapabilityType.PROXY, seleniumProxy);
//
//        FirefoxBinary firefoxBinary = new FirefoxBinary();
//        firefoxBinary.addCommandLineOptions("--headless");
//
//        FirefoxOptions firefoxOptions = new FirefoxOptions();
//        firefoxOptions.setBinary(firefoxBinary);
//
//
//        DesiredCapabilities cap = DesiredCapabilities.firefox();
//
//        cap.setCapability(FirefoxOptions.FIREFOX_OPTIONS, firefoxOptions);
//        cap.setCapability(CapabilityType.PROXY, seleniumProxy);
//
//        proxy.enableHarCaptureTypes(CaptureType.REQUEST_CONTENT, CaptureType.RESPONSE_CONTENT);
//

        this.driver = driver;
    }


    public String getMovieImage(String baseUrl) throws InterruptedException {

        driver.get(baseUrl);
        synchronized (driver)
        {
            driver.wait(300);
        }

        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);


        WebElement switchLabel = driver.findElement(By.cssSelector("#usefull_info"));
        String url = ((JavascriptExecutor)driver) .executeScript("return window.getComputedStyle(arguments[0], ':before').getPropertyValue('background-image');",switchLabel).toString();
        return url.replace("url(\"", "").replace("\")", "");
    }


    public VideoQuery queryVideo(String query) throws InterruptedException {
        //proxy.newHar();
        driver.get("https://solarmovie.id/");
        System.out.println("Video query: " + query);

        //if(proxy.getHar() == null) System.out.println("null");


        System.out.println("here");
        synchronized (driver)
        {
            driver.wait(300);
        }

        WebElement search = driver.findElement(By.id("fast-search"));
        search.click();
        search.sendKeys(query);
        synchronized (driver)
        {
            driver.wait(300);
        }
        String originalHandle = driver.getWindowHandle();

        //Do something to open new tabs

        for(String handle : driver.getWindowHandles()) {
            if (!handle.equals(originalHandle)) {
                driver.switchTo().window(handle);
                driver.close();
            }
        }

        driver.switchTo().window(originalHandle);

        synchronized (driver)
        {
            driver.wait(1000);
        }

        List<WebElement> webElements = driver.findElements(By.className("search-point"));
        if(webElements == null || webElements.size() == 0){
            driver.close();
            System.out.println("not found!");
            return null;
        }
        String url = webElements.get(0).getAttribute("href");
        String name = webElements.get(0).getAttribute("title");
        name = name.split(" : ")[0];
        if(isQuery){
            proxy.stop();
            driver.close();
        }



        return new VideoQuery(name, url);
    }



    public List<String> getEpisodeLinks(String url) throws InterruptedException {
        driver.get(url);
        synchronized (driver)
        {
            driver.wait(300);
        }

        WebElement listContainer = driver.findElement(By.id("episodes-select-pane"));
        List<WebElement> lis = listContainer.findElements(By.tagName("li"));
        List<String> urls = new ArrayList<>();
        for(WebElement li : lis){
            System.out.println();
            String id = li.getAttribute("data-ep-id");
            String link = url + "/" + id;
            System.out.println(li.getText() + ":" + link);
            urls.add(link);
        }
        proxy.stop();
        driver.close();
        return urls;
    }

    public SeriesQuery querySeries(String query) throws InterruptedException {
        //proxy.newHar();
        driver.get("https://solarmovie.id/");
        System.out.println("Video query: " + query);

        //if(proxy.getHar() == null) System.out.println("null");


        System.out.println("here");
        synchronized (driver)
        {
            driver.wait(300);
        }

        WebElement search = driver.findElement(By.id("fast-search"));
        search.click();
        search.sendKeys(query);
        synchronized (driver)
        {
            driver.wait(300);
        }
        String originalHandle = driver.getWindowHandle();

        //Do something to open new tabs

        for(String handle : driver.getWindowHandles()) {
            if (!handle.equals(originalHandle)) {
                driver.switchTo().window(handle);
                driver.close();
            }
        }

        driver.switchTo().window(originalHandle);

        synchronized (driver)
        {
            driver.wait(1000);
        }

        List<WebElement> webElements = driver.findElements(By.className("search-point"));
        if(webElements == null || webElements.size() == 0){
            driver.close();
            System.out.println("not found!");
            return null;
        }
        String url = webElements.get(0).getAttribute("href");
        String name = webElements.get(0).getAttribute("title");
        name = name.split(" : ")[0];
        System.out.println("Name: " + name + ", Url: " + url);
        if(isQuery){
            proxy.stop();
            driver.close();
        }

        return new SeriesQuery(url, name, this);
    }

    public  String captureScreenshot (WebDriver driver, String screenshotName){

        try {
            TakesScreenshot ts = (TakesScreenshot)driver;
            File source = ts.getScreenshotAs(OutputType.FILE);
            String dest = "C:\\Users\\James\\IdeaProjects\\meraynorjames\\" + screenshotName + ".png";
            File destination = new File(dest);
            FileUtils.copyFile(source, destination);
            return dest;
        }

        catch (IOException e) {return e.getMessage();}
    }

    public  String getMovieUrl(String baseUrl) throws InterruptedException {
        driver.get(baseUrl);
        synchronized (driver)
        {
            driver.wait(300);
        }



        driver.findElement(By.className("selected-box-title")).click();
        synchronized (driver)
        {
            driver.wait(300);
        }
        driver.findElement(By.xpath("//li[@data-value='server_4']")).click();

        String originalHandle = driver.getWindowHandle();

        //Do something to open new tabs
//

        String url = getURL(driver);

        while(url.equalsIgnoreCase("NA")){
            url = getURL(driver);
            Thread.sleep(500);
        }

        for(String handle : driver.getWindowHandles()) {
            if (!handle.equals(originalHandle)) {
                driver.switchTo().window(handle);
                driver.close();
            }
        }
        driver.switchTo().window(originalHandle);

        driver.close();
        return url;
    }

    public  String getGenre(String baseUrl) throws InterruptedException {
        driver.get(baseUrl);
        synchronized (driver)
        {
            driver.wait(300);
        }


        WebElement genreList = driver.findElement(By.className("v"));
        List<WebElement> allFormChildElements = genreList.findElements(By.xpath("*"));
        return allFormChildElements.get(0).getText();
    }

    public  String getName(String baseUrl) throws InterruptedException {
        driver.get(baseUrl);
        synchronized (driver)
        {
            driver.wait(300);
        }
        WebElement genreList = driver.findElement(By.className("movie_title"));
        List<WebElement> allFormChildElements = genreList.findElements(By.xpath("*"));
        return allFormChildElements.get(0).getText();
    }


    private  String getURL(WebDriver driver) throws InterruptedException {
        List<HarEntry> entries = proxy.getHar().getLog().getEntries();

        for (HarEntry entry : entries) {
            if(entry.getRequest().getUrl().contains("/stream2/")){
                System.out.println("Found: " + entry.getRequest().getUrl());
                return entry.getRequest().getUrl();
            }
        }
        return "NA";
    }

}

