/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javafx.fxml.FXML
 *  javafx.scene.control.Label
 */
package com.example.modbusJFXDemo;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ErrorController {
    @FXML
    private Label errorMessage;

    public void setErrorText(String text) {
        this.errorMessage.setText(text);
    }

    @FXML
    private void close() {
        this.errorMessage.getScene().getWindow().hide();
    }
}

