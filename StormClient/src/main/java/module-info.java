module me.radu.stormclient {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;
    requires org.apache.logging.log4j;
    requires static lombok;

    opens me.radu.gui to javafx.fxml;
    opens me.radu.core to javafx.fxml;

    opens me.radu.network to com.google.gson;
    opens me.radu.data to com.google.gson;

    exports me.radu.gui;
    exports me.radu.network;
    exports me.radu.core;
    exports me.radu.gui.controller;
    opens me.radu.gui.controller to javafx.fxml;
}
