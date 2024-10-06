module vsas {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;

    opens javafx to javafx.fxml;
    exports javafx;
    exports javafx.model;
    opens javafx.model to javafx.fxml;
}