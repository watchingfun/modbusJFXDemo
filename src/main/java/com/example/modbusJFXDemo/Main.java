/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  javafx.application.Application
 *  javafx.application.Platform
 *  javafx.fxml.FXMLLoader
 *  javafx.scene.Parent
 *  javafx.scene.Scene
 *  javafx.stage.Modality
 *  javafx.stage.Stage
 *  org.kordamp.bootstrapfx.BootstrapFX
 */
package com.example.modbusJFXDemo;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.kordamp.bootstrapfx.BootstrapFX;

public class Main
        extends Application {
    public void start(Stage primaryStage) throws IOException {
        Thread.setDefaultUncaughtExceptionHandler(Main::showError);
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 460.0, 540.0);
        scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
        primaryStage.setTitle("水泵控制测试程序");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        Main.launch((String[]) new String[0]);
    }

    private static void showError(Thread t, Throwable e) {
        if (Platform.isFxApplicationThread()) {
            Main.showErrorDialog(e);
        } else {
            System.err.println("An unexpected error occurred in " + t.getName() + " thread. " + e.getMessage());
        }
    }

    public static void showErrorDialog(Throwable e) {
        StringWriter errorMsg = new StringWriter();
        e.printStackTrace(new PrintWriter(errorMsg));
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("error.fxml"));
        try {
            Parent root = loader.load();
            ((ErrorController) loader.getController()).setErrorText(errorMsg.toString());
            dialog.setScene(new Scene(root, 250.0, 400.0));
            dialog.show();
        } catch (IOException exc) {
            exc.printStackTrace();
        }
    }
}

