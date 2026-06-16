package com.expense;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        ExpenseApp app = new ExpenseApp();
        Scene scene = new Scene(app, 650, 500);
        stage.setTitle("Office Expense Manager");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
