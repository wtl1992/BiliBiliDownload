package sys;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * @author: wtl
 * @License: (C) Copyright 2020
 * @Contact: 1050100468@qq.com
 * @Date: 2020/8/12 5:44
 * @Version: 1.0
 * @Description:
 */
public class SysConst {

    public static final String EMPTY_STRING = "";

    public static final String BILIBILI_AV = "av";


    public static void showDialog(Node node,String content){
        Platform.runLater(()->{
            Dialog dialog = new Dialog();
            dialog.setResizable(false);
            dialog.initOwner(node.getScene().getWindow());
            dialog.setTitle("信息提示");
            dialog.setContentText(content);
            Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
            dialog.getDialogPane().getScene().getStylesheets().add(SysConst.class.getResource("/css/dialog.css").toExternalForm());
            stage.getIcons().add(new Image(SysConst.class.getResource("/images/index_logo.png").toExternalForm()));
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE,ButtonType.OK);
            dialog.setHeight(200);
            dialog.show();
        });
    }
}
