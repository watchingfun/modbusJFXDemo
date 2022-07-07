/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  cn.hutool.core.util.ReflectUtil
 *  com.serotonin.modbus4j.ModbusFactory
 *  com.serotonin.modbus4j.ModbusMaster
 *  com.serotonin.modbus4j.exception.ModbusInitException
 *  com.serotonin.modbus4j.exception.ModbusTransportException
 *  com.serotonin.modbus4j.ip.IpParameters
 *  com.serotonin.modbus4j.msg.ModbusRequest
 *  com.serotonin.modbus4j.msg.ModbusResponse
 *  com.serotonin.modbus4j.msg.WriteCoilRequest
 *  com.serotonin.modbus4j.msg.WriteCoilResponse
 *  com.serotonin.modbus4j.msg.WriteRegisterRequest
 *  com.serotonin.modbus4j.msg.WriteRegisterResponse
 *  com.serotonin.modbus4j.sero.messaging.MessageControl
 *  javafx.application.Platform
 *  javafx.concurrent.Task
 *  javafx.fxml.FXML
 *  javafx.fxml.FXMLLoader
 *  javafx.fxml.Initializable
 *  javafx.scene.Parent
 *  javafx.scene.Scene
 *  javafx.scene.control.Button
 *  javafx.scene.control.TextArea
 *  javafx.scene.control.TextField
 *  javafx.scene.layout.GridPane
 *  javafx.scene.layout.VBox
 *  javafx.stage.Modality
 *  javafx.stage.Stage
 *  javafx.stage.StageStyle
 */
package com.example.modbusJFXDemo;

import cn.hutool.core.util.ReflectUtil;
import com.serotonin.modbus4j.ModbusFactory;
import com.serotonin.modbus4j.ModbusMaster;
import com.serotonin.modbus4j.exception.ModbusInitException;
import com.serotonin.modbus4j.exception.ModbusTransportException;
import com.serotonin.modbus4j.ip.IpParameters;
import com.serotonin.modbus4j.msg.ModbusRequest;
import com.serotonin.modbus4j.msg.ModbusResponse;
import com.serotonin.modbus4j.msg.WriteCoilRequest;
import com.serotonin.modbus4j.msg.WriteCoilResponse;
import com.serotonin.modbus4j.msg.WriteRegisterRequest;
import com.serotonin.modbus4j.msg.WriteRegisterResponse;
import com.serotonin.modbus4j.sero.messaging.MessageControl;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javax.swing.SwingUtilities;

public class HelloController
        implements Initializable {
    @FXML
    private VBox vBox;
    @FXML
    private GridPane gridPane;
    @FXML
    private TextField textField_ip;
    @FXML
    private TextField textField_port;
    @FXML
    private Button button_on;
    @FXML
    private Button button_off;
    @FXML
    private TextArea logTextarea;
    private static ModbusFactory modbusFactory = new ModbusFactory();
    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static DeviceEnum deviceEnum = DeviceEnum.水泵;
    private static final Object[] switchValue = new Object[]{true, false};
    private static final int sid = 255;
    private Stage loadingAlert = null;

    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.textField_ip.setText("192.168.1.17");
        this.textField_port.setText("9999");
        this.initLoadingDialog();
    }

    public static WriteRegisterResponse writeRegister(ModbusMaster modbusMaster, int slaveId, int writeOffset, int writeValue) throws ModbusTransportException {
        WriteRegisterRequest request = new WriteRegisterRequest(slaveId, writeOffset, writeValue);
        WriteRegisterResponse response = (WriteRegisterResponse) modbusMaster.send(request);
        return response;
    }

    public static WriteCoilResponse writeCoil(ModbusMaster master, int slaveId, int writeOffset, boolean writeValue) throws ModbusTransportException {
        WriteCoilRequest request = new WriteCoilRequest(slaveId, writeOffset, writeValue);
        WriteCoilResponse response = (WriteCoilResponse) master.send(request);
        return response;
    }

    private static ModbusMaster createTcpMaster(String host, int port) throws ModbusInitException {
        IpParameters params = new IpParameters();
        params.setHost(host);
        params.setPort(port);
        ModbusMaster tcpMaster = modbusFactory.createTcpMaster(params, true);
        tcpMaster.init();
        MessageControl conn = (MessageControl) ReflectUtil.getFieldValue(tcpMaster, "conn");
        conn.DEBUG = true;
        conn.setTimeout(1000);
        conn.setRetries(1);
        return tcpMaster;
    }

    @FXML
    private void onButtonOnClicked() {
        this.send(255, switchValue[0], deviceEnum, "发送开启:" + this.textField_ip.getText() + ":" + this.textField_port.getText());
    }

    @FXML
    private void onButtonOffClicked() {
        this.send(255, switchValue[1], deviceEnum, "发送关闭:" + this.textField_ip.getText() + ":" + this.textField_port.getText());
    }

    private void send(final int slaveId, final Object val, final DeviceEnum deviceEnum, String remark) {
        this.showLoadingDialog();
        this.appendText(remark != null ? remark : "发送指令");
        final ModbusMaster[] modbusMaster = new ModbusMaster[]{null};
        Task<ModbusResponse> task = new Task<ModbusResponse>() {

            protected ModbusResponse call() throws Exception {
                modbusMaster[0] = HelloController.createTcpMaster(HelloController.this.textField_ip.getText(), Integer.parseInt(HelloController.this.textField_port.getText()));
                return HelloController.this.sendCommand(modbusMaster[0], slaveId, val, deviceEnum);
            }

            protected void succeeded() {
                super.succeeded();
                ModbusResponse response = this.getValue();
                HelloController.this.log(response);
            }

            protected void failed() {
                super.failed();
                Throwable e = this.getException();
                if (e instanceof ModbusTransportException) {
                    HelloController.this.appendText("消息传输异常，请检查地址(" + slaveId + ")是否正确: " + e.getMessage());
                } else if (e instanceof ModbusInitException) {
                    HelloController.this.appendText("连接初始化异常，请检查网络: " + e.getMessage());
                } else {
                    HelloController.this.appendText("未知异常:" + e.getMessage());
                }
            }

            protected void done() {
                super.done();
                if (modbusMaster[0] != null) {
                    modbusMaster[0].destroy();
                }
                Platform.runLater(() -> HelloController.this.hideLoadingDialog());
            }
        };
        new Thread(task).start();
    }

    private void log(ModbusResponse response) {
        if (response.isException()) {
            this.appendText("设备返回错误: " + response.getExceptionMessage());
        } else {
            this.appendText("发送成功: 从设备地址：" + response.getSlaveId() + " 功能码：" + response.getFunctionCode());
        }
    }

    private void appendText(String text) {
        try {
            SwingUtilities.invokeLater(() -> this.logTextarea.appendText(formatter.format(LocalDateTime.now()) + ": " + text + "\n"));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public ModbusResponse sendCommand(ModbusMaster modbusMaster, int slaveId, Object val, DeviceEnum deviceEnum) throws ModbusTransportException {
        switch (deviceEnum) {
            case 水泵: {
                return HelloController.writeCoil(modbusMaster, slaveId, 0, (Boolean) val);
            }
            case 空开: {
                return HelloController.writeRegister(modbusMaster, slaveId, 17, (Integer) val);
            }
        }
        throw new RuntimeException("未知设备: Unknown device");
    }

    private void initLoadingDialog() {
        this.loadingAlert = new Stage();
        this.loadingAlert.initModality(Modality.APPLICATION_MODAL);
        this.loadingAlert.initStyle(StageStyle.UNDECORATED);
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("loadingAlert.fxml"));
        try {
            Parent root = (Parent) loader.load();
            this.loadingAlert.setScene(new Scene(root, 200.0, 100.0));
        } catch (IOException exc) {
            exc.printStackTrace();
        }
    }

    private void showLoadingDialog() {
        this.gridPane.setDisable(true);
        this.loadingAlert.show();
    }

    private void hideLoadingDialog() {
        this.gridPane.setDisable(false);
        this.loadingAlert.hide();
    }
}

