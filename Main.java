import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import java.util.ArrayList;
import java.util.List;

// =======================================================
// MAIN
// =======================================================
public class Main extends Application {

    @Override
    public void start(Stage stage) {
        stage.setTitle("Citizen Complaint Management System");
        LoginView.show(stage);
    }

    public static void main(String[] args) {
        launch(args);
    }

    // =======================================================
    // ENUM — type-safe status (no more typo-prone strings)
    // =======================================================
    enum Status {
        PENDING("Pending"),
        IN_PROGRESS("In Progress"),
        COMPLETED("Completed");

        private final String label;
        Status(String label) { this.label = label; }

        @Override
        public String toString() { return label; }
    }

    // =======================================================
    // INTERFACE (ABSTRACTION)
    // =======================================================
    interface ComplaintOperations {
        void addComplaint(Complaint c);
        void advanceStatus(Complaint c, String reply);
    }

    // =======================================================
    // INHERITANCE — User is the shared base for any account type
    // =======================================================
    static abstract class User {
        protected final String cnic;
        protected final String name;

        protected User(String cnic, String name) {
            this.cnic = cnic;
            this.name = name;
        }

        public String getCnic() { return cnic; }
        public String getName() { return name; }

        // POLYMORPHISM — each subtype describes itself differently
        public abstract String getRole();
    }

    static class Citizen extends User {
        public Citizen(String cnic, String name) {
            super(cnic, name);
        }

        @Override
        public String getRole() { return "Citizen"; }
    }

    static class Admin extends User {
        public Admin(String cnic, String name) {
            super(cnic, name);
        }

        @Override
        public String getRole() { return "Admin"; }
    }

    // =======================================================
    // ENCAPSULATION — Complaint hides its fields behind getters/setters
    // =======================================================
    static class Complaint {
        private final int id;
        private final String cnic;
        private final String name;
        private final String address;
        private Status status;
        private String reply;

        public Complaint(int id, String cnic, String name, String address) {
            this.id = id;
            this.cnic = cnic;
            this.name = name;
            this.address = address;
            this.status = Status.PENDING;
            this.reply = "No reply yet";
        }

        public int getId() { return id; }
        public String getCnic() { return cnic; }
        public String getName() { return name; }
        public String getAddress() { return address; }
        public Status getStatus() { return status; }
        public String getReply() { return reply; }

        public void setStatus(Status status) { this.status = status; }
        public void setReply(String reply) { this.reply = reply; }
    }

    // =======================================================
    // COMPLAINT SERVICE — single shared instance (Singleton)
    // =======================================================
    static class ComplaintService implements ComplaintOperations {

        private static final ComplaintService instance = new ComplaintService();
        public static ComplaintService getInstance() { return instance; }

        private final List<Complaint> complaints = new ArrayList<>();
        private int nextId = 1;

        private ComplaintService() {}

        @Override
        public void addComplaint(Complaint c) {
            complaints.add(c);
        }

        public Complaint createComplaint(String cnic, String name, String address) {
            Complaint c = new Complaint(nextId++, cnic, name, address);
            addComplaint(c);
            return c;
        }

        // Moves a complaint forward one step: Pending -> In Progress -> Completed
        @Override
        public void advanceStatus(Complaint c, String reply) {
            switch (c.getStatus()) {
                case PENDING -> c.setStatus(Status.IN_PROGRESS);
                case IN_PROGRESS -> c.setStatus(Status.COMPLETED);
                case COMPLETED -> { /* already at final stage */ }
            }
            if (reply != null && !reply.isBlank()) {
                c.setReply(reply);
            }
        }

        public List<Complaint> getAll() {
            return complaints;
        }

        public List<Complaint> getByCitizen(String cnic) {
            List<Complaint> list = new ArrayList<>();
            for (Complaint c : complaints) {
                if (c.getCnic().equals(cnic)) {
                    list.add(c);
                }
            }
            return list;
        }
    }

    // =======================================================
    // CITIZEN SERVICE — signup + login, single shared instance
    // =======================================================
    static class CitizenService {

        private static final CitizenService instance = new CitizenService();
        public static CitizenService getInstance() { return instance; }

        private final List<Citizen> citizens = new ArrayList<>();

        private CitizenService() {
            // seed data so login works out of the box
            citizens.add(new Citizen("12345", "Ali"));
            citizens.add(new Citizen("54321", "Sara"));
        }

        public Citizen login(String cnic) {
            for (Citizen c : citizens) {
                if (c.getCnic().equals(cnic)) {
                    return c;
                }
            }
            return null;
        }

        public boolean exists(String cnic) {
            return login(cnic) != null;
        }

        // returns null on success, or an error message on failure
        public String register(String cnic, String name) {
            if (cnic == null || cnic.isBlank() || name == null || name.isBlank()) {
                return "CNIC and Name are required.";
            }
            if (exists(cnic)) {
                return "An account with this CNIC already exists.";
            }
            citizens.add(new Citizen(cnic.trim(), name.trim()));
            return null;
        }
    }

    // =======================================================
    // ADMIN SERVICE — fixed admin credentials (CNIC-based, no password per scope)
    // =======================================================
    static class AdminService {

        private static final AdminService instance = new AdminService();
        public static AdminService getInstance() { return instance; }

        private final List<Admin> admins = new ArrayList<>();

        private AdminService() {
            admins.add(new Admin("00000", "System Admin"));
        }

        public Admin login(String cnic) {
            for (Admin a : admins) {
                if (a.getCnic().equals(cnic)) {
                    return a;
                }
            }
            return null;
        }
    }

    // =======================================================
    // SHARED UI HELPERS
    // =======================================================
    static class UiUtil {
        static Label title(String text) {
            Label l = new Label(text);
            l.setFont(Font.font("System", FontWeight.BOLD, 20));
            return l;
        }

        static Label error(String text) {
            Label l = new Label(text);
            l.setStyle("-fx-text-fill: #c0392b;");
            return l;
        }

        static Label success(String text) {
            Label l = new Label(text);
            l.setStyle("-fx-text-fill: #1e8449;");
            return l;
        }

        static void setScene(Stage stage, Parent root, int w, int h) {
            stage.setScene(new Scene(root, w, h));
        }
    }

    // =======================================================
    // LOGIN VIEW — citizen login, citizen signup, admin login
    // =======================================================
    static class LoginView {

        public static void show(Stage stage) {

            CitizenService citizenService = CitizenService.getInstance();
            AdminService adminService = AdminService.getInstance();

            Label heading = UiUtil.title("Citizen Complaint Management System");

            // ---- Citizen login box ----
            TextField cnicField = new TextField();
            cnicField.setPromptText("Enter your CNIC");

            Label loginMsg = new Label();

            Button citizenLoginBtn = new Button("Citizen Login");
            citizenLoginBtn.setMaxWidth(Double.MAX_VALUE);
            citizenLoginBtn.setOnAction(e -> {
                String cnic = cnicField.getText().trim();
                if (cnic.isEmpty()) {
                    loginMsg.setText("Please enter your CNIC.");
                    loginMsg.setStyle("-fx-text-fill: #c0392b;");
                    return;
                }
                Citizen c = citizenService.login(cnic);
                if (c == null) {
                    loginMsg.setText("No account found for this CNIC. Please sign up below.");
                    loginMsg.setStyle("-fx-text-fill: #c0392b;");
                } else {
                    CitizenDashboard.show(stage, c);
                }
            });

            VBox loginBox = new VBox(8, new Label("Already registered?"), cnicField, citizenLoginBtn, loginMsg);

            // ---- Citizen signup box ----
            TextField signupCnic = new TextField();
            signupCnic.setPromptText("New CNIC");

            TextField signupName = new TextField();
            signupName.setPromptText("Full Name");

            Label signupMsg = new Label();

            Button signupBtn = new Button("Sign Up as Citizen");
            signupBtn.setMaxWidth(Double.MAX_VALUE);
            signupBtn.setOnAction(e -> {
                String error = citizenService.register(signupCnic.getText(), signupName.getText());
                if (error != null) {
                    signupMsg.setText(error);
                    signupMsg.setStyle("-fx-text-fill: #c0392b;");
                } else {
                    signupMsg.setText("Account created! You can now log in above.");
                    signupMsg.setStyle("-fx-text-fill: #1e8449;");
                    signupCnic.clear();
                    signupName.clear();
                }
            });

            VBox signupBox = new VBox(8, new Label("New citizen?"), signupCnic, signupName, signupBtn, signupMsg);

            // ---- Admin login box ----
            TextField adminCnicField = new TextField();
            adminCnicField.setPromptText("Admin CNIC (try 00000)");

            Label adminMsg = new Label();

            Button adminLoginBtn = new Button("Admin Login");
            adminLoginBtn.setMaxWidth(Double.MAX_VALUE);
            adminLoginBtn.setOnAction(e -> {
                Admin a = adminService.login(adminCnicField.getText().trim());
                if (a == null) {
                    adminMsg.setText("Invalid admin CNIC.");
                    adminMsg.setStyle("-fx-text-fill: #c0392b;");
                } else {
                    AdminDashboard.show(stage, a);
                }
            });

            VBox adminBox = new VBox(8, new Label("Administrator?"), adminCnicField, adminLoginBtn, adminMsg);

            VBox left = new VBox(20, loginBox, new Separator(), signupBox);
            left.setPadding(new Insets(10));

            VBox right = new VBox(20, adminBox);
            right.setPadding(new Insets(10));

            HBox panels = new HBox(30, left, new Separator(javafx.geometry.Orientation.VERTICAL), right);
            panels.setPadding(new Insets(10));

            VBox root = new VBox(20, heading, panels);
            root.setPadding(new Insets(25));
            root.setAlignment(Pos.TOP_CENTER);

            UiUtil.setScene(stage, root, 620, 460);
            stage.show();
        }
    }

    // =======================================================
    // CITIZEN DASHBOARD
    // =======================================================
    static class CitizenDashboard {

        static ComplaintService complaintService = ComplaintService.getInstance();

        public static void show(Stage stage, Citizen citizen) {

            Label heading = UiUtil.title("Welcome, " + citizen.getName() + " (" + citizen.getCnic() + ")");

            TextField address = new TextField();
            address.setPromptText("Enter complaint location / details");

            Label submitMsg = new Label();

            TableView<Complaint> table = buildTable();
            refreshTable(table, complaintService.getByCitizen(citizen.getCnic()));

            Button submit = new Button("Submit Complaint");
            submit.setOnAction(e -> {
                String text = address.getText().trim();
                if (text.isEmpty()) {
                    submitMsg.setText("Please describe your complaint before submitting.");
                    submitMsg.setStyle("-fx-text-fill: #c0392b;");
                    return;
                }
                complaintService.createComplaint(citizen.getCnic(), citizen.getName(), text);
                submitMsg.setText("Complaint submitted!");
                submitMsg.setStyle("-fx-text-fill: #1e8449;");
                address.clear();
                refreshTable(table, complaintService.getByCitizen(citizen.getCnic()));
            });

            Button refresh = new Button("Refresh List");
            refresh.setOnAction(e -> refreshTable(table, complaintService.getByCitizen(citizen.getCnic())));

            HBox formRow = new HBox(10, address, submit);

            Button logout = new Button("Logout");
            logout.setOnAction(e -> LoginView.show(stage));

            HBox topBar = new HBox(10, refresh, logout);
            topBar.setAlignment(Pos.CENTER_RIGHT);

            VBox root = new VBox(12, heading, formRow, submitMsg, new Separator(), table, topBar);
            root.setPadding(new Insets(20));
            VBox.setVgrow(table, Priority.ALWAYS);

            UiUtil.setScene(stage, root, 700, 480);
        }

        private static TableView<Complaint> buildTable() {
            TableView<Complaint> table = new TableView<>();

            TableColumn<Complaint, String> idCol = new TableColumn<>("ID");
            idCol.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getId())));

            TableColumn<Complaint, String> addressCol = new TableColumn<>("Address");
            addressCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAddress()));
            addressCol.setPrefWidth(200);

            TableColumn<Complaint, String> statusCol = new TableColumn<>("Status");
            statusCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus().toString()));

            TableColumn<Complaint, String> replyCol = new TableColumn<>("Admin Reply");
            replyCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getReply()));
            replyCol.setPrefWidth(220);

            table.getColumns().addAll(idCol, addressCol, statusCol, replyCol);
            return table;
        }

        private static void refreshTable(TableView<Complaint> table, List<Complaint> data) {
            table.setItems(FXCollections.observableArrayList(data));
        }
    }

    // =======================================================
    // ADMIN DASHBOARD
    // =======================================================
    static class AdminDashboard {

        static ComplaintService complaintService = ComplaintService.getInstance();

        public static void show(Stage stage, Admin admin) {

            Label heading = UiUtil.title("Admin Panel — " + admin.getName());

            TableView<Complaint> table = new TableView<>();

            TableColumn<Complaint, String> idCol = new TableColumn<>("ID");
            idCol.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getId())));

            TableColumn<Complaint, String> cnicCol = new TableColumn<>("CNIC");
            cnicCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCnic()));

            TableColumn<Complaint, String> nameCol = new TableColumn<>("Name");
            nameCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getName()));

            TableColumn<Complaint, String> addressCol = new TableColumn<>("Address");
            addressCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAddress()));
            addressCol.setPrefWidth(180);

            TableColumn<Complaint, String> statusCol = new TableColumn<>("Status");
            statusCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus().toString()));

            table.getColumns().addAll(idCol, cnicCol, nameCol, addressCol, statusCol);
            table.setItems(FXCollections.observableArrayList(complaintService.getAll()));

            TextField reply = new TextField();
            reply.setPromptText("Reply / note for selected complaint");

            Label actionMsg = new Label();

            Button advance = new Button("Advance Status →");
            advance.setOnAction(e -> {
                Complaint selected = table.getSelectionModel().getSelectedItem();
                if (selected == null) {
                    actionMsg.setText("Select a complaint first.");
                    actionMsg.setStyle("-fx-text-fill: #c0392b;");
                    return;
                }
                if (selected.getStatus() == Status.COMPLETED) {
                    actionMsg.setText("This complaint is already completed.");
                    actionMsg.setStyle("-fx-text-fill: #c0392b;");
                    return;
                }
                complaintService.advanceStatus(selected, reply.getText());
                table.refresh();
                reply.clear();
                actionMsg.setText("Status updated to: " + selected.getStatus());
                actionMsg.setStyle("-fx-text-fill: #1e8449;");
            });

            Button refresh = new Button("Refresh");
            refresh.setOnAction(e ->
                    table.setItems(FXCollections.observableArrayList(complaintService.getAll())));

            Button logout = new Button("Logout");
            logout.setOnAction(e -> LoginView.show(stage));

            HBox actionRow = new HBox(10, reply, advance);
            HBox topBar = new HBox(10, refresh, logout);
            topBar.setAlignment(Pos.CENTER_RIGHT);

            VBox root = new VBox(12, heading, table, actionRow, actionMsg, topBar);
            root.setPadding(new Insets(20));
            VBox.setVgrow(table, Priority.ALWAYS);

            UiUtil.setScene(stage, root, 760, 520);
        }
    }
}