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
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

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




        List<String> allowUrlPatterns = new ArrayList<String>();
        allowUrlPatterns.add("https?://.*(solarmovie.id)+.*");
        allowUrlPatterns.add("https?://.*(stream-3-2.loadshare.org)+.*");
        allowUrlPatterns.add("https?://.*(stream-2-2.loadshare.org/)+.*");
        allowUrlPatterns.add("https?://.*(stream-1-2.loadshare.org/)+.*");
        // All the URLs that are not from our sites are blocked and a status code of 403 is returned

        proxy.whitelistRequests(allowUrlPatterns, 404);

        FirefoxOptions options = new FirefoxOptions();
        options.setHeadless(true);

        FirefoxProfile firefoxProfile = new FirefoxProfile();
        firefoxProfile.setPreference("media.volume_scale", "0.0");
        options.setProfile(firefoxProfile);

        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability(CapabilityType.PROXY, seleniumProxy);
        capabilities.setCapability("acceptInsecureCerts",true);
        capabilities.setCapability(FirefoxOptions.FIREFOX_OPTIONS, options);
        capabilities.setCapability("pageLoadStrategy", "eager");
        WebDriver driver = new FirefoxDriver(capabilities);

        proxy.enableHarCaptureTypes(CaptureType.REQUEST_CONTENT, CaptureType.RESPONSE_CONTENT);

        proxy.newHar("Solarmovies");
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
    public String getMovieImage(String baseUrl) throws InterruptedException {

        driver.get(baseUrl);
        synchronized (driver)
        {
            driver.wait(300);
        }

        driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);


        WebElement switchLabel = driver.findElement(By.cssSelector("#usefull_info"));
        String url = ((JavascriptExecutor)driver) .executeScript("return window.getComputedStyle(arguments[0], ':before').getPropertyValue('background-image');",switchLabel).toString();
        return url.replace("url(\"", "").replace("\")", "");
    }

    private WebElement getElement(WebDriver driver, By by){
        try{
            return driver.findElement(by);
        }catch (Exception e){
            try {
                Thread.sleep(51);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            return getElement(driver, by);
        }
    }


    private List<WebElement> getElements(WebDriver driver, By by){
        try{
            return driver.findElements(by);
        }catch (Exception e){
            try {
                Thread.sleep(50);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            System.out.println("Couldnt find, retrying...");
            return getElements(driver, by);
        }
    }

    public VideoQuery queryVideo(String query) throws InterruptedException {
        //proxy.newHar();
        driver.get("https://solarmovie.id/");
        System.out.println("in query video");



        WebElement search = getElement(driver, By.id("fast-search"));

        search.click();
        search.sendKeys(query);



//        synchronized (driver)
//        {
//            driver.wait(300);
//        }
//        String originalHandle = driver.getWindowHandle();
//
//        //Do something to open new tabs
//
//        for(String handle : driver.getWindowHandles()) {
//            if (!handle.equals(originalHandle)) {
//                driver.switchTo().window(handle);
//                driver.close();
//            }
//        }
//
//        driver.switchTo().window(originalHandle);

        List<WebElement> webElements = getElements(driver, By.className("search-point"));
        while(webElements.size() == 0){
            webElements = getElements(driver, By.className("search-point"));
            Thread.sleep(100);
            System.out.println("elements null... retrying");
        }
        String url = webElements.get(0).getAttribute("href");
        String name = webElements.get(0).getAttribute("title");
        name = name.split(" : ")[0];
        System.out.println(name + ":" + url);
        if(isQuery){
            proxy.stop();
            driver.close();
        }



        return new VideoQuery(name, url);
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
        System.out.println("getting mp4 for: " + baseUrl);
        synchronized (driver)
        {
            driver.wait(300);
        }

        getElement(driver, By.className("selected-box-title")).click();


        //WebDriverWait wait1 = new WebDriverWait(driver, 5);

        WebElement server =getElement(driver, By.xpath("//li[@data-value='server_4']"));

        //WebElement server = getElement(driver, By.xpath("//#select_serv/option[@value='server_4']"));

        try{
            server.click();
        }catch(Exception e){
//            WebElement server1 =getElement(driver, By.xpath("//li[@data-value='server_3']"));
//            System.out.println("using other server");
//            server1.click();
            Thread.sleep(2000);
        }


        String url = getURL(driver);

        while(url.equalsIgnoreCase("NA")){
            url = getURL(driver);
            Thread.sleep(500);
        }
        driver.close();
        return url;
    }

    public  String getGenre(String baseUrl) throws InterruptedException {
        driver.get(baseUrl);

        System.out.println("Getting genre for: " + baseUrl);

        WebElement genreList = getElement(driver,By.className("v"));
        List<WebElement> allFormChildElements = genreList.findElements(By.xpath("*"));
        return allFormChildElements.get(0).getText();
    }

    public  String getName(String baseUrl) throws InterruptedException {
        driver.get(baseUrl);
        System.out.println("Getting name for: " + baseUrl);
        WebElement genreList = getElement(driver, By.className("movie_title"));
        List<WebElement> allFormChildElements = genreList.findElements(By.xpath("*"));
        return allFormChildElements.get(0).getText();
    }


    private  String getURL(WebDriver driver) throws InterruptedException {
        List<HarEntry> entries = proxy.getHar().getLog().getEntries();
        for (HarEntry entry : entries) {
            if(entry.getRequest().getUrl().contains("/stream2/")){
                System.out.println("Found: " + entry.getRequest().getUrl());
                return entry.getRequest().getUrl();
            }else{
                System.out.println("Haven't found url yet...");
            }
        }
        return "NA";
    }

}

