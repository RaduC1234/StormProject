module me.radu.stormclient {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;
    requires org.apache.logging.log4j;
    requires static lombok;


    opens me.radu to javafx.fxml;
    exports me.radu.core;
    opens me.radu.core to javafx.fxml;
}