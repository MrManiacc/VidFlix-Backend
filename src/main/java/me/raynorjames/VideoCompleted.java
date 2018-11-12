package me.raynorjames;

public interface VideoCompleted {

    void queryCompleted(VideoQuery videoQuery);
    void videoCompleted(Video video);
    void queryNotFound(String query);
    void mp4NotFound(VideoManager videoManager);
}
