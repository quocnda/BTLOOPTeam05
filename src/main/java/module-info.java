module com.example.btl1_dictionary {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.xerial.sqlitejdbc;
    requires java.desktop;
    requires javafx.web;
    requires freetts;
    requires simplecaptcha;
    requires java.ocr.api;
    requires org.mongodb.driver.core;
    requires org.mongodb.bson;
    requires com.google.gson;
    requires org.mongodb.driver.sync.client;


    opens com.example.btl1_dictionary to javafx.fxml;
    exports com.example.btl1_dictionary;
}