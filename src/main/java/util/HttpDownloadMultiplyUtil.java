package util;

import entity.Video;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import sys.SysConst;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author: wtl
 * @License: (C) Copyright 2020
 * @Contact: 1050100468@qq.com
 * @Date: 2020/8/13 6:39
 * @Version: 1.0
 * @Description:
 */
public class HttpDownloadMultiplyUtil {

    private static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();

    private static final ThreadPoolExecutor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(5, AVAILABLE_PROCESSORS * 2 + 1,
            100, TimeUnit.MINUTES, new ArrayBlockingQueue<>(5), new ThreadPoolExecutor.CallerRunsPolicy());

    private static final ThreadPoolExecutor TASK_THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(1, 1,
            100, TimeUnit.MINUTES, new ArrayBlockingQueue<>(1), new ThreadPoolExecutor.CallerRunsPolicy());

    public static void download(VBox contentVBox, VBox downloadVBox, String path) {
        List<Video> videoList = (List<Video>) contentVBox.getUserData();
        AtomicReference<Integer> index = new AtomicReference<>(0);
        videoList.forEach(video -> {
            HBox hBox = new HBox();
            hBox.prefWidthProperty().bind(downloadVBox.widthProperty());
            hBox.getStyleClass().add("downloadHBox");

            index.getAndSet(index.get() + 1);

            Label indexLabel = new Label(String.valueOf(index.get()));
            Label fileNameLabel = new Label(video.getPartName());
            Label progressLabel = new Label("0%");
            Label fileSizeLabel = new Label("0");
            Label speedLabel = new Label("0KB/S");
            indexLabel.setId("downloadPageLabel");
            fileNameLabel.setId("downloadPageLabel");
            fileNameLabel.setUserData(video.getUrl());
            fileNameLabel.setStyle("-fx-pref-width: 200px;-fx-alignment: center_left");
            fileSizeLabel.setId("downloadPageLabel");
            progressLabel.setId("downloadPageLabel");
            speedLabel.setId("downloadPageLabel");
            hBox.getChildren().addAll(indexLabel, fileNameLabel, progressLabel, fileSizeLabel, speedLabel);
            downloadVBox.getChildren().add(hBox);
        });

        Semaphore semaphore = new Semaphore(5);

        TASK_THREAD_POOL_EXECUTOR.execute(() -> {
            ObservableList<Node> children = downloadVBox.getChildren();
            Queue<Node> nodeQueue = new LinkedBlockingDeque<>();
            children.forEach(node -> {
                HBox hBox = (HBox) node;
                nodeQueue.add(node);
            });

            while (!nodeQueue.isEmpty()){
                THREAD_POOL_EXECUTOR.execute(() -> {
                    Label progressLabel = null;
                    try {
                        semaphore.acquire();

                        HBox hBox = (HBox) nodeQueue.remove();
                        Label fileNameLabel = (Label) hBox.getChildren().get(1);
                        Label fileSizeLabel = (Label) hBox.getChildren().get(2);
                        progressLabel = (Label) hBox.getChildren().get(3);
                        Label speedLabel = (Label) hBox.getChildren().get(4);
                        String url = (String) fileNameLabel.getUserData();


                        URLConnection urlConnection = new URL(url).openConnection();

                        Platform.runLater(() -> {
                            fileSizeLabel.setText(
                                    BigDecimal.valueOf(urlConnection.getContentLengthLong())
                                            .multiply(BigDecimal.valueOf(1.0))
                                            .divide(BigDecimal.valueOf(1024), RoundingMode.HALF_UP)
                                            .divide(BigDecimal.valueOf(1024), RoundingMode.HALF_UP)
                                            .setScale(3, RoundingMode.HALF_UP)
                                            .doubleValue() + "MB");
                        });
                        int length = -1;
                        byte[] buffer = new byte[10240];

                        FileOutputStream fileOutputStream = new FileOutputStream(path + "/" + fileNameLabel.getText() + ".mp4");
                        FileChannel channel = fileOutputStream.getChannel();
                        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(10240);

                        InputStream inputStream = urlConnection.getInputStream();

                        int currentSize = 0;
                        long totalSize = urlConnection.getContentLengthLong();

                        while ((length = inputStream.read(buffer)) != -1) {
                            long startTime = System.nanoTime();
                            byteBuffer.put(buffer, 0, length);
                            byteBuffer.flip();
                            channel.write(byteBuffer);
                            byteBuffer.clear();
                            long endTime = System.nanoTime();

                            //设置进度
                            currentSize += length;

                            String progress = BigDecimal.valueOf(currentSize)
                                    .multiply(BigDecimal.valueOf(100.0))
                                    .divide(BigDecimal.valueOf(totalSize), 3, BigDecimal.ROUND_HALF_UP)
                                    .doubleValue() + "%";
                            Label finalProgressLabel = progressLabel;
                            Platform.runLater(() -> {
                                finalProgressLabel.setText(progress);
                            });
                            BigDecimal bigDecimal = BigDecimal.valueOf(length)
                                    .multiply(BigDecimal.valueOf(1000.0))
                                    .divide(BigDecimal.valueOf(endTime - startTime), 3, RoundingMode.HALF_UP);
                            String speed = length / (endTime - startTime) >= 1024
                                    ?
                                    bigDecimal
                                            .divide(BigDecimal.valueOf(1024),3,RoundingMode.HALF_UP)
                                            .doubleValue() + "MB/s"
                                    :
                                    bigDecimal.doubleValue() + "KB/s";

                            Platform.runLater(() -> {
                                speedLabel.setText(speed);
                            });
                        }

                        channel.close();
                        fileOutputStream.close();
                        inputStream.close();

                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        Label finalProgressLabel1 = progressLabel;
                        Platform.runLater(() -> {
                            finalProgressLabel1.setText("100%");
                        });
                        semaphore.release();
                    }
                });
            }

            SysConst.showDialog(contentVBox,"视频已经下载完成！！！");
        });
    }
}
