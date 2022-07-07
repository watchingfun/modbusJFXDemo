module com.example.modbusJFXDemo {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.kordamp.bootstrapfx.core;
    requires java.desktop;
    requires modbus4j;
    requires hutool.all;

    opens com.example.modbusJFXDemo to javafx.fxml;

    exports com.example.modbusJFXDemo;
}

