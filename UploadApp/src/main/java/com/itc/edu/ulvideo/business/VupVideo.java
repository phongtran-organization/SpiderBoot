/*
 * Copyright (c) 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.itc.edu.ulvideo.business;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatus;
import com.google.common.collect.Lists;
import com.itc.edu.ulvideo.util.Config;
import com.itc.edu.ulvideo.util.Constant;
import com.itc.edu.ulvideo.util.FileUtils;
import java.io.File;
import java.io.FileInputStream;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Upload a video to the authenticated user's channel. Use OAuth 2.0 to
 * authorize the request. Note that you must add your video files to the project
 * folder to upload them with this application.
 *
 * @author Jeremy Walker
 */
public class VupVideo {

    private static final Logger logger = Logger.getLogger(VupVideo.class);
    /**
     * Define a global variable that specifies the MIME type of the video being
     * uploaded.
     */
    private static final String VIDEO_FILE_FORMAT = Config.videoFormat;

    //private static final String SAMPLE_VIDEO_FILENAME = "sample-video.mp4";
    /**
     * Define a global instance of a Youtube object, which will be used to make
     * YouTube Data API requests.
     */
    private YouTube youtube;

    public VupVideo(VupVideo vupVideo) {
        List<String> scopes = Lists.newArrayList(Config.authLink);
        try {
            Credential credential = Auth.authorize(scopes, "uploadvideo");
            this.youtube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, credential).setApplicationName(
                    "uploadvideo").build();
        } catch (Exception ex) {
            logger.error("Inital vUpVideo|", ex);
        }
    }

    /**
     * Upload the user-selected video to the user's YouTube channel. The code
     * looks for the video in the application's project folder and uses OAuth
     * 2.0 to authorize the API request.
     *
     * @param args command line args (not used).
     */
    public boolean uploadFile(String srcFile) {

        boolean bresult = false;
        int retry = 0;
        // This OAuth 2.0 access scope allows an application to upload files
        // to the authenticated user's YouTube channel, but doesn't allow
        // other types of access.

        List<String> scopes = Lists.newArrayList(Config.authLink);
        logger.info("Start upload file " + srcFile);
        do {
            long startTime = System.currentTimeMillis();
            if (retry > 0) {
                logger.info("Retry upload file " + srcFile);
            }
            try {
                // Authorize the request.
                Credential credential = Auth.authorize(scopes, "uploadvideo");

                // This object is used to make YouTube Data API requests.
                youtube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, credential).setApplicationName(
                        "uploadvideo").build();

                logger.info("Uploading: " + srcFile);

                // Add extra information to the video before uploading.
                Video videoObjectDefiningMetadata = new Video();

                // Set the video to be publicly visible. This is the default
                // setting. Other supporting settings are "unlisted" and "private."
                VideoStatus status = new VideoStatus();
                status.setPrivacyStatus("public");
                videoObjectDefiningMetadata.setStatus(status);

                // Most of the video's metadata is set on the VideoSnippet object.
                VideoSnippet snippet = new VideoSnippet();

                // This code uses a Calendar instance to create a unique name and
                // description for test purposes so that you can easily upload
                // multiple files. You should remove this code from your project
                // and use your own standard names instead.
                Calendar cal = Calendar.getInstance();
                snippet.setTitle("Test Upload via Java on " + cal.getTime());
                snippet.setDescription(
                        "Video uploaded via YouTube Data API V3 using the Java library " + "on " + cal.getTime());

                // Set the keyword tags that you want to associate with the video.
                List<String> tags = new ArrayList<String>();
                tags.add("test");
                tags.add("example");
                tags.add("java");
                tags.add("YouTube Data API V3");
                tags.add("erase me");
                snippet.setTags(tags);

                // Add the completed snippet object to the video resource.
                videoObjectDefiningMetadata.setSnippet(snippet);
                InputStream input = new FileInputStream(srcFile);
                InputStreamContent mediaContent = new InputStreamContent(VIDEO_FILE_FORMAT,input);

                // Insert the video. The command sends three arguments. The first
                // specifies which information the API request is setting and which
                // information the API response should return. The second argument
                // is the video resource that contains metadata about the new video.
                // The third argument is the actual video content.
                YouTube.Videos.Insert videoInsert = youtube.videos()
                        .insert("snippet,statistics,status", videoObjectDefiningMetadata, mediaContent);

                // Set the upload type and add an event listener.
                MediaHttpUploader uploader = videoInsert.getMediaHttpUploader();

                // Indicate whether direct media upload is enabled. A value of
                // "True" indicates that direct media upload is enabled and that
                // the entire media content will be uploaded in a single request.
                // A value of "False," which is the default, indicates that the
                // request will use the resumable media upload protocol, which
                // supports the ability to resume an upload operation after a
                // network interruption or other transmission failure, saving
                // time and bandwidth in the event of network failures.
                uploader.setDirectUploadEnabled(false);
                bresult = true;
                MediaHttpUploaderProgressListener progressListener = new MediaHttpUploaderProgressListener() {
                    public void progressChanged(MediaHttpUploader uploader) throws IOException {
                        switch (uploader.getUploadState()) {
                            case INITIATION_STARTED:
                                logger.info("Initiation Started");
                                break;
                            case INITIATION_COMPLETE:
                                logger.info("Initiation Completed");
                                break;
                            case MEDIA_IN_PROGRESS:
                                logger.info("Upload in progress");
                                logger.info("Upload percentage: " + uploader.getProgress());
                                break;
                            case MEDIA_COMPLETE:
                                logger.info("Upload Completed!");
                                break;
                            case NOT_STARTED:
                                logger.info("Upload Not Started!");
                                break;
                        }
                    }
                };
                uploader.setProgressListener(progressListener);

                // Call the API and upload the video.
                Video returnedVideo = videoInsert.execute();
                bresult = true;
                /*
                // Print data about the newly inserted video from the API response.
                logger.info("\n================== Returned Video ==================\n");
                logger.info("  - Id: " + returnedVideo.getId());
                logger.info("  - Title: " + returnedVideo.getSnippet().getTitle());
                logger.info("  - Tags: " + returnedVideo.getSnippet().getTags());
                logger.info("  - Privacy Status: " + returnedVideo.getStatus().getPrivacyStatus());
                logger.info("  - Video Count: " + returnedVideo.getStatistics().getViewCount());
                */
            } catch (GoogleJsonResponseException e) {
                logger.error("GoogleJsonResponseException code: " + e.getDetails().getCode() + " : "
                        + e.getDetails().getMessage());
                e.printStackTrace();
                bresult = false;
            } catch (IOException e) {
                logger.error("IOException: " + e.getMessage());
                e.printStackTrace();
                bresult = false;
            } catch (Throwable t) {
                logger.error("Throwable: " + t.getMessage());
                t.printStackTrace();
                bresult = false;
            } finally {
                retry++;
                if (bresult) {
                    long millis = (System.currentTimeMillis() - startTime);
                    logger.info("Upload complete| " + srcFile + "|time: " + String.format("%02d min, %02d sec",
                            TimeUnit.MILLISECONDS.toMinutes(millis),
                            TimeUnit.MILLISECONDS.toSeconds(millis)
                            - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
                    ));
                }
            }
        } while (!bresult && retry <= Config.uploadRetry);
        return bresult;

    }

}