import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class AttendanceManagementSystemDB {
    private Connection conn;
    private JTextField nameField;
    private JTextField idField;
    private JTextArea displayArea;
    private JFrame frame;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(AttendanceManagementSystemDB::new);
    }

    public AttendanceManagementSystemDB() {
        connectToDatabase();
        showLoginGUI();
    }

    private void connectToDatabase() {
        try {
            String url = "jdbc:mysql://localhost:3306/attendance_db";
            String user = "root"; // Your MySQL username
            String password = "Aditya@12345"; // Your MySQL password

            conn = DriverManager.getConnection(url, user, password);
            System.out.println("Database connection successful!");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Database connection failed.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showLoginGUI() {
        JFrame loginFrame = new JFrame("Login");
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setSize(300, 150);
        loginFrame.setLayout(new GridLayout(3, 2));
        loginFrame.getContentPane().setBackground(Color.BLACK); // Black background

        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JButton loginButton = new JButton("Login");

        // Set components colors
        usernameField.setBackground(Color.DARK_GRAY);
        usernameField.setForeground(Color.WHITE);
        passwordField.setBackground(Color.DARK_GRAY);
        passwordField.setForeground(Color.WHITE);
        loginButton.setBackground(Color.GRAY);
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setBorder(BorderFactory.createLineBorder(Color.WHITE)); // White border

        // Add hover effect for button
        loginButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                loginButton.setBackground(Color.LIGHT_GRAY);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                loginButton.setBackground(Color.GRAY);
            }
        });

        loginFrame.add(new JLabel("Username:") {{
            setForeground(Color.WHITE);
        }});
        loginFrame.add(usernameField);
        loginFrame.add(new JLabel("Password:") {{
            setForeground(Color.WHITE);
        }});
        loginFrame.add(passwordField);
        loginFrame.add(new JLabel(""));
        loginFrame.add(loginButton);

        // Add action listener for login button
        loginButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            String role = authenticateUser(username, password);
            if (role != null) {
                loginFrame.dispose();
                if (role.equals("admin")) {
                    setupAdminGUI();
                } else {
                    setupUserGUI();
                }
            } else {
                JOptionPane.showMessageDialog(loginFrame, "Invalid credentials.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Set frame visibility with fade-in effect
        loginFrame.setLocationRelativeTo(null); // Center the login frame
        loginFrame.setVisible(true);
        loginFrame.setOpacity(0.0f); // Start with opacity 0

        // Thread to fade in
        new Thread(() -> {
            for (float i = 0; i <= 1.0; i += 0.05f) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                loginFrame.setOpacity(i);
            }
        }).start(); // Start the fade-in effect in a separate thread
    }

    // Method to authenticate user and return the role (admin or user)
    private String authenticateUser(String username, String password) {
        String sql = "SELECT role FROM users WHERE username = ? AND password = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("role"); // Returns role if found
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Return null if authentication fails
    }

    // GUI for Admin
    // GUI for Admin
    private JComboBox<String> subjectComboBox;

    private void setupAdminGUI() {
        frame = new JFrame("Admin - Attendance Management System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(Color.BLACK); // Black background

        // Create a monochrome panel for input fields
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(7, 2));
        panel.setBackground(Color.BLACK); // Panel background

        // Create and style components
        panel.add(new JLabel("Student Name:") {{
            setForeground(Color.WHITE);
        }});
        nameField = new JTextField();
        nameField.setBackground(Color.DARK_GRAY);
        nameField.setForeground(Color.WHITE);
        panel.add(nameField);

        panel.add(new JLabel("Student ID:") {{
            setForeground(Color.WHITE);
        }});
        idField = new JTextField();
        idField.setBackground(Color.DARK_GRAY);
        idField.setForeground(Color.WHITE);
        panel.add(idField);

        // Add subject selection combo box
        panel.add(new JLabel("Select Subject:") {{
            setForeground(Color.WHITE);
        }});
        subjectComboBox = new JComboBox<>();
        subjectComboBox.setBackground(Color.DARK_GRAY);
        subjectComboBox.setForeground(Color.WHITE);
        populateSubjectComboBox();
        panel.add(subjectComboBox);

        JButton addButton = createStyledButton("Add Student");
        addButton.addActionListener(new AddStudentListener());
        panel.add(addButton);

        JButton presentButton = createStyledButton("Mark Present");
        presentButton.addActionListener(new MarkAttendanceListener("PRESENT"));
        panel.add(presentButton);

        JButton absentButton = createStyledButton("Mark Absent");
        absentButton.addActionListener(new MarkAttendanceListener("ABSENT"));
        panel.add(absentButton);

        JButton displayButton = createStyledButton("Display Attendance");
        displayButton.addActionListener(new DisplayAttendanceListener());
        panel.add(displayButton);

        // Add "Logout" button at the bottom
        JButton logoutButton = createStyledButton("Logout");
        logoutButton.addActionListener(e -> {
            frame.dispose(); // Close the current window
            showLoginGUI(); // Go back to the login page
        });
        panel.add(new JLabel("")); // Empty label for alignment purposes
        panel.add(logoutButton);

        frame.add(panel, BorderLayout.NORTH);

        // Create a text area for displaying attendance records
        displayArea = new JTextArea();
        displayArea.setEditable(false);
        displayArea.setLineWrap(true);
        displayArea.setWrapStyleWord(true);
        displayArea.setBackground(Color.DARK_GRAY);
        displayArea.setForeground(Color.WHITE);
        displayArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        frame.add(new JScrollPane(displayArea), BorderLayout.CENTER);

        // Set frame visibility with fade-in effect
        frame.setVisible(true);
        frame.setOpacity(0.0f);
        for (float i = 0; i <= 1.0; i += 0.05f) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            frame.setOpacity(i);
        }
    }

    private void populateSubjectComboBox() {
        String sql = "SELECT subject_name FROM subjects";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                subjectComboBox.addItem(rs.getString("subject_name"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }



    // GUI for User
    private void setupUserGUI() {
        frame = new JFrame("User - Attendance Management System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(Color.BLACK); // Black background

        // Create a text area for displaying attendance records
        displayArea = new JTextArea();
        displayArea.setEditable(false);
        displayArea.setLineWrap(true);
        displayArea.setWrapStyleWord(true);
        displayArea.setBackground(Color.DARK_GRAY);
        displayArea.setForeground(Color.WHITE);
        displayArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        // Only view attendance
        JButton displayButton = createStyledButton("Display Attendance");
        displayButton.addActionListener(new DisplayAttendanceListener());

        // Create a logout button
        JButton logoutButton = createStyledButton("Logout");
        logoutButton.addActionListener(e -> {
            frame.dispose(); // Close the user frame
            showLoginGUI(); // Show the login frame
        });

        // Create a panel to hold the buttons
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());
        panel.setBackground(Color.BLACK); // Panel background
        panel.add(displayButton);
        panel.add(logoutButton);

        // Add panel and display area to the frame
        frame.add(panel, BorderLayout.NORTH);
        frame.add(new JScrollPane(displayArea), BorderLayout.CENTER); // Add the text area with a scroll pane

        // Set frame visibility with fade-in effect
        frame.setVisible(true);
        frame.setOpacity(0.0f);
        for (float i = 0; i <= 1.0; i += 0.05f) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            frame.setOpacity(i);
        }
    }



    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(Color.GRAY);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(Color.WHITE)); // White border

        // Add hover effect for button
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(Color.LIGHT_GRAY);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(Color.GRAY);
            }
        });

        return button;
    }

    private class AddStudentListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String name = nameField.getText();
            int id;
            try {
                id = Integer.parseInt(idField.getText());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Invalid ID. Please enter a number.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String sql = "INSERT INTO students (id, name, attendance_status) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                stmt.setString(2, name);
                stmt.setString(3, "ABSENT"); // Default status is 'ABSENT'
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(frame, "Student added successfully!");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(frame, "Error adding student to the database.", "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }

            nameField.setText("");
            idField.setText("");
        }
    }

    private class MarkAttendanceListener implements ActionListener {
        private final String status;

        public MarkAttendanceListener(String status) {
            this.status = status;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            int id;
            try {
                id = Integer.parseInt(idField.getText());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Invalid ID. Please enter a number.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String subjectName = (String) subjectComboBox.getSelectedItem();
            String sqlGetSubjectId = "SELECT subject_id FROM subjects WHERE subject_name = ?";
            int subjectId = 0;
            try (PreparedStatement stmt = conn.prepareStatement(sqlGetSubjectId)) {
                stmt.setString(1, subjectName);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        subjectId = rs.getInt("subject_id");
                    }
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

            String sql = "INSERT INTO attendance (student_id, subject_id, attendance_status, date_time) VALUES (?, ?, ?, CURRENT_TIMESTAMP)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                stmt.setInt(2, subjectId);
                stmt.setString(3, status);
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(frame, "Attendance marked as " + status + " for subject: " + subjectName);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(frame, "Error marking attendance.", "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }

            idField.setText("");
        }
    }


    private class DisplayAttendanceListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // SQL query to retrieve student info along with subject info
            String sql = "SELECT s.id AS student_id, s.name AS student_name, sub.subject_name, a.attendance_status, a.date_time " +
                    "FROM attendance a " +
                    "JOIN students s ON a.student_id = s.id " +
                    "JOIN subjects sub ON a.subject_id = sub.subject_id";

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                // StringBuilder to collect attendance records
                StringBuilder attendanceRecords = new StringBuilder("Attendance Records (Subject-wise):\n");
                while (rs.next()) {
                    int studentId = rs.getInt("student_id");
                    String studentName = rs.getString("student_name");
                    String subjectName = rs.getString("subject_name");
                    String attendanceStatus = rs.getString("attendance_status");
                    Timestamp dateTime = rs.getTimestamp("date_time");

                    // Append each attendance record to the string builder
                    attendanceRecords.append("Student ID: ").append(studentId)
                            .append(" | Name: ").append(studentName)
                            .append(" | Subject: ").append(subjectName)
                            .append(" | Status: ").append(attendanceStatus)
                            .append(" | Date & Time: ").append(dateTime)
                            .append("\n");
                }

                // Update the display area with the collected records
                displayArea.setText(attendanceRecords.toString());
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(frame, "Error retrieving attendance records.", "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }


}