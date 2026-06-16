module com.expense{
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    opens com.expense to javafx.controls;
}
