/*
 *
 * Copyright 2014 Jules White
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.magnum.dataup;

import org.apache.commons.io.IOUtils;
import org.magnum.dataup.model.Video;
import org.magnum.dataup.model.VideoStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import retrofit.http.Streaming;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Collection;

@Controller
public class VideoController {

    /**
     * Sina sample
     * You will need to create one or more Spring controllers to fulfill the
     * requirements of the assignment. If you use this file, please rename it
     * to something other than "AnEmptyController"
     * <p>
     * <p>
     * ________  ________  ________  ________          ___       ___  ___  ________  ___  __
     * |\   ____\|\   __  \|\   __  \|\   ___ \        |\  \     |\  \|\  \|\   ____\|\  \|\  \
     * \ \  \___|\ \  \|\  \ \  \|\  \ \  \_|\ \       \ \  \    \ \  \\\  \ \  \___|\ \  \/  /|_
     * \ \  \  __\ \  \\\  \ \  \\\  \ \  \ \\ \       \ \  \    \ \  \\\  \ \  \    \ \   ___  \
     * \ \  \|\  \ \  \\\  \ \  \\\  \ \  \_\\ \       \ \  \____\ \  \\\  \ \  \____\ \  \\ \  \
     * \ \_______\ \_______\ \_______\ \_______\       \ \_______\ \_______\ \_______\ \__\\ \__\
     * \|_______|\|_______|\|_______|\|_______|        \|_______|\|_______|\|_______|\|__| \|__|
     */
    @Autowired
    private VideRepo videRepo;

    @RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH,
            method = RequestMethod.GET)
    @ResponseBody
    public Collection<Video> getVideoList(HttpServletResponse response) {
        response.setContentType(org.springframework.http.MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_OK);
        return videRepo.getVideos();
    }

    @RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH, method = RequestMethod.POST)
    @ResponseBody
    public Video addVideo(@RequestBody Video video) {
        return videRepo.save(video);
    }

    @RequestMapping(value = VideoSvcApi.VIDEO_DATA_PATH, method = RequestMethod.POST,
            consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE
    )
    public @ResponseBody
    VideoStatus setVideoData(@PathVariable(VideoSvcApi.ID_PARAMETER) Long id,
                             @RequestPart(VideoSvcApi.DATA_PARAMETER) MultipartFile data,
                             HttpServletResponse response
    ) {

        VideoStatus videoStatus = new VideoStatus(VideoStatus.VideoState.PROCESSING);
        Video video = videRepo.getVideo(id);
        if (video != null) {
            videRepo.saveVideo(video, data);
            videoStatus = new VideoStatus(VideoStatus.VideoState.READY);
        } else {
            videoStatus = new VideoStatus(VideoStatus.VideoState.PROCESSING);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
        return videoStatus;
    }

    @Streaming
    @RequestMapping(value = VideoSvcApi.VIDEO_DATA_PATH,
            method = RequestMethod.GET)
    public void getData(@PathVariable(VideoSvcApi.ID_PARAMETER) long id,
                        HttpServletResponse response) throws IOException {
        Video video = videRepo.getVideo(id);
        if (video == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } else {
            if (response.getContentType() == null) {
                response.setContentType("video/mp4");
            }
            try {
                videRepo.copyVideoData(video, response.getOutputStream());
            } catch (Exception e) {
            }
        }
    }
}
