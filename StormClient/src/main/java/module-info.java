module me.radu.stormclient {
    requires javafx.controls;
    requires javafx.fxml;


    opens me.radu to javafx.fxml;
    exports me.radu;
}