import java.io.*;
import java.util.Scanner;

public class SQSystem {
    static final int MAX_STUDENTS = 100;
    static final int MAX_QUEUE = 5;

    static final String STUDENT_FILE = "students.txt";
    static final String RECORD_FILE = "queueing_records.txt";

    static Scanner input = new Scanner(System.in);

    static class Student {
        String studentNumber;
        String lastName;
        String firstName;
        String middleInitial;
        String course;
        String yearLevel;

        Student(String studentNumber, String lastName, String firstName,
                String middleInitial, String course, String yearLevel) {
            this.studentNumber = studentNumber;
            this.lastName = lastName;
            this.firstName = firstName;
            this.middleInitial = middleInitial;
            this.course = course;
            this.yearLevel = yearLevel;
        }

        String getFullName() {
            if (middleInitial == null || middleInitial.trim().isEmpty()
                    || middleInitial.equals("-")) {
                return firstName + " " + lastName;
            }
            return firstName + " " + middleInitial + ". " + lastName;
        }
    }

    static class QueueEntry {
        Student student;
        String service;
        String status;

        QueueEntry(Student student) {
            this.student = student;
            this.service = "";
            this.status = "WAITING FOR SERVICE";
        }
    }

    static Student[] students = new Student[MAX_STUDENTS];
    static int studentCount = 0;

    // The general queue can hold a maximum of five students.
    static QueueEntry[] queue = new QueueEntry[MAX_QUEUE];
    static int queueCount = 0;

    // Each window can serve one student at a time.
    static QueueEntry window1 = null;
    static QueueEntry window2 = null;

    public static void main(String[] args) {
        loadStudents();

        while (true) {
            System.out.println("\n========================================");
            System.out.println("       REGISTRAR QUEUEING SYSTEM");
            System.out.println("========================================");
            System.out.println("1. Sign In");
            System.out.println("2. Exit");
            System.out.print("Choose an option: ");
            String choice = input.nextLine().trim();

            if (choice.equals("1")) {
                if (login()) {
                    mainMenu();
                }
            } else if (choice.equals("2")) {
                System.out.println("System closed.");
                break;
            } else {
                System.out.println("\u001B[31mInvalid choice. Please select 1 or 2.\u001B[0m");
            }
        }
        input.close();
    }

    static boolean login() {
        // One fixed account only. No user registration or Add User feature.
        final String ADMIN_USERNAME = "admin";
        final String ADMIN_PASSWORD = "admin123";

        System.out.println("\n------------- SIGN IN -------------");
        System.out.print("Username: ");
        String username = input.nextLine().trim();
        System.out.print("Password: ");
        String password = input.nextLine().trim();

        if (username.equals(ADMIN_USERNAME) && password.equals(ADMIN_PASSWORD)) {
            System.out.println("\u001B[32mLogin successful. Welcome, Admin!\u001B[0m");
            return true;
        }

        System.out.println("\u001B[31mIncorrect username or password.\u001B[0m");
        return false;
    }

    static void mainMenu() {
        boolean loggedIn = true;

        while (loggedIn) {
            System.out.println("\n========================================");
            System.out.println("             MAIN MENU");
            System.out.println("========================================");
            System.out.println("1. Add Queue");
            System.out.println("2. Add Service");
            System.out.println("3. View Line / Waiting List");
            System.out.println("4. Serve for Window 1");
            System.out.println("5. Serve for Window 2");
            System.out.println("6. Done Service for Window 1");
            System.out.println("7. Done Service for Window 2");
            System.out.println("8. Record of Queueing");
            System.out.println("9. Logout");
            System.out.print("Choose an option: ");

            String choice = input.nextLine().trim();

            switch (choice) {
                case "1":
                    addQueue();
                    break;
                case "2":
                    addService();
                    break;
                case "3":
                    viewWaitingList();
                    break;
                case "4":
                    serveWindow(1);
                    break;
                case "5":
                    serveWindow(2);
                    break;
                case "6":
                    doneService(1);
                    break;
                case "7":
                    doneService(2);
                    break;
                case "8":
                    showQueueingRecords();
                    break;
                case "9":
                    loggedIn = false;
                    System.out.println("\u001B[32mYou have logged out.\u001B[0m");
                    break;
                default:
                    System.out.println("\u001B[31mInvalid choice. Please select 1 to 9.\u001B[0m");
            }
        }
    }

    static void loadStudents() {
        studentCount = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(STUDENT_FILE))) {
            String line;

            while ((line = reader.readLine()) != null && studentCount < MAX_STUDENTS) {
                if (line.trim().isEmpty() || line.startsWith("#")) {
                    continue;
                }

                String[] data = line.split("\\|", -1);
                if (data.length >= 6) {
                    students[studentCount] = new Student(
                            data[0].trim(), data[1].trim(), data[2].trim(),
                            data[3].trim(), data[4].trim(), data[5].trim());
                    studentCount++;
                }
            }
        } catch (IOException e) {
            System.out.println("\u001B[31mCould not read " + STUDENT_FILE + ".\u001B[0m");
            System.out.println("Make sure the file is in the project folder.");
        }
    }

    static void addQueue() {
        loadStudents();

        if (queueCount >= MAX_QUEUE) {
            System.out.println("\u001B[33mThe general queue is full. Maximum is 5 students.\u001B[0m");
            displayQueue();
            return;
        }

        if (studentCount == 0) {
            System.out.println("\u001B[36mNo student records found in "
                    + STUDENT_FILE + ".\u001B[0m");
            return;
        }

        System.out.println("\n--------------- STUDENT LIST ---------------");
        System.out.printf("%-4s %-15s %-25s %-15s %-10s%n",
                "No.", "Student Number", "Student Name", "Course", "Year");
        for (int i = 0; i < studentCount; i++) {
            System.out.printf("%-4d %-15s %-25s %-15s %-10s%n",
                    i + 1, students[i].studentNumber, students[i].getFullName(),
                    students[i].course, students[i].yearLevel);
        }

        System.out.print("Enter student number to add to queue: ");
        String number = input.nextLine().trim();
        Student selected = findStudent(number);

        if (selected == null) {
            System.out.println("\u001B[31mStudent number not found in the file.\u001B[0m");
            return;
        }

        if (isStudentInQueue(number)) {
            System.out.println("\u001B[33mThis student is already in the queue.\u001B[0m");
            return;
        }

        queue[queueCount] = new QueueEntry(selected);
        queueCount++;
        appendRecord("ADDED TO QUEUE", selected, "-");
        System.out.println("\u001B[32m " + selected.getFullName() + " has been added to the queue.\u001B[0m");
        displayQueue();
    }

    static void addService() {
        if (queueCount == 0) {
            System.out.println("\u001B[33mThe queue is empty. Use Add Queue first.\u001B[0m");
            return;
        }

        boolean hasUnassigned = false;
        System.out.println("\n-------- STUDENTS WAITING FOR SERVICE --------");
        for (int i = 0; i < queueCount; i++) {
            if (queue[i].service.isEmpty()) {
                hasUnassigned = true;
                System.out.printf("%d. %s | Student No.: %s%n",
                        i + 1, queue[i].student.getFullName(),
                        queue[i].student.studentNumber);
            }
        }

        if (!hasUnassigned) {
            System.out.println("\u001B[33mAll students in the queue already have a service.\u001B[0m");
            return;
        }

        System.out.print("Enter student number: ");
        String number = input.nextLine().trim();
        QueueEntry selected = null;

        for (int i = 0; i < queueCount; i++) {
            if (queue[i].student.studentNumber.equalsIgnoreCase(number)
                    && queue[i].service.isEmpty()) {
                selected = queue[i];
                break;
            }
        }

        if (selected == null) {
            System.out.println("\u001B[33mStudent not found in queue or service is already assigned.\u001B[0m");
            return;
        }

        System.out.println("\n------------ REGISTRAR SERVICES ------------");
        System.out.println("1. Requesting of Diploma");
        System.out.println("2. Requesting of Transcript of Records (TOR)");
        System.out.println("3. Requesting of Certificate of Enrollment");
        System.out.println("4. Requesting of Good Moral Certificate");
        System.out.println("5. Requesting of Grades");
        System.out.println("6. Other Registrar Concern");
        System.out.print("Choose service: ");
        String choice = input.nextLine().trim();

        String service;
        switch (choice) {
            case "1":
                service = "Requesting of Diploma";
                break;
            case "2":
                service = "Requesting of TOR";
                break;
            case "3":
                service = "Certificate of Enrollment";
                break;
            case "4":
                service = "Good Moral Certificate";
                break;
            case "5":
                service = "Requesting of Grades";
                break;
            case "6":
                System.out.print("Enter service description: ");
                service = input.nextLine().trim();
                if (service.isEmpty()) {
                    System.out.println("\u001B[31mService description cannot be empty.\u001B[0m");
                    return;
                }
                break;
            default:
                System.out.println("\u001B[31mInvalid service choice.\u001B[0m");
                return;
        }

        selected.service = service;
        selected.status = "WAITING FOR WINDOW";
        appendRecord("SERVICE ADDED", selected.student, service);
        System.out.println("\u001B[32mService added. The student is now waiting for an available window.\u001B[0m");
        displayQueue();
        displayWindows();
    }

    static void viewWaitingList() {
        System.out.println("\n========================================");
        System.out.println("        LINE / WAITING LIST");
        System.out.println("========================================");

        if (queueCount == 0) {
            System.out.println("\u001B[36mNo students are currently waiting.\u001B[0m");
        } else {
            System.out.println("Queue Capacity: " + queueCount + "/5");
            System.out.println("----------------------------------------");

            for (int i = 0; i < queueCount; i++) {
                QueueEntry entry = queue[i];

                String service = entry.service.isEmpty()
                        ? "Service not assigned"
                        : entry.service;

                System.out.println("Queue Number: " + (i + 1));
                System.out.println("Student No.: "
                        + entry.student.studentNumber);
                System.out.println("Student Name: "
                        + entry.student.getFullName());
                System.out.println("Course: "
                        + entry.student.course);
                System.out.println("Year Level: "
                        + entry.student.yearLevel);
                System.out.println("Service: " + service);
                System.out.println("Status: " + entry.status);
                System.out.println("----------------------------------------");
            }
        }

        System.out.println("\n========== WINDOW STATUS ==========");

        if (window1 == null) {
            System.out.println("\u001B[36mWindow 1: AVAILABLE\u001B[0m");
        } else {
            System.out.println("Window 1: BUSY");
            System.out.println("Student: "
                    + window1.student.getFullName());
            System.out.println("Service: " + window1.service);
        }

        System.out.println();

        if (window2 == null) {
            System.out.println("\u001B[36mWindow 2: AVAILABLE\u001B[0m");
        } else {
            System.out.println("Window 2: BUSY");
            System.out.println("Student: "
                    + window2.student.getFullName());
            System.out.println("Service: " + window2.service);
        }

        System.out.println("==================================");
    }

    static void serveWindow(int windowNumber) {
        QueueEntry current = (windowNumber == 1) ? window1 : window2;

        if (current != null) {
            System.out.println("\u001B[33mWindow " + windowNumber
                    + " is currently busy serving "
                    + current.student.getFullName() + ".\u001B[0m");
            return;
        }

        int nextIndex = -1;

        // Find the first student waiting for a window.
        for (int i = 0; i < queueCount; i++) {
            if (!queue[i].service.isEmpty()
                    && queue[i].status.equals("WAITING FOR WINDOW")) {
                nextIndex = i;
                break;
            }
        }

        if (nextIndex == -1) {
            System.out.println("\u001B[36mNo student with an assigned service is waiting.\u001B[0m");
            displayWindows();
            return;
        }

        // Assign the first eligible student to the selected window.
        QueueEntry next = queue[nextIndex];
        next.status = "IN WINDOW " + windowNumber;

        if (windowNumber == 1) {
            window1 = next;
        } else {
            window2 = next;
        }

        // Remove the student from the general waiting list.
        for (int i = nextIndex; i < queueCount - 1; i++) {
            queue[i] = queue[i + 1];
        }

        queue[queueCount - 1] = null;
        queueCount--;

        appendRecord("SERVED AT WINDOW " + windowNumber,
                next.student, next.service);

        System.out.println("\u001B[32mStudent assigned successfully!\u001B[0m");
        System.out.println("Window: " + windowNumber);
        System.out.println("Student: " + next.student.getFullName());
        System.out.println("Service: " + next.service);

        displayQueue();
        displayWindows();
    }

    static void doneService(int windowNumber) {
        QueueEntry current = (windowNumber == 1) ? window1 : window2;

        if (current == null) {
            System.out.println("\u001B[33mWindow " + windowNumber
                    + " is already available.\u001B[0m");
            displayWindows();
            return;
        }

        appendRecord("SERVICE COMPLETED AT WINDOW " + windowNumber,
                current.student, current.service);
        System.out.println("\u001B[32mService completed for " + current.student.getFullName() + " at Window "
                + windowNumber + ".\u001B[0m");

        removeFromQueue(current);

        if (windowNumber == 1) {
            window1 = null;
        } else {
            window2 = null;
        }

        displayWindows();
        displayQueue();
    }

    static void removeFromQueue(QueueEntry entry) {
        for (int i = 0; i < queueCount; i++) {
            if (queue[i] == entry) {
                for (int j = i; j < queueCount - 1; j++) {
                    queue[j] = queue[j + 1];
                }
                queue[queueCount - 1] = null;
                queueCount--;
                return;
            }
        }
    }

    static void displayQueue() {
        System.out.println("\n============= GENERAL QUEUE =============");
        System.out.println("Waiting list capacity: " + queueCount + "/" + MAX_QUEUE);

        if (queueCount == 0) {
            System.out.println("No students in the queue.");
            return;
        }

        for (int i = 0; i < queueCount; i++) {
            String serviceText = queue[i].service.isEmpty()
                    ? "Service not assigned"
                    : queue[i].service;
            System.out.printf("%d. %-22s | %-30s | %s%n",
                    i + 1, queue[i].student.getFullName(),
                    serviceText, queue[i].status);
        }
    }

    static void displayWindows() {
        System.out.println("\n=============== SERVICE WINDOWS ===============");
        displayOneWindow(1, window1);
        displayOneWindow(2, window2);
    }

    static void displayOneWindow(int number, QueueEntry entry) {
        if (entry == null) {
            System.out.println("\u001B[36mWindow " + number
                    + ": AVAILABLE\u001B[0m");
        } else {
            System.out.println("Window " + number + ": BUSY");
            System.out.println("  Student: " + entry.student.getFullName());
            System.out.println("  Service: " + entry.service);
        }
    }

    static void showQueueingRecords() {
        System.out.println("\n\u001B[35m=============== QUEUEING RECORDS ===============\u001B[0m");
        File file = new File(RECORD_FILE);

        if (!file.exists() || file.length() == 0) {
            System.out.println("\u001B[35mNo queueing records yet.\u001B[0m");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            System.out.println("\u001B[31mUnable to read queueing records.\u001B[0m");
        }
    }

    static void appendRecord(String action, Student student, String service) {
        try (FileWriter writer = new FileWriter(RECORD_FILE, true);
                BufferedWriter bufferedWriter = new BufferedWriter(writer)) {
            bufferedWriter.write(action + " | Student No.: " + student.studentNumber
                    + " | Name: " + student.getFullName()
                    + " | Course: " + student.course
                    + " | Year: " + student.yearLevel
                    + " | Service: " + service);
            bufferedWriter.newLine();
        } catch (IOException e) {
            System.out.println("\u001B[31mCould not save queueing record.\u001B[0m");
        }
    }

    static Student findStudent(String number) {
        for (int i = 0; i < studentCount; i++) {
            if (students[i].studentNumber.equalsIgnoreCase(number)) {
                return students[i];
            }
        }
        return null;
    }

    static boolean isStudentInQueue(String number) {
        for (int i = 0; i < queueCount; i++) {
            if (queue[i].student.studentNumber.equalsIgnoreCase(number)) {
                return true;
            }
        }
        return false;
    }
}