package me.raynorjames;

import java.net.URISyntaxException;
import java.net.UnknownHostException;

public class VideoQuery {
    private String name;
    private String link;
    private SolarGrabber solarGrabber;
    private Video video;
    private long timePasseed = 0L;
    private boolean foundVideo = false;
    public VideoQuery(String name, String link) {
        this.name = name;
        this.link = link;
    }

    public VideoQuery(String query, boolean isQuery) throws InterruptedException, UnknownHostException, URISyntaxException {
        long startTime = System.currentTimeMillis();
        this.solarGrabber = new SolarGrabber(isQuery, query);
        VideoQuery data = solarGrabber.queryVideo(query);
        System.out.println("here2");
        foundVideo = true;
        this.name = data.name;
        this.link = data.link;
        long stopTime = System.currentTimeMillis();
        timePasseed = stopTime - startTime;

    }


    public boolean didFindVideo() {
        return foundVideo;
    }

    public Video getVideo() throws InterruptedException {
        return new Video(link, solarGrabber);
    }

    public String getName() {
        return name;
    }

    public String getLink() {
        return link;
    }

    public String toString(){
        return "{\"name\": \"" + name + "\", \"link\": \"" + link + "\"}";
    }
}
