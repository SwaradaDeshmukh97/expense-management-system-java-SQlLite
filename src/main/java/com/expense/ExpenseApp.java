package com.expense;

import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.beans.property.*;
import javafx.stage.*;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExpenseApp extends VBox {

private final Database db = new Database("myexpense.db");  
private final TextField nameField    = new TextField();  
private final TextField priceField   = new TextField();  
private final TextField dateField    = new TextField();  
private final TableView<ExpenseRow> table = new TableView<>();  
private final ObservableList<ExpenseRow> data = FXCollections.observableArrayList();  
private int selectedRowId = -1;  

// Colors  
private static final String GREEN      = "#2E7D32";  
private static final String DARK_GREEN = "#1B5E20";  
private static final String GOLD       = "#F9A825";  
private static final String LIGHT_GOLD = "#FFF8E1";  
private static final String WHITE      = "white";  

public ExpenseApp() {  
    setSpacing(0);  
    setStyle("-fx-background-color: " + LIGHT_GOLD + ";");  

    getChildren().addAll(  
        buildHeader(),  
        buildInputSection(),  
        buildButtonBar(),  
        buildTable()  
    );  

    refreshTable();  
}  

// ── Header ──────────────────────────────────────────────  
private HBox buildHeader() {  
    Label title = new Label("💰 Office Expense Manager");  
    title.setFont(Font.font("Arial", FontWeight.BOLD, 22));  
    title.setStyle("-fx-text-fill: " + WHITE + ";");  

    Button adminBtn = styledButton("🔐 Admin Panel", GOLD, DARK_GREEN);  
    adminBtn.setOnAction(e -> showAdminLogin());  

    HBox header = new HBox(title, adminBtn);  
    HBox.setHgrow(title, Priority.ALWAYS);  
    header.setAlignment(Pos.CENTER_LEFT);  
    header.setPadding(new Insets(14, 20, 14, 20));  
    header.setSpacing(10);  
    header.setStyle("-fx-background-color: " + DARK_GREEN + ";");  
    return header;  
}  

// ── Input Section ────────────────────────────────────────  
private GridPane buildInputSection() {  
    GridPane grid = new GridPane();  
    grid.setHgap(12);  
    grid.setVgap(10);  
    grid.setPadding(new Insets(20, 20, 10, 20));  
    grid.setStyle("-fx-background-color: " + LIGHT_GOLD + ";");  

    String labelStyle = "-fx-font-weight: bold; -fx-font-size: 13px;" +  
                        "-fx-text-fill: " + DARK_GREEN + ";";  
    String fieldStyle = "-fx-border-color: " + GREEN + "; -fx-border-radius: 4;" +  
                        "-fx-padding: 6; -fx-font-size: 13px;";  

    Label l1 = new Label("Item Name:");     l1.setStyle(labelStyle);  
    Label l2 = new Label("Item Price (₹):"); l2.setStyle(labelStyle);  
    Label l3 = new Label("Purchase Date:"); l3.setStyle(labelStyle);  

    nameField.setStyle(fieldStyle);  
    priceField.setStyle(fieldStyle);  
    dateField.setStyle(fieldStyle);  

    nameField.setPromptText("e.g. Printer Paper");  
    priceField.setPromptText("e.g. 250.00");  
    dateField.setPromptText("e.g. 25 April 2026");  

    grid.add(l1, 0, 0); grid.add(nameField, 1, 0);  
    grid.add(l2, 0, 1); grid.add(priceField, 1, 1);  
    grid.add(l3, 0, 2); grid.add(dateField, 1, 2);  

    ColumnConstraints c1 = new ColumnConstraints(130);  
    ColumnConstraints c2 = new ColumnConstraints();  
    c2.setHgrow(Priority.ALWAYS);  
    grid.getColumnConstraints().addAll(c1, c2);  

    return grid;  
}  

// ── Button Bar ───────────────────────────────────────────  
private HBox buildButtonBar() {  
    Button curDateBtn = styledButton("📅 Today", "#0288D1", WHITE);  
    Button saveBtn    = styledButton("💾 Save",  GREEN, WHITE);  
    Button clearBtn   = styledButton("🧹 Clear",GOLD,DARK_GREEN);  
    Button updateBtn  = styledButton("✏️ Update",  "#6A1B9A", WHITE);  
    Button deleteBtn  = styledButton("🗑️ Delete", "#C62828", WHITE);  
    Button totalBtn   = styledButton("📊 Total",DARK_GREEN, WHITE);  
    Button exitBtn    = styledButton("🚪 Exit","#424242", WHITE);  

    curDateBtn.setOnAction(e -> dateField.setText(  
            LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))));  

    saveBtn.setOnAction(e -> {  
        if (nameField.getText().isEmpty() || priceField.getText().isEmpty()) {  
            showAlert("⚠️ Warning", "Please fill in all fields.", Alert.AlertType.WARNING);  
            return;  
        }  
        try {  
            double price = Double.parseDouble(priceField.getText());  
            double budget = db.getBudget();  
            double spent  = db.getTotalSpent();  
            db.insertRecord(nameField.getText(), price, dateField.getText());  
            refreshTable();  
            clearFields();  
            if (spent + price > budget) {  
                showAlert("⚠️ Budget Alert",  
                    String.format("You have exceeded your budget of ₹%.2f!", budget),  
                    Alert.AlertType.WARNING);  
            }  
        } catch (NumberFormatException ex) {  
            showAlert("❌ Error", "Enter a valid price.", Alert.AlertType.ERROR);  
        }  
    });  

    clearBtn.setOnAction(e -> clearFields());  

    updateBtn.setOnAction(e -> {  
        if (selectedRowId == -1) {  
            showAlert("⚠️ Warning", "Select a record to update.", Alert.AlertType.WARNING);  
            return;  
        }  
        try {  
            db.updateRecord(nameField.getText(),  
                    Double.parseDouble(priceField.getText()),  
                    dateField.getText(), selectedRowId);  
            refreshTable();  
            clearFields();  
            selectedRowId = -1;  
        } catch (NumberFormatException ex) {  
            showAlert("❌ Error", "Enter a valid price.", Alert.AlertType.ERROR);  
        }  
    });  

    deleteBtn.setOnAction(e -> {  
        if (selectedRowId == -1) {  
            showAlert("⚠️ Warning", "Select a record to delete.", Alert.AlertType.WARNING);  
            return;  
        }  
        db.deleteRecord(selectedRowId);  
        refreshTable();  
        clearFields();  
        selectedRowId = -1;  
    });  

    totalBtn.setOnAction(e -> {  
        double spent  = db.getTotalSpent();  
        double budget = db.getBudget();  
        showAlert("📊 Total Balance",  
            String.format("Budget:    ₹%.2f%nSpent:     ₹%.2f%nRemaining: ₹%.2f",  
                    budget, spent, budget - spent),  
            Alert.AlertType.INFORMATION);  
    });  

    exitBtn.setOnAction(e -> { db.close(); System.exit(0); });  

    HBox bar = new HBox(8, curDateBtn, saveBtn, clearBtn,  
                        updateBtn, deleteBtn, totalBtn, exitBtn);  
    bar.setPadding(new Insets(10, 20, 10, 20));  
    bar.setStyle("-fx-background-color: #E8F5E9; -fx-border-color: " +  
                 GREEN + "; -fx-border-width: 1 0 1 0;");  
    bar.setAlignment(Pos.CENTER_LEFT);  
    return bar;  
}  

// ── Table ────────────────────────────────────────────────  
private VBox buildTable() {  
    TableColumn<ExpenseRow, Integer> colId = new TableColumn<>("S.No");  
    colId.setCellValueFactory(new PropertyValueFactory<>("rowId"));  
    colId.setPrefWidth(60);  

    TableColumn<ExpenseRow, String> colName = new TableColumn<>("Item Name");  
    colName.setCellValueFactory(new PropertyValueFactory<>("itemName"));  
    colName.setPrefWidth(200);  

    TableColumn<ExpenseRow, Double> colPrice = new TableColumn<>("Price (₹)");  
    colPrice.setCellValueFactory(new PropertyValueFactory<>("itemPrice"));  
    colPrice.setPrefWidth(120);  

    TableColumn<ExpenseRow, String> colDate = new TableColumn<>("Purchase Date");  
    colDate.setCellValueFactory(new PropertyValueFactory<>("purchaseDate"));  
    colDate.setPrefWidth(180);  

    table.getColumns().addAll(colId, colName, colPrice, colDate);  
    table.setItems(data);  
    table.setStyle("-fx-font-size: 13px;");  
    VBox.setVgrow(table, Priority.ALWAYS);  

    // Row click fills fields  
    table.getSelectionModel().selectedItemProperty().addListener((obs, o, newVal) -> {  
        if (newVal != null) {  
            selectedRowId = newVal.getRowId();  
            nameField.setText(newVal.getItemName());  
            priceField.setText(String.valueOf(newVal.getItemPrice()));  
            dateField.setText(newVal.getPurchaseDate());  
        }  
    });  

    // Alternate row colors  
    table.setRowFactory(tv -> new TableRow<>() {  
        @Override  
        protected void updateItem(ExpenseRow item, boolean empty) {  
            super.updateItem(item, empty);  
            if (!empty && getIndex() % 2 == 0) {  
                setStyle("-fx-background-color: #F1F8E9;");  
            } else {  
                setStyle("");  
            }  
        }  
    });  

    VBox wrapper = new VBox(table);  
    wrapper.setPadding(new Insets(10, 20, 20, 20));  
    wrapper.setStyle("-fx-background-color: " + LIGHT_GOLD + ";");  
    VBox.setVgrow(wrapper, Priority.ALWAYS);  
    return wrapper;  
}  

// ── Admin Login Popup ────────────────────────────────────  
private void showAdminLogin() {  
    Stage loginStage = new Stage();  
    loginStage.setTitle("Admin Login");  
    loginStage.initModality(Modality.APPLICATION_MODAL);  

    Label userLabel = new Label("Username:");  
    Label passLabel = new Label("Password:");  
    TextField userField = new TextField();  
    PasswordField passField = new PasswordField();  
    Button loginBtn = styledButton("Login", DARK_GREEN, WHITE);  
    Button cancelBtn = styledButton("Cancel", "#757575", WHITE);  

    userLabel.setStyle("-fx-font-weight:bold; -fx-text-fill:" + DARK_GREEN + ";");  
    passLabel.setStyle("-fx-font-weight:bold; -fx-text-fill:" + DARK_GREEN + ";");  

    loginBtn.setOnAction(e -> {  
        if (db.validateAdmin(userField.getText(), passField.getText())) {  
            loginStage.close();  
            showAdminPanel();  
        } else {  
            showAlert("❌ Error", "Invalid username or password.", Alert.AlertType.ERROR);  
        }  
    });  

    cancelBtn.setOnAction(e -> loginStage.close());  

    GridPane grid = new GridPane();  
    grid.setHgap(10); grid.setVgap(12);  
    grid.setPadding(new Insets(20));  
    grid.add(userLabel, 0, 0); grid.add(userField, 1, 0);  
    grid.add(passLabel, 0, 1); grid.add(passField, 1, 1);  

    HBox btns = new HBox(10, loginBtn, cancelBtn);  
    btns.setAlignment(Pos.CENTER_RIGHT);  

    VBox root = new VBox(12, grid, btns);  
    root.setPadding(new Insets(20));  
    root.setStyle("-fx-background-color:" + LIGHT_GOLD +  
                  "; -fx-border-color:" + GREEN + "; -fx-border-width:2;");  

    loginStage.setScene(new Scene(root, 320, 180));  
    loginStage.showAndWait();  
}  

// ── Admin Panel ──────────────────────────────────────────  
private void showAdminPanel() {  
    Stage adminStage = new Stage();  
    adminStage.setTitle("🔐 Admin Panel");  
    adminStage.initModality(Modality.APPLICATION_MODAL);  

    // Budget section  
    Label budgetLabel = new Label("Set Budget Limit (₹):");  
    budgetLabel.setStyle("-fx-font-weight:bold; -fx-text-fill:" + DARK_GREEN + ";");  
    TextField budgetField = new TextField(String.valueOf(db.getBudget()));  
    Button setBudgetBtn = styledButton("💰 Set Budget", GREEN, WHITE);  

    setBudgetBtn.setOnAction(e -> {  
        try {  
            db.setBudget(Double.parseDouble(budgetField.getText()));  
            showAlert("✅ Success", "Budget updated successfully!", Alert.AlertType.INFORMATION);  
        } catch (NumberFormatException ex) {  
            showAlert("❌ Error", "Enter a valid budget amount.", Alert.AlertType.ERROR);  
        }  
    });  

    HBox budgetRow = new HBox(10, budgetLabel, budgetField, setBudgetBtn);  
    budgetRow.setAlignment(Pos.CENTER_LEFT);  

    // Export section  
    Label exportLabel = new Label("Export all records to CSV:");  
    exportLabel.setStyle("-fx-font-weight:bold; -fx-text-fill:" + DARK_GREEN + ";");  
    Button exportBtn = styledButton("📁 Export CSV", GOLD, DARK_GREEN);  

    exportBtn.setOnAction(e -> exportToCSV(adminStage));  

    HBox exportRow = new HBox(10, exportLabel, exportBtn);  
    exportRow.setAlignment(Pos.CENTER_LEFT);  

    // Summary  
    double spent  = db.getTotalSpent();  
    double budget = db.getBudget();  
    Label summary = new Label(String.format(  
        "📊  Budget: ₹%.2f   |   Spent: ₹%.2f   |   Remaining: ₹%.2f",  
        budget, spent, budget - spent));  
    summary.setStyle("-fx-font-size:13px; -fx-text-fill:" + DARK_GREEN +  
                     "; -fx-font-weight:bold;");  

    Separator sep1 = new Separator();  
    Separator sep2 = new Separator();  

    Button closeBtn = styledButton("Close", "#757575", WHITE);  
    closeBtn.setOnAction(e -> adminStage.close());  

    VBox root = new VBox(14,  
        buildAdminHeader(),  
        summary, sep1,  
        budgetRow, sep2,  
        exportRow,  
        closeBtn  
    );  
    root.setPadding(new Insets(20));  
    root.setStyle("-fx-background-color:" + LIGHT_GOLD + ";");  
    VBox.setMargin(closeBtn, new Insets(10, 0, 0, 0));  

    adminStage.setScene(new Scene(root, 520, 280));  
    adminStage.showAndWait();  
}  

private HBox buildAdminHeader() {  
    Label title = new Label("🔐 Admin Panel");  
    title.setFont(Font.font("Arial", FontWeight.BOLD, 16));  
    title.setStyle("-fx-text-fill: white;");  
    HBox header = new HBox(title);  
    header.setPadding(new Insets(10, 16, 10, 16));  
    header.setStyle("-fx-background-color:" + DARK_GREEN +  
                    "; -fx-background-radius: 6;");  
    return header;  
}  

// ── CSV Export ───────────────────────────────────────────  
private void exportToCSV(Stage owner) {  
    FileChooser fc = new FileChooser();  
    fc.setTitle("Save CSV");  
    fc.getExtensionFilters().add(  
            new FileChooser.ExtensionFilter("CSV Files", "*.csv"));  
    fc.setInitialFileName("expenses.csv");  
    File file = fc.showSaveDialog(owner);  

    if (file != null) {  
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {  
            pw.println("S.No,Item Name,Item Price,Purchase Date");  
            for (Object[] row : db.fetchRecords()) {  
                pw.printf("%s,%s,%.2f,%s%n",  
                        row[0], row[1], row[2], row[3]);  
            }  
            showAlert("✅ Exported",  
                    "File saved to:\n" + file.getAbsolutePath(),  
                    Alert.AlertType.INFORMATION);  
        } catch (IOException ex) {  
            showAlert("❌ Error", "Export failed: " + ex.getMessage(),  
                    Alert.AlertType.ERROR);  
        }  
    }  
}  

// ── Helpers ──────────────────────────────────────────────  
private Button styledButton(String text, String bg, String fg) {  
    Button btn = new Button(text);  
    btn.setStyle(String.format(  
        "-fx-background-color:%s; -fx-text-fill:%s;" +  
        "-fx-font-weight:bold; -fx-padding:7 12;" +  
        "-fx-background-radius:5; -fx-cursor:hand;", bg, fg));  
    return btn;  
}  

private void refreshTable() {  
    data.clear();  
    List<Object[]> rows = db.fetchRecords();  
    for (Object[] row : rows) {  
        data.add(new ExpenseRow(  
                (int) row[0], (String) row[1],  
                (double) row[2], (String) row[3]));  
    }  
}  

private void clearFields() {  
    nameField.clear();  
    priceField.clear();  
    dateField.clear();  
    selectedRowId = -1;  
}  

private void showAlert(String title, String msg, Alert.AlertType type) {  
    Alert alert = new Alert(type, msg, ButtonType.OK);  
    alert.setTitle(title);  
    alert.setHeaderText(null);  
    alert.showAndWait();  
}  

// ── ExpenseRow model ─────────────────────────────────────  
public static class ExpenseRow {  
    private final SimpleIntegerProperty rowId;  
    private final SimpleStringProperty  itemName;  
    private final SimpleDoubleProperty  itemPrice;  
    private final SimpleStringProperty  purchaseDate;  

    public ExpenseRow(int rowId, String itemName,  
                      double itemPrice, String purchaseDate) {  
        this.rowId        = new SimpleIntegerProperty(rowId);  
        this.itemName     = new SimpleStringProperty(itemName);  
        this.itemPrice    = new SimpleDoubleProperty(itemPrice);  
        this.purchaseDate = new SimpleStringProperty(purchaseDate);  
    }  

    public int    getRowId()       { return rowId.get(); }  
    public String getItemName()    { return itemName.get(); }  
    public double getItemPrice()   { return itemPrice.get(); }  
    public String getPurchaseDate(){ return purchaseDate.get(); }  
}

}
