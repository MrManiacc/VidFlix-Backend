package me.raynorjames;

import io.socket.client.Socket;

import java.util.List;

public class SeriesQuery {
    private String url, name;
    private SolarGrabber solarGrabber;
    private List<String> urls;

    public SeriesQuery(String url, String name, SolarGrabber solarGrabber){
        this.url = url;
        this.solarGrabber = solarGrabber;
        try {
            this.urls = solarGrabber.getEpisodeLinks(url);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void start(Socket socket, String genre){
        QueueManager queueManager= new QueueManager(urls, socket, genre, true);
        queueManager.start();
    }


    public void start(Socket socket){
        QueueManager queueManager= new QueueManager(urls, socket, true);
        queueManager.start();
    }

}
