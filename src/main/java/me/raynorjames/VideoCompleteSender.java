package me.raynorjames;

import io.socket.client.Socket;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class VideoCompleteSender implements VideoCompleted {
    private Socket socket;
    private int index;
    public static List<Video> completedVideos = new ArrayList<>();
    private static int totalCompleted = 0;
    private int total;
    public static boolean shouldStop = false;
    public VideoCompleteSender(Socket socket, int index, int total){
        this.socket = socket;
        this.index = index;
        this.total = total;
    }

    @Override
    public void queryCompleted(VideoQuery videoQuery) {
        System.out.println("Finished query: " + videoQuery.getName());
    }


    private JSONObject getBulkVideoJson(){
        JSONObject videoObj = new JSONObject();
        try {
            videoObj.put("bulk", completedVideos);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return videoObj;
    }

    @Override
    public void videoCompleted(Video video) {
        if(index == -5){
            socket.emit("video", ConnectionManager.getVideoJson(video));
            System.out.println("Sending: " + video.toString());
            System.out.println("video sent");
            System.exit(0);
        }else{
            totalCompleted++;
            System.out.println(totalCompleted + "/" + total);
            boolean present = false;
            for(Video v : completedVideos){
                if(v.getName().equals(video.getName())) present = true;
            }
            if(!present){
                completedVideos.add(video);
            }
            if(totalCompleted == total){
                socket.emit("bulkvideo", getBulkVideoJson());
                shouldStop = true;
                System.exit(0);
            }
        }
    }

    @Override
    public void queryNotFound(String query) {
        socket.emit("not_found");
        System.out.println("query not found");
    }

    @Override
    public void mp4NotFound(VideoManager videoManager) {
        socket.emit("not_found");
        System.out.println("mp4 not found");
    }
}
