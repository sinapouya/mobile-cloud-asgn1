package org.magnum.dataup;

import jdk.nashorn.internal.objects.annotations.Constructor;
import org.magnum.dataup.model.Video;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.servlet.ServletOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class VideRepo {
    private VideoFileManager videoFileManager;
    @PostConstruct
    public void init(){
        try {
            this.videoFileManager = VideoFileManager.get();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static final AtomicLong currentId = new AtomicLong(0L);

    private Map<Long, Video> videos = new HashMap<Long, Video>();

    public Video save(Video video) {
        checkAndSetId(video);
        video.setDataUrl(getDataUrl(video.getId()));
        videos.put(video.getId(), video);
        return video;
    }

    public void checkAndSetId(Video entity) {
        if(entity.getId() == 0){
            entity.setId(currentId.incrementAndGet());
        }
    }

    public String getDataUrl(long videoId){
        String url = "http://localhost:8080/video/" + videoId + "/data";
        return url;
    }

    public Collection<Video> getVideos() {
        return videos.values();
    }
    public Video getVideo(Long id) {
        return videos.get(id);
    }

    public void setVideos(Map<Long, Video> videos) {
        this.videos = videos;
    }

    public void saveVideo(Video v, MultipartFile videoData){
        try {
            this.videoFileManager.saveVideoData(v, videoData.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void copyVideoData(Video video, ServletOutputStream outputStream) {
        this.videoFileManager.copyVideoData(video,outputStream);
    }
}

