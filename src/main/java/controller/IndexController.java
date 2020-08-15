package controller;

import entity.Video;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import sys.SysConst;
import util.HttpDownloadMultiplyUtil;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author: wtl
 * @License: (C) Copyright 2020
 * @Contact: 1050100468@qq.com
 * @Date: 2020/8/10 6:00
 * @Version: 1.0
 * @Description:
 */
public class IndexController implements Initializable {

    private static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();

    private static final ThreadPoolExecutor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(5, AVAILABLE_PROCESSORS * 2 + 1,
            100, TimeUnit.MINUTES, new ArrayBlockingQueue<>(5), new ThreadPoolExecutor.CallerRunsPolicy());
    @FXML
    private AnchorPane rootAnchorPane;

    @FXML
    private FlowPane topFlowPane;

    @FXML
    private AnchorPane contentAnchorPane;

    @FXML
    private HBox topHBox;

    @FXML
    private HBox topHBoxSave;

    @FXML
    private TextField saveTextField;

    @FXML
    private ScrollPane contentScrollPane;

    @FXML
    private VBox contentVBox;

    @FXML
    private VBox fixedVBox;

    @FXML
    private AnchorPane getDownloadPage;

    @FXML
    private AnchorPane downloadPage;

    @FXML
    private ScrollPane downloadScrollPane;

    @FXML
    private VBox downloadVBox;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        topFlowPane.prefWidthProperty().bind(rootAnchorPane.widthProperty());
        contentAnchorPane.prefWidthProperty().bind(rootAnchorPane.widthProperty());
        topHBox.prefWidthProperty().bind(rootAnchorPane.widthProperty());
        topHBoxSave.prefWidthProperty().bind(rootAnchorPane.widthProperty());
        contentScrollPane.prefWidthProperty().bind(rootAnchorPane.widthProperty());
        contentScrollPane.prefHeightProperty().bind(rootAnchorPane.heightProperty().subtract(105));
        contentVBox.prefWidthProperty().bind(contentScrollPane.widthProperty());
        downloadPage.prefWidthProperty().bind(rootAnchorPane.widthProperty());
        downloadPage.prefHeightProperty().bind(rootAnchorPane.heightProperty().subtract(30));
        downloadScrollPane.prefWidthProperty().bind(downloadPage.widthProperty());
        downloadScrollPane.prefHeightProperty().bind(downloadPage.heightProperty());
        downloadVBox.prefWidthProperty().bind(downloadPage.widthProperty());
        ObservableList<Node> children = topFlowPane.getChildren();
        children.get(0).getStyleClass().add("focus");
        downloadPage.setVisible(false);
        children.forEach(node -> {
            Label label = (Label) node;
            label.setOnMouseClicked((mouseEvent) -> {
                children.get(0).getStyleClass().remove("focus");
                children.get(1).getStyleClass().remove("focus");
                label.getStyleClass().add("focus");

                if (label == children.get(0)) {
                    downloadPage.setVisible(false);
                    getDownloadPage.setVisible(true);
                } else {
                    downloadPage.setVisible(true);
                    getDownloadPage.setVisible(false);
                }
            });
        });

        fixedVBox.setVisible(false);

        contentVBox.setOnMouseClicked((mouseEvent) -> {
            if (MouseButton.PRIMARY == mouseEvent.getButton()) {
                fixedVBox.setVisible(false);
            }
        });

        fixedVBox.setOnMouseClicked((mouseEvent) -> {
            fixedVBox.setVisible(false);
            if (SysConst.EMPTY_STRING.equalsIgnoreCase(saveTextField.getText())) {
                SysConst.showDialog(rootAnchorPane, "请选择保存路径！！！");
            } else {
                HttpDownloadMultiplyUtil.download(contentVBox, downloadVBox, saveTextField.getText());
            }
        });
    }


    public void openChooseClickFunc(MouseEvent mouseEvent) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(new File("D:/"));
        directoryChooser.setTitle("请选择一个文件夹");
        File directory = directoryChooser.showDialog(rootAnchorPane.getScene().getWindow());

        if (null != directory) {
            saveTextField.setText(directory.getAbsolutePath());
        }
    }

    /**
     * 处理分析哔哩哔哩网址的结果
     *
     * @param mouseEvent mouseEvent
     */
    public void analyzeClickFunc(MouseEvent mouseEvent) throws Exception {
        TextField textField = (TextField) topHBox.getChildren().get(1);
        String url = textField.getText();
        List<Video> videoList = new ArrayList<>();
        if (!SysConst.EMPTY_STRING.equalsIgnoreCase(url)) {
            THREAD_POOL_EXECUTOR.execute(() -> {
                CloseableHttpClient httpClient = HttpClients.custom().build();
                String getBiliBiliVideoPageBVidUrl = null;
                String bVid = url.split("/")[4].split("\\?")[0];
                getBiliBiliVideoPageBVidUrl = "https://ljxwtl.cn/bilibili/getBiliBiliVideoPageBVids/" + bVid;


                HttpGet httpGet = new HttpGet(getBiliBiliVideoPageBVidUrl);
                CloseableHttpResponse response = null;
                JSONArray jsonArray = null;
                try {
                    response = httpClient.execute(httpGet);
                    String result = new String(EntityUtils.toByteArray(response.getEntity()));
                    jsonArray = JSONArray.fromObject(result);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (Objects.nonNull(jsonArray)) {
                    for (int i = 0; i < jsonArray.size(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        System.out.println(jsonObject);
                        HBox hBox = new HBox();
                        hBox.prefWidthProperty().bind(contentVBox.widthProperty());
                        hBox.setAlignment(Pos.CENTER_LEFT);
                        hBox.getStyleClass().add("item");
                        Label labelIndex = new Label(String.valueOf((i + 1)));
                        labelIndex.setPrefWidth(50);
                        labelIndex.setAlignment(Pos.CENTER);
                        Label labelFileName = new Label(jsonObject.getString("part"));
                        String cid = jsonObject.getString("cid");
                        videoList.add(Video.builder()
                                .url("https://ljxwtl.cn/bilibili/getVideoStream?cid=" + cid + "&bVid=" + bVid)
                                .partName(labelFileName.getText())
                                .cid(cid)
                                .bVid(bVid).build());
                        hBox.getChildren().addAll(labelIndex, labelFileName);

                        hBox.setOnMouseClicked((me) -> {
                            if (MouseButton.SECONDARY == me.getButton()) {
                                double sceneX = me.getSceneX();
                                double sceneY = me.getSceneY();
                                fixedVBox.setLayoutX(sceneX);
                                fixedVBox.setLayoutY(sceneY - 30);
                                fixedVBox.setVisible(true);
                            }
                        });
                        Platform.runLater(() -> {
                            contentVBox.getChildren().add(hBox);
                            contentVBox.setUserData(videoList);
                        });
                    }

                    SysConst.showDialog(rootAnchorPane, "解析完成！！！");
                }

            });
        } else {
            SysConst.showDialog(rootAnchorPane, "请输入解析视频地址！！！");
        }

    }
}
