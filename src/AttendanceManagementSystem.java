import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class AttendanceManagementSystem {
    static class Student {
        String name;
        int id;
        int attendanceCount;

        Student(String name, int id) {
            this.name = name;
            this.id = id;
            this.attendanceCount = 0;
        }
    }

    private static final ArrayList<Student> students = new ArrayList<>();
    private static final HashMap<Integer, Student> studentMap = new HashMap<>();
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        boolean running = true;

        while (running) {
            System.out.println("\n--- Attendance Management System ---");
            System.out.println("1. Add Student");
            System.out.println("2. Mark Attendance");
            System.out.println("3. Display Attendance");
            System.out.println("4. Exit");
            System.out.print("Choose an option: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1 -> addStudent();
                case 2 -> markAttendance();
                case 3 -> displayAttendance();
                case 4 -> running = false;
                default -> System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private static void addStudent() {
        System.out.print("Enter student name: ");
        String name = scanner.nextLine();
        System.out.print("Enter student ID: ");
        int id = scanner.nextInt();

        Student student = new Student(name, id);
        students.add(student);
        studentMap.put(id, student);
        System.out.println("Student added successfully.");
    }

    private static void markAttendance() {
        System.out.print("Enter student ID to mark attendance: ");
        int id = scanner.nextInt();

        Student student = studentMap.get(id);
        if (student != null) {
            student.attendanceCount++;
            System.out.println("Attendance marked for " + student.name + ".");
        } else {
            System.out.println("Student not found.");
        }
    }

    private static void displayAttendance() {
        System.out.println("\n--- Attendance Records ---");
        for (Student student : students) {
            System.out.println("ID: " + student.id + " | Name: " + student.name + " | Attendance: " + student.attendanceCount);
        }
    }
}
