package me.raynorjames;

import io.socket.client.Socket;

import java.util.ArrayList;
import java.util.List;

public class QueueManager implements Runnable{
    private List<VideoManager> currentQueue = new ArrayList<>();
    private List<String> currentQueryQueue = new ArrayList<>();
    private String[] queriesArray;
    private boolean[] activeQueries;

    private boolean running = false;
    private Socket socket;
    private String genre;
    private boolean usingGenre = false;
    private int total;
    private boolean isUrl = false;

    public QueueManager(List<String> queries, Socket socket, String genre, boolean isUrl){
        this.queriesArray = queries.toArray(new String[queries.size()]);
        this.activeQueries = new boolean[queriesArray.length];
        for(int i = 0; i < activeQueries.length; i++) activeQueries[i] = false;
        total = queries.size();
        this.socket = socket;
        this.genre = genre;
        this.isUrl = isUrl;
        usingGenre = true;
    }

    public QueueManager(List<String> queries, Socket socket, boolean isUrl){
        this.queriesArray = queries.toArray(new String[queries.size()]);
        this.activeQueries = new boolean[queriesArray.length];
        for(int i = 0; i < activeQueries.length; i++) activeQueries[i] = false;
        this.socket = socket;
        total = queries.size();
        this.isUrl = isUrl;
        usingGenre = false;
    }




    public void start(){
        Thread thread = new Thread(this);
        running = true;
        thread.start();
    }

    private int findIndexByName(String name){
        for(int i = 0; i < queriesArray.length; i++){
            if(queriesArray[i].equals(name)){
                return i;
            }
        }

        return -1;
    }

    private boolean isActiveByName(String name){
        String query = queriesArray[findIndexByName(name)];
        return !query.equals("DONE");
    }

    private boolean isActiveByIndex(int index){
        String query = queriesArray[index];
        return !query.equals("DONE");
    }

    private boolean lastStared = false;
    @Override
    public void run() {
        while(running) {

            if(!isUrl){
                int totalRunning = 0;
                //Checks how many are running, and if one of them is finished it will be removed from the current queue
                //Also removes from queries list when does
                for (int j = 0; j < currentQueue.size(); j++) {
                    VideoManager videoManager = currentQueue.get(j);
                    if (videoManager.isRunning()) totalRunning++;
                    else {
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (!videoManager.isRunning()) {
//                        queriesArray[findIndexByName(currentQueue.get(j).getQuery())] = "DONE";
                            currentQueue.remove(j);
                            System.out.println("Removed: " + videoManager.getQuery());
                        }
                    }
                }

                if (totalRunning < 2) {
                    int lastIndex = 0;
                    for (int i = 0; i < queriesArray.length; i++) {
                        if (!activeQueries[i]) {
                            lastIndex = i;
                            //Should print out the last index with done name
                        }
                    }


                    if (!lastStared) {

                        System.out.println(lastIndex);
                        System.out.println(lastIndex + ":" + queriesArray[lastIndex] + ":" + activeQueries[lastIndex]);
                        if (lastIndex == 0) {
                            lastStared = true;
                        }
                        if (usingGenre) {
                            System.out.println("USING GENRE");
                            VideoManager videoManager = new VideoManager(queriesArray[lastIndex], false, 13000, genre, new VideoCompleteSender(socket, lastIndex, total));
                            currentQueue.add(videoManager);
                            activeQueries[lastIndex] = true;
                        } else {
                            System.out.println("NOT USING GENRE");
                            VideoManager videoManager = new VideoManager(queriesArray[lastIndex], false, 13000, new VideoCompleteSender(socket, lastIndex, total));
                            currentQueue.add(videoManager);
                            activeQueries[lastIndex] = true;
                        }

                    }
                }

                if(VideoCompleteSender.shouldStop)
                    running = false;

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }else{
                int totalRunning = 0;
                //Checks how many are running, and if one of them is finished it will be removed from the current queue
                //Also removes from queries list when does
                for (int j = 0; j < currentQueue.size(); j++) {
                    VideoManager videoManager = currentQueue.get(j);
                    if (videoManager.isRunning()) totalRunning++;
                    else {
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (!videoManager.isRunning()) {
//                        queriesArray[findIndexByName(currentQueue.get(j).getQuery())] = "DONE";
                            currentQueue.remove(j);
                            System.out.println("Removed: " + videoManager.getQuery());
                        }
                    }
                }

                if (totalRunning < 2) {
                    for (int i = 0; i < activeQueries.length; i++) {
                        System.out.println(i + ":" + activeQueries[i]);
                    }
                    int lastIndex = 0;
                    for (int i = 0; i < queriesArray.length; i++) {
                        if (!activeQueries[i]) {
                            lastIndex = i;
                            //Should print out the last index with done name
                        }
                    }


                    if (!lastStared) {
                        if (lastIndex == 0) {
                            lastStared = true;
                        }
                            VideoManager videoManager = new VideoManager(queriesArray[lastIndex], 13000, new VideoCompleteSender(socket, lastIndex, total));
                            currentQueue.add(videoManager);
                            activeQueries[lastIndex] = true;
                    }
                }


                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
