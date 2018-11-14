package me.raynorjames;

import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class VideoManager implements Runnable{
    private String query;
    private boolean isQuery;
    private VideoQuery videoQuery;
    private VideoCompleted videoCompleted;
    private Video video;
    private long videoTimeout;
    private boolean isRunning = false;
    private boolean usingGenre = false;
    private String genre;
    private static List<String> alreadyStarted = new ArrayList<>();
    private String url;
    private boolean isUrl;
    private int index;

    public VideoManager(int index, String query, boolean isQuery, long videoTimeout, String genre, VideoCompleted videoCompletedCB){
        if(!alreadyStarted.contains(query)){
            this.index = index;
            this.query = query;
            this.isQuery = isQuery;
            this.videoTimeout = videoTimeout;
            Thread thread = new Thread(this);
            this.videoCompleted = videoCompletedCB;
            usingGenre = true;
            this.genre = genre;
            thread.start();
            System.out.println("Starting: " + query);
            alreadyStarted.add(query);
            isUrl = false;
        }else{
            System.out.println("Tried to start " + query + " again, but was blocked.");
        }
    }




    public VideoManager(int index, String query, boolean isQuery, long videoTimeout, VideoCompleted videoCompletedCB){
        if(!alreadyStarted.contains(query)){
            this.index = index;
            this.query = query;
            this.isQuery = isQuery;
            this.videoTimeout = videoTimeout;
            Thread thread = new Thread(this);
            this.videoCompleted = videoCompletedCB;
            usingGenre = false;
            thread.start();
            System.out.println("Starting: " + query);
            alreadyStarted.add(query);
            isUrl = false;

        }else{
            System.out.println("Tried to start " + query + " again, but was blocked.");
        }
    }




    public VideoManager(int index, String url, long videoTimeout, VideoCompleted videoCompleted){
        this.index = index;
        isUrl = true;
        this.url = url;
        this.query = url;
        this.videoTimeout = videoTimeout;
        Thread thread = new Thread(this);
        this.videoCompleted = videoCompleted;
        usingGenre = false;
        alreadyStarted.add(url);
        usingGenre = false;
        thread.start();
    }
    public VideoManager(int index, String url, long videoTimeout, VideoCompleted videoCompleted, String genre){
        this.index = index;
        isUrl = true;
        this.url = url;
        this.query = url;
        this.videoTimeout = videoTimeout;
        Thread thread = new Thread(this);
        this.videoCompleted = videoCompleted;
        usingGenre = true;
        alreadyStarted.add(url);
        this.genre = genre;
        thread.start();
    }

    @Override
    public void run() {
        isRunning = true;
        if(!isUrl){
            try {
                this.videoQuery = new VideoQuery(query, isQuery);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            } finally {
                if(videoQuery.didFindVideo())
                    videoCompleted.queryCompleted(videoQuery);
                else
                    videoCompleted.queryNotFound(query);
            }
            if(!isQuery && videoQuery.didFindVideo()){
                try {
                    video = videoQuery.getVideo();
                    if(index != -5)
                        video.setIndex(index);
                } catch (Exception e) {
                    System.out.println("Exception with video '" + query + "'");
                    e.printStackTrace();

                }finally {
                    if(video.getUrl().equalsIgnoreCase(("NOT_FOUND"))) videoCompleted.mp4NotFound(this);

                    else{
                        if(usingGenre)
                            video.setGenre(genre);

                        videoCompleted.videoCompleted(video);
                    }
                }
            }
            isRunning = false;
        }else{
            try {
                Video redownloaded = new Video(this.url, new SolarGrabber(false, "NA"));
                if(index != -5)
                    redownloaded.setIndex(index);
                System.out.println(redownloaded.toString());
                if(usingGenre)
                    redownloaded.setGenre(genre);
                videoCompleted.videoCompleted(redownloaded);
                isRunning = false;
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    public String getQuery() {
        return query;
    }
}
