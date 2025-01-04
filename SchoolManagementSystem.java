import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.stream.Collectors;

public class SchoolManagementSystem {
    static final String DATA_FILE = "schoolData.txt";
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            University university = new University();
            university.loadData(DATA_FILE);
            
            SchoolManagementSystemGUI gui = new SchoolManagementSystemGUI(university);
            gui.setVisible(true);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                university.saveData(DATA_FILE);
            }));
        });
    }
}

class SchoolManagementSystemGUI extends JFrame {
    private static final long serialVersionUID = 1L;
    private University university;

    public SchoolManagementSystemGUI(University university) {
        this.university = university;
        initializeUI();
    }

    private void initializeUI() {
        setTitle("School Management System");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());

        JLabel headerLabel = new JLabel("School Management System", JLabel.CENTER);
        mainPanel.add(headerLabel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(4, 2, 10, 10));

        String[] buttonLabels = {
            "Manage Students", "Manage Teachers", "Manage Courses", "Manage Administrative Staff",
            "System Statistics", "Search Student", "Generate Reports", "Exit"
        };

        for (String label : buttonLabels) {
            JButton button = new JButton(label);
            button.addActionListener(new ButtonClickListener());
            centerPanel.add(button);
        }

        mainPanel.add(centerPanel, BorderLayout.CENTER);
        add(mainPanel);
    }

    private class ButtonClickListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            switch (command) {
                case "Manage Students":
                    manageStudents();
                    break;
                case "Manage Teachers":
                    manageTeachers();
                    break;
                case "Manage Courses":
                    manageCourses();
                    break;
                case "Manage Administrative Staff":
                    manageAdministrativeStaff();
                    break;
                case "System Statistics":
                    displaySystemStats();
                    break;
                case "Search Student":
                    searchStudent();
                    break;
                case "Generate Reports":
                    generateReports();
                    break;
                case "Exit":
                    university.saveData(SchoolManagementSystem.DATA_FILE);
                    System.exit(0);
                    break;
            }
        }
    }
    private void manageStudents() {
        String[] options = {"Add Student", "Remove Student", "Display Students", "Enroll in Course"};
        int choice = JOptionPane.showOptionDialog(this, "Choose an action:", "Manage Students",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

        switch (choice) {
            case 0:
                addStudent();
                break;
            case 1:
                removeStudent();
                break;
            case 2:
                displayStudents();
                break;

            case 3:
                enrollStudentInCourse();
                break;
        }
    }
    private void addStudent() {
        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));

        panel.add(new JLabel("Name:"));
        JTextField nameField = new JTextField();
        panel.add(nameField);

        panel.add(new JLabel("Email:"));
        JTextField emailField = new JTextField();
        panel.add(emailField);

        panel.add(new JLabel("City:"));
        JTextField cityField = new JTextField();
        panel.add(cityField);

        panel.add(new JLabel("House Number:"));
        JTextField houseNumberField = new JTextField();
        panel.add(houseNumberField);

        panel.add(new JLabel("Date of Birth (yyyy-MM-dd):"));
        JTextField dobField = new JTextField();
        panel.add(dobField);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Add New Student",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String city = cityField.getText().trim();
            String houseNumberText = houseNumberField.getText().trim();
            String dobString = dobField.getText().trim();

            if (name.isEmpty() || email.isEmpty() || city.isEmpty() || houseNumberText.isEmpty() || dobString.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int houseNumber;
            try {
                houseNumber = Integer.parseInt(houseNumberText);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "House number must be a valid integer.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Date dateOfBirth;
            try {
                dateOfBirth = new SimpleDateFormat("yyyy-MM-dd").parse(dobString);
            } catch (ParseException e) {
                JOptionPane.showMessageDialog(this, "Invalid date format. Please use yyyy-MM-dd.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int id = university.getStudentRepository().size() + 1;
            Student student = new Student(name, id, email, city, houseNumber, dateOfBirth, 0.0);
            university.getStudentRepository().add(student);
            JOptionPane.showMessageDialog(this, "Student added: " + student);
        }
    }

    private void removeStudent() {
        String idStr = JOptionPane.showInputDialog(this, "Enter Student ID to remove:");
        if (idStr != null && !idStr.trim().isEmpty()) {
            try {
                int id = Integer.parseInt(idStr);
                Student student = university.getStudentRepository().findById(id);
                if (student != null) {
                    university.getStudentRepository().remove(student);
                    // Remove student from all courses
                    for (Course course : university.getCourseRepository().getAll()) {
                        course.removeStudent(student);
                    }
                    JOptionPane.showMessageDialog(this, "Student removed: " + student);
                } else {
                    JOptionPane.showMessageDialog(this, "Student with ID " + id + " not found.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid ID format. Please enter a numeric ID.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void displayStudents() {
        List<Student> students = university.getStudentRepository().getAll();
        if (students.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No students found.", "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        StringBuilder sb = new StringBuilder("Students:\n");
        for (Student s : students) {
            sb.append(s).append("\n");
        }
        JOptionPane.showMessageDialog(this, sb.toString(), "All Students", JOptionPane.INFORMATION_MESSAGE);
    }

    private void enrollStudentInCourse() {
        String studentIdStr = JOptionPane.showInputDialog(this, "Enter student ID:");
        String courseIdStr = JOptionPane.showInputDialog(this, "Enter course ID:");
        String marksStr = JOptionPane.showInputDialog(this, "Enter Marks:");
        if (studentIdStr != null && courseIdStr != null && marksStr != null) {
            try {
                int studentId = Integer.parseInt(studentIdStr);
                int courseId = Integer.parseInt(courseIdStr);
                double marks = Double.parseDouble(marksStr);
                Student student = university.getStudentRepository().findById(studentId);
                Course course = university.getCourseRepository().findById(courseId);
                if (student != null && course != null) {
                    student.addCourse(course, marks);
                    course.addStudent(student);
                    JOptionPane.showMessageDialog(this, "Student enrolled in course successfully.");
                } else {
                    JOptionPane.showMessageDialog(this, "Student or Course not found.");
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid input. Please enter numeric values for IDs and marks.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void manageTeachers() {
        String[] options = {"Add Teacher", "Remove Teacher", "Display Teachers", "Assign Course"};
        int choice = JOptionPane.showOptionDialog(this, "Choose an action:", "Manage Teachers",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

        switch (choice) {
            case 0:
                addTeacher();
                break;
            case 1:
                removeTeacher();
                break;
            case 2:
                displayTeachers();
                break;
            case 3:
                assignCourseToTeacher();
                break;
        }
    }

    private void addTeacher() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));

        panel.add(new JLabel("Teacher Name:"));
        JTextField nameField = new JTextField();
        panel.add(nameField);

        panel.add(new JLabel("Specialization:"));
        JTextField specializationField = new JTextField();
        panel.add(specializationField);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Add Teacher",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String specialization = specializationField.getText().trim();

            if (!name.isEmpty() && !specialization.isEmpty()) {
                int id = university.getTeacherRepository().size() + 1;
                Teacher teacher = new Teacher(name, id, specialization);
                university.getTeacherRepository().add(teacher);
                JOptionPane.showMessageDialog(this, "Teacher added: " + teacher);
            } else {
                JOptionPane.showMessageDialog(this, "All fields must be filled.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void removeTeacher() {
        String idStr = JOptionPane.showInputDialog(this, "Enter Teacher ID to remove:");
        if (idStr != null && !idStr.trim().isEmpty()) {
            try {
                int id = Integer.parseInt(idStr);
                Teacher teacher = university.getTeacherRepository().findById(id);
                if (teacher != null) {
                    university.getTeacherRepository().remove(teacher);
                    // Remove teacher from all courses
                    for (Course course : university.getCourseRepository().getAll()) {
                        if (course.getTeacher() != null && course.getTeacher().getTeacherID() == id) {
                            course.setTeacher(null);
                        }
                    }
                    JOptionPane.showMessageDialog(this, "Teacher removed: " + teacher);
                } else {
                    JOptionPane.showMessageDialog(this, "Teacher with ID " + id + " not found.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid ID format. Please enter a numeric ID.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void displayTeachers() {
        List<Teacher> teachers = university.getTeacherRepository().getAll();
        if (teachers.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No teachers found.", "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        StringBuilder sb = new StringBuilder("Teachers:\n");
        for (Teacher t : teachers) {
            sb.append("Teacher: ").append(t.getName())
              .append(" (ID: ").append(t.getTeacherID())
              .append(", Specialization: ").append(t.getSpecialization())
              .append(")\n");
    
            // Display assigned courses
            sb.append("   Assigned Courses: ");
            if (t.getCourses().isEmpty()) {
                sb.append("None");
            } else {
                for (Course c : t.getCourses()) {
                    sb.append(c.getTitle()).append(" (ID: ").append(c.getId()).append(")\n");
                    
                    // Display students enrolled in the course
                    sb.append("      Enrolled Students: ");
                    if (c.getStudents().isEmpty()) {
                        sb.append("None");
                    } else {
                        for (Student s : c.getStudents()) {
                            sb.append(s.getName()).append(" (ID: ").append(s.getId()).append("), ");
                        }
                        sb.setLength(sb.length() - 2); // Remove trailing comma and space
                    }
                    sb.append("\n");
                }
        
            }
            sb.append("\n");
        }
        JOptionPane.showMessageDialog(this, sb.toString(), "All Teachers", JOptionPane.INFORMATION_MESSAGE);
    }

    private void assignCourseToTeacher() {
        String teacherIdStr = JOptionPane.showInputDialog(this, "Enter teacher ID:");
        String courseIdStr = JOptionPane.showInputDialog(this, "Enter course ID:");
        if (teacherIdStr != null && courseIdStr != null) {
            try {
                int teacherId = Integer.parseInt(teacherIdStr);
                int courseId = Integer.parseInt(courseIdStr);
                Teacher teacher = university.getTeacherRepository().findById(teacherId);
                Course course = university.getCourseRepository().findById(courseId);
                if (teacher != null && course != null) {
                    teacher.addCourse(course);
                    course.addTeacher(teacher); // Use the new method to add a teacher
                    JOptionPane.showMessageDialog(this, "Course assigned to teacher successfully.");
                } else {
                    JOptionPane.showMessageDialog(this, "Teacher or Course not found.");
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid ID format. Please enter numeric IDs.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ... existing code ...

private void manageCourses() {
    String[] options = {"Add Course", "Remove Course", "Display Courses", "Add Student Marks", "Calculate Average Grade", "List Teachers in Course"};
    int choice = JOptionPane.showOptionDialog(this, "Choose an action:", "Manage Courses",
            JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

    switch (choice) {
        case 0:
            addCourse();
            break;
        case 1:
            removeCourse();
            break;
        case 2:
            displayCourses();
            break;
        case 3:
            addStudentMarks();
            break;
        case 4:
            calculateAverageGrade();
            break;
        case 5:
            listTeachersInCourse();
            break;
    }
}

private void listTeachersInCourse() {
    String courseName = JOptionPane.showInputDialog(this, "Enter Course Name:");
    if (courseName != null && !courseName.trim().isEmpty()) {
        List<Course> courses = university.getCourseRepository().getAll();
        Course foundCourse = courses.stream()
                .filter(course -> course.getTitle().equalsIgnoreCase(courseName))
                .findFirst()
                .orElse(null);

        if (foundCourse != null) {
            Teacher teacher = foundCourse.getTeacher();
            if (teacher == null) {
                JOptionPane.showMessageDialog(this, "No teacher assigned to this course.", "Information", JOptionPane.INFORMATION_MESSAGE);
            } else {
                StringBuilder sb = new StringBuilder("Teacher for " + foundCourse.getTitle() + ":\n");
                sb.append("Name: ").append(teacher.getName())
                  .append(", ID: ").append(teacher.getTeacherID()).append("\n");
                JOptionPane.showMessageDialog(this, sb.toString(), "Teacher in Course", JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Course not found.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

// ... existing code ...

    private void addCourse() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));

        panel.add(new JLabel("Course Title:"));
        JTextField titleField = new JTextField();
        panel.add(titleField);

        panel.add(new JLabel("Credits:"));
        JTextField creditsField = new JTextField();
        panel.add(creditsField);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Add Course",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            String title = titleField.getText().trim();
            String creditsStr = creditsField.getText().trim();

            if (!title.isEmpty() && !creditsStr.isEmpty()) {
                try {
                    int credits = Integer.parseInt(creditsStr);
                    int id = university.getCourseRepository().size() + 1;
                    Course course = new Course(id, title, credits);
                    university.getCourseRepository().add(course);
                    JOptionPane.showMessageDialog(this, "Course added: " + course.getTitle());
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Invalid input for credits. Please enter a numeric value.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "All fields must be filled.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void removeCourse() {
        String idStr = JOptionPane.showInputDialog(this, "Enter Course ID to remove:");
        if (idStr != null && !idStr.trim().isEmpty()) {
            try {
                int id = Integer.parseInt(idStr);
                Course course = university.getCourseRepository().findById(id);
                if (course != null) {
                    university.getCourseRepository().remove(course);
                    // Remove course from all students
                    for (Student student : university.getStudentRepository().getAll()) {
                        student.removeCourse(course);
                    }
                    // Remove course from all teachers
                    for (Teacher teacher : university.getTeacherRepository().getAll()) {
                        teacher.removeCourse(id);
                    }
                    JOptionPane.showMessageDialog(this, "Course removed: " + course.getTitle());
                } else {
                    JOptionPane.showMessageDialog(this, "Course with ID " + id + " not found.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid ID format. Please enter a numeric ID.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void displayCourses() {
        List<Course> courses = university.getCourseRepository().getAll();
        if (courses.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No courses found.", "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        StringBuilder sb = new StringBuilder("Courses:\n");
        for (Course c : courses) {
            sb.append(c.getTitle()).append(" (ID: ").append(c.getId()).append(")\n");
        }
        JOptionPane.showMessageDialog(this, sb.toString(), "All Courses", JOptionPane.INFORMATION_MESSAGE);
    }

    private void addStudentMarks() {
        String courseIdStr = JOptionPane.showInputDialog(this, "Enter Course ID:");
        String studentIdStr = JOptionPane.showInputDialog(this, "Enter Student ID:");
        String marksStr = JOptionPane.showInputDialog(this, "Enter Marks:");

        if (courseIdStr != null && studentIdStr != null && marksStr != null) {
            try {
                int courseId = Integer.parseInt(courseIdStr);
                int studentId = Integer.parseInt(studentIdStr);
                double marks = Double.parseDouble(marksStr);

                Course course = university.getCourseRepository().findById(courseId);
                Student student = university.getStudentRepository().findById(studentId);

                if (course != null && student != null) {
                    student.addCourse(course, marks);
                    course.addStudent(student);
                    JOptionPane.showMessageDialog(this, "Marks added successfully.");
                } else {
                    JOptionPane.showMessageDialog(this, "Course or Student not found.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid input. Please enter numeric values for IDs and marks.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void calculateAverageGrade() {
        String courseIdStr = JOptionPane.showInputDialog(this, "Enter course ID:");

        if (courseIdStr != null && !courseIdStr.trim().isEmpty()) {
            try {
                int courseId = Integer.parseInt(courseIdStr);
                Course course = university.getCourseRepository().findById(courseId);

                if (course != null) {
                    double averageGrade = course.averageGrade();
                    double medianGrade = course.calculateMedianGrade();
                    JOptionPane.showMessageDialog(this,
                            String.format("Course: %s\nAverage grade: %.2f\nMedian grade: %.2f",
                                    course.getTitle(), averageGrade, medianGrade));
                } else {
                    JOptionPane.showMessageDialog(this, "Course not found.");
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid course ID. Please enter a number.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void manageAdministrativeStaff() {
        String[] options = {"Add Staff", "Remove Staff", "Display Staff"};
        int choice = JOptionPane.showOptionDialog(this, "Choose an action:", "Manage Administrative Staff",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

        switch (choice) {
            case 0:
                addAdministrativeStaff();
                break;
            case 1:
                removeAdministrativeStaff();
                break;
            case 2:
                displayAdministrativeStaff();
                break;
        }
    }

    private void addAdministrativeStaff() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));

        panel.add(new JLabel("Staff Name:"));
        JTextField nameField = new JTextField();
        panel.add(nameField);

        panel.add(new JLabel("Role:"));
        JTextField roleField = new JTextField();
        panel.add(roleField);

        panel.add(new JLabel("Department:"));
        JTextField departmentField = new JTextField();
        panel.add(departmentField);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Add Administrative Staff",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String role = roleField.getText().trim();
            String department = departmentField.getText().trim();

            if (!name.isEmpty() && !role.isEmpty() && !department.isEmpty()) {
                int id = university.getAdministrativeStaffRepository().size() + 1;
                AdministrativeStaff staff = new AdministrativeStaff(id, name, role, department);
                university.getAdministrativeStaffRepository().add(staff);
                JOptionPane.showMessageDialog(this, "Administrative Staff added: " + staff);
            } else {
                JOptionPane.showMessageDialog(this, "All fields must be filled.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void removeAdministrativeStaff() {
        String idStr = JOptionPane.showInputDialog(this, "Enter staff ID to remove:");
        if (idStr != null && !idStr.trim().isEmpty()) {
            try {
                int id = Integer.parseInt(idStr);
                AdministrativeStaff staff = university.getAdministrativeStaffRepository().findById(id);
                if (staff != null) {
                    university.getAdministrativeStaffRepository().remove(staff);
                    JOptionPane.showMessageDialog(this, "Administrative Staff removed: " + staff);
                } else {
                    JOptionPane.showMessageDialog(this, "Administrative Staff with ID " + id + " not found.");
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid ID format. Please enter a numeric ID.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void displayAdministrativeStaff() {
        List<AdministrativeStaff> staffList = university.getAdministrativeStaffRepository().getAll();
        if (staffList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No administrative staff found.", "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        StringBuilder sb = new StringBuilder("Administrative Staff:\n");
        for (AdministrativeStaff staff : staffList) {
            sb.append(staff).append("\n");
        }
        JOptionPane.showMessageDialog(this, sb.toString(), "All Administrative Staff", JOptionPane.INFORMATION_MESSAGE);
    }

    private void displaySystemStats() {
        String stats = "Total Students: " + Student.getTotalStudents() + "\n" +
                       "Total Teachers: " + Teacher.getTotalTeachers() + "\n" +
                       "Total Courses: " + university.getCourseRepository().size() + "\n" +
                       "Total Administrative Staff: " + university.getAdministrativeStaffRepository().size();
        JOptionPane.showMessageDialog(this, stats, "System Statistics", JOptionPane.INFORMATION_MESSAGE);
    }

    private void searchStudent() {
        String name = JOptionPane.showInputDialog(this, "Enter student name to search:");
        if (name != null && !name.trim().isEmpty()) {
            List<Student> matchingStudents = university.searchStudentByName(name);
            
            if (matchingStudents.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No students found with the name '" + name + "'", 
                    "Search Results", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            StringBuilder result = new StringBuilder();
            for (Student s : matchingStudents) {
                result.append("ID: ").append(s.getId())
                      .append(", Name: ").append(s.getName())
                      .append(", Email: ").append(s.getEmail())
                      .append(", Address: ").append(s.getAddress().getCity())
                      .append(", Courses Enrolled: ").append(s.getCourses().size())
                      .append("\n\n");
            }
            
            JTextArea textArea = new JTextArea(result.toString());
            textArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(400, 300));
            
            JOptionPane.showMessageDialog(this, scrollPane, "Search Results for '" + name + "'", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void generateReports() {
        String[] options = {"Student Enrollment Report", "Teacher Workload Report", "Course Statistics Report"};
        int choice = JOptionPane.showOptionDialog(this, "Choose a report to generate:", "Generate Reports",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

        switch (choice) {
            case 0:
                generateStudentEnrollmentReport();
                break;
            case 1:
                generateTeacherWorkloadReport();
                break;
            case 2:
                generateCourseStatisticsReport();
                break;
        }
    }

    private void generateStudentEnrollmentReport() {
        StringBuilder report = new StringBuilder("Student Enrollment Report:\n\n");
        for (Course course : university.getCourseRepository().getAll()) {
            report.append("Course: ").append(course.getTitle()).append("\n");
            report.append("Enrolled Students:\n");
            for (Student student : course.getStudents()) {
                report.append("- ").append(student.getName()).append(" (ID: ").append(student.getId()).append(")\n");
            }
            report.append("\n");
        }
        displayReport(report.toString(), "Student Enrollment Report");
    }

    private void generateTeacherWorkloadReport() {
        StringBuilder report = new StringBuilder("Teacher Workload Report:\n\n");
        for (Teacher teacher : university.getTeacherRepository().getAll()) {
            report.append("Teacher: ").append(teacher.getName()).append(" (ID: ").append(teacher.getTeacherID()).append(")\n");
            report.append("Assigned Courses:\n");
            for (Course course : teacher.getCourses()) {
                report.append("- ").append(course.getTitle()).append(" (ID: ").append(course.getId()).append(")\n");
            }
            report.append("\n");
        }
        displayReport(report.toString(), "Teacher Workload Report");
    }

    private void generateCourseStatisticsReport() {
        StringBuilder report = new StringBuilder("Course Statistics Report:\n\n");
        for (Course course : university.getCourseRepository().getAll()) {
            report.append("Course: ").append(course.getTitle()).append(" (ID: ").append(course.getId()).append(")\n");
            report.append("Credits: ").append(course.getCredits()).append("\n");
            report.append("Enrolled Students: ").append(course.getStudents().size()).append("\n");
            report.append("Average Grade: ").append(String.format("%.2f", course.averageGrade())).append("\n");
            report.append("Median Grade: ").append(String.format("%.2f", course.calculateMedianGrade())).append("\n\n");
        }
        displayReport(report.toString(), "Course Statistics Report");
    }

    private void displayReport(String reportContent, String title) {
        JTextArea textArea = new JTextArea(reportContent);
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 400));
        JOptionPane.showMessageDialog(this, scrollPane, title, JOptionPane.INFORMATION_MESSAGE);
    }
}



class Repository<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<T> items;

    public Repository() {
        items = new ArrayList<>();
    }

    public void add(T item) {
        items.add(item);
        System.out.println(item.getClass().getSimpleName() + " added to the repository.");
    }

    public void remove(T item) {
        if (items.remove(item)) {
            System.out.println(item.getClass().getSimpleName() + " removed from the repository.");
        } else {
            System.out.println(item.getClass().getSimpleName() + " not found in the repository.");
        }
    }

    public List<T> getAll() {
        return new ArrayList<>(items);
    }

    public int size() {
        return items.size();
    }

    public T findById(int id) {
        for (T item : items) {
            if (item instanceof Student && ((Student) item).getId() == id) {
                return item;
            } else if (item instanceof Teacher && ((Teacher) item).getTeacherID() == id) {
                return item;
            } else if (item instanceof Course && ((Course) item).getId() == id) {
                return item;
            } else if (item instanceof AdministrativeStaff && ((AdministrativeStaff) item).getStaffID() == id) {
                return item;
            }
        }
        return null;
    }
}

class University implements Serializable {
    private static final long serialVersionUID = 1L;
    private Repository<Student> studentRepository;
    private Repository<Teacher> teacherRepository;
    private Repository<Course> courseRepository;
    private Repository<AdministrativeStaff> administrativeStaffRepository;

    public University() {
        studentRepository = new Repository<>();
        teacherRepository = new Repository<>();
        courseRepository = new Repository<>();
        administrativeStaffRepository = new Repository<>();
    }

    public void displaySystemStats() {
        System.out.println("System Statistics:");
        System.out.println("Total Students: " + Student.getTotalStudents());
        System.out.println("Total Teachers: " + Teacher.getTotalTeachers());
        System.out.println("Total Courses: " + courseRepository.size());
        System.out.println("Total Administrative Staff: " + administrativeStaffRepository.size());
    }

    public void loadData(String filename) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            studentRepository = (Repository<Student>) ois.readObject();
            teacherRepository = (Repository<Teacher>) ois.readObject();
            courseRepository = (Repository<Course>) ois.readObject();
            administrativeStaffRepository = (Repository<AdministrativeStaff>) ois.readObject();
            System.out.println("Data loaded successfully from " + filename);
        } catch (FileNotFoundException e) {
            System.out.println("No existing data file found. Starting with empty repositories.");
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading data from " + filename + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void saveData(String filename) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(studentRepository);
            oos.writeObject(teacherRepository);
            oos.writeObject(courseRepository);
            oos.writeObject(administrativeStaffRepository);
            System.out.println("Data saved successfully to " + filename);
        } catch (IOException e) {
            System.out.println("Error saving data to " + filename + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Repository<Student> getStudentRepository() {
        return studentRepository;
    }

    public Repository<Teacher> getTeacherRepository() {
        return teacherRepository;
    }

    public Repository<Course> getCourseRepository() {
        return courseRepository;
    }

    public Repository<AdministrativeStaff> getAdministrativeStaffRepository() {
        return administrativeStaffRepository;
    }

    public List<Student> searchStudentByName(String name) {
        List<Student> students = studentRepository.getAll();
        List<Student> matchingStudents = new ArrayList<>();
        for (Student s : students) {
            if (s.getName().toLowerCase().contains(name.toLowerCase())) {
                matchingStudents.add(s);
            }
        }
        return matchingStudents;
    }

    public void filterCoursesByCredits(int minCredits) {
        List<Course> allCourses = courseRepository.getAll();
        for (Course c : allCourses) {
            if (c.getCredits() >= minCredits) {
                System.out.println(c.getTitle());
            }
        }
    }
}

class Person implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private String email;
    private Date dateOfBirth;

    public Person() {}

    public Person(String name, String email, Date date) {
        this.name = name;
        this.email = email;
        this.dateOfBirth = date;
    }

    public Person(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Name: " + name + ", Email: " + email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setDateOfBirth(Date date) {
        this.dateOfBirth = date;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }
}

class Address implements Serializable {
    private static final long serialVersionUID = 1L;
    private String city;
    private int houseNumber;

    public Address() {}

    public Address(int number, String city) {
        this.houseNumber = number;
        this.city = city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCity() {
        return city;
    }

    public void setHouseNumber(int number) {
        this.houseNumber = number;
    }

    public int getHouseNumber() {
        return houseNumber;
    }
}

class Course implements Serializable {
    private static final long serialVersionUID = 1L;
    private static int totalCourses;
    private int courseID;
    private String title;
    private int credits;
    private Teacher assignedTeacher;
    private List<Student> students;
    private List<Teacher> assignedTeachers;
    public Course() {
        totalCourses++;
    }

    public Course(int id, String title, int credit) {
        this.courseID = id;
        this.title = title;
        this.credits = credit;
        this.assignedTeachers = new ArrayList<>(); // Initialize the list
        this.students = new ArrayList<>();
        totalCourses++;
    }

    public void addTeacher(Teacher teacher) {
        if (!assignedTeachers.contains(teacher)) {
            assignedTeachers.add(teacher);
        }
    }

    public void removeTeacher(Teacher teacher) {
        assignedTeachers.remove(teacher);
    }

    public List<Teacher> getTeachers() {
        return assignedTeachers;
    }

    public void addStudent(Student s) {
        if (!students.contains(s)) {
            students.add(s);
        }
    }

    public void removeStudent(Student student) {
        if (students.remove(student)) {
            System.out.println("Student with id: " + student.getId() + " is removed from the course");
        } else {
            System.out.println("Student with id: " + student.getId() + " is not found in the course");
        }
    }

    public double averageGrade() {
        if (students.isEmpty()) return 0;
        double totalMarks = 0;
        int count = 0;
        for (Student student : students) {
            Optional<CourseRecord> record = student.getCourses().stream()
                .filter(cr -> cr.getCourse().getId() == this.courseID)
                .findFirst();
            if (record.isPresent()) {
                totalMarks += record.get().getMarks();
                count++;
            }
        }
        return count > 0 ? totalMarks / count : 0;
    }

    public Double calculateMedianGrade() {
        List<Double> grades = students.stream()
            .flatMap(s -> s.getCourses().stream())
            .filter(cr -> cr.getCourse().getId() == this.courseID)
            .map(CourseRecord::getMarks)
            .sorted()
            .collect(Collectors.toList());

        if (grades.isEmpty()) {
            return 0.0;
        }
        int size = grades.size();
        if (size % 2 == 1) {
            return grades.get(size / 2);
        } else {
            return (grades.get(size / 2 - 1) + grades.get(size / 2)) / 2.0;
        }
    }

    public int getId() {
        return courseID;
    }

    public void setId(int id) {
        this.courseID = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credit) {
        this.credits = credit;
    }

    public Teacher getTeacher() {
        return assignedTeacher;
    }

    public void setTeacher(Teacher teacher) {
        this.assignedTeacher = teacher;
    }

    public List<Student> getStudents() {
        return students;
    }

    public void setStudents(List<Student> students) {
        this.students = students;
    }

    public static int getTotalCourses() {
        return totalCourses;
    }
}

class CourseRecord implements Serializable {
    private static final long serialVersionUID = 1L;
    private Course course;
    private Double marks;

    public CourseRecord(Course course, Double marks) {
        this.course = course;
        this.marks = marks;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public Double getMarks() {
        return marks;
    }

    public void setMarks(Double marks) {
        this.marks = marks;
    }

    @Override
    public String toString() {
        return "Course: " + course.getTitle() + ", Marks: " + marks;
    }
}

class Student extends Person implements Serializable {
    private static final long serialVersionUID = 1L;
    private int studentID;
    private Address address;
    private List<CourseRecord> courseRecords;
    private static int totalStudents;
    private Double studentScore;

    public Student() {
        totalStudents++;
    }

    public Student(String name, int id, String email, String city, int houseNumber, Date dateOfBirth, Double score) {
        super(name, email, dateOfBirth);
        this.studentID = id;
        this.address = new Address(houseNumber, city);
        this.courseRecords = new ArrayList<>();
        this.studentScore = score;
        totalStudents++;
    }

    public void addCourse(Course c, Double marks) {
        CourseRecord existingRecord = courseRecords.stream()
            .filter(cr -> cr.getCourse().getId() == c.getId())
            .findFirst()
            .orElse(null);

        if (existingRecord != null) {
            existingRecord.setMarks(marks);
        } else {
            CourseRecord newRecord = new CourseRecord(c, marks);
            courseRecords.add(newRecord);
        }
        System.out.println("Student " + this.studentID + " successfully enrolled in " + c.getTitle() + " with marks: " + marks);
    }

    public void removeCourse(Course course) {
        courseRecords.removeIf(cr -> cr.getCourse().getId() == course.getId());
    }

    public void displayCoursesAndMarks() {
        if (courseRecords.isEmpty()) {
            System.out.println("No courses enrolled for student " + this.studentID);
            return;
        }
        System.out.println("Courses and Marks for student ID: " + this.studentID);
        for (CourseRecord record : courseRecords) {
            System.out.println(record);
        }
    }

    @Override
    public String toString() {
        return "Student ID: " + studentID + ", Name: " + getName() + ", Courses Enrolled: " + courseRecords.size();
    }

    public List<CourseRecord> getCourses() {
        return courseRecords;
    }

    public void setCourses(List<CourseRecord> courses) {
        this.courseRecords = courses;
    }

    public void setId(int id) {
        this.studentID = id;
    }

    public int getId() {
        return studentID;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Address getAddress() {
        return address;
    }

    public static int getTotalStudents() {
        return totalStudents;
    }

    public Double getScore() {
        return studentScore;
    }

    public void setScore(Double score) {
        this.studentScore = score;
    }
}

class Teacher extends Person implements Reportable, Serializable {
    private static final long serialVersionUID = 1L;
    private static int totalTeachers;
    private int teacherID;
    private String specialization;
    private List<Course> courses;

    public Teacher(String name, int id, String specialization) {
        super(name);
        this.teacherID = id;
        this.specialization = specialization;
        courses = new ArrayList<>();
        totalTeachers++;
    }

    public void addCourse(Course c) {
        if (!courses.contains(c)) {
            courses.add(c);
        }
    }
   
    @Override
    public String toString() {
        StringBuilder assignedCoursesList = new StringBuilder("[ ");
        for (Course course : courses) {
            assignedCoursesList.append(course.getTitle()).append(", ");
        }
        if (!courses.isEmpty()) {
            assignedCoursesList.setLength(assignedCoursesList.length() - 2);
        }
        assignedCoursesList.append(" ]");

        return "Teacher: T" + teacherID + ", Name: " + getName() + ", Specialization: " + specialization + ", Assigned Courses: " + assignedCoursesList;
    }

    public void removeCourse(int id) {
        courses.removeIf(c -> c.getId() == id);
    }

    public void exportToFile(String filename, String reportContent) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(reportContent);
            System.out.println("Teacher report saved to file: " + filename);
        } catch (IOException e) {
            System.out.println("Error writing teacher report to file: " + e.getMessage());
        }
    }

    @Override
    public String generateReport(List<Person> people) {
        int studentsCount = courses.stream().mapToInt(c -> c.getStudents().size()).sum();
        return "Teacher Report:\n" +
               "Total Students assigned: " + studentsCount +
               "\nTotal Courses assigned: " + courses.size();
    }

    public static int getTotalTeachers() {
        return totalTeachers;
    }

    public int getTeacherID() {
        return teacherID;
    }

    public void setTeacherID(int teacherID) {
        this.teacherID = teacherID;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public List<Course> getCourses() {
        return courses;
    }

    public void setCourses(List<Course> courses) {
        this.courses = courses;
    }
}

interface Reportable {
    String generateReport(List<Person> people);
    void exportToFile(String filename, String reportContent);
}

class AdministrativeStaff extends Person implements Reportable, Serializable {
    private static final long serialVersionUID = 1L;
    private int staffID;
    private String role;
    private String department;

    public AdministrativeStaff(int id, String name, String role, String department) {
        super(name);
        this.staffID = id;
        this.role = role;
        this.department = department;
    }

    @Override
    public void exportToFile(String filename, String reportContent) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(reportContent);
            System.out.println("Administrator report saved to file: " + filename);
        } catch (IOException e) {
            System.out.println("Error writing administrator report to file: " + e.getMessage());
        }
    }

    @Override
    public String generateReport(List<Person> people) {
        int totalStudents = 0;
        int totalTeachers = 0;
        Set<Course> uniqueCourses = new HashSet<>();

        for (Person person : people) {
            if (person instanceof Student) {
                totalStudents++;
                Student s = (Student) person;
                uniqueCourses.addAll(s.getCourses().stream().map(CourseRecord::getCourse).collect(Collectors.toSet()));
            } else if (person instanceof Teacher) {
                totalTeachers++;
            }
        }

        return "Report Summary:\n" +
                "Total Students: " + totalStudents + "\n" +
                "Total Teachers: " + totalTeachers + "\n" +
                "Total Unique Courses: " + uniqueCourses.size();
    }

    @Override
    public String toString() {
        return "Admin Staff: " + staffID + ", Name: " + getName() + ", Role: " + role + ", Department: " + department;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public int getStaffID() {
        return staffID;
    }

    public void setStaffID(int staffID) {
        this.staffID = staffID;
    }
    
}
