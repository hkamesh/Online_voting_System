import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.*;
public class OnlineVotingSystem extends Application {
private Connection conn;
    public static void main(String[] args) {
        launch(args);    }
    public void start(Stage primaryStage) {
        connectDatabase();
        Button adminButton = new Button("Admin Login");
        Button voterButton = new Button("Voter Login");
        adminButton.setOnAction(e -> showAdminLoginScreen(primaryStage));
        voterButton.setOnAction(e -> showVoterLoginScreen(primaryStage));
        VBox roleSelectionLayout = new VBox(10, adminButton, voterButton);
        roleSelectionLayout.setAlignment(Pos.CENTER);
        Scene roleSelectionScene = new Scene(roleSelectionLayout, 300, 200);
        primaryStage.setScene(roleSelectionScene);
        primaryStage.setTitle("Online Voting System");
        primaryStage.show();
    }    private void connectDatabase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); 
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/votingdb", "root", "1qaz2wsx");
        } catch (ClassNotFoundException e) {
            showAlert("MySQL JDBC Driver not found.");
            e.printStackTrace();:                                                                                                        
        } catch (SQLException e) {
            showAlert("Connection to database failed.");
            e.printStackTrace();        }    }
    private void showAdminLoginScreen(Stage stage) {
        Label adminLabel = new Label("Admin Password:");
        PasswordField adminPasswordField = new PasswordField();
        Button loginButton = new Button("Login");
        loginButton.setOnAction(e -> {
            String adminPassword = adminPasswordField.getText();
            if (authenticateAdmin(adminPassword)) {
                showAdminPanel(stage);
            } else {
                showAlert("Invalid admin password!");           }        });
        VBox adminLoginLayout = new VBox(10, adminLabel, adminPasswordField, loginButton);
        adminLoginLayout.setAlignment(Pos.CENTER);
        Scene adminLoginScene = new Scene(adminLoginLayout, 300, 200);
        stage.setScene(adminLoginScene);    }
    private boolean authenticateAdmin(String password) {
        return "admin123".equals(password);    }
    private void showAdminPanel(Stage stage) {
        Label candidateLabel = new Label("Manage Candidates:");
        ComboBox<String> candidateBox = new ComboBox<>();
        loadCandidates(candidateBox);
        TextField newCandidateField = new TextField();
        newCandidateField.setPromptText("New candidate name");
        Button addCandidateButton = new Button("Add Candidate");
        Button deleteCandidateButton = new Button("Delete Candidate");
        Button showResultsButton = new Button("Show Results");
        addCandidateButton.setOnAction(e -> {
            String newCandidate = newCandidateField.getText();
            if (!newCandidate.isEmpty()) {:                                                                                    
                addCandidate(newCandidate);
                candidateBox.getItems().clear();
                loadCandidates(candidateBox);            }        });
        deleteCandidateButton.setOnAction(e -> {
            String selectedCandidate = candidateBox.getValue();
            if (selectedCandidate != null) {
                deleteCandidate(selectedCandidate);
                candidateBox.getItems().clear();
                loadCandidates(candidateBox);
            } else {
                showAlert("Select a candidate to delete.");            }        });
        showResultsButton.setOnAction(e -> showVoteResults());
        VBox adminLayout = new VBox(10, candidateLabel, candidateBox, newCandidateField, addCandidateButton, deleteCandidateButton, showResultsButton);
        adminLayout.setAlignment(Pos.CENTER);
        Scene adminScene = new Scene(adminLayout, 300, 300);
        stage.setScene(adminScene);
    }    private void addCandidate(String name) {
        try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO Candidates (name, voteCount) VALUES (?, 0)")) {
            stmt.setString(1, name);
            stmt.executeUpdate();
            showAlert("Candidate added successfully!");
        } catch (SQLException e) {
            e.printStackTrace();        }    }
    private void deleteCandidate(String name) {
        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM Candidates WHERE name = ?")) {
            stmt.setString(1, name);
            stmt.executeUpdate();
            showAlert("Candidate deleted successfully!");
        } catch (SQLException e) {
            e.printStackTrace();        }    }:                                                                                   
    private void showVoteResults() {
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT name, voteCount FROM Candidates");
            StringBuilder results = new StringBuilder("Vote Results:\n");
            while (rs.next()) {
                results.append(rs.getString("name")).append(": ").append(rs.getInt("voteCount")).append(" votes\n");            }
            showAlert(results.toString());
        } catch (SQLException e) {
            e.printStackTrace();        }    }
    private void showVoterLoginScreen(Stage stage) {
        Label nameLabel = new Label("Name:");
        TextField nameField = new TextField();
        Label passwordLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();
        Button loginButton = new Button("Login");
        loginButton.setOnAction(e -> {
            String name = nameField.getText();
            String password = passwordField.getText();
            if (authenticateVoter(name, password)) {
                showVotingScreen(stage, name);
            } else {
                showAlert("Invalid credentials or already voted!");            }        });
        VBox voterLoginLayout = new VBox(10, nameLabel, nameField, passwordLabel, passwordField, loginButton);
        voterLoginLayout.setAlignment(Pos.CENTER);
        Scene voterLoginScene = new Scene(voterLoginLayout, 300, 200);
        stage.setScene(voterLoginScene);
    }    private boolean authenticateVoter(String name, String password) {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM Voters WHERE name = ? AND password = ? AND hasVoted = FALSE")) {
            stmt.setString(1, name);                                                                                      
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();        }
        return false;    }
    private void showVotingScreen(Stage stage, String voterName) {
        Label candidateLabel = new Label("Select Candidate:");
        ComboBox<String> candidateBox = new ComboBox<>();
        loadCandidates(candidateBox);
        Button voteButton = new Button("Vote");
        voteButton.setOnAction(e -> {
            String candidate = candidateBox.getValue();
            if (candidate != null) {
                castVote(voterName, candidate);
                showAlert("Vote cast successfully!");
                stage.close();
            } else {
                showAlert("Please select a candidate.");            }        });
        VBox votingLayout = new VBox(10, candidateLabel, candidateBox, voteButton);
        votingLayout.setAlignment(Pos.CENTER);
        Scene votingScene = new Scene(votingLayout, 300, 200);
        stage.setScene(votingScene);    }
    private void loadCandidates(ComboBox<String> candidateBox) {
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT name FROM Candidates");
            while (rs.next()) {
                candidateBox.getItems().add(rs.getString("name"));            }
        } catch (SQLException e) {
            e.printStackTrace();}}
       private void castVote(String voterName, String candidateName) {                             
        try {
            conn.setAutoCommit(false);
            try (PreparedStatement voterStmt = conn.prepareStatement("UPDATE Voters SET hasVoted = TRUE WHERE name = ?")) {
                voterStmt.setString(1, voterName);
                voterStmt.executeUpdate();            }
            try (PreparedStatement candidateStmt = conn.prepareStatement("UPDATE Candidates SET voteCount = voteCount + 1 WHERE name = ?")) {
                candidateStmt.setString(1, candidateName);
                candidateStmt.executeUpdate();            }
            conn.commit();
        } catch (SQLException e) {
            try {
                conn.rollback();
                showAlert("Error casting vote. Changes have been rolled back.");
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();}}}
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(message);
        alert.showAndWait();    }
    public void stop() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();}                                                                                                 
        } catch (SQLException e) {
            e.printStackTrace();}}}
