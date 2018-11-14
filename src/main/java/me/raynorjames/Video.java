package me.raynorjames;

public class Video implements Comparable<Video>{
    private String name;
    private String url;
    private String baseUrl;
    private String img;
    private String genre;
    private SolarGrabber solarGrabber;
    private int index;

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


    public void setIndex(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

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

    @Override
    public int compareTo(Video other) {
        if(getIndex() < other.getIndex()) return -1;
        if(getIndex() > other.getIndex()) return 1;
        return 0;
    }
}
