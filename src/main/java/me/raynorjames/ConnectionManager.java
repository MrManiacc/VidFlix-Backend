package me.raynorjames;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class ConnectionManager {
    private Socket socket;

    public Socket getSocket() {
        return socket;
    }

    public ConnectionManager(int port) throws URISyntaxException {
        socket = IO.socket("http://localhost:" + port);
        socket.on("query", new Emitter.Listener() {

            @Override
            public void call(Object... objects) {
                try {
                    JSONObject query = new JSONObject(objects[0].toString());
                    onQueryReceived(query.get("query").toString(), query.get("genre").toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
        socket.on("redl", new Emitter.Listener() {

            @Override
            public void call(Object... objects) {

                onRedownloadReceieved((String) objects[0]);

            }
        });

        socket.on("series", new Emitter.Listener() {

            @Override
            public void call(Object... objects) {

                try {
                    JSONObject query = new JSONObject(objects[0].toString());
                    onTvSeriesReceived(query.get("query").toString(), query.get("genre").toString());
                  // onTvSeriesReceived((String) objects[0], (String) objects[1]);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });


        socket.connect();
    }

    private void onTvSeriesReceived(String url, String genre) throws InterruptedException, URISyntaxException, UnknownHostException {
        SolarGrabber solarGrabber = new SolarGrabber(false, 10000L);
        SeriesQuery seriesQuery = solarGrabber.querySeries(url);
        if(genre.equals(""))
            seriesQuery.start(socket);
        else{
            seriesQuery.start(socket, genre);
        }
    }

    public static JSONObject getVideoJson(Video video){
        JSONObject videoObj = new JSONObject();
        try {
            videoObj.put("genre", video.getGenre());
            videoObj.put("name", video.getName());
            videoObj.put("img", video.getImg());
            videoObj.put("mp4", video.getUrl());
            videoObj.put("baseUrl", video.getBaseUrl());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return videoObj;
    }

    private void onRedownloadReceieved(String url){
        System.out.println(url);
        VideoManager videoManager = new VideoManager(url, 13000, new VideoCompleted() {
            @Override
            public void queryCompleted(VideoQuery videoQuery) {

            }

            @Override
            public void videoCompleted(Video video) {
                System.out.println(video.toString());
                socket.emit("redownload", getVideoJson(video));
            }

            @Override
            public void queryNotFound(String query) {

            }

            @Override
            public void mp4NotFound(VideoManager videoManager) {

            }
        });
    }

    private void onQueryReceived(String query, String genre){
        boolean useGenre = false;
        if(!genre.equals("")) useGenre = true;
        System.out.println(genre);
        String[] split = query.split("\\+");
        String[] sortedQueiers = new String[split.length];

        if(split.length > 1){
            List<String> queries = new ArrayList<>();
            for(int i = 0; i < split.length; i++){
                String normalized = split[i].replace("\\+", "");
                System.out.println(normalized);
                queries.add(normalized);
                sortedQueiers[i] = normalized;
            }
            for(String s: sortedQueiers) System.out.println("Query: " + s);



            QueueManager queueManager = null;
            if(useGenre){
                queueManager= new QueueManager(queries, socket, genre, false);

            }else{
                queueManager= new QueueManager(queries, socket, false);
            }
            queueManager.start();
        }else{
            new VideoManager(query, false, 13000, new VideoCompleteSender(socket, -5, 1));
        }

    }

}
