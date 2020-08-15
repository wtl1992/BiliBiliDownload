import controller.IndexController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * @author: wtl
 * @License: (C) Copyright 2020
 * @Contact: 1050100468@qq.com
 * @Date: 2020/8/10 5:56
 * @Version: 1.0
 * @Description:
 */
public class Startup extends Application {
    public static void main(String[] args) {
        Application.launch(Startup.class,args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/index.fxml"));
        fxmlLoader.setController(new IndexController());

        Scene scene = new Scene((Parent) fxmlLoader.load());

        primaryStage.setTitle("哔哩哔哩下载工具");
        primaryStage.setScene(scene);
        primaryStage.getIcons().add(new Image(getClass().getResource("/images/index_logo.png").toExternalForm()));

        scene.getStylesheets().add(getClass().getResource("/css/index.css").toExternalForm());

        primaryStage.setOnCloseRequest(mouseEvent->{
            System.exit(0);
        });
        primaryStage.setWidth(600);
        primaryStage.setHeight(600);
        primaryStage.show();
    }
}
