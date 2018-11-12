package me.raynorjames;

public class Video {
    private String name;
    private String url;
    private String baseUrl;
    private String img;
    private String genre;
    private SolarGrabber solarGrabber;

    public Video(String data, SolarGrabber solarGrabber) throws InterruptedException {
        this.solarGrabber = solarGrabber;
        this.baseUrl = data;
        grabMovieInfo(data + "-watch-online-free.html");
    }


    private void grabMovieInfo(String baseUrl) throws InterruptedException {
        long start = System.currentTimeMillis();
        this.name = solarGrabber.getName(baseUrl);
        this.genre = solarGrabber.getGenre(baseUrl);
        this.img = solarGrabber.getMovieImage(baseUrl);
        this.url = solarGrabber.getMovieUrl(baseUrl);
        if(!this.url.contains("https://")) this.url = "NOT_FOUND";
        long stop = System.currentTimeMillis();
        float time  = (stop - start) / 1000.0F;
        System.out.println("Done in: " + time + "s");
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getImg() {
        return img;
    }

    public String getGenre() {
        return genre;
    }

    public String getBaseUrl(){return baseUrl; }

    @Override
    public String toString() {
        return "{" +
                "\"genre\" : " + "\"" + genre + "\"," +
                "\"name\" : " + "\"" + name + "\"," +
                "\"img\" : " + "\"" + img + "\"," +
                "\"mp4\" : " + "\"" + url + "\"," +
                "\"baseUrl\" : " + "\"" + baseUrl + "\"" +
                "}";
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }
}
