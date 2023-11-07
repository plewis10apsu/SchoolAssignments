module edu.apsu.langlearn {
    requires javafx.controls;
    requires javafx.fxml;


    opens edu.apsu.langlearn to javafx.fxml;
    exports edu.apsu.langlearn;
}